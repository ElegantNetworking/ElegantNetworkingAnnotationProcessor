package hohserg.elegant.networking.annotation.processor;

import com.squareup.javapoet.*;
import hohserg.elegant.networking.annotation.processor.dom.*;
import hohserg.elegant.networking.api.ElegantPacket;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hohserg.elegant.networking.annotation.processor.dom.DataClassRepr.prepare;
import static java.util.stream.Collectors.*;
import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.METHOD;
import static javax.lang.model.element.Modifier.*;

public class ElegantPacketProcessor extends AbstractProcessor {


    public ClassName byteBuf = ClassName.get("io.netty.buffer", "ByteBuf");

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @SneakyThrows
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<TypeElement> elegantPackets = new HashSet<>();
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(ElegantPacket.class)) {
            if (annotatedElement.getKind() == CLASS) {
                TypeElement typeElement = (TypeElement) annotatedElement;

                if (annotatedElement.getModifiers().contains(Modifier.PUBLIC)) {
                    if (havePacketInterfaces(typeElement)) {
                        currentElement = typeElement;
                        note(annotatedElement, "Found elegant packet class " + typeElement.getQualifiedName());

                        //elementUtils.getAllMembers(typeElement).forEach(e -> note(typeElement, "subelement " + e + e.getModifiers()));
                        //typeElement.getEnclosedElements().forEach(e -> note(typeElement, "subelement " + e));
                        elegantPackets.add(typeElement);
                    } else
                        error(annotatedElement, "The elegant packet class must implement ClientToServerPacket or ServerToClientPacket");
                } else
                    error(annotatedElement, "The elegant packet class must be public");
            } else
                error(annotatedElement, "@ElegantPacket can be applied only to classes");
        }

        List<TypeElement> sortedPackets = elegantPackets.stream().sorted(Comparator.comparing(e -> e.getQualifiedName().toString())).collect(Collectors.toList());
        for (int i = 0; i < sortedPackets.size(); i++) {
            int packetId = i + 1;
            TypeElement packet = sortedPackets.get(i);
            try {
                buildSerializatorClass(prepare(packet), packetId);
            } catch (Exception e) {
                error(packet, "Failure on building serializer: \n" + e + "\n" + Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(joining("\n")));
            }
        }
        return false;
    }

    private void buildSerializatorClass(DataClassRepr typeElement, int packetId) throws IOException {
        /*typeElement.getFields().forEach(f -> note(f.getName() + f.getModifiers()));
        typeElement.getMethods().forEach(f -> note(f.toString()));
        typeElement.getConstructors().forEach(f -> note(f.toString()));*/

        Set<ClassRepr> serializableTypes = getAllSerializableTypes(typeElement).collect(toSet());

        note("Required to serializer follow types: \n\n\n" + serializableTypes.stream().map(Objects::toString).collect(joining("\n")));

        List<MethodSpec> requiredMethods = getRequiredMethods(serializableTypes).flatMap(this::generateMethod).collect(toList());


        ClassName packet = ClassName.get(typeElement.getElement());
        TypeSpec serializer = TypeSpec.classBuilder(typeElement.getSimpleName() + "Serializer")
                .addModifiers(PUBLIC)
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get("hohserg.elegant.networking.impl", "ISerializer"), packet))
                .addMethod(generateMainSerializeMethod(typeElement, packet))
                .addMethods(requiredMethods)
                .build();

        JavaFile javaFile = JavaFile.builder(elementUtils.getPackageOf(typeElement.getElement()).getQualifiedName().toString(), serializer).build();

        javaFile.writeTo(filer);
    }

    private Stream<MethodSpec> generateMethod(MethodRequirement methodRequirement) {
        if (methodRequirement instanceof MethodRequirement.GenericMethod) {
            return Stream.of(
                    generateGenericSerializer(((MethodRequirement.GenericMethod) methodRequirement)),
                    generateGenericUnserializer(((MethodRequirement.GenericMethod) methodRequirement))
            );

        } else if (methodRequirement instanceof MethodRequirement.ConcreticMethod) {
            return Stream.of(
                    generateConcreticSerializer(((MethodRequirement.ConcreticMethod) methodRequirement).getForType()),
                    generateConcreticUnserializer(((MethodRequirement.ConcreticMethod) methodRequirement).getForType())
            );

        } else if (methodRequirement instanceof MethodRequirement.MapMethod) {
            return Stream.of(
                    generateMapSerializer(((MethodRequirement.MapMethod) methodRequirement)),
                    generateMapUnserializer(((MethodRequirement.MapMethod) methodRequirement))
            );

        } else if (methodRequirement instanceof MethodRequirement.CollectionMethod) {
            return Stream.of(
                    generateCollectionSerializer(((MethodRequirement.CollectionMethod) methodRequirement)),
                    generateCollectionUnserializer(((MethodRequirement.CollectionMethod) methodRequirement))
            );

        } else
            throw new UnsupportedOperationException(methodRequirement.toString());
    }

    private MethodSpec generateCollectionSerializer(MethodRequirement.CollectionMethod methodRequirement) {
        CollectionClassRepr forType = methodRequirement.getForType();

        MethodSpec.Builder builder = MethodSpec.methodBuilder("serialize" + forType.getSimpleName() + "Generic")
                .returns(void.class)
                .addParameter(TypeName.get(forType.getOriginal()), "value")
                .addParameter(byteBuf, "acc");

        builder.addStatement("acc.writeInt(value.size())");

        TypeName elementTypeName = TypeName.get(forType.getElementType().getOriginal());
        builder.beginControlFlow("for ($T e :value)", elementTypeName);
        builder.addStatement("serialize" + forType.getElementType().getSimpleName() + "Generic(e,acc)");
        builder.endControlFlow();

        return builder.build();
    }

    private MethodSpec generateCollectionUnserializer(MethodRequirement.CollectionMethod methodRequirement) {
        CollectionClassRepr forType = methodRequirement.getForType();

        MethodSpec.Builder builder = MethodSpec.methodBuilder("unserialize" + forType.getSimpleName() + "Generic")
                .returns(TypeName.get(forType.getOriginal()))
                .addParameter(byteBuf, "buf");

        builder.addStatement("int size = buf.readInt()");
        builder.addStatement(forType.getConcreteBuilder());

        builder.beginControlFlow("for (int i=0;i<size;i++)");
        builder.addStatement("$T e = unserialize" + forType.getElementType().getSimpleName() + "Generic(buf)", TypeName.get(forType.getElementType().getOriginal()));
        builder.addStatement("value.add(e)");
        builder.endControlFlow();

        builder.addStatement("return " + forType.getConcreteFinalizer());

        return builder.build();
    }

    private MethodSpec generateMapSerializer(MethodRequirement.MapMethod methodRequirement) {
        MapClassRepr forType = methodRequirement.getForType();

        MethodSpec.Builder builder = MethodSpec.methodBuilder("serialize" + forType.getSimpleName() + "Generic")
                .returns(void.class)
                .addParameter(TypeName.get(forType.getOriginal()), "value")
                .addParameter(byteBuf, "acc");

        builder.addStatement("acc.writeInt(value.size())");

        builder.beginControlFlow("for (Map.Entry<String, Integer> entry :value.entrySet())");
        builder.addStatement("$T k = entry.getKey()", TypeName.get(forType.getKeyType().getOriginal()));
        builder.addStatement("$T v = entry.getValue()", TypeName.get(forType.getValueType().getOriginal()));
        builder.addStatement("serialize" + forType.getKeyType().getSimpleName() + "Generic(k,acc)");
        builder.addStatement("serialize" + forType.getValueType().getSimpleName() + "Generic(v,acc)");
        builder.endControlFlow();

        return builder.build();
    }

    private MethodSpec generateMapUnserializer(MethodRequirement.MapMethod methodRequirement) {
        MapClassRepr forType = methodRequirement.getForType();

        MethodSpec.Builder builder = MethodSpec.methodBuilder("unserialize" + forType.getSimpleName() + "Generic")
                .returns(TypeName.get(forType.getOriginal()))
                .addParameter(byteBuf, "buf");

        builder.addStatement("int size = buf.readInt()");
        builder.addStatement(forType.getConcreteBuilder());

        builder.beginControlFlow("for (int i=0;i<size;i++)");
        builder.addStatement("$T k = unserialize" + forType.getKeyType().getSimpleName() + "Generic(buf)", TypeName.get(forType.getKeyType().getOriginal()));
        builder.addStatement("$T v = unserialize" + forType.getValueType().getSimpleName() + "Generic(buf)", TypeName.get(forType.getValueType().getOriginal()));
        builder.addStatement("value.put(k, v)");
        builder.endControlFlow();

        builder.addStatement("return " + forType.getConcreteFinalizer());

        return builder.build();
    }

    private MethodSpec generateConcreticSerializer(DataClassRepr forType) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("serialize" + forType.getSimpleName() + "Concretic")
                .returns(void.class)
                .addParameter(ClassName.get(forType.getOriginal()), "value")
                .addParameter(byteBuf, "acc");

        for (FieldRepr field : onlySerializableFields(forType.getFields()))
            builder.addStatement("serialize" + field.getType().getSimpleName() + "Generic($L, acc)", getFieldGetAccess(forType, field));

        return builder.build();
    }

    private MethodSpec generateConcreticUnserializer(DataClassRepr forType) {
        Map<Boolean, List<FieldRepr>> collect2 = onlySerializableFields(forType.getFields()).stream().collect(Collectors.partitioningBy(f -> f.getModifiers().contains(FINAL)));
        List<FieldRepr> finalFields = collect2.get(true);
        List<FieldRepr> mutableFields = collect2.get(false);

        MethodSpec.Builder builder = MethodSpec.methodBuilder("unserialize" + forType.getSimpleName() + "Concretic")
                .returns(TypeName.get(forType.getOriginal()))
                .addParameter(byteBuf, "buf");

        if (forType.getConstructors().stream().noneMatch(c -> c.getArguments().equals(finalFields.stream().map(FieldRepr::getType).collect(toList()))))
            throw new IllegalStateException("Constructor for final fields not found");

        builder.addCode(forType.getName() + " value = new " + forType.getName() + "(");
        for (int i = 0; i < finalFields.size(); i++) {
            builder.addCode("unserialize" + finalFields.get(i).getType().getSimpleName() + "Generic(buf)");
            if (i < finalFields.size() - 1)
                builder.addCode(", ");
        }
        builder.addCode(");\n");

        for (FieldRepr field : mutableFields)
            builder.addStatement(getFieldSetAccess(forType, field), "unserialize" + field.getType().getSimpleName() + "Generic(buf)");

        builder.addStatement("return value");

        return builder.build();
    }

    private List<FieldRepr> onlySerializableFields(List<FieldRepr> fields) {
        return fields.stream().filter(f -> !f.getModifiers().contains(TRANSIENT) && !f.getModifiers().contains(STATIC)).collect(toList());
    }

    private MethodSpec generateGenericSerializer(MethodRequirement.GenericMethod methodRequirement) {
        List<DataClassRepr> implementations = methodRequirement.getSealedImplementations();
        DataClassRepr forType = methodRequirement.getForType();

        MethodSpec.Builder builder = MethodSpec.methodBuilder("serialize" + forType.getSimpleName() + "Generic")
                .returns(void.class)
                .addParameter(ClassName.get(forType.getOriginal()), "value")
                .addParameter(byteBuf, "acc");

        builder.beginControlFlow("if (false) ");
        builder.addStatement("return");

        for (int concreteIndex = 0; concreteIndex < implementations.size(); concreteIndex++) {
            DataClassRepr concreteType = implementations.get(concreteIndex);
            builder.nextControlFlow("else if (value instanceof $L)", concreteType.getName());
            builder.addStatement("acc.writeByte($L)", concreteIndex);
            builder.addStatement("serialize" + concreteType.getSimpleName() + "Concretic(value, acc)");
        }

        builder.nextControlFlow("else");
        builder.addStatement("throw new IllegalStateException(\"Unexpected implementation of $L: \"+value.getClass().getName())", forType.getName());
        builder.endControlFlow();

        return builder.build();
    }

    private MethodSpec generateGenericUnserializer(MethodRequirement.GenericMethod methodRequirement) {
        List<DataClassRepr> implementations = methodRequirement.getSealedImplementations();
        DataClassRepr forType = methodRequirement.getForType();

        MethodSpec.Builder builder = MethodSpec.methodBuilder("unserialize" + forType.getSimpleName() + "Generic")
                .returns(TypeName.get(forType.getOriginal()))
                .addParameter(byteBuf, "buf");

        builder.addStatement("byte concreteIndex = buf.readByte()");

        builder.beginControlFlow("if (false) ");
        builder.addStatement("return null");

        for (int concreteIndex = 0; concreteIndex < implementations.size(); concreteIndex++) {
            DataClassRepr concreteType = implementations.get(concreteIndex);
            builder.nextControlFlow("else if (concreteIndex == $L)", concreteIndex);
            builder.addStatement("return unserialize" + concreteType.getSimpleName() + "Concretic(buf)");
        }

        builder.nextControlFlow("else");
        builder.addStatement("throw new IllegalStateException(\"Unexpected implementation of $L: concreteIndex = \"+concreteIndex)", forType.getName());
        builder.endControlFlow();

        return builder.build();
    }


    private String getFieldGetAccess(DataClassRepr typeElement, FieldRepr field) {
        return isPrivate(field) ? getterAccess(typeElement, field) : "value." + field.getName();
    }

    private String getFieldSetAccess(DataClassRepr typeElement, FieldRepr field) {
        return isPrivate(field) ? setterAccess(typeElement, field) : "value." + field.getName() + " = $L";
    }

    private String getterAccess(DataClassRepr typeElement, FieldRepr field) {
        String capitalized = StringUtils.capitalize(field.getName());

        boolean get_accessor = typeElement.getMethods().stream().anyMatch(m -> m.getName().equals("get" + capitalized) && m.getResultType().equals(field.getType()) && m.getArguments().isEmpty());
        if (get_accessor)
            return "value." + "get" + capitalized + "()";
        else if (field.getType() instanceof PrimitiveClassRepr && ((PrimitiveClassRepr) field.getType()).getKind() == PrimitiveClassRepr.PrimitiveKind.BOOLEAN) {
            boolean is_accessor = typeElement.getMethods().stream().anyMatch(m -> m.getName().equals("is" + capitalized) && m.getResultType().equals(field.getType()) && m.getArguments().isEmpty());
            if (is_accessor)
                return "value." + "is" + capitalized + "()";
            else
                throw new IllegalStateException("Private fields must have getters");
        } else
            throw new IllegalStateException("Private fields must have getters");
    }

    private String setterAccess(DataClassRepr typeElement, FieldRepr field) {
        String capitalized = StringUtils.capitalize(field.getName());

        boolean set_accessor = typeElement.getMethods().stream().anyMatch(m -> m.getName().equals("set" + capitalized) && m.getArguments().size() == 1 && m.getArguments().get(0).equals(field.getType()));
        if (set_accessor)
            return "value." + "set" + capitalized + "($L)";
        else
            throw new IllegalStateException("Private non-final fields must have setters");
    }

    private MethodSpec generateMainSerializeMethod(DataClassRepr typeElement, ClassName packet) {
        MethodSpec.Builder serialize = MethodSpec.methodBuilder("serialize")
                .addModifiers(PUBLIC);

        serialize
                .returns(void.class)
                .addParameter(packet, "value")
                .addParameter(byteBuf, "acc");

        serialize
                .addStatement("serialize" + typeElement.getSimpleName() + "Concretic(value, acc)");

        return serialize.build();
    }

    private Stream<MethodRequirement> getRequiredMethods(Set<ClassRepr> serializableTypes) {
        return serializableTypes.stream().flatMap(this::getRequirementMethodsForType).distinct();
    }

    private Stream<MethodRequirement> getRequirementMethodsForType(ClassRepr classRepr) {
        note("Generation serializer for " + classRepr);

        if (classRepr instanceof DataClassRepr) {
            DataClassRepr dataClassRepr = (DataClassRepr) classRepr;

            List<DataClassRepr> sealedImplementations = InheritanceUtils.getAllSealedImplementations(dataClassRepr)
                    .stream().sorted(Comparator.comparing(DataClassRepr::getName)).collect(toList());
            note(classRepr.getName() + " have implementations: " + sealedImplementations.stream().map(ClassRepr::getName).collect(toSet()));

            return Stream.concat(
                    Stream.of(new MethodRequirement.GenericMethod(dataClassRepr, sealedImplementations)),
                    sealedImplementations.stream().map(MethodRequirement.ConcreticMethod::new)
            );
        } else if (classRepr instanceof MapClassRepr) {
            MapClassRepr mapClassRepr = (MapClassRepr) classRepr;
            return Stream.of(new MethodRequirement.MapMethod(mapClassRepr));

        } else if (classRepr instanceof CollectionClassRepr) {
            return Stream.of(new MethodRequirement.CollectionMethod((CollectionClassRepr) classRepr));

        } else
            throw new UnsupportedOperationException(classRepr.toString());
    }

    private Stream<ClassRepr> getAllSerializableTypes(ClassRepr classRepr) {
        return Stream.concat(Stream.of(classRepr),

                (classRepr instanceof DataClassRepr) ?
                        onlySerializableFields(((DataClassRepr) classRepr).getFields())
                                .stream()
                                .map(FieldRepr::getType)
                                .filter(t -> nonExistsMethod(elementUtils.getTypeElement("hohserg.elegant.networking.impl.ISerializer"), t))
                                .flatMap(t -> Stream.concat(Stream.of(t), getAllSerializableTypes(t)))
                        : Stream.empty());
    }


    /**
     * private Collection<MethodSpec> createSerializationMethods1(List<FieldRepr> serializableFields) {
     * Map<String, MethodSpec> r = new HashMap<>();
     * <p>
     * Stack<TypeMirror> needToCreateSerializers = new Stack<>();
     * needToCreateSerializers.addAll(serializableFields.stream()
     * .filter(f -> !f.getModifiers().contains(TRANSIENT) && !f.getModifiers().contains(STATIC))
     * .filter(this::isNotPrimitive)
     * .filter(f -> nonExistsMethod(elementUtils.getTypeElement("hohserg.elegant.networking.impl.ISerializer"), f.getType()))
     * .map(FieldRepr::getType)
     * .collect(toList()));
     * <p>
     * while (!needToCreateSerializers.empty()) {
     * TypeMirror type = needToCreateSerializers.pop();
     * boolean isFinalClass = typeUtils.asElement(type).getModifiers().contains(FINAL);
     * note("createSerializationMethods1#test " + field.getType() + " is final " + isFinalClass);
     * if (isFinalClass) {
     * <p>
     * } else {
     * <p>
     * }
     * }
     * <p>
     * return r.values();
     * }
     * <p>
     * private boolean isNotPrimitive(FieldRepr fieldRepr) {
     * return !fieldRepr.getType().getKind().isPrimitive();
     * }
     * <p>
     * private MethodSpec createPacketIdMethod(DataClassRepr typeElement, int packetId) {
     * return MethodSpec.methodBuilder("packetId")
     * .addModifiers(PUBLIC)
     * .returns(int.class)
     * .addStatement("return $L", packetId)
     * .build();
     * }
     * <p>
     * private Stream<MethodSpec> createSerializationMethods(FieldRepr f) {
     * <p>
     * DataClassRepr typeElement = LombokUtils.correctFieldModifiers(f.typeRepresentation(typeUtils));
     * List<FieldRepr> serializableFields = typeElement.getFields().stream()
     * .filter(f1 -> !f1.getModifiers().contains(TRANSIENT) && !f1.getModifiers().contains(STATIC))
     * .collect(Collectors.toList());
     * <p>
     * Map<Boolean, List<FieldRepr>> collect2 = serializableFields.stream().collect(Collectors.partitioningBy(f1 -> f1.getModifiers().contains(FINAL)));
     * <p>
     * List<FieldRepr> finalFields = collect2.get(true);
     * List<FieldRepr> mutableFields = collect2.get(false);
     * <p>
     * <p>
     * MethodSpec.Builder serialize = MethodSpec.methodBuilder("serialize" + typeSimpleName(f.getType()))
     * .returns(void.class)
     * .addParameter(TypeName.get(f.getType()), "value")
     * .addParameter(ByteBuf, "acc");
     * <p>
     * for (FieldRepr field : ImmutableList.<FieldRepr>builder().addAll(finalFields).addAll(mutableFields).build())
     * serialize.addStatement(getSerializeStatement(field.getType()), getFieldGetAccess(typeElement, field));
     * <p>
     * MethodSpec.Builder unserialize = MethodSpec.methodBuilder("unserialize" + typeSimpleName(f.getType()))
     * .returns(TypeName.get(f.getType()))
     * .addParameter(ByteBuf, "buf");
     * <p>
     * if (typeElement.getConstructors().stream().noneMatch(c -> c.getArguments().equals(finalFields.stream().map(FieldRepr::getType).collect(toList()))))
     * throw new IllegalStateException("Constructor for final fields not found");
     * <p>
     * ClassName typeName = ClassName.get(typeElement.getElement());
     * unserialize.addCode(typeName.canonicalName() + " value = new " + typeName.canonicalName() + "(");
     * for (int i = 0; i < finalFields.size(); i++) {
     * unserialize.addCode(getUnserializationStatement(finalFields.get(i).getType()));
     * if (i < finalFields.size() - 1)
     * unserialize.addCode(", ");
     * }
     * unserialize.addCode(");\n");
     * <p>
     * for (FieldRepr field : mutableFields)
     * unserialize.addStatement(getFieldSetAccess(typeElement, field), getUnserializationStatement(field.getType()));
     * <p>
     * unserialize.addStatement("return value");
     * <p>
     * return //Stream.concat(
     * Stream.of(serialize.build(), unserialize.build());//,
     * //serializableFields.stream().filter(f1 -> nonExistsMethod(typeElement, "serialize" + typeSimpleName(f1.getType()))).flatMap(f1 -> createSerializationMethods(f1)));
     * <p>
     * return null;
     * }
     * <p>
     * private TypeMirror MapTypeMirror;
     */

    private boolean nonExistsMethod(TypeElement holder, ClassRepr type) {
        String methodName = "serialize" + type.getSimpleName() + "Generic";


        boolean b = elementUtils.getAllMembers(holder).stream().noneMatch(m -> m.getKind() == METHOD && m.getSimpleName().toString().equals(methodName));
        note("nonExistsMethod " + methodName + " noneMatch " + b);
        return b;
    }

    /**
     * private MethodSpec createUnserializeMethod(DataClassRepr typeElement, List<FieldRepr> finalFields, List<FieldRepr> mutableFields, ClassName packet) {
     * MethodSpec.Builder unserialize = MethodSpec.methodBuilder("unserialize")
     * .addModifiers(PUBLIC)
     * .returns(packet)
     * .addParameter(ByteBuf, "buf");
     * <p>
     * if (typeElement.getConstructors().stream().noneMatch(c -> c.getArguments().equals(finalFields.stream().map(FieldRepr::getType).collect(toList()))))
     * throw new IllegalStateException("Constructor for final fields not found");
     * <p>
     * unserialize.addCode(packet.simpleName() + " value = new " + packet.simpleName() + "(");
     * for (int i = 0; i < finalFields.size(); i++) {
     * unserialize.addCode(getUnserializationStatement(finalFields.get(i).getType()));
     * if (i < finalFields.size() - 1)
     * unserialize.addCode(", ");
     * }
     * unserialize.addCode(");\n");
     * <p>
     * for (FieldRepr field : mutableFields)
     * unserialize.addStatement(getFieldSetAccess(typeElement, field), getUnserializationStatement(field.getType()));
     * <p>
     * unserialize.addStatement("return value");
     * <p>
     * return unserialize.build();
     * }
     * <p>
     * private MethodSpec generateMainSerializeMethod(DataClassRepr typeElement, List<FieldRepr> finalFields, List<FieldRepr> mutableFields, ClassName packet) {
     * MethodSpec.Builder serialize = MethodSpec.methodBuilder("serialize")
     * .addModifiers(PUBLIC)
     * .returns(void.class)
     * .addParameter(packet, "value")
     * .addParameter(ByteBuf, "acc");
     * <p>
     * for (FieldRepr field : ImmutableList.<FieldRepr>builder().addAll(finalFields).addAll(mutableFields).build())
     * serialize.addStatement(getSerializeStatement(field.getType()), getFieldGetAccess(typeElement, field));
     * <p>
     * return serialize.build();
     * }
     * <p>
     * private String getUnserializationStatement(TypeMirror type) {
     * switch (type.getKind()) {
     * case INT:
     * return "buf.readInt()";
     * <p>
     * case BYTE:
     * return "buf.readByte()";
     * <p>
     * case CHAR:
     * return "buf.readChar()";
     * <p>
     * case LONG:
     * return "buf.readLong()";
     * <p>
     * case FLOAT:
     * return "buf.readFloat()";
     * <p>
     * case DOUBLE:
     * return "buf.readDouble()";
     * <p>
     * case SHORT:
     * return "buf.readShort()";
     * <p>
     * case BOOLEAN:
     * return "buf.readBoolean()";
     * <p>
     * case DECLARED:
     * return "unserialize" + typeSimpleName(type) + "(buf)";
     * default:
     * throw new IllegalStateException("You are really?");
     * }
     * }
     * <p>
     * private String typeSimpleName(TypeMirror type) {
     * if (type.toString().startsWith("java.util.Map")) {
     * return "Map";
     * } else {
     * String typeFullName = type.toString();
     * return typeFullName.substring(typeFullName.lastIndexOf('.') + 1);
     * }
     * }
     * <p>
     * private String getFieldGetAccess(DataClassRepr typeElement, FieldRepr field) {
     * return isPrivate(field) ? getterAccess(typeElement, field) : "value." + field.getName();
     * }
     * <p>
     * private String getFieldSetAccess(DataClassRepr typeElement, FieldRepr field) {
     * return isPrivate(field) ? setterAccess(typeElement, field) : "value." + field.getName() + " = $L";
     * }
     * <p>
     * private String setterAccess(DataClassRepr typeElement, FieldRepr field) {
     * String capitalized = StringUtils.capitalize(field.getName());
     * <p>
     * boolean set_accessor = typeElement.getMethods().stream().anyMatch(m -> m.getName().equals("set" + capitalized) && m.getArguments().size() == 1 && m.getArguments().get(0).equals(field.getType()));
     * if (set_accessor)
     * return "value." + "set" + capitalized + "($L)";
     * else
     * throw new IllegalStateException("Private non-final fields must have setters");
     * }
     * <p>
     * private String getSerializeStatement(TypeMirror type) {
     * switch (type.getKind()) {
     * case INT:
     * return "acc.writeInt($L)";
     * <p>
     * case BYTE:
     * return "acc.writeByte($L)";
     * <p>
     * case CHAR:
     * return "acc.writeChar($L)";
     * <p>
     * case LONG:
     * return "acc.writeLong($L)";
     * <p>
     * case FLOAT:
     * return "acc.writeFloat($L)";
     * <p>
     * case DOUBLE:
     * return "acc.writeDouble($L)";
     * <p>
     * case SHORT:
     * return "acc.writeShort($L)";
     * <p>
     * case BOOLEAN:
     * return "acc.writeBoolean($L)";
     * <p>
     * case DECLARED:
     * return "serialize" + typeSimpleName(type) + "($L,acc)";
     * default:
     * throw new IllegalStateException("You are really? " + type);
     * }
     * }
     * <p>
     * private String getterAccess(DataClassRepr typeElement, FieldRepr field) {
     * String capitalized = StringUtils.capitalize(field.getName());
     * <p>
     * boolean get_accessor = typeElement.getMethods().stream().anyMatch(m -> m.getName().equals("get" + capitalized) && m.getResultType().equals(field.getType()) && m.getArguments().isEmpty());
     * if (get_accessor)
     * return "value." + "get" + capitalized + "()";
     * else if (field.getType().getKind() == TypeKind.BOOLEAN) {
     * boolean is_accessor = typeElement.getMethods().stream().anyMatch(m -> m.getName().equals("is" + capitalized) && m.getResultType().equals(field.getType()) && m.getArguments().isEmpty());
     * if (is_accessor)
     * return "value." + "is" + capitalized + "()";
     * else
     * throw new IllegalStateException("Private fields must have getters");
     * } else
     * throw new IllegalStateException("Private fields must have getters");
     * }
     */

    private boolean isPrivate(FieldRepr f) {
        return f.getModifiers().contains(PRIVATE);
    }

    private boolean havePacketInterfaces(TypeElement typeElement) {
        TypeElement currentClass = typeElement;
        while (true) {
            for (TypeMirror anInterface : currentClass.getInterfaces())
                if (anInterface.toString().equals("hohserg.elegant.networking.api.ClientToServerPacket") || anInterface.toString().equals("hohserg.elegant.networking.api.ServerToClientPacket"))
                    return true;

            TypeMirror superClassType = currentClass.getSuperclass();

            if (superClassType.getKind() == TypeKind.NONE)
                return false;

            currentClass = (TypeElement) typeUtils.asElement(superClassType);
        }
    }

    private static Element currentElement;

    public static void note(String msg) {
        note(currentElement, msg);
    }

    public static void note(Element e, String msg) {
        messager.printMessage(Diagnostic.Kind.NOTE, msg, e);
    }

    private void error(Element e, String msg) {
        messager.printMessage(Diagnostic.Kind.ERROR, msg, e);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> r = new HashSet<>();
        r.add(ElegantPacket.class.getCanonicalName());
        return r;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    public static Types typeUtils;
    public static Elements elementUtils;
    private Filer filer;
    private static Messager messager;

}
