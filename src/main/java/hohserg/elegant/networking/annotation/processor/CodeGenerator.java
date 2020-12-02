package hohserg.elegant.networking.annotation.processor;

import com.squareup.javapoet.*;
import hohserg.elegant.networking.annotation.processor.dom.ClassRepr;
import hohserg.elegant.networking.annotation.processor.dom.DataClassRepr;
import hohserg.elegant.networking.annotation.processor.dom.FieldRepr;
import hohserg.elegant.networking.annotation.processor.dom.PrimitiveClassRepr;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static hohserg.elegant.networking.annotation.processor.ElegantPacketProcessor.*;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.*;

public class CodeGenerator {
    static ClassName byteBuf = ClassName.get("io.netty.buffer", "ByteBuf");

    static JavaFile generateSerializerSource(DataClassRepr typeElement, Set<ClassRepr> serializableTypes, int packetId) {
        List<MethodSpec> requiredMethods = getRequiredMethods(serializableTypes)
                .flatMap(methodRequirement -> Stream.of(
                        methodRequirement.generateSerializer(),
                        methodRequirement.generateUnserializer()
                ))
                .collect(toList());


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

    private static Stream<MethodRequirement> getRequiredMethods(Set<ClassRepr> serializableTypes) {
        if (options.containsKey(printDetailsOption))
            serializableTypes.forEach(classRepr -> note("Generation serializer for " + classRepr.getName()));

        return serializableTypes.stream().flatMap(ClassRepr::getRequirementMethods).distinct();
    }

    private static MethodSpec generatePacketIdMethod(DataClassRepr typeElement, int packetId) {
        return MethodSpec.methodBuilder("packetId")
                .addModifiers(PUBLIC)
                .returns(int.class)
                .addStatement("return $L", packetId)
                .build();
    }

    private static MethodSpec generateMainSerializeMethod(DataClassRepr typeElement, ClassName packet) {
        MethodSpec.Builder serialize = MethodSpec.methodBuilder("serialize")
                .addModifiers(PUBLIC);

        serialize
                .returns(void.class)
                .addParameter(packet, "value")
                .addParameter(byteBuf, "acc");

        serialize
                .addStatement(MethodRequirement.serialize_Prefix + typeElement.getSimpleName() + MethodRequirement.Concretic_Suffix + "(value, acc)");

        return serialize.build();
    }

    private static MethodSpec generateMainUnserializeMethod(DataClassRepr typeElement, ClassName packet) {
        MethodSpec.Builder unserialize = MethodSpec.methodBuilder("unserialize")
                .addModifiers(PUBLIC);

        unserialize
                .returns(packet)
                .addParameter(byteBuf, "buf");

        unserialize
                .addStatement("return " + MethodRequirement.unserialize_Prefix + typeElement.getSimpleName() + MethodRequirement.Concretic_Suffix + "(buf)");

        return unserialize.build();
    }

    static List<FieldRepr> onlySerializableFields(List<FieldRepr> fields) {
        return fields.stream().filter(f -> !f.getModifiers().contains(TRANSIENT) && !f.getModifiers().contains(STATIC)).collect(toList());
    }

    static String getFieldGetAccess(DataClassRepr typeElement, FieldRepr field) {
        return isPrivate(field) ? getterAccess(typeElement, field) : "value." + field.getName();
    }

    static String getFieldSetAccess(DataClassRepr typeElement, FieldRepr field) {
        return isPrivate(field) ? setterAccess(typeElement, field) : "value." + field.getName() + " = $L";
    }

    private static String getterAccess(DataClassRepr typeElement, FieldRepr field) {
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

    private static String setterAccess(DataClassRepr typeElement, FieldRepr field) {
        String capitalized = StringUtils.capitalize(field.getName());

        boolean set_accessor = typeElement.getMethods().stream().anyMatch(m -> m.getName().equals("set" + capitalized) && m.getArguments().size() == 1 && m.getArguments().get(0).equals(field.getType()));
        if (set_accessor)
            return "value." + "set" + capitalized + "($L)";
        else
            throw new IllegalStateException("Private non-final fields must have setters");
    }

    private static boolean isPrivate(FieldRepr f) {
        return f.getModifiers().contains(PRIVATE);
    }
}
