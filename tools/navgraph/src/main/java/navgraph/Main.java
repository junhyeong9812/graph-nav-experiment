package navgraph;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * navgraph: Spring Boot 소스에서 기계적으로 추출 가능한 구조 사실만 모아
 * NAV.md(인덱스)와 nav/<도메인>.md(상세)를 만든다. 코드 주석은 읽지 않는다.
 *
 * 사용: navgraph <fream-back 루트> <출력 디렉토리>
 */
public class Main {

    static final String BASE_PKG = "com.fream.back";
    static final String DOMAIN_PKG = BASE_PKG + ".domain.";
    static final String GLOBAL = "global";
    static final int NAV_LIMIT = 8192;

    static final Set<String> MAPPING_ANN = Set.of("GetMapping", "PostMapping", "PutMapping", "DeleteMapping", "PatchMapping", "RequestMapping");
    static final Set<String> ASSOC_ANN = Set.of("OneToMany", "ManyToOne", "OneToOne", "ManyToMany");
    static final Set<String> EXTRA_FIELD_ANN = Set.of("Embedded", "ElementCollection", "EmbeddedId");
    static final Set<String> INHERIT_ANN = Set.of("Inheritance", "DiscriminatorColumn", "DiscriminatorValue", "MappedSuperclass");
    static final Set<String> ADVICE_ANN = Set.of("Pointcut", "Around", "Before", "After", "AfterReturning", "AfterThrowing");
    static final Set<String> STEREOTYPES = Set.of("Service", "Component", "Repository", "RestController", "Controller", "Configuration", "Aspect", "ControllerAdvice", "RestControllerAdvice");
    static final Set<String> EVENT_LISTENER_ANN = Set.of("EventListener", "TransactionalEventListener");
    static final List<String> ROLE_ORDER = List.of("controller", "service-command", "service-query", "service", "repository", "custom", "entity", "event", "config", "aop", "dto", "exception", "other");

    // ---------- 모델 ----------

    static final class CInfo {
        String fqn, simple, pkg, domain, sub, relPath, kind, role;
        CompilationUnit cu;
        TypeDeclaration<?> decl;
        boolean topLevel, test;
        Set<String> annotations = new LinkedHashSet<>();
        List<String> supersRaw = new ArrayList<>();
        Map<String, FieldInfo> fields = new LinkedHashMap<>();
        Map<String, String> constants = new LinkedHashMap<>();
        Set<String> methodNames = new LinkedHashSet<>();
        Map<String, Set<Integer>> methodArity = new HashMap<>();
        Map<String, Boolean> methodVarargs = new HashMap<>();
    }

    static final class FieldInfo {
        String name, typeRaw, typeFqn; // typeFqn: 프로젝트 타입이면 FQN
        boolean injected, resolvedBySolver;
        FieldDeclaration decl;
    }

    record Call(String text, String targetFqn) {}

    record Endpoint(String http, String path, String handler, List<String> calls) {}

    record KafkaSend(String owner, String method, String topic, String domain) {}

    record KafkaListen(String owner, String method, List<String> topics, String extra, String domain) {}

    record SpringPublish(String owner, String method, String eventType, String domain) {}

    record SpringListen(String owner, String method, String eventType, String ann, String domain) {}

    record Advice(String aspect, String method, String annText, List<String> boundAnnFqns, String domain) {}

    Path root, mainJava, testJava, outDir;
    final Map<String, CInfo> types = new TreeMap<>();
    final Map<String, List<String>> bySimple = new TreeMap<>();
    final List<CInfo> testTypes = new ArrayList<>();
    final Map<String, TreeSet<String>> filesByDomain = new TreeMap<>();
    final Map<String, TreeSet<String>> noTypeFiles = new TreeMap<>();
    final Map<String, List<Endpoint>> endpoints = new TreeMap<>();
    final Map<String, Map<String, Map<String, LinkedHashSet<String>>>> chains = new TreeMap<>(); // domain -> class -> method -> calls
    final List<KafkaSend> kafkaSends = new ArrayList<>();
    final List<KafkaListen> kafkaListens = new ArrayList<>();
    final List<SpringPublish> springPublishes = new ArrayList<>();
    final List<SpringListen> springListens = new ArrayList<>();
    final List<Advice> advices = new ArrayList<>();
    final Map<String, List<String>> annotatedSites = new TreeMap<>(); // annotation fqn -> Class#method
    int solverOk = 0, solverFail = 0, methodExact = 0, methodGuess = 0;

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("usage: navgraph <fream-back root> <out dir>");
            System.exit(2);
        }
        Main m = new Main();
        m.root = Paths.get(args[0]).toAbsolutePath().normalize();
        m.mainJava = m.root.resolve("src/main/java");
        m.testJava = m.root.resolve("src/test/java");
        m.outDir = Paths.get(args[1]).toAbsolutePath().normalize();
        m.run();
    }

    void run() throws IOException {
        CombinedTypeSolver ts = new CombinedTypeSolver();
        ts.add(new ReflectionTypeSolver());
        ts.add(new JavaParserTypeSolver(mainJava));
        ParserConfiguration cfg = new ParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17)
                .setAttributeComments(false)
                .setSymbolResolver(new JavaSymbolSolver(ts));
        JavaParser parser = new JavaParser(cfg);

        for (Path p : listJava(mainJava)) parseFile(parser, p, false);
        ParserConfiguration tcfg = new ParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17)
                .setAttributeComments(false);
        JavaParser tparser = new JavaParser(tcfg);
        // src/test 는 입력에서 제외한다(에이전트 사본에 없음, 판정 전용).

        for (CInfo c : types.values()) bySimple.computeIfAbsent(c.simple, k -> new ArrayList<>()).add(c.fqn);
        for (CInfo c : types.values()) collectMembers(c);
        for (CInfo c : types.values()) c.role = role(c);
        for (CInfo c : types.values()) if (c.topLevel) analyzeBodies(c);

        writeOutput();
        System.err.printf("navgraph: types=%d tests=%d fieldTypeSolver ok=%d fallback=%d callMethod exact=%d guess=%d%n",
                types.size(), testTypes.size(), solverOk, solverFail, methodExact, methodGuess);
    }

    static List<Path> listJava(Path dir) throws IOException {
        try (Stream<Path> s = Files.walk(dir)) {
            return s.filter(p -> p.toString().endsWith(".java")).sorted().collect(Collectors.toList());
        }
    }

    void parseFile(JavaParser parser, Path p, boolean test) throws IOException {
        ParseResult<CompilationUnit> r = parser.parse(p);
        if (!r.isSuccessful() || r.getResult().isEmpty()) {
            System.err.println("parse failed: " + root.relativize(p));
            return;
        }
        CompilationUnit cu = r.getResult().get();
        String pkg = cu.getPackageDeclaration().map(pd -> pd.getNameAsString()).orElse("");
        String rel = root.relativize(p).toString().replace('\\', '/');
        if (!test) {
            String dom = !pkg.isEmpty() ? domainOf(pkg) : domainOfPath(rel);
            filesByDomain.computeIfAbsent(dom, k -> new TreeSet<>()).add(rel);
            if (cu.getTypes().isEmpty()) noTypeFiles.computeIfAbsent(dom, k -> new TreeSet<>()).add(rel);
        }
        for (TypeDeclaration<?> td : cu.getTypes()) register(cu, td, pkg, pkg, rel, true, test);
    }

    void register(CompilationUnit cu, TypeDeclaration<?> td, String pkg, String prefix, String rel, boolean top, boolean test) {
        CInfo c = new CInfo();
        c.cu = cu;
        c.decl = td;
        c.pkg = pkg;
        c.simple = top ? td.getNameAsString() : prefix.substring(prefix.lastIndexOf('.') + 1) + "." + td.getNameAsString();
        c.fqn = prefix + "." + td.getNameAsString();
        c.relPath = rel;
        c.topLevel = top;
        c.test = test;
        c.domain = domainOf(pkg);
        c.sub = subOf(pkg);
        if (td instanceof ClassOrInterfaceDeclaration cid) {
            c.kind = cid.isInterface() ? "interface" : (cid.isAbstract() ? "abstract class" : "class");
            cid.getExtendedTypes().forEach(t -> c.supersRaw.add(t.getNameWithScope()));
            cid.getImplementedTypes().forEach(t -> c.supersRaw.add(t.getNameWithScope()));
        } else if (td instanceof EnumDeclaration ed) {
            c.kind = "enum";
            ed.getImplementedTypes().forEach(t -> c.supersRaw.add(t.getNameWithScope()));
        } else if (td instanceof RecordDeclaration rd) {
            c.kind = "record";
            rd.getImplementedTypes().forEach(t -> c.supersRaw.add(t.getNameWithScope()));
        } else if (td instanceof AnnotationDeclaration) {
            c.kind = "@interface";
        } else c.kind = "type";
        td.getAnnotations().forEach(a -> c.annotations.add(a.getNameAsString()));
        if (test) {
            if (top) testTypes.add(c);
            return;
        }
        types.put(c.fqn, c);
        for (BodyDeclaration<?> m : td.getMembers()) {
            if (m instanceof TypeDeclaration<?> inner) register(cu, inner, pkg, c.fqn, rel, false, false);
        }
    }

    static String domainOfPath(String rel) {
        String pre = "src/main/java/" + DOMAIN_PKG.replace('.', '/');
        if (!rel.startsWith(pre)) return GLOBAL;
        String rest = rel.substring(pre.length());
        return rest.contains("/") ? rest.substring(0, rest.indexOf('/')) : GLOBAL;
    }

    int fileCount(String d) {
        return filesByDomain.getOrDefault(d, new TreeSet<>()).size();
    }

    static String domainOf(String pkg) {
        if (pkg.startsWith(DOMAIN_PKG)) {
            String rest = pkg.substring(DOMAIN_PKG.length());
            int i = rest.indexOf('.');
            return i < 0 ? rest : rest.substring(0, i);
        }
        return GLOBAL;
    }

    /** 도메인(또는 global) 패키지 아래 하위 패키지. 예: service.command */
    static String subOf(String pkg) {
        String base;
        if (pkg.startsWith(DOMAIN_PKG)) base = DOMAIN_PKG + domainOf(pkg);
        else if (pkg.startsWith(BASE_PKG + ".global")) base = BASE_PKG + ".global";
        else return "";
        return pkg.length() > base.length() ? pkg.substring(base.length() + 1) : "";
    }

    // ---------- 멤버 수집 ----------

    void collectMembers(CInfo c) {
        TypeDeclaration<?> td = c.decl;
        boolean component = c.annotations.stream().anyMatch(STEREOTYPES::contains);
        boolean lombokCtor = c.annotations.contains("RequiredArgsConstructor") || c.annotations.contains("AllArgsConstructor");
        boolean plainClass = "class".equals(c.kind) || "abstract class".equals(c.kind);
        boolean dataLike = c.annotations.stream().anyMatch(a -> Set.of("Entity", "Embeddable", "MappedSuperclass", "Data", "Value").contains(a));
        boolean isInterface = "interface".equals(c.kind);
        Set<String> ctorAssigned = new HashSet<>();
        for (ConstructorDeclaration cd : td.getConstructors()) {
            Set<String> params = cd.getParameters().stream().map(NodeWithName -> NodeWithName.getNameAsString()).collect(Collectors.toSet());
            for (AssignExpr ae : cd.findAll(AssignExpr.class)) {
                String target = null;
                if (ae.getTarget() instanceof FieldAccessExpr fa && fa.getScope() instanceof ThisExpr) target = fa.getNameAsString();
                else if (ae.getTarget() instanceof NameExpr ne) target = ne.getNameAsString();
                if (target != null && ae.getValue() instanceof NameExpr v && params.contains(v.getNameAsString())) ctorAssigned.add(target);
            }
        }
        for (FieldDeclaration fd : td.getFields()) {
            boolean isStatic = fd.isStatic() || isInterface;
            boolean isFinal = fd.isFinal() || isInterface;
            boolean autowired = fd.getAnnotations().stream().anyMatch(a -> Set.of("Autowired", "Inject", "Resource").contains(a.getNameAsString()));
            boolean valueAnn = fd.getAnnotations().stream().anyMatch(a -> a.getNameAsString().equals("Value"));
            for (VariableDeclarator vd : fd.getVariables()) {
                FieldInfo f = new FieldInfo();
                f.name = vd.getNameAsString();
                f.decl = fd;
                f.typeRaw = vd.getType().asString();
                if (!isStatic && !valueAnn && plainClass && !dataLike) {
                    boolean cand = autowired
                            || (isFinal && vd.getInitializer().isEmpty() && lombokCtor)
                            || ctorAssigned.contains(f.name);
                    // 스테레오타입 빈이 아닌 클래스(직접 생성되는 필터, 어노테이션 없는 *Impl 등)는
                    // 빈 성격 타입을 받는 필드만 주입으로 본다.
                    f.injected = cand && (component || autowired || beanLikeType(c, vd.getType()));
                }
                f.typeFqn = resolveFieldType(c, vd.getType(), f);
                c.fields.put(f.name, f);
                if (isStatic && isFinal && vd.getInitializer().isPresent()) {
                    // 상수 후보: 평가는 나중에 지연 평가
                    c.constants.put(f.name, null);
                }
            }
        }
        for (MethodDeclaration md : td.getMethods()) {
            c.methodNames.add(md.getNameAsString());
            c.methodArity.computeIfAbsent(md.getNameAsString(), k -> new HashSet<>()).add(md.getParameters().size());
            if (md.getParameters().stream().anyMatch(Parameter::isVarArgs)) c.methodVarargs.put(md.getNameAsString(), true);
        }
        if (td instanceof RecordDeclaration rd) {
            rd.getParameters().forEach(p -> {
                c.methodNames.add(p.getNameAsString());
                c.methodArity.computeIfAbsent(p.getNameAsString(), k -> new HashSet<>()).add(0);
            });
        }
    }

    boolean beanLikeType(CInfo c, Type t) {
        if (!t.isClassOrInterfaceType()) return false;
        String raw = stripGenerics(t.asClassOrInterfaceType().getNameWithScope());
        String fq = resolveName(raw, c);
        if (fq == null) return raw.equals("JPAQueryFactory") || raw.endsWith("Template") || raw.equals("EntityManager");
        CInfo tt = types.get(fq);
        if (tt.annotations.stream().anyMatch(STEREOTYPES::contains)) return true;
        if ("interface".equals(tt.kind)) {
            if (tt.simple.endsWith("Repository") || tt.simple.endsWith("RepositoryCustom") || tt.supersRaw.stream().anyMatch(x -> x.endsWith("Repository"))) return true;
            for (CInfo o : types.values())
                if (o.annotations.stream().anyMatch(STEREOTYPES::contains) && o.supersRaw.stream().anyMatch(x -> stripGenerics(x).equals(tt.simple))) return true;
        }
        return false;
    }

    /** 필드 선언 타입을 심볼 솔버로 해석하고, 실패하면 import 기반 이름 해석으로 대체한다. */
    String resolveFieldType(CInfo c, Type type, FieldInfo f) {
        Type t = type;
        if (t.isArrayType()) return null;
        if (!t.isClassOrInterfaceType()) return null;
        ClassOrInterfaceType ct = t.asClassOrInterfaceType();
        String byName = resolveName(ct.getNameWithScope(), c);
        if (!f.injected) return byName; // 주입 필드만 솔버를 쓴다(호출 사슬 대상)
        try {
            ResolvedType rt = ct.resolve();
            if (rt.isReferenceType()) {
                String q = rt.asReferenceType().getQualifiedName();
                f.resolvedBySolver = true;
                solverOk++;
                return types.containsKey(q) ? q : null;
            }
        } catch (Throwable e) {
            // 해석 실패 → 이름 기반 추정
        }
        solverFail++;
        if (System.getenv("NAVGRAPH_DEBUG") != null) System.err.println("fallback: " + c.simple + "." + f.name + " : " + f.typeRaw + " -> " + byName);
        f.resolvedBySolver = false;
        return byName;
    }

    /** import·같은 패키지·중첩 타입 기준으로 프로젝트 타입 FQN을 찾는다. 없으면 null. */
    String resolveName(String name, CInfo ctx) {
        if (name == null) return null;
        int lt = name.indexOf('<');
        if (lt >= 0) name = name.substring(0, lt);
        name = name.replace("[]", "").trim();
        if (types.containsKey(name)) return name;
        if (name.contains(".")) {
            String first = name.substring(0, name.indexOf('.'));
            String rest = name.substring(name.indexOf('.'));
            String f = resolveName(first, ctx);
            if (f != null && types.containsKey(f + rest)) return f + rest;
            return null;
        }
        // 자기 자신·중첩 타입
        String outerTop = ctx.topLevel ? ctx.fqn : ctx.pkg + "." + ctx.simple.substring(0, ctx.simple.indexOf('.'));
        if (ctx.simple.equals(name) || ctx.fqn.endsWith("." + name)) return ctx.fqn;
        if (types.containsKey(ctx.fqn + "." + name)) return ctx.fqn + "." + name;
        if (types.containsKey(outerTop + "." + name)) return outerTop + "." + name;
        for (ImportDeclaration im : ctx.cu.getImports()) {
            if (im.isStatic()) continue;
            String in = im.getNameAsString();
            if (!im.isAsterisk()) {
                if (in.endsWith("." + name)) return types.containsKey(in) ? in : null;
            }
        }
        if (types.containsKey(ctx.pkg + "." + name)) return ctx.pkg + "." + name;
        for (ImportDeclaration im : ctx.cu.getImports()) {
            if (im.isStatic() || !im.isAsterisk()) continue;
            String cand = im.getNameAsString() + "." + name;
            if (types.containsKey(cand)) return cand;
        }
        return null;
    }

    // ---------- 역할 ----------

    String role(CInfo c) {
        Set<String> a = c.annotations;
        String sub = c.sub;
        String n = c.simple;
        if (a.contains("Aspect")) return "aop";
        if (a.contains("RestController") || a.contains("Controller")) return "controller";
        if (a.contains("Entity") || a.contains("MappedSuperclass") || a.contains("Embeddable")) return "entity";
        if (a.contains("Configuration") || sub.equals("config") || sub.startsWith("config.")) return "config";
        if (isMessaging(c)) return "event";
        if (n.endsWith("RepositoryImpl") || n.endsWith("RepositoryCustom")
                || (n.endsWith("Impl") && sub.startsWith("repository"))
                || (n.endsWith("Custom") && sub.startsWith("repository"))) return "custom";
        if (a.contains("Repository") || n.endsWith("Repository")
                || c.supersRaw.stream().anyMatch(s -> s.endsWith("Repository"))) return "repository";
        if (sub.startsWith("service") || a.contains("Service")) {
            if (sub.contains("command")) return "service-command";
            if (sub.contains("query")) return "service-query";
            return "service";
        }
        if (sub.equals("dto") || sub.startsWith("dto.") || n.endsWith("Dto") || n.endsWith("DTO")) return "dto";
        if (sub.equals("exception") || sub.startsWith("exception.") || c.supersRaw.stream().anyMatch(s -> s.endsWith("Exception"))) return "exception";
        if (sub.equals("entity") || sub.startsWith("entity.")) return "entity";
        return "other";
    }

    boolean isMessaging(CInfo c) {
        String sub = c.sub;
        if (sub.endsWith("kafka") || sub.contains(".kafka.") || sub.startsWith("kafka")) {
            if (!sub.contains("config")) return true;
        }
        for (MethodDeclaration md : c.decl.getMethods()) {
            for (AnnotationExpr an : md.getAnnotations()) {
                String n = an.getNameAsString();
                if (n.equals("KafkaListener") || EVENT_LISTENER_ANN.contains(n)) return true;
            }
        }
        for (FieldInfo f : c.fields.values()) {
            if (f.typeRaw.startsWith("KafkaTemplate") || f.typeRaw.startsWith("ApplicationEventPublisher")) return true;
        }
        return false;
    }

    // ---------- 본문 분석 ----------

    void analyzeBodies(CInfo c) {
        boolean controller = c.role.equals("controller");
        List<String> basePaths = List.of("");
        if (controller) {
            for (AnnotationExpr an : c.decl.getAnnotations()) {
                if (an.getNameAsString().equals("RequestMapping")) {
                    List<String> bp = annStrings(an, c, "value", "path");
                    if (!bp.isEmpty()) basePaths = bp;
                }
            }
        }
        for (MethodDeclaration md : c.decl.getMethods()) {
            String mname = md.getNameAsString();
            Set<String> shadow = new HashSet<>();
            md.getParameters().forEach(p -> shadow.add(p.getNameAsString()));
            md.findAll(VariableDeclarator.class).forEach(v -> shadow.add(v.getNameAsString()));
            md.findAll(com.github.javaparser.ast.expr.LambdaExpr.class).forEach(l -> l.getParameters().forEach(p -> shadow.add(p.getNameAsString())));
            LinkedHashSet<String> calls = new LinkedHashSet<>();
            for (MethodCallExpr mc : md.findAll(MethodCallExpr.class)) {
                FieldInfo f = scopeField(c, mc, shadow);
                if (f == null) {
                    boolean selfScope = mc.getScope().isEmpty() || mc.getScope().get() instanceof ThisExpr;
                    if (selfScope && c.methodNames.contains(mc.getNameAsString()) && !mc.getNameAsString().equals(mname)) {
                        calls.add("this#" + mc.getNameAsString());
                    }
                    continue;
                }
                String tr = f.typeRaw;
                if (tr.startsWith("KafkaTemplate") && (mc.getNameAsString().equals("send"))) {
                    String topic = mc.getArguments().isEmpty() ? "?" : String.join("|", evalStrings(mc.getArgument(0), c));
                    kafkaSends.add(new KafkaSend(c.simple, mname, topic, c.domain));
                    calls.add("kafka:" + topic);
                    continue;
                }
                if (tr.startsWith("ApplicationEventPublisher") && mc.getNameAsString().equals("publishEvent")) {
                    String et = mc.getArguments().isEmpty() ? "?" : eventTypeOf(mc.getArgument(0), c, md);
                    springPublishes.add(new SpringPublish(c.simple, mname, et, c.domain));
                    calls.add("event:" + et);
                    continue;
                }
                if (f.typeFqn == null) continue;
                CInfo target = types.get(f.typeFqn);
                boolean found = hasMethod(target, mc.getNameAsString(), mc.getArguments().size(), new HashSet<>());
                if (found) methodExact++; else methodGuess++;
                boolean guess = !found || !f.resolvedBySolver;
                calls.add(target.simple + "#" + mc.getNameAsString() + (guess ? "~" : ""));
            }
            // 리스너·AOP·어노테이션 적용 지점
            for (AnnotationExpr an : md.getAnnotations()) {
                String an_ = an.getNameAsString();
                if (an_.equals("KafkaListener")) {
                    List<String> topics = annStrings(an, c, "topics", "value");
                    if (topics.isEmpty()) topics = annStrings(an, c, "topicPattern");
                    List<String> extra = new ArrayList<>();
                    for (String k : List.of("groupId", "containerFactory")) {
                        List<String> v = annStrings(an, c, k);
                        if (!v.isEmpty()) extra.add(k + "=" + String.join(",", v));
                    }
                    kafkaListens.add(new KafkaListen(c.simple, mname, topics, String.join(" ", extra), c.domain));
                } else if (EVENT_LISTENER_ANN.contains(an_)) {
                    List<String> ev = annClassValues(an, c);
                    if (ev.isEmpty() && md.getParameters().size() > 0) {
                        String p = md.getParameter(0).getType().asString();
                        String fq = resolveName(p, c);
                        ev = List.of(fq != null ? types.get(fq).simple : p);
                    }
                    for (String e : ev) springListens.add(new SpringListen(c.simple, mname, e, an_, c.domain));
                } else if (ADVICE_ANN.contains(an_) && c.role.equals("aop")) {
                    List<String> bound = new ArrayList<>();
                    for (Parameter p : md.getParameters()) {
                        String fq = resolveName(p.getType().asString(), c);
                        if (fq != null && "@interface".equals(types.get(fq).kind)) bound.add(fq);
                    }
                    advices.add(new Advice(c.simple, mname, an.toString(), bound, c.domain));
                }
                String afq = resolveName(an_, c);
                if (afq != null && "@interface".equals(types.get(afq).kind)) {
                    annotatedSites.computeIfAbsent(afq, k -> new ArrayList<>()).add(c.domain + ":" + c.simple + "#" + mname);
                }
            }
            if (controller) {
                for (AnnotationExpr an : md.getAnnotations()) {
                    String an_ = an.getNameAsString();
                    if (!MAPPING_ANN.contains(an_)) continue;
                    List<String> https;
                    if (an_.equals("RequestMapping")) {
                        https = new ArrayList<>();
                        for (Expression e : annExprs(an, "method")) {
                            for (Expression x : flatten(e)) {
                                String s = x.toString();
                                https.add(s.substring(s.lastIndexOf('.') + 1));
                            }
                        }
                        if (https.isEmpty()) https = List.of("ANY");
                    } else https = List.of(an_.replace("Mapping", "").toUpperCase(Locale.ROOT));
                    List<String> paths = annStrings(an, c, "value", "path");
                    if (paths.isEmpty()) paths = List.of("");
                    for (String b : basePaths)
                        for (String p : paths)
                            for (String h : https)
                                endpoints.computeIfAbsent(c.domain, k -> new ArrayList<>())
                                        .add(new Endpoint(h, joinPath(b, p), c.simple + "#" + mname, new ArrayList<>(calls)));
                }
            } else if (!calls.isEmpty()) {
                chains.computeIfAbsent(c.domain, k -> new TreeMap<>())
                        .computeIfAbsent(c.simple, k -> new LinkedHashMap<>())
                        .computeIfAbsent(mname, k -> new LinkedHashSet<>()).addAll(calls);
            }
        }
    }

    FieldInfo scopeField(CInfo c, MethodCallExpr mc, Set<String> shadow) {
        if (mc.getScope().isEmpty()) return null;
        Expression s = mc.getScope().get();
        String name = null;
        if (s instanceof NameExpr ne) {
            name = ne.getNameAsString();
            if (shadow.contains(name)) return null;
        } else if (s instanceof FieldAccessExpr fa && fa.getScope() instanceof ThisExpr) {
            name = fa.getNameAsString();
        }
        if (name == null) return null;
        FieldInfo f = c.fields.get(name);
        if (f == null || !f.injected) return null;
        return f;
    }

    boolean hasMethod(CInfo t, String name, int arity, Set<String> seen) {
        if (t == null || !seen.add(t.fqn)) return false;
        if (t.methodNames.contains(name)) {
            Set<Integer> ar = t.methodArity.getOrDefault(name, Set.of());
            if (ar.contains(arity) || t.methodVarargs.containsKey(name)) return true;
        }
        for (String s : t.supersRaw) {
            String fq = resolveName(s, t);
            if (fq != null && hasMethod(types.get(fq), name, arity, seen)) return true;
        }
        return false;
    }

    String eventTypeOf(Expression arg, CInfo c, MethodDeclaration md) {
        if (arg instanceof ObjectCreationExpr oc) {
            String fq = resolveName(oc.getType().getNameWithScope(), c);
            return fq != null ? types.get(fq).simple : oc.getType().getNameWithScope();
        }
        if (arg instanceof NameExpr ne) {
            for (VariableDeclarator v : md.findAll(VariableDeclarator.class)) {
                if (v.getNameAsString().equals(ne.getNameAsString())) {
                    String fq = resolveName(v.getType().asString(), c);
                    if (fq != null) return types.get(fq).simple;
                    if (v.getInitializer().isPresent() && v.getInitializer().get() instanceof ObjectCreationExpr oc) {
                        return eventTypeOf(oc, c, md);
                    }
                    return v.getType().asString() + "~";
                }
            }
            for (Parameter p : md.getParameters()) {
                if (p.getNameAsString().equals(ne.getNameAsString())) {
                    String fq = resolveName(p.getType().asString(), c);
                    return fq != null ? types.get(fq).simple : p.getType().asString();
                }
            }
        }
        return "expr(" + arg + ")";
    }

    // ---------- 어노테이션 값 ----------

    static List<Expression> annExprs(AnnotationExpr an, String... keys) {
        List<Expression> out = new ArrayList<>();
        Set<String> ks = Set.of(keys);
        if (an instanceof SingleMemberAnnotationExpr sm) {
            if (ks.contains("value")) out.add(sm.getMemberValue());
        } else if (an instanceof NormalAnnotationExpr na) {
            for (String k : keys)
                for (MemberValuePair p : na.getPairs())
                    if (p.getNameAsString().equals(k)) out.add(p.getValue());
        }
        return out;
    }

    static List<Expression> flatten(Expression e) {
        if (e instanceof ArrayInitializerExpr ai) {
            List<Expression> out = new ArrayList<>();
            for (Expression x : ai.getValues()) out.addAll(flatten(x));
            return out;
        }
        return List.of(e);
    }

    List<String> annStrings(AnnotationExpr an, CInfo c, String... keys) {
        List<String> out = new ArrayList<>();
        for (Expression e : annExprs(an, keys)) out.addAll(evalStrings(e, c));
        return out;
    }

    List<String> annClassValues(AnnotationExpr an, CInfo c) {
        List<String> out = new ArrayList<>();
        for (Expression e : annExprs(an, "value", "classes")) {
            for (Expression x : flatten(e)) {
                if (x instanceof ClassExpr ce) {
                    String n = ce.getType().asString();
                    String fq = resolveName(n, c);
                    out.add(fq != null ? types.get(fq).simple : n);
                }
            }
        }
        return out;
    }

    /** 문자열 표현식 평가: 리터럴·상수·연결은 해석, 못 하면 expr(원문). */
    List<String> evalStrings(Expression e, CInfo c) {
        List<String> out = new ArrayList<>();
        for (Expression x : flatten(e)) {
            String v = evalString(x, c, new HashSet<>());
            out.add(v != null ? v : "expr(" + x + ")");
        }
        return out;
    }

    String evalString(Expression e, CInfo c, Set<String> guard) {
        if (e instanceof StringLiteralExpr s) return s.getValue();
        if (e instanceof EnclosedExpr en) return evalString(en.getInner(), c, guard);
        if (e instanceof BinaryExpr be && be.getOperator() == BinaryExpr.Operator.PLUS) {
            String l = evalString(be.getLeft(), c, guard), r = evalString(be.getRight(), c, guard);
            return (l != null && r != null) ? l + r : null;
        }
        if (e instanceof NameExpr ne) return constantValue(c, ne.getNameAsString(), guard);
        if (e instanceof FieldAccessExpr fa) {
            String owner = resolveName(fa.getScope().toString(), c);
            if (owner == null) return null;
            return constantValue(types.get(owner), fa.getNameAsString(), guard);
        }
        return null;
    }

    String constantValue(CInfo c, String name, Set<String> guard) {
        if (c == null || !guard.add(c.fqn + "#" + name)) return null;
        if (c.constants.containsKey(name)) {
            for (FieldDeclaration fd : c.decl.getFields())
                for (VariableDeclarator vd : fd.getVariables())
                    if (vd.getNameAsString().equals(name) && vd.getInitializer().isPresent())
                        return evalString(vd.getInitializer().get(), c, guard);
        }
        // 정적 import
        for (ImportDeclaration im : c.cu.getImports()) {
            if (!im.isStatic()) continue;
            String in = im.getNameAsString();
            if (!im.isAsterisk() && in.endsWith("." + name)) {
                CInfo o = types.get(in.substring(0, in.lastIndexOf('.')));
                if (o != null) return constantValue(o, name, guard);
            } else if (im.isAsterisk()) {
                CInfo o = types.get(in);
                if (o != null && o.constants.containsKey(name)) return constantValue(o, name, guard);
            }
        }
        return null;
    }

    static String joinPath(String a, String b) {
        String s = (a == null ? "" : a) + (b == null || b.isEmpty() ? "" : (b.startsWith("/") ? b : "/" + b));
        if (a != null && a.endsWith("/") && b != null && b.startsWith("/")) s = a + b.substring(1);
        s = s.replaceAll("/{2,}", "/");
        return s.isEmpty() ? "/" : s;
    }

    // ---------- 출력 ----------

    List<String> domains() {
        TreeSet<String> d = new TreeSet<>();
        for (CInfo c : types.values()) if (!c.domain.equals(GLOBAL)) d.add(c.domain);
        return new ArrayList<>(d);
    }

    List<CInfo> tops(String domain) {
        return types.values().stream().filter(c -> c.topLevel && c.domain.equals(domain))
                .sorted(Comparator.comparing(c -> c.relPath)).collect(Collectors.toList());
    }

    String basePathOf(String domain) {
        return domain.equals(GLOBAL) ? "src/main/java/com/fream/back/" : "src/main/java/com/fream/back/domain/" + domain + "/";
    }

    String relToBase(CInfo c) {
        String base = basePathOf(c.domain);
        return c.relPath.startsWith(base) ? c.relPath.substring(base.length()) : c.relPath;
    }

    List<String> publicMethods(CInfo c) {
        LinkedHashSet<String> out = new LinkedHashSet<>();
        boolean itf = "interface".equals(c.kind);
        for (MethodDeclaration md : c.decl.getMethods()) {
            if (md.isPublic() || (itf && !md.isPrivate())) out.add(md.getNameAsString());
        }
        return new ArrayList<>(out);
    }

    List<String> injectedTypes(CInfo c) {
        List<String> out = new ArrayList<>();
        for (FieldInfo f : c.fields.values()) {
            if (!f.injected) continue;
            String t = f.typeFqn != null ? types.get(f.typeFqn).simple : stripGenerics(f.typeRaw);
            if (f.typeFqn != null && !f.resolvedBySolver) t += "~";
            out.add(t);
        }
        return out;
    }

    static String stripGenerics(String s) {
        int i = s.indexOf('<');
        return i < 0 ? s : s.substring(0, i);
    }

    /** 도메인 → (다른 도메인 → 그 도메인 타입 → 참조하는 이 도메인 클래스들) */
    Map<String, Map<String, Map<String, TreeSet<String>>>> domainDeps() {
        Map<String, Map<String, Map<String, TreeSet<String>>>> out = new TreeMap<>();
        Set<String> roles = Set.of("service-command", "service-query", "service", "repository", "custom", "entity", "event");
        for (CInfo c : types.values()) {
            if (!c.topLevel) continue;
            Set<String> refs = new LinkedHashSet<>();
            for (ImportDeclaration im : c.cu.getImports()) {
                if (im.isStatic() || im.isAsterisk()) continue;
                String in = im.getNameAsString();
                if (types.containsKey(in)) refs.add(in);
            }
            for (FieldInfo f : c.fields.values()) if (f.typeFqn != null) refs.add(f.typeFqn);
            for (String r : refs) {
                CInfo t = types.get(r);
                CInfo tt = t.topLevel ? t : types.get(t.pkg + "." + t.simple.substring(0, t.simple.indexOf('.')));
                if (tt == null) continue;
                if (tt.domain.equals(c.domain) || tt.domain.equals(GLOBAL)) continue;
                if (!roles.contains(tt.role)) continue;
                out.computeIfAbsent(c.domain, k -> new TreeMap<>())
                        .computeIfAbsent(tt.domain, k -> new TreeMap<>())
                        .computeIfAbsent(tt.simple, k -> new TreeSet<>()).add(c.simple);
            }
        }
        return out;
    }

    void writeOutput() throws IOException {
        if (Files.exists(outDir)) {
            try (Stream<Path> s = Files.walk(outDir)) {
                for (Path p : s.sorted(Comparator.reverseOrder()).collect(Collectors.toList())) Files.delete(p);
            }
        }
        Files.createDirectories(outDir.resolve("nav"));
        Map<String, Map<String, Map<String, TreeSet<String>>>> deps = domainDeps();
        for (String d : domains()) write(outDir.resolve("nav/" + d + ".md"), detail(d, deps));
        write(outDir.resolve("nav/global.md"), detail(GLOBAL, deps));
        String nav = null;
        for (int level = 0; level <= 9; level++) {  // 7~9: 도메인이 아주 많을 때(부풀린 변형)만 쓰는 압축 단계
            nav = index(deps, level);
            if (nav.getBytes(StandardCharsets.UTF_8).length <= NAV_LIMIT) break;
        }
        if (nav.getBytes(StandardCharsets.UTF_8).length > NAV_LIMIT) throw new IllegalStateException("NAV.md exceeds limit");
        write(outDir.resolve("NAV.md"), nav);
    }

    static void write(Path p, String s) throws IOException {
        Files.writeString(p, s, StandardCharsets.UTF_8);
    }

    static String capList(List<String> l, int cap) {
        if (cap <= 0 || l.size() <= cap) return String.join(", ", l);
        return String.join(", ", l.subList(0, cap)) + " +" + (l.size() - cap);
    }

    List<String> controllerSummaries(String d) {
        List<String> out = new ArrayList<>();
        for (CInfo c : tops(d)) {
            if (!c.role.equals("controller")) continue;
            List<String> bp = new ArrayList<>();
            for (AnnotationExpr an : c.decl.getAnnotations())
                if (an.getNameAsString().equals("RequestMapping")) bp.addAll(annStrings(an, c, "value", "path"));
            out.add(c.simple + (bp.isEmpty() ? "" : " " + String.join("|", bp)));
        }
        return out;
    }

    List<String> entityNames(String d) {
        return tops(d).stream().filter(c -> c.role.equals("entity") && c.annotations.contains("Entity"))
                .map(c -> c.simple).collect(Collectors.toList());
    }

    /** 이 도메인이 발행하는 kafka 토픽·스프링 이벤트 → 구독 도메인 */
    List<String> messagingSummary(String d) {
        TreeMap<String, TreeSet<String>> m = new TreeMap<>();
        for (KafkaSend s : kafkaSends) {
            if (!s.domain().equals(d)) continue;
            TreeSet<String> subs = m.computeIfAbsent("kafka " + s.topic(), k -> new TreeSet<>());
            for (KafkaListen l : kafkaListens) if (l.topics().contains(s.topic())) subs.add(l.domain());
        }
        for (SpringPublish p : springPublishes) {
            if (!p.domain().equals(d)) continue;
            TreeSet<String> subs = m.computeIfAbsent("event " + p.eventType(), k -> new TreeSet<>());
            for (SpringListen l : springListens) if (l.eventType().equals(p.eventType())) subs.add(l.domain());
        }
        List<String> out = new ArrayList<>();
        m.forEach((k, v) -> out.add(k + "→" + (v.isEmpty() ? "(소스 내 구독 없음)" : String.join("/", v))));
        return out;
    }

    String index(Map<String, Map<String, Map<String, TreeSet<String>>>> deps, int level) {
        boolean counts = level < 1;
        int ctlCap = level >= 8 ? 1 : level >= 5 ? 2 : level >= 2 ? 4 : 0;
        int entCap = level >= 8 ? 1 : level >= 5 ? 2 : level >= 3 ? 4 : 0;
        int globalCap = level >= 6 ? 3 : level >= 4 ? 6 : 0;
        StringBuilder sb = new StringBuilder();
        sb.append("# NAV — 코드 구조 지도 (자동 추출)\n\n");
        sb.append("`src/main/java`에서 javaparser로 기계 추출한 구조 사실만 담았다. 코드 주석은 옮기지 않았다.\n");
        sb.append("도메인 상세는 `nav/<도메인>.md`, global 패키지는 `nav/global.md`: 엔드포인트 표, 호출 사슬, 클래스 목록(파일 경로), JPA 연관, 이벤트·Kafka, AOP.\n");
        sb.append("표기: 도메인 패키지 = `src/main/java/com/fream/back/domain/<도메인>/` · `A#m` = 클래스 A의 메서드 m · `~` = 이름 기반 추정 · api = 컨트롤러 base path · ent = @Entity · dep = 참조하는 다른 도메인(서비스·리포지토리·엔티티·메시징 클래스) · msg = 발행 → 구독 도메인.\n\n");
        sb.append("## 도메인\n\n");
        for (String d : domains()) {
            List<CInfo> ts = tops(d);
            StringBuilder line = new StringBuilder("- **" + d + "**");
            if (counts) line.append(" (").append(fileCount(d)).append(")");
            List<String> ctl = controllerSummaries(d);
            if (!ctl.isEmpty() && level < 9) line.append(" · api: ").append(capList(ctl, ctlCap));
            List<String> ent = entityNames(d);
            if (!ent.isEmpty() && level < 9) line.append(" · ent: ").append(capList(ent, entCap));
            Map<String, Map<String, TreeSet<String>>> dd = deps.getOrDefault(d, Map.of());
            if (!dd.isEmpty()) line.append(" · dep: ").append(level >= 7 ? String.valueOf(dd.size()) : String.join(", ", dd.keySet()));
            List<String> msg = messagingSummary(d);
            if (!msg.isEmpty() && level < 8) line.append(" · msg: ").append(String.join("; ", msg));
            sb.append(line).append("\n");
        }
        sb.append("\n## global (`src/main/java/com/fream/back/global/`)\n\n");
        TreeMap<String, List<String>> bySub = new TreeMap<>();
        for (CInfo c : tops(GLOBAL)) {
            String[] seg = c.sub.split("\\.");
            String s = c.sub.isEmpty() ? "(root)" : (seg.length >= 2 ? seg[0] + "." + seg[1] : seg[0]);
            bySub.computeIfAbsent(s, k -> new ArrayList<>()).add(c.simple);
        }
        bySub.forEach((s, l) -> {
            sb.append("- **").append(s).append("**");
            if (counts) sb.append(" (").append(l.size()).append(")");
            sb.append(": ").append(capList(l, globalCap)).append("\n");
        });
        return sb.toString();
    }

    String detail(String d, Map<String, Map<String, Map<String, TreeSet<String>>>> deps) {
        StringBuilder sb = new StringBuilder();
        List<CInfo> ts = tops(d);
        sb.append("# ").append(d).append(d.equals(GLOBAL) ? " (global 패키지와 루트)" : " 도메인").append("\n\n");
        sb.append("기준 경로: `").append(basePathOf(d)).append("` · 파일 ").append(fileCount(d)).append("개 · `~` = 이름 기반 추정(심볼 확인 실패 또는 소스에 선언 없는 상속·생성 메서드)\n\n");
        TreeSet<String> nt = noTypeFiles.getOrDefault(d, new TreeSet<>());
        if (!nt.isEmpty()) sb.append("타입 선언이 없는 파일(파싱 결과 클래스 0개): ").append(nt.stream().map(x -> "`" + x + "`").collect(Collectors.joining(", "))).append("\n\n");

        // 1. 엔드포인트
        sb.append("## 1. 엔드포인트\n\n");
        List<Endpoint> eps = endpoints.getOrDefault(d, List.of());
        if (eps.isEmpty()) sb.append("(없음)\n\n");
        else {
            sb.append("| HTTP | 경로 | 핸들러 | 직접 호출 |\n|---|---|---|---|\n");
            for (Endpoint e : eps)
                sb.append("| ").append(e.http()).append(" | `").append(e.path()).append("` | `").append(e.handler())
                        .append("` | ").append(e.calls().isEmpty() ? "" : "`" + String.join("`, `", e.calls()) + "`").append(" |\n");
            sb.append("\n");
        }

        // 2. 호출 사슬
        sb.append("## 2. 호출 사슬 (주입 필드 기준, 컨트롤러 제외)\n\n");
        Map<String, Map<String, LinkedHashSet<String>>> ch = chains.getOrDefault(d, Map.of());
        if (ch.isEmpty()) sb.append("(없음)\n\n");
        else {
            ch.forEach((cls, ms) -> {
                sb.append("- `").append(cls).append("`\n");
                ms.forEach((m, calls) -> sb.append("  - `").append(m).append("` → ").append(String.join(", ", calls)).append("\n"));
            });
            sb.append("\n");
        }

        // 3. 클래스 목록
        sb.append("## 3. 클래스 목록 (역할별)\n\n");
        Map<String, List<CInfo>> byRole = new LinkedHashMap<>();
        for (String r : ROLE_ORDER) byRole.put(r, new ArrayList<>());
        for (CInfo c : ts) byRole.get(c.role).add(c);
        for (String r : ROLE_ORDER) {
            List<CInfo> l = byRole.get(r);
            if (l.isEmpty()) continue;
            sb.append("### ").append(r).append(" (").append(l.size()).append(")\n\n");
            if (r.equals("exception") || r.equals("dto")) {
                for (CInfo c : l) sb.append("- `").append(c.simple).append("` ").append(relToBase(c)).append("\n");
                sb.append("\n");
                continue;
            }
            for (CInfo c : l) {
                sb.append("- `").append(c.simple).append("`");
                if (!c.kind.equals("class")) sb.append(" (").append(c.kind).append(")");
                sb.append(" ").append(relToBase(c));
                if (!c.supersRaw.isEmpty()) sb.append(" · extends/implements ").append(String.join(", ", c.supersRaw));
                List<String> pm = publicMethods(c);
                if (!pm.isEmpty()) sb.append(" · public: ").append(String.join(", ", pm));
                List<String> inj = injectedTypes(c);
                if (!inj.isEmpty()) sb.append(" · 주입: ").append(String.join(", ", inj));
                sb.append("\n");
            }
            sb.append("\n");
        }

        // 다른 도메인 의존
        sb.append("### 다른 도메인 참조 (import·주입 기준)\n\n");
        Map<String, Map<String, TreeSet<String>>> dd = deps.getOrDefault(d, Map.of());
        if (dd.isEmpty()) sb.append("(없음)\n");
        dd.forEach((od, m) -> {
            sb.append("- **").append(od).append("**: ");
            List<String> parts = new ArrayList<>();
            m.forEach((t, users) -> parts.add("`" + t + "` ← " + String.join(", ", users)));
            sb.append(String.join("; ", parts)).append("\n");
        });
        sb.append("\n### 이 도메인을 참조하는 다른 도메인\n\n");
        boolean any = false;
        for (var e : deps.entrySet()) {
            Map<String, TreeSet<String>> m = e.getValue().get(d);
            if (m == null) continue;
            any = true;
            sb.append("- **").append(e.getKey()).append("**: ").append(String.join(", ", m.keySet())).append("\n");
        }
        if (!any) sb.append("(없음)\n");
        sb.append("\n");

        // 4. JPA
        sb.append("## 4. JPA 엔티티 연관\n\n");
        boolean anyEnt = false;
        for (CInfo c : ts) {
            if (!(c.annotations.contains("Entity") || c.annotations.contains("MappedSuperclass") || c.annotations.contains("Embeddable"))) continue;
            anyEnt = true;
            sb.append("- `").append(c.simple).append("`");
            List<String> anns = new ArrayList<>();
            for (AnnotationExpr an : c.decl.getAnnotations()) {
                String n = an.getNameAsString();
                if (n.equals("Entity") || n.equals("Embeddable") || INHERIT_ANN.contains(n) || n.equals("Table")) anns.add(an.toString());
            }
            sb.append(" ").append(String.join(" ", anns));
            if (!c.supersRaw.isEmpty()) sb.append(" · extends ").append(String.join(", ", c.supersRaw));
            sb.append("\n");
            for (FieldInfo f : c.fields.values()) {
                List<String> fa = new ArrayList<>();
                boolean assoc = false;
                for (AnnotationExpr an : f.decl.getAnnotations()) {
                    String n = an.getNameAsString();
                    if (ASSOC_ANN.contains(n)) assoc = true;
                    if (ASSOC_ANN.contains(n) || EXTRA_FIELD_ANN.contains(n) || n.equals("JoinColumn") || n.equals("JoinTable")) fa.add(an.toString());
                }
                if (fa.isEmpty()) continue;
                String target = assocTarget(c, f.decl.getVariable(0).getType());
                sb.append("  - ").append(f.name).append(": ").append(String.join(" ", fa)).append(" → `").append(target).append("`");
                if (assoc) {
                    String tf = resolveName(target, c);
                    if (tf != null && !types.get(tf).domain.equals(d)) sb.append(" [").append(types.get(tf).domain).append("]");
                }
                sb.append("\n");
            }
        }
        if (!anyEnt) sb.append("(없음)\n");
        // 다른 도메인 → 이 도메인 엔티티
        List<String> incoming = new ArrayList<>();
        for (CInfo c : types.values()) {
            if (!c.topLevel || c.domain.equals(d) || !c.annotations.contains("Entity")) continue;
            for (FieldInfo f : c.fields.values()) {
                String assocAnn = null;
                for (AnnotationExpr an : f.decl.getAnnotations()) if (ASSOC_ANN.contains(an.getNameAsString())) assocAnn = an.getNameAsString();
                if (assocAnn == null) continue;
                String target = assocTarget(c, f.decl.getVariable(0).getType());
                String tf = resolveName(target, c);
                if (tf != null && types.get(tf).domain.equals(d))
                    incoming.add("`" + c.simple + "." + f.name + "` @" + assocAnn + " → `" + target + "` [" + c.domain + "]");
            }
        }
        if (!incoming.isEmpty()) {
            sb.append("\n다른 도메인 엔티티에서 들어오는 연관:\n\n");
            for (String s : incoming) sb.append("- ").append(s).append("\n");
        }
        sb.append("\n");

        // 5. 이벤트·메시징
        sb.append("## 5. 이벤트·메시징\n\n");
        StringBuilder ev = new StringBuilder();
        for (KafkaSend s : kafkaSends) {
            if (!s.domain().equals(d)) continue;
            List<String> subs = new ArrayList<>();
            for (KafkaListen l : kafkaListens) if (l.topics().contains(s.topic())) subs.add("`" + l.owner() + "#" + l.method() + "` [" + l.domain() + "]");
            ev.append("- 발행 kafka `").append(s.topic()).append("`: `").append(s.owner()).append("#").append(s.method())
                    .append("` → ").append(subs.isEmpty() ? "(소스 내 구독 없음)" : String.join(", ", subs)).append("\n");
        }
        for (KafkaListen l : kafkaListens) {
            if (!l.domain().equals(d)) continue;
            List<String> pubs = new ArrayList<>();
            for (KafkaSend s : kafkaSends) if (l.topics().contains(s.topic())) pubs.add("`" + s.owner() + "#" + s.method() + "` [" + s.domain() + "]");
            ev.append("- 구독 kafka `").append(String.join("`, `", l.topics())).append("`: `").append(l.owner()).append("#").append(l.method()).append("`");
            if (!l.extra().isEmpty()) ev.append(" (").append(l.extra()).append(")");
            ev.append(" ← ").append(pubs.isEmpty() ? "(소스 내 발행 없음)" : String.join(", ", pubs)).append("\n");
        }
        for (SpringPublish p : springPublishes) {
            if (!p.domain().equals(d)) continue;
            List<String> subs = new ArrayList<>();
            for (SpringListen l : springListens) if (l.eventType().equals(p.eventType())) subs.add("`" + l.owner() + "#" + l.method() + "` @" + l.ann() + " [" + l.domain() + "]");
            ev.append("- 발행 event `").append(p.eventType()).append("`: `").append(p.owner()).append("#").append(p.method())
                    .append("` → ").append(subs.isEmpty() ? "(소스 내 구독 없음)" : String.join(", ", subs)).append("\n");
        }
        for (SpringListen l : springListens) {
            if (!l.domain().equals(d)) continue;
            List<String> pubs = new ArrayList<>();
            for (SpringPublish p : springPublishes) if (l.eventType().equals(p.eventType())) pubs.add("`" + p.owner() + "#" + p.method() + "` [" + p.domain() + "]");
            ev.append("- 구독 event `").append(l.eventType()).append("` @").append(l.ann()).append(": `").append(l.owner()).append("#").append(l.method())
                    .append("` ← ").append(pubs.isEmpty() ? "(소스 내 발행 없음)" : String.join(", ", pubs)).append("\n");
        }
        sb.append(ev.length() == 0 ? "(없음)\n" : ev.toString()).append("\n");

        // 6. AOP
        sb.append("## 6. AOP\n\n");
        StringBuilder ao = new StringBuilder();
        TreeSet<String> mine = new TreeSet<>();
        for (Advice a : advices) {
            if (!a.domain().equals(d)) continue;
            ao.append("- `").append(a.aspect()).append("#").append(a.method()).append("` ").append(a.annText());
            if (!a.boundAnnFqns().isEmpty())
                ao.append(" · 바인딩: ").append(a.boundAnnFqns().stream().map(b -> "`@" + types.get(b).simple + "`").collect(Collectors.joining(", ")));
            ao.append("\n");
            mine.addAll(a.boundAnnFqns());
            for (String fq : types.keySet())
                if ("@interface".equals(types.get(fq).kind) && a.annText().contains(fq)) mine.add(fq);
        }
        if (!mine.isEmpty()) {
            ao.append("\n어노테이션 적용 지점(메서드에 붙은 곳):\n\n");
            for (String b : mine) {
                List<String> sites = annotatedSites.getOrDefault(b, List.of());
                ao.append("- `@").append(types.get(b).simple).append("`: ")
                        .append(sites.isEmpty() ? "(없음)" : sites.stream()
                                .map(x -> x.startsWith(d + ":") ? x.substring(d.length() + 1) : x)
                                .map(x -> "`" + x + "`").collect(Collectors.joining(", ")))
                        .append("\n");
            }
        }
        // 이 도메인 메서드에 붙은, aspect가 바인딩하는 어노테이션
        Set<String> adviceAnns = new TreeSet<>();
        for (Advice a : advices) adviceAnns.addAll(a.boundAnnFqns());
        for (Advice a : advices) {
            for (String fq : types.keySet()) {
                if ("@interface".equals(types.get(fq).kind) && a.annText().contains(fq)) adviceAnns.add(fq);
            }
        }
        List<String> here = new ArrayList<>();
        for (String fq : adviceAnns) {
            for (String s : annotatedSites.getOrDefault(fq, List.of())) {
                if (s.startsWith(d + ":") && !types.get(fq).domain.equals(d))
                    here.add("`@" + types.get(fq).simple + "` [" + types.get(fq).domain + "] on `" + s.substring(d.length() + 1) + "`");
            }
        }
        if (!here.isEmpty()) {
            ao.append("\n다른 도메인 aspect 대상 어노테이션이 붙은 이 도메인 메서드:\n\n");
            for (String s : here) ao.append("- ").append(s).append("\n");
        }
        sb.append(ao.length() == 0 ? "(없음)\n" : ao.toString()).append("\n");

        return sb.toString();
    }

    String assocTarget(CInfo c, Type t) {
        if (t.isClassOrInterfaceType()) {
            ClassOrInterfaceType ct = t.asClassOrInterfaceType();
            if (ct.getTypeArguments().isPresent() && !ct.getTypeArguments().get().isEmpty()) {
                NodeList<Type> args = ct.getTypeArguments().get();
                return args.get(args.size() - 1).asString();
            }
            return ct.getNameWithScope();
        }
        return t.asString();
    }
}
