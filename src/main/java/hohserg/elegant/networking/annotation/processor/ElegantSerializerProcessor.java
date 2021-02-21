package hohserg.elegant.networking.annotation.processor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import hohserg.elegant.networking.Refs;
import hohserg.elegant.networking.annotation.processor.code.generator.AbstractGenerator;
import hohserg.elegant.networking.annotation.processor.code.generator.TypeUtils;
import org.apache.commons.lang3.tuple.Pair;

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
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static hohserg.elegant.networking.Refs.ElegantPacket_name;
import static hohserg.elegant.networking.Refs.ISerializer_name;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static javax.lang.model.element.ElementKind.*;

@SupportedAnnotationTypes(Refs.ElegantPacket_name)
public class ElegantSerializerProcessor extends BaseProcessor implements TypeUtils {

    private Set<TypeElement> packetsFromService;
    private Set<String> serializersFromService;
    public InheritanceUtils inheritanceUtils;

    public static Elements elementUtils1;
    public static Types typeUtils1;
    private CodeGenerator codeGenerator;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        inheritanceUtils = new InheritanceUtils(typeUtils);
        elementUtils1 = processingEnv.getElementUtils();
        typeUtils1 = processingEnv.getTypeUtils();
        codeGenerator = new CodeGenerator(typeUtils, elementUtils);

        specials = initSpecials();
    }

    private ImmutableMap<String, AbstractGenerator> initSpecials() {
        ImmutableMap.Builder<String, AbstractGenerator> specialsBuilder = ImmutableMap.builder();

        mutableCollectionSpecial(specialsBuilder, Collection.class, ArrayList.class);
        mutableCollectionSpecial(specialsBuilder, List.class, ArrayList.class);
        mutableCollectionSpecial(specialsBuilder, ArrayList.class, ArrayList.class);
        mutableCollectionSpecial(specialsBuilder, LinkedList.class, LinkedList.class);
        mutableCollectionSpecial(specialsBuilder, Queue.class, LinkedList.class);
        immutableCollectionSpecial(specialsBuilder, ImmutableList.class, ImmutableList.class);

        mutableCollectionSpecial(specialsBuilder, Set.class, HashSet.class);
        mutableCollectionSpecial(specialsBuilder, HashSet.class, HashSet.class);
        specialsBuilder.put(EnumSet.class.getCanonicalName(), SpecialTypeSupport.commonCollectionSpecial(typeUtils, elementUtils, EnumSet.class.getCanonicalName(), type -> EnumSet.class.getCanonicalName() + ".noneOf(" + type.getTypeArguments().get(0) + ".class)", "$L.add(e)", "$L"));
        mutableCollectionSpecial(specialsBuilder, LinkedHashSet.class, LinkedHashSet.class);
        immutableCollectionSpecial(specialsBuilder, ImmutableSet.class, ImmutableSet.class);

        mutableMapSpecial(specialsBuilder, Map.class, HashMap.class);
        mutableMapSpecial(specialsBuilder, HashMap.class, HashMap.class);
        mutableMapSpecial(specialsBuilder, SortedMap.class, TreeMap.class);
        mutableMapSpecial(specialsBuilder, NavigableMap.class, TreeMap.class);
        mutableMapSpecial(specialsBuilder, TreeMap.class, TreeMap.class);
        mutableMapSpecial(specialsBuilder, LinkedHashMap.class, LinkedHashMap.class);
        immutableMapSpecial(specialsBuilder, ImmutableMap.class, ImmutableMap.class);
        specialsBuilder.put(EnumMap.class.getCanonicalName(), SpecialTypeSupport.commonMapSpecial(typeUtils, elementUtils, EnumMap.class.getCanonicalName(), type -> "new " + EnumMap.class.getCanonicalName() + "(" + type.getTypeArguments().get(0) + ".class)", "$L.put($L,$L)", "$L"));

        specialsBuilder.put(Optional.class.getCanonicalName(), new SpecialTypeSupport.OptionalSupport(typeUtils, elementUtils));

        specialsBuilder.put(Pair.class.getCanonicalName(), new SpecialTypeSupport.PairSupport(typeUtils, elementUtils));

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
        handleUnexpectedErrors(() -> {
            if (annotations.contains(elementUtils.getTypeElement(ElegantPacket_name))) {
                List<TypeElement> elegantPackets = roundEnv.getElementsAnnotatedWith(elementUtils.getTypeElement(ElegantPacket_name))
                        .stream()
                        .filter(validate(e -> e.getKind() == CLASS || e.getKind() == INTERFACE || e.getKind() == ENUM, "@" + elementUtils.getTypeElement(ElegantPacket_name).getSimpleName() + " can be applied only to classes and interfaces"))
                        .map(e -> ((TypeElement) e))
                        .filter(validate(e -> e.getModifiers().contains(Modifier.PUBLIC), "The elegant packet class must be public"))
                        .filter(validate(isHaveInterface(Refs.ClientToServerPacket_name, Refs.ServerToClientPacket_name), "The elegant packet class must implement ClientToServerPacket or ServerToClientPacket"))
                        .collect(toList());

                elegantPackets.forEach(e -> noteDetailed(e, "Found elegant packet class"));

                elegantPackets.forEach(e -> {
                    Map<TypeMirror, List<? extends TypeMirror>> types = new HashMap<>();
                    getAllSerializableTypes(e.asType(), types);
                    noteDetailed("Required to serialize " + types);

                    List<MethodSpec> serializationMethods = types.entrySet().stream()
                            .sorted(Comparator.comparing(i -> i.getKey().toString()))
                            .flatMap(i -> {
                                try {
                                    return generateMethodsForType(i.getKey(), i.getValue());
                                } catch (AnnotationProcessorException exception) {
                                    error(exception.element, exception.msg);
                                    return Stream.empty();
                                }
                            })
                            .collect(toList());
                    JavaFile javaFile = codeGenerator.generateSerializerClass(e, serializationMethods);


                    try {
                        javaFile.writeTo(filer);
                    } catch (IOException exception) {
                        error(e, "Unable to write serializer class for elegant packet with exception " + e.toString());
                    }
                });
            }
        });
        return false;
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
                if (specialTypeSupport != null)
                    specialTypeSupport.getAllSerializableTypes(this, (DeclaredType) type, types);
                else {
                    //note("toConcreteTypeParameter " + toConcreteTypeParameter);

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
            List<DeclaredType> r = allClassesInPackage(packageElement).filter(t -> typeUtils.directSupertypes(t).contains(type)).collect(toList());
            if (!type.asElement().getModifiers().contains(Modifier.ABSTRACT))
                r.add(type);
            return r;
        }
    }

    private Stream<DeclaredType> allClassesInPackage(Element container) {
        Stream<DeclaredType> inCurrent = container.getEnclosedElements().stream().filter(e -> e instanceof TypeElement).flatMap(e -> Stream.concat(Stream.of(((DeclaredType) e.asType())), allClassesInPackage(e)));
        Stream<DeclaredType> inSubPackages = container.getEnclosedElements().stream().filter(e -> e instanceof PackageElement).map(e -> (PackageElement) e).flatMap(this::allClassesInPackage);
        return Stream.concat(inCurrent, inSubPackages);
    }

    private Predicate<TypeElement> isHaveInterface(String... atLeastOne) {
        return e -> Arrays.stream(atLeastOne).anyMatch(i -> inheritanceUtils.isImplements(e.asType(), elementUtils1.getTypeElement(i)));
    }

    public <E extends Element> Predicate<E> validate(Predicate<E> f, String errorMsg) {
        return e -> {
            if (f.test(e))
                return true;
            else {
                error(e, errorMsg);
                return false;
            }
        };
    }
}
