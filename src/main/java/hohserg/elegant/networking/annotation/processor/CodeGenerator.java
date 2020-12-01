package hohserg.elegant.networking.annotation.processor;

import com.squareup.javapoet.*;
import hohserg.elegant.networking.annotation.processor.dom.*;
import hohserg.elegant.networking.annotation.processor.dom.containers.ArrayClassRepr;
import hohserg.elegant.networking.annotation.processor.dom.containers.CollectionClassRepr;
import hohserg.elegant.networking.annotation.processor.dom.containers.MapClassRepr;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hohserg.elegant.networking.annotation.processor.ElegantPacketProcessor.*;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.*;

public class CodeGenerator {
    public static ClassName byteBuf = ClassName.get("io.netty.buffer", "ByteBuf");

    public static JavaFile generateSerializerSource(DataClassRepr typeElement, Set<ClassRepr> serializableTypes, int packetId) {
        List<MethodSpec> requiredMethods = getRequiredMethods(serializableTypes).flatMap(MethodRequirement::generateMethods).collect(toList());


        ClassName packet = ClassName.get(typeElement.getElement());
        TypeSpec serializer = TypeSpec.classBuilder(typeElement.getSimpleName() + "Serializer")
                .addAnnotation(AnnotationSpec.builder(ClassName.get("hohserg.elegant.networking.impl", "SerializerMark")).addMember("packetClass", packet.canonicalName() + ".class").build())
                .addModifiers(PUBLIC)
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get("hohserg.elegant.networking.impl", "ISerializer"), packet))
                .addMethod(generateMainSerializeMethod(typeElement, packet))
                .addMethod(generateMainUnserializeMethod(typeElement, packet))
                .addMethod(generatePacketIdMethod(typeElement, packetId))
                .addMethods(requiredMethods)
                .build();

        return JavaFile.builder(elementUtils.getPackageOf(typeElement.getElement()).getQualifiedName().toString(), serializer).build();
    }

    public static MethodSpec generatePacketIdMethod(DataClassRepr typeElement, int packetId) {
        return MethodSpec.methodBuilder("packetId")
                .addModifiers(PUBLIC)
                .returns(int.class)
                .addStatement("return $L", packetId)
                .build();
    }

    public static MethodSpec generateMainSerializeMethod(DataClassRepr typeElement, ClassName packet) {
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

    public static MethodSpec generateMainUnserializeMethod(DataClassRepr typeElement, ClassName packet) {
        MethodSpec.Builder unserialize = MethodSpec.methodBuilder("unserialize")
                .addModifiers(PUBLIC);

        unserialize
                .returns(packet)
                .addParameter(byteBuf, "buf");

        unserialize
                .addStatement("return unserialize" + typeElement.getSimpleName() + "Concretic(buf)");

        return unserialize.build();
    }

    public static MethodSpec generateArraySerializer(MethodRequirement.ArrayMethod methodRequirement) {
        ArrayClassRepr forType = methodRequirement.getForType();

        MethodSpec.Builder builder = MethodSpec.methodBuilder("serialize" + forType.getSimpleName() + "Generic")
                .returns(void.class)
                .addParameter(TypeName.get(forType.getOriginal()), "value")
                .addParameter(byteBuf, "acc");

        builder.addStatement("acc.writeInt(value.length)");

        TypeName elementTypeName = TypeName.get(forType.getElementType().getOriginal());
        builder.beginControlFlow("for ($T e :value)", elementTypeName);
        builder.addStatement("serialize" + forType.getElementType().getSimpleName() + "Generic(e,acc)");
        builder.endControlFlow();

        return builder.build();
    }

    public static MethodSpec generateArrayUnserializer(MethodRequirement.ArrayMethod methodRequirement) {
        ArrayClassRepr forType = methodRequirement.getForType();

        MethodSpec.Builder builder = MethodSpec.methodBuilder("unserialize" + forType.getSimpleName() + "Generic")
                .returns(TypeName.get(forType.getOriginal()))
                .addParameter(byteBuf, "buf");

        TypeName elementTypeName = TypeName.get(forType.getElementType().getOriginal());

        builder.addStatement("int size = buf.readInt()");
        builder.addStatement("$T[] value = new  $T[size]", elementTypeName, elementTypeName);

        builder.beginControlFlow("for (int i=0;i<size;i++)");
        builder.addStatement("$T e = unserialize" + forType.getElementType().getSimpleName() + "Generic(buf)", TypeName.get(forType.getElementType().getOriginal()));
        builder.addStatement("value[i] = e");
        builder.endControlFlow();

        builder.addStatement("return value");

        return builder.build();
    }

    public static MethodSpec generateEnumSerializer(MethodRequirement.EnumMethod methodRequirement) {
        EnumClassRepr forType = methodRequirement.getForType();

        return MethodSpec.methodBuilder("serialize" + forType.getSimpleName() + "Generic")
                .returns(void.class)
                .addParameter(TypeName.get(forType.getOriginal()), "value")
                .addParameter(byteBuf, "acc")
                .addStatement("acc.writeByte(value.ordinal())")
                .build();
    }

    public static MethodSpec generateEnumUnserializer(MethodRequirement.EnumMethod methodRequirement) {
        EnumClassRepr forType = methodRequirement.getForType();

        MethodSpec.Builder builder = MethodSpec.methodBuilder("unserialize" + forType.getSimpleName() + "Generic")
                .returns(TypeName.get(forType.getOriginal()))
                .addParameter(byteBuf, "buf");

        builder.addStatement("return $T.values()[buf.readByte()]", forType.getOriginal());
        return builder.build();
    }

    public static MethodSpec generateCollectionSerializer(MethodRequirement.CollectionMethod methodRequirement) {
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

    public static MethodSpec generateCollectionUnserializer(MethodRequirement.CollectionMethod methodRequirement) {
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

    public static MethodSpec generateMapSerializer(MethodRequirement.MapMethod methodRequirement) {
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

    public static MethodSpec generateMapUnserializer(MethodRequirement.MapMethod methodRequirement) {
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

    public static MethodSpec generateConcreticSerializer(DataClassRepr forType) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("serialize" + forType.getSimpleName() + "Concretic")
                .returns(void.class)
                .addParameter(ClassName.get(forType.getOriginal()), "value")
                .addParameter(byteBuf, "acc");

        for (FieldRepr field : onlySerializableFields(forType.getFields()))
            builder.addStatement("serialize" + field.getType().getSimpleName() + "Generic($L, acc)", getFieldGetAccess(forType, field));

        return builder.build();
    }

    public static MethodSpec generateConcreticUnserializer(DataClassRepr forType) {
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

    public static List<FieldRepr> onlySerializableFields(List<FieldRepr> fields) {
        return fields.stream().filter(f -> !f.getModifiers().contains(TRANSIENT) && !f.getModifiers().contains(STATIC)).collect(toList());
    }

    public static MethodSpec generateGenericSerializer(MethodRequirement.GenericMethod methodRequirement) {
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

    public static MethodSpec generateGenericUnserializer(MethodRequirement.GenericMethod methodRequirement) {
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

    public static String getFieldGetAccess(DataClassRepr typeElement, FieldRepr field) {
        return isPrivate(field) ? getterAccess(typeElement, field) : "value." + field.getName();
    }

    public static String getFieldSetAccess(DataClassRepr typeElement, FieldRepr field) {
        return isPrivate(field) ? setterAccess(typeElement, field) : "value." + field.getName() + " = $L";
    }

    public static String getterAccess(DataClassRepr typeElement, FieldRepr field) {
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

    public static String setterAccess(DataClassRepr typeElement, FieldRepr field) {
        String capitalized = StringUtils.capitalize(field.getName());

        boolean set_accessor = typeElement.getMethods().stream().anyMatch(m -> m.getName().equals("set" + capitalized) && m.getArguments().size() == 1 && m.getArguments().get(0).equals(field.getType()));
        if (set_accessor)
            return "value." + "set" + capitalized + "($L)";
        else
            throw new IllegalStateException("Private non-final fields must have setters");
    }

    public static boolean isPrivate(FieldRepr f) {
        return f.getModifiers().contains(PRIVATE);
    }

    public static Stream<MethodRequirement> getRequiredMethods(Set<ClassRepr> serializableTypes) {
        if (options.containsKey(printDetailsOption))
            serializableTypes.forEach(classRepr -> note("Generation serializer for " + classRepr.getName()));

        return serializableTypes.stream().flatMap(ClassRepr::getRequirementMethods).distinct();
    }

}
