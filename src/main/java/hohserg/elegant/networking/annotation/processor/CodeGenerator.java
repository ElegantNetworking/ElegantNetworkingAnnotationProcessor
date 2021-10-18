package hohserg.elegant.networking.annotation.processor;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.*;
import hohserg.elegant.networking.annotation.processor.code.generator.*;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static hohserg.elegant.networking.Refs.*;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.ElementKind.CONSTRUCTOR;
import static javax.lang.model.element.Modifier.*;

public class CodeGenerator implements ICodeGenerator, AccessUtils, MethodNames, TypeUtils, FunctionalUtils {
    public CodeGenerator(ElegantSerializerProcessor processor, Types typeUtils, Elements elementUtils, Map<String, AbstractGenerator> specials, Messager messager) {
        this.processor = processor;
        this.typeUtils = typeUtils;
        this.elementUtils = elementUtils;
        this.specials = specials;
        this.messager = messager;
    }

    private ElegantSerializerProcessor processor;
    private Types typeUtils;
    private Elements elementUtils;
    private final Map<String, AbstractGenerator> specials;
    private final Messager messager;

    public Types getTypeUtils() {
        return typeUtils;
    }

    public Elements getElementUtils() {
        return elementUtils;
    }

    Stream<MethodSpec> enumMethods(DeclaredType type) {
        return Stream.of(enumSerializer(type), enumUnserializer(type));
    }

    Stream<MethodSpec> arrayMethods(ArrayType type) {
        return Stream.of(arraySerializer(type), arrayUnserializer(type));
    }

    Stream<MethodSpec> concreticMethods(DeclaredType type) {
        TypeElement element = (TypeElement) type.asElement();
        List<Map.Entry<DeclaredType, AbstractGenerator>> specialBaseTypes = getSpecialBaseTypes(type);
        if (specialBaseTypes.isEmpty())
            return Stream.of(concreticSerializer(element, type), concreticUnserializer(element, type));
        else {
            AbstractGenerator customSpecialGenerator = SpecialTypeSupport.getCustomSpecialGenerator(typeUtils, elementUtils, specialBaseTypes.get(0), type);
            return customSpecialGenerator.generateMethodsForType(type, ImmutableList.of());
        }
    }

    Stream<MethodSpec> genericMethods(DeclaredType type, List<DeclaredType> implementations) {
        TypeElement element = (TypeElement) type.asElement();
        if (implementations.size() == 0)
            throw new AnnotationProcessorException(element, "Not found implementations of this type. Implementations must be sealed in same package");
        else if (implementations.size() == 1)
            return Stream.of(genericSingleSerializer(element, type, implementations), genericSingleUnserializer(element, type, implementations));
        else
            return Stream.of(genericSerializer(element, type, implementations), genericUnserializer(element, type, implementations));
    }


    private MethodSpec arraySerializer(ArrayType type) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(getGenericSerializeMethodName(type))
                .returns(void.class)
                .addParameter(TypeName.get(type), "value")
                .addParameter(byteBuf, "acc");

        builder.addStatement("acc.writeInt(value.length)");

        TypeName elementTypeName = TypeName.get(type.getComponentType());
        builder.beginControlFlow("for ($T e :value)", elementTypeName);
        builder.addStatement(getGenericSerializeMethodName(type.getComponentType()) + "(e,acc)");
        builder.endControlFlow();

        return builder.build();
    }

    private MethodSpec arrayUnserializer(ArrayType type) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(getGenericUnserializeMethodName(type))
                .returns(TypeName.get(type))
                .addParameter(byteBuf, "buf");

        TypeName elementTypeName = TypeName.get(type.getComponentType());

        builder.addStatement("int size = buf.readInt()");
        builder.addStatement("$T[] value = new  $T[size]", elementTypeName, elementTypeName);

        builder.beginControlFlow("for (int i=0;i<size;i++)");
        builder.addStatement("$T e = " + getGenericUnserializeMethodName(type.getComponentType()) + "(buf)", TypeName.get(type.getComponentType()));
        builder.addStatement("value[i] = e");
        builder.endControlFlow();

        builder.addStatement("return value");

        return builder.build();
    }


    private MethodSpec enumSerializer(DeclaredType type) {
        return MethodSpec.methodBuilder(getGenericSerializeMethodName(type))
                .returns(void.class)
                .addParameter(TypeName.get(type), "value")
                .addParameter(byteBuf, "acc")
                .addStatement("acc.writeByte(value.ordinal())")
                .build();
    }

    private MethodSpec enumUnserializer(DeclaredType type) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(getGenericUnserializeMethodName(type))
                .returns(TypeName.get(type))
                .addParameter(byteBuf, "buf");

        builder.addStatement("return $T.values()[buf.readByte()]", type);
        return builder.build();
    }

    private MethodSpec concreticSerializer(TypeElement element, DeclaredType type) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(getConcreticSerializeMethodName(type))
                .returns(void.class)
                .addParameter(TypeName.get(type), "value")
                .addParameter(byteBuf, "acc");

        if (haveSerializationOverride(type))
            builder.addStatement("value.serialize(acc)");
        else {
            Function<TypeMirror, TypeMirror> refine = refineParameterizedTypes(type);
            for (VariableElement field : Stream.concat(
                    getSerializableFinalFields(element),
                    getSerializableNonFinalFields(element)
            ).collect(toList()))
                builder.addStatement(getGenericSerializeMethodName(refine.apply(field.asType())) + "($L, acc)", getFieldGetAccess(type, field));
        }

        return builder.build();
    }

    private MethodSpec concreticUnserializer(TypeElement element, DeclaredType type) {
        Function<TypeMirror, TypeMirror> refine = refineParameterizedTypes(type);

        List<TypeMirror> finalFields = getSerializableFinalFields(element)
                .map(Element::asType)
                .map(refine)
                .collect(toList());
        List<VariableElement> mutableFields = getSerializableNonFinalFields(element).collect(toList());

        MethodSpec.Builder builder = MethodSpec.methodBuilder(getConcreticUnserializeMethodName(type))
                .returns(TypeName.get(type))
                .addParameter(byteBuf, "buf");

        if (haveSerializationOverride(type))
            builder.addStatement("return new $T(buf)", type);
        else {
            if (getConstructors(element).noneMatch(c -> {
                List<TypeMirror> constructorSignature = c.getParameters().stream().map(Element::asType).collect(toList());

                return signatureEquals(constructorSignature, finalFields);
            }))
                throw new AnnotationProcessorException(element, "Constructor for final fields not found. Required " + finalFields);

            builder.addCode("$T value = new $T(", type, type);
            for (int i = 0; i < finalFields.size(); i++) {
                builder.addCode(getGenericUnserializeMethodName(finalFields.get(i)) + "(buf)");
                if (i < finalFields.size() - 1)
                    builder.addCode(", ");
            }
            builder.addCode(");\n");

            for (VariableElement field : mutableFields)
                builder.addStatement(getFieldSetAccess(type, field), getGenericUnserializeMethodName(refine.apply(field.asType())) + "(buf)");

            builder.addStatement("return value");
        }

        return builder.build();
    }

    private boolean signatureEquals(List<TypeMirror> a, List<TypeMirror> b) {
        if (a.size() == b.size()) {
            for (int i = 0; i < a.size(); i++)
                if (!typeEquals(a.get(i), b.get(i)))
                    return false;

            return true;
        } else
            return false;
    }

    private List<Map.Entry<DeclaredType, AbstractGenerator>> getSpecialBaseTypes(DeclaredType declaredType) {
        return specials.entrySet().stream()
                .map(leftMapper(elementUtils::getTypeElement))
                .filter(e -> e.getKey() != null)
                .map(leftMapper(Element::asType))
                .map(leftMapper(typeUtils::erasure))
                .map(leftMapper(t -> (DeclaredType) t))
                .filter(t -> typeUtils.isSubtype(typeUtils.erasure(declaredType), t.getKey()))
                .collect(toList());
    }

    private Stream<VariableElement> getSerializableFinalFields(TypeElement element) {
        return getSerializableFields(element)
                .filter(this::isFinal);
    }

    private Stream<VariableElement> getSerializableNonFinalFields(TypeElement element) {
        return getSerializableFields(element)
                .filter(f -> !isFinal(f));
    }

    private Stream<VariableElement> getSerializableFields(TypeElement element) {
        return getFields(element)
                .filter(f -> !f.getModifiers().contains(TRANSIENT) && !f.getModifiers().contains(STATIC));
    }


    private boolean haveSerializationOverride(DeclaredType type) {
        return new InheritanceUtils(typeUtils).getAllInterfaces(type).anyMatch(e -> e == elementUtils.getTypeElement("hohserg.elegant.networking.api.IByteBufSerializable").asType()) &&
                elementUtils.getAllMembers((TypeElement) type.asElement())
                        .stream().anyMatch(e -> {
                    if (e.getKind() == CONSTRUCTOR) {
                        List<? extends TypeMirror> parameterTypes = ((ExecutableType) e.asType()).getParameterTypes();
                        return (parameterTypes.size() == 1 && parameterTypes.get(0) == elementUtils.getTypeElement(byteBuf.canonicalName()).asType());
                    } else
                        return false;
                });
    }

    private MethodSpec genericSingleSerializer(TypeElement element, DeclaredType type, List<DeclaredType> implementations) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(getGenericSerializeMethodName(type))
                .returns(void.class)
                .addParameter(TypeName.get(type), "value")
                .addParameter(byteBuf, "acc");

        builder.addStatement(getConcreticSerializeMethodName(implementations.get(0)) + "(value, acc)");

        return builder.build();
    }

    private MethodSpec genericSerializer(TypeElement element, DeclaredType type, List<DeclaredType> implementations) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(getGenericSerializeMethodName(type))
                .returns(void.class)
                .addParameter(TypeName.get(type), "value")
                .addParameter(byteBuf, "acc");

        builder.beginControlFlow("if (false) ");
        builder.addStatement("return");

        for (int concreteIndex = 0; concreteIndex < implementations.size(); concreteIndex++) {
            DeclaredType concreteType = implementations.get(concreteIndex);
            builder.nextControlFlow("else if (value instanceof $T)", ClassName.get((TypeElement) concreteType.asElement()));
            builder.addStatement("acc.writeByte($L)", concreteIndex);
            builder.addStatement(getConcreticSerializeMethodName(concreteType) + "(($T)value, acc)", concreteType);
        }

        builder.nextControlFlow("else");
        builder.addStatement("throw new IllegalStateException(\"Unexpected implementation of $L: \"+value.getClass().getName())", element.getQualifiedName());
        builder.endControlFlow();

        return builder.build();
    }

    private MethodSpec genericSingleUnserializer(TypeElement element, DeclaredType type, List<DeclaredType> implementations) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(getGenericUnserializeMethodName(type))
                .returns(TypeName.get(type))
                .addParameter(byteBuf, "buf");

        builder.addStatement("return " + getConcreticUnserializeMethodName(implementations.get(0)) + "(buf)");

        return builder.build();
    }

    private MethodSpec genericUnserializer(TypeElement element, DeclaredType type, List<DeclaredType> implementations) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(getGenericUnserializeMethodName(type))
                .returns(TypeName.get(type))
                .addParameter(byteBuf, "buf");

        builder.addStatement("byte concreteIndex = buf.readByte()");

        builder.beginControlFlow("if (false) ");
        builder.addStatement("return null");

        for (int concreteIndex = 0; concreteIndex < implementations.size(); concreteIndex++) {
            DeclaredType concreteType = implementations.get(concreteIndex);
            builder.nextControlFlow("else if (concreteIndex == $L)", concreteIndex);
            builder.addStatement("return " + getConcreticUnserializeMethodName(concreteType) + "(buf)");
        }

        builder.nextControlFlow("else");
        builder.addStatement("throw new IllegalStateException(\"Unexpected implementation of $L: concreteIndex = \"+concreteIndex)", element.getQualifiedName());
        builder.endControlFlow();

        return builder.build();
    }

    JavaFile generateSerializerClass(TypeElement e, List<MethodSpec> serializationMethods) {
        ClassName packet = ClassName.get(e);
        TypeSpec serializer = TypeSpec.classBuilder(getCompanionName(e, "Serializer"))
                .addAnnotation(AnnotationSpec.builder(ClassName.get(elementUtils.getTypeElement(SerializerMark_name))).addMember("packetClass", packet.canonicalName() + ".class").build())
                .addModifiers(PUBLIC)
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get(elementUtils.getTypeElement(ISerializer_name)), packet))
                .addMethod(generateMainSerializeMethod(e, packet))
                .addMethod(generateMainUnserializeMethod(e, packet))
                .addMethods(serializationMethods)
                .build();

        return JavaFile.builder(elementUtils.getPackageOf(e).getQualifiedName().toString(), serializer).build();
    }

    private MethodSpec generateMainSerializeMethod(TypeElement typeElement, ClassName packet) {
        MethodSpec.Builder serialize = MethodSpec.methodBuilder("serialize")
                .addModifiers(PUBLIC);

        serialize
                .returns(void.class)
                .addParameter(packet, "value")
                .addParameter(byteBuf, "acc");

        serialize
                .addStatement(getGenericSerializeMethodName(typeElement.asType()) + "(value, acc)");

        return serialize.build();
    }

    private MethodSpec generateMainUnserializeMethod(TypeElement typeElement, ClassName packet) {
        MethodSpec.Builder unserialize = MethodSpec.methodBuilder("unserialize")
                .addModifiers(PUBLIC);

        unserialize
                .returns(packet)
                .addParameter(byteBuf, "buf");

        unserialize
                .addStatement("return " + getGenericUnserializeMethodName(typeElement.asType()) + "(buf)");

        return unserialize.build();
    }

    public JavaFile generatePacketProvider(TypeElement e, String modid) {
        TypeSpec provider = TypeSpec.classBuilder(getCompanionName(e, "Provider"))
                .addAnnotation(AnnotationSpec.builder(ClassName.get(elementUtils.getTypeElement(PacketProviderMark_name))).build())
                .addModifiers(PUBLIC)
                .addSuperinterface(ClassName.get(elementUtils.getTypeElement(IPacketProvider_name)))
                .addMethod(generatePacketClassGetterMethod(e))
                .addMethod(generateModidGetterMethod(modid))
                .build();

        return JavaFile.builder(elementUtils.getPackageOf(e).getQualifiedName().toString(), provider).build();

    }

    public String getCompanionName(TypeElement e, String suffix) {
        return (e.getNestingKind().isNested() ? e.getEnclosingElement().getSimpleName() + "$" : "") + e.getSimpleName() + suffix;
    }

    private MethodSpec generateModidGetterMethod(String modid) {
        return MethodSpec.methodBuilder("modid")
                .addModifiers(PUBLIC)
                .returns(String.class)
                .addStatement("return $S", modid)
                .build();
    }

    private MethodSpec generatePacketClassGetterMethod(TypeElement e) {
        return MethodSpec.methodBuilder("getPacketClass")
                .addModifiers(PUBLIC)
                .returns(ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(ClassName.get(elementUtils.getTypeElement(IByteBufSerializable_name)))))
                .addStatement("return $L.class", e.getQualifiedName())
                .build();

    }
}
