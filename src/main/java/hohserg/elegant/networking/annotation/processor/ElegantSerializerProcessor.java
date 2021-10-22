package hohserg.elegant.networking.annotation.processor;

import com.google.common.collect.ImmutableMap;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import hohserg.elegant.networking.annotation.processor.code.generator.AbstractGenerator;
import hohserg.elegant.networking.annotation.processor.code.generator.TypeUtils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static hohserg.elegant.networking.Refs.*;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static javax.lang.model.element.ElementKind.*;

@SupportedAnnotationTypes({ElegantPacket_name, ElegantSerializable_name, Mod_name_1_8_plus, Mod_name_1_7_minus})
public class ElegantSerializerProcessor extends BaseProcessor implements TypeUtils {

    private Set<TypeElement> allElegantPackets = new HashSet<>();
    private Optional<String> maybeModid = Optional.empty();

    private InheritanceUtils inheritanceUtils;
    private CodeGenerator codeGenerator;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        inheritanceUtils = new InheritanceUtils(typeUtils);
        specials = initSpecials();
        codeGenerator = new CodeGenerator(this, typeUtils, elementUtils, specials, messager);

        maybeModid = loadCachedModid();
    }

    private ImmutableMap<String, AbstractGenerator> initSpecials() {
        ImmutableMap.Builder<String, AbstractGenerator> specialsBuilder = ImmutableMap.builder();

        mutableCollectionSpecial(specialsBuilder, Collection.class, ArrayList.class);
        mutableCollectionSpecial(specialsBuilder, List.class, ArrayList.class);
        mutableCollectionSpecial(specialsBuilder, ArrayList.class, ArrayList.class);
        mutableCollectionSpecial(specialsBuilder, LinkedList.class, LinkedList.class);
        mutableCollectionSpecial(specialsBuilder, Queue.class, LinkedList.class);
        specialsBuilder.put(ImmutableList_name, SpecialTypeSupport.immutableCollectionSpecial(typeUtils, elementUtils, ImmutableList_name));//using string contant because shadowing of library

        mutableCollectionSpecial(specialsBuilder, Set.class, HashSet.class);
        mutableCollectionSpecial(specialsBuilder, HashSet.class, HashSet.class);
        specialsBuilder.put(EnumSet.class.getCanonicalName(), SpecialTypeSupport.commonCollectionSpecial(typeUtils, elementUtils, EnumSet.class.getCanonicalName(), type -> EnumSet.class.getCanonicalName() + ".noneOf(" + type.getTypeArguments().get(0) + ".class)", "$L.add(e)", "$L"));
        mutableCollectionSpecial(specialsBuilder, LinkedHashSet.class, LinkedHashSet.class);
        specialsBuilder.put(ImmutableSet_name, SpecialTypeSupport.immutableCollectionSpecial(typeUtils, elementUtils, ImmutableSet_name));//using string contant because shadowing of library

        mutableMapSpecial(specialsBuilder, Map.class, HashMap.class);
        mutableMapSpecial(specialsBuilder, HashMap.class, HashMap.class);
        mutableMapSpecial(specialsBuilder, SortedMap.class, TreeMap.class);
        mutableMapSpecial(specialsBuilder, NavigableMap.class, TreeMap.class);
        mutableMapSpecial(specialsBuilder, TreeMap.class, TreeMap.class);
        mutableMapSpecial(specialsBuilder, LinkedHashMap.class, LinkedHashMap.class);
        specialsBuilder.put(ImmutableMap_name, SpecialTypeSupport.immutableMapSpecial(typeUtils, elementUtils, ImmutableMap_name));//using string contant because shadowing of library
        specialsBuilder.put(EnumMap.class.getCanonicalName(), SpecialTypeSupport.commonMapSpecial(typeUtils, elementUtils, EnumMap.class.getCanonicalName(), type -> "new " + EnumMap.class.getCanonicalName() + "(" + type.getTypeArguments().get(0) + ".class)", "$L.put($L,$L)", "$L"));

        specialsBuilder.put(Optional.class.getCanonicalName(), new SpecialTypeSupport.OptionalSupport(typeUtils, elementUtils));

        specialsBuilder.put(Pair_name, new SpecialTypeSupport.PairSupport(typeUtils, elementUtils, Pair_name));

        return specialsBuilder.build();
    }

    private void mutableCollectionSpecial(ImmutableMap.Builder<String, AbstractGenerator> specialsBuilder, Class fieldType, Class collectionType) {
        specialsBuilder.put(fieldType.getCanonicalName(), SpecialTypeSupport.mutableCollectionSpecial(typeUtils, elementUtils, collectionType.getCanonicalName()));
    }

    private void immutableCollectionSpecial(ImmutableMap.Builder<String, AbstractGenerator> specialsBuilder, Class fieldType, Class collectionType) {
        specialsBuilder.put(fieldType.getCanonicalName(), SpecialTypeSupport.immutableCollectionSpecial(typeUtils, elementUtils, collectionType.getCanonicalName()));
    }

    private void mutableMapSpecial(ImmutableMap.Builder<String, AbstractGenerator> specialsBuilder, Class fieldType, Class collectionType) {
        specialsBuilder.put(fieldType.getCanonicalName(), SpecialTypeSupport.mutableMapSpecial(typeUtils, elementUtils, collectionType.getCanonicalName()));
    }

    private void immutableMapSpecial(ImmutableMap.Builder<String, AbstractGenerator> specialsBuilder, Class fieldType, Class collectionType) {
        specialsBuilder.put(fieldType.getCanonicalName(), SpecialTypeSupport.immutableMapSpecial(typeUtils, elementUtils, collectionType.getCanonicalName()));
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        noteDebug("ElegantSerializerProcessor#process/annotations " + annotations);
        handleUnexpectedErrors(() -> {

            if (!maybeModid.isPresent())
                Stream.of(Mod_name_1_8_plus, Mod_name_1_7_minus).map(elementUtils::getTypeElement).filter(Objects::nonNull).findAny()
                        .ifPresent(Mod_element -> {
                            if (annotations.contains(Mod_element)) {
                                maybeModid = roundEnv.getElementsAnnotatedWith(Mod_element).iterator().next()
                                        .getAnnotationMirrors()
                                        .stream()
                                        .filter(am -> am.getAnnotationType().asElement() == Mod_element)
                                        .flatMap(am -> am.getElementValues()
                                                .entrySet()
                                                .stream()
                                                .filter(e -> e.getKey().getSimpleName().toString().equals("modid") || e.getKey().getSimpleName().toString().equals("value"))
                                                .map(e -> e.getValue().getValue().toString())
                                        ).findAny();
                            }
                        });

            if (isExistsSerializableInCurrentRound(annotations, ElegantPacket_name, ElegantSerializable_name)) {

                List<TypeElement> elegantPackets = roundEnv.getElementsAnnotatedWith(elementUtils.getTypeElement(ElegantPacket_name))
                        .stream()
                        .filter(validate(e -> e.getKind() == CLASS || e.getKind() == INTERFACE || e.getKind() == ENUM, "@" + elementUtils.getTypeElement(ElegantPacket_name).getSimpleName() + " can be applied only to classes and interfaces"))
                        .map(e -> ((TypeElement) e))
                        .filter(validate(e -> e.getModifiers().contains(Modifier.PUBLIC), "The elegant packet class must be public"))
                        .filter(validate(isHaveInterface(ClientToServerPacket_name, ServerToClientPacket_name), "The elegant packet class must implement ClientToServerPacket or ServerToClientPacket"))
                        .collect(toList());


                elegantPackets.forEach(e -> noteDetailed("Found elegant packet class", e));


                List<TypeElement> elegantSerializable = roundEnv.getElementsAnnotatedWith(elementUtils.getTypeElement(ElegantSerializable_name))
                        .stream()
                        .filter(validate(e -> e.getKind() == CLASS || e.getKind() == INTERFACE || e.getKind() == ENUM, "@" + elementUtils.getTypeElement(ElegantSerializable_name).getSimpleName() + " can be applied only to classes and interfaces"))
                        .map(e -> ((TypeElement) e))
                        .filter(validate(e -> e.getModifiers().contains(Modifier.PUBLIC), "The elegant serializable class must be public"))
                        .filter(validate(isHaveInterface(IByteBufSerializable_name), "The elegant serializable class must implement ClientToServerPacket or ServerToClientPacket"))
                        .collect(toList());

                elegantSerializable.forEach(e -> noteDetailed("Found elegant serializable class", e));

                elegantSerializable.addAll(elegantPackets);
                allElegantPackets.addAll(elegantPackets);

                elegantSerializable.forEach(e -> {
                    Map<TypeMirror, List<? extends TypeMirror>> types = new TypeMap<>();
                    getAllSerializableTypes(e.asType(), types);
                    noteDebug("For " + e.getQualifiedName() + "required to serialize " + types);

                    List<MethodSpec> serializationMethods = types.entrySet().stream()
                            .sorted(Comparator.comparing(i -> i.getKey().toString()))
                            .flatMap(i -> {
                                try {
                                    return generateMethodsForType(i.getKey(), i.getValue());
                                } catch (AnnotationProcessorException exception) {
                                    errorAPException(exception);
                                    return Stream.empty();
                                }
                            })
                            .collect(toList());


                    writeJavaFile(
                            e,
                            codeGenerator.generateSerializerClass(e, serializationMethods),

                            "Unable to write serializer class for elegant packet "
                    );
                });
            }

            if (!allElegantPackets.isEmpty())
                maybeModid.ifPresent(modid -> {
                    noteDebug("Current modid is " + modid);
                    allElegantPackets.forEach(e ->
                            writeJavaFile(
                                    e,
                                    codeGenerator.generatePacketProvider(e, modid),
                                    "Unable to write packet provider class for elegant packet "
                            )
                    );
                    allElegantPackets.clear();
                });

            if(roundEnv.processingOver())
                maybeModid.ifPresent(this::saveCachedModid);

        });
        return false;
    }

    private void writeJavaFile(TypeElement basedOn, JavaFile javaFile, String failureMessage) {
        try {
            javaFile.writeTo(filer);
        } catch (IOException exception) {
            error(failureMessage + basedOn, exception);
        }
    }

    private final String cachedModidLocation = tmpFolder + "cachedModid.txt";

    private Optional<String> loadCachedModid() {
        try {
            FileObject resourceForRead = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", cachedModidLocation);

            try (
                    InputStream inputStream = resourceForRead.openInputStream();
                    Scanner s = new Scanner(inputStream).useDelimiter("\\A")) {
                return s.hasNext() ? Optional.of(s.next()) : Optional.empty();
            }
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private void saveCachedModid(String modid) {
        try {
            FileObject resourceForWrite = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", cachedModidLocation);

            try (Writer writer = resourceForWrite.openWriter()) {
                writer.write(modid);
                writer.flush();
            }
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Cannot write cachedModid file: " + e);
        }
    }

    public boolean isExistsSerializableInCurrentRound(Set<? extends TypeElement> existsAnnotations, String... requiredAnnotations) {
        return Arrays.stream(requiredAnnotations).map(elementUtils::getTypeElement).filter(Objects::nonNull).anyMatch(existsAnnotations::contains);
    }

    private Stream<MethodSpec> generateMethodsForType(TypeMirror type, List<? extends TypeMirror> implementations) {
        if (type instanceof DeclaredType) {
            DeclaredType declaredType = (DeclaredType) type;
            TypeElement element = (TypeElement) declaredType.asElement();
            AbstractGenerator specialTypeSupport = specials.get(element.getQualifiedName().toString());
            if (specialTypeSupport != null)
                return specialTypeSupport.generateMethodsForType(type, implementations);
            else {
                if (element.getKind() == ENUM)
                    return codeGenerator.enumMethods(declaredType);
                else if (element.getModifiers().contains(Modifier.ABSTRACT))
                    return codeGenerator.genericMethods(declaredType, (List<DeclaredType>) implementations);
                else
                    return Stream.concat(codeGenerator.genericMethods(declaredType, (List<DeclaredType>) implementations), codeGenerator.concreticMethods(declaredType));
            }

        } else if (type instanceof ArrayType)
            return codeGenerator.arrayMethods((ArrayType) type);
        else
            throw new IllegalArgumentException("Unsupported type: " + type);
    }

    private Map<String, AbstractGenerator> specials;

    public void getAllSerializableTypes(TypeMirror type, Map<TypeMirror, List<? extends TypeMirror>> types) {
        if (!types.containsKey(type)) {
            if (type instanceof DeclaredType) {
                TypeElement element = (TypeElement) ((DeclaredType) type).asElement();
                AbstractGenerator specialTypeSupport = specials.get(element.getQualifiedName().toString());
                noteDebug("getAllSerializableTypes " + element.getQualifiedName().toString());
                if (specialTypeSupport != null)
                    specialTypeSupport.getAllSerializableTypes(this, (DeclaredType) type, types);
                else if (nonExistsSerializer(type)) {
                    List<DeclaredType> allImplementations = getAllImplementations(((DeclaredType) type));
                    types.put(type, allImplementations);
                    Set<TypeMirror> fieldTypes = element.getEnclosedElements().stream()
                            .filter(e -> e.getKind() == FIELD)
                            .filter(e -> !e.getModifiers().contains(Modifier.STATIC) && !e.getModifiers().contains(Modifier.TRANSIENT))
                            .peek(e -> {
                                if (e.asType() instanceof DeclaredType && !((DeclaredType) ((DeclaredType) e.asType()).asElement().asType()).getTypeArguments().isEmpty() && ((DeclaredType) e.asType()).getTypeArguments().isEmpty())
                                    throw new AnnotationProcessorException(e, "All type parameters must be passed");//"Parameterizable type must be parameterized" xD
                            })
                            .map(Element::asType)
                            .map(refineParameterizedTypes((DeclaredType) type))
                            .filter(this::nonExistsSerializer)
                            .collect(toSet());
                    fieldTypes.forEach(t -> getAllSerializableTypes(t, types));
                    allImplementations.forEach(t -> getAllSerializableTypes(t, types));
                }
            } else if (type instanceof ArrayType) {
                types.put(type, Collections.emptyList());
                getAllSerializableTypes(((ArrayType) type).getComponentType(), types);
            }
        }
    }

    private boolean nonExistsSerializer(TypeMirror type) {
        TypeElement holder = elementUtils.getTypeElement(ISerializer_name);
        String methodName = codeGenerator.getGenericSerializeMethodName(type);
        return elementUtils
                .getAllMembers(holder)
                .stream()
                .noneMatch(m -> m.getKind() == METHOD && m.getSimpleName().toString().equals(methodName));
    }

    private List<DeclaredType> getAllImplementations(DeclaredType type) {
        Element element = type.asElement();
        if (element.getModifiers().contains(Modifier.FINAL))
            return Collections.singletonList(type);
        else {
            Element packageElement = elementUtils.getPackageOf(element);
            //noteDetailed("getAllImplementations " + type);
            List<DeclaredType> r = allClassesInPackage(packageElement).filter(t -> typeUtils.directSupertypes(t).contains(type)).collect(toList());
            if (!type.asElement().getModifiers().contains(Modifier.ABSTRACT))
                r.add(type);
            return r;
        }
    }

    private Stream<DeclaredType> allClassesInPackage(Element container) {
        //noteDetailed("allClassesInPackage " + container);
        Stream<DeclaredType> inCurrent = container.getEnclosedElements().stream().filter(e -> e instanceof TypeElement).flatMap(e -> Stream.concat(Stream.of(((DeclaredType) e.asType())), allClassesInPackage(e)));
        Stream<DeclaredType> inSubPackages = container.getEnclosedElements().stream().filter(e -> e instanceof PackageElement).map(e -> (PackageElement) e).flatMap(this::allClassesInPackage);
        return Stream.concat(inCurrent, inSubPackages);
    }

    private Predicate<TypeElement> isHaveInterface(String... atLeastOne) {
        return e -> Arrays.stream(atLeastOne).anyMatch(i -> inheritanceUtils.isImplements(e.asType(), elementUtils.getTypeElement(i)));
    }

    public <E extends Element> Predicate<E> validate(Predicate<E> f, String errorMsg) {
        return e -> {
            if (f.test(e))
                return true;
            else {
                error(errorMsg, e);
                return false;
            }
        };
    }
}
