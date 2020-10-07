package hohserg.elegant.networking.annotation.processor;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.*;
import hohserg.elegant.networking.annotation.processor.dom.ClassRepr;
import hohserg.elegant.networking.annotation.processor.dom.FieldRepr;
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

import static hohserg.elegant.networking.annotation.processor.dom.ClassRepr.prepare;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.METHOD;
import static javax.lang.model.element.Modifier.*;

public class ElegantPacketProcessor extends AbstractProcessor {
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

        List<TypeElement> sortedPackets = elegantPackets.stream().sorted().collect(Collectors.toList());
        for (int i = 0; i < sortedPackets.size(); i++) {
            int packetId = i + 1;
            TypeElement packet = sortedPackets.get(i);
            buildSerializatorClass(LombokUtils.correctFieldModifiers(prepare(packet)), packetId);
        }
        return false;
    }

    private ClassName ByteBuf = ClassName.get("io.netty.buffer", "ByteBuf");

    private void buildSerializatorClass(ClassRepr typeElement, int packetId) throws IOException {
        /*typeElement.getFields().forEach(f -> note(f.getName() + f.getModifiers()));
        typeElement.getMethods().forEach(f -> note(f.toString()));
        typeElement.getConstructors().forEach(f -> note(f.toString()));*/

        List<FieldRepr> serializableFields = typeElement.getFields().stream()
                .filter(f -> !f.getModifiers().contains(TRANSIENT) && !f.getModifiers().contains(STATIC))
                .collect(Collectors.toList());

        Map<Boolean, List<FieldRepr>> collect2 = serializableFields.stream().collect(Collectors.partitioningBy(f -> f.getModifiers().contains(FINAL)));

        List<FieldRepr> finalFields = collect2.get(true);
        List<FieldRepr> mutableFields = collect2.get(false);

        ClassName packet = ClassName.get(typeElement.getElement());

        List<MethodSpec> additionSerializers = serializableFields.stream()
                .filter(this::isNotPrimitive)
                .filter(f -> nonExistsMethod(elementUtils.getTypeElement("hohserg.elegant.networking.impl.ISerializer"), "serialize" + typeSimpleName(f.getType())))
                .flatMap(f -> createSerializationMethods(f)).collect(toList());


        TypeSpec serializer = TypeSpec.classBuilder(typeElement.getElement().getSimpleName() + "Serializer")
                .addModifiers(PUBLIC)
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get("hohserg.elegant.networking.impl", "ISerializer"), packet))
                .addMethod(createSerializeMethod(typeElement, finalFields, mutableFields, packet))
                .addMethod(createUnserializeMethod(typeElement, finalFields, mutableFields, packet))
                .addMethod(createPacketIdMethod(typeElement, packetId))
                .addMethods(additionSerializers)
                .build();

        JavaFile javaFile = JavaFile.builder(elementUtils.getPackageOf(typeElement.getElement()).getQualifiedName().toString(), serializer).build();

        javaFile.writeTo(filer);
    }

    private boolean isNotPrimitive(FieldRepr fieldRepr) {
        return !fieldRepr.getType().getKind().isPrimitive();
    }

    private MethodSpec createPacketIdMethod(ClassRepr typeElement, int packetId) {
        return MethodSpec.methodBuilder("packetId")
                .addModifiers(PUBLIC)
                .returns(int.class)
                .addStatement("return $L", packetId)
                .build();
    }

    private Stream<MethodSpec> createSerializationMethods(FieldRepr f) {
        ClassRepr typeElement = LombokUtils.correctFieldModifiers(f.typeRepresentation(typeUtils));
        List<FieldRepr> serializableFields = typeElement.getFields().stream()
                .filter(f1 -> !f1.getModifiers().contains(TRANSIENT) && !f1.getModifiers().contains(STATIC))
                .collect(Collectors.toList());

        Map<Boolean, List<FieldRepr>> collect2 = serializableFields.stream().collect(Collectors.partitioningBy(f1 -> f1.getModifiers().contains(FINAL)));

        List<FieldRepr> finalFields = collect2.get(true);
        List<FieldRepr> mutableFields = collect2.get(false);


        MethodSpec.Builder serialize = MethodSpec.methodBuilder("serialize" + typeSimpleName(f.getType()))
                .returns(void.class)
                .addParameter(TypeName.get(f.getType()), "value")
                .addParameter(ByteBuf, "acc");

        for (FieldRepr field : ImmutableList.<FieldRepr>builder().addAll(finalFields).addAll(mutableFields).build())
            serialize.addStatement(getSerializeStatement(field.getType()), getFieldGetAccess(typeElement, field));

        MethodSpec.Builder unserialize = MethodSpec.methodBuilder("unserialize" + typeSimpleName(f.getType()))
                .returns(TypeName.get(f.getType()))
                .addParameter(ByteBuf, "buf");

        if (typeElement.getConstructors().stream().noneMatch(c -> c.getArguments().equals(finalFields.stream().map(FieldRepr::getType).collect(toList()))))
            throw new IllegalStateException("Constructor for final fields not found");

        ClassName typeName = ClassName.get(typeElement.getElement());
        unserialize.addCode(typeName.canonicalName() + " value = new " + typeName.canonicalName() + "(");
        for (int i = 0; i < finalFields.size(); i++) {
            unserialize.addCode(getUnserializationStatement(finalFields.get(i).getType()));
            if (i < finalFields.size() - 1)
                unserialize.addCode(", ");
        }
        unserialize.addCode(");\n");

        for (FieldRepr field : mutableFields)
            unserialize.addStatement(getFieldSetAccess(typeElement, field), getUnserializationStatement(field.getType()));

        unserialize.addStatement("return value");

        return //Stream.concat(
                Stream.of(serialize.build(), unserialize.build());//,
        //serializableFields.stream().filter(f1 -> nonExistsMethod(typeElement, "serialize" + typeSimpleName(f1.getType()))).flatMap(f1 -> createSerializationMethods(f1)));
    }

    private boolean nonExistsMethod(TypeElement typeElement, String methodName) {
        return elementUtils.getAllMembers(typeElement).stream().noneMatch(m -> {
            note("test " + m.getSimpleName().toString() + " " + methodName);
            return m.getKind() == METHOD && m.getSimpleName().toString().equals(methodName);
        });
    }

    private MethodSpec createUnserializeMethod(ClassRepr typeElement, List<FieldRepr> finalFields, List<FieldRepr> mutableFields, ClassName packet) {
        MethodSpec.Builder unserialize = MethodSpec.methodBuilder("unserialize")
                .addModifiers(PUBLIC)
                .returns(packet)
                .addParameter(ByteBuf, "buf");

        if (typeElement.getConstructors().stream().noneMatch(c -> c.getArguments().equals(finalFields.stream().map(FieldRepr::getType).collect(toList()))))
            throw new IllegalStateException("Constructor for final fields not found");

        unserialize.addCode(packet.simpleName() + " value = new " + packet.simpleName() + "(");
        for (int i = 0; i < finalFields.size(); i++) {
            unserialize.addCode(getUnserializationStatement(finalFields.get(i).getType()));
            if (i < finalFields.size() - 1)
                unserialize.addCode(", ");
        }
        unserialize.addCode(");\n");

        for (FieldRepr field : mutableFields)
            unserialize.addStatement(getFieldSetAccess(typeElement, field), getUnserializationStatement(field.getType()));

        unserialize.addStatement("return value");

        return unserialize.build();
    }

    private MethodSpec createSerializeMethod(ClassRepr typeElement, List<FieldRepr> finalFields, List<FieldRepr> mutableFields, ClassName packet) {
        MethodSpec.Builder serialize = MethodSpec.methodBuilder("serialize")
                .addModifiers(PUBLIC)
                .returns(void.class)
                .addParameter(packet, "value")
                .addParameter(ByteBuf, "acc");

        for (FieldRepr field : ImmutableList.<FieldRepr>builder().addAll(finalFields).addAll(mutableFields).build())
            serialize.addStatement(getSerializeStatement(field.getType()), getFieldGetAccess(typeElement, field));

        return serialize.build();
    }

    private String getUnserializationStatement(TypeMirror type) {
        switch (type.getKind()) {
            case INT:
                return "buf.readInt()";

            case BYTE:
                return "buf.readByte()";

            case CHAR:
                return "buf.readChar()";

            case LONG:
                return "buf.readLong()";

            case FLOAT:
                return "buf.readFloat()";

            case DOUBLE:
                return "buf.readDouble()";

            case SHORT:
                return "buf.readShort()";

            case BOOLEAN:
                return "buf.readBoolean()";

            case DECLARED:
                return "unserialize" + typeSimpleName(type) + "(buf)";
            default:
                throw new IllegalStateException("You are really?");
        }
    }

    private String typeSimpleName(TypeMirror type) {
        String typeFullName = type.toString();
        return typeFullName.substring(typeFullName.lastIndexOf('.') + 1);
    }

    private String getFieldGetAccess(ClassRepr typeElement, FieldRepr field) {
        return isPrivate(field) ? getterAccess(typeElement, field) : "value." + field.getName();
    }

    private String getFieldSetAccess(ClassRepr typeElement, FieldRepr field) {
        return isPrivate(field) ? setterAccess(typeElement, field) : "value." + field.getName() + " = $L";
    }

    private String setterAccess(ClassRepr typeElement, FieldRepr field) {
        String capitalized = StringUtils.capitalize(field.getName());

        boolean set_accessor = typeElement.getMethods().stream().anyMatch(m -> m.getName().equals("set" + capitalized) && m.getArguments().size() == 1 && m.getArguments().get(0).equals(field.getType()));
        if (set_accessor)
            return "value." + "set" + capitalized + "($L)";
        else
            throw new IllegalStateException("Private non-final fields must have setters");
    }

    private String getSerializeStatement(TypeMirror type) {
        switch (type.getKind()) {
            case INT:
                return "acc.writeInt($L)";

            case BYTE:
                return "acc.writeByte($L)";

            case CHAR:
                return "acc.writeChar($L)";

            case LONG:
                return "acc.writeLong($L)";

            case FLOAT:
                return "acc.writeFloat($L)";

            case DOUBLE:
                return "acc.writeDouble($L)";

            case SHORT:
                return "acc.writeShort($L)";

            case BOOLEAN:
                return "acc.writeBoolean($L)";

            case DECLARED:
                return "serialize" + typeSimpleName(type) + "($L,acc)";
            default:
                throw new IllegalStateException("You are really?");
        }
    }

    private String getterAccess(ClassRepr typeElement, FieldRepr field) {
        String capitalized = StringUtils.capitalize(field.getName());

        boolean get_accessor = typeElement.getMethods().stream().anyMatch(m -> m.getName().equals("get" + capitalized) && m.getResultType().equals(field.getType()) && m.getArguments().isEmpty());
        if (get_accessor)
            return "value." + "get" + capitalized + "()";
        else if (field.getType().getKind() == TypeKind.BOOLEAN) {
            boolean is_accessor = typeElement.getMethods().stream().anyMatch(m -> m.getName().equals("is" + capitalized) && m.getResultType().equals(field.getType()) && m.getArguments().isEmpty());
            if (is_accessor)
                return "value." + "is" + capitalized + "()";
            else
                throw new IllegalStateException("Private fields must have getters");
        } else
            throw new IllegalStateException("Private fields must have getters");
    }

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

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private static Messager messager;

}
