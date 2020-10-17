package hohserg.elegant.networking.annotation.processor;

import hohserg.elegant.networking.annotation.processor.dom.*;
import hohserg.elegant.networking.api.ElegantPacket;
import lombok.SneakyThrows;

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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hohserg.elegant.networking.annotation.processor.CodeGenerator.generateSerializerSource;
import static hohserg.elegant.networking.annotation.processor.dom.DataClassRepr.prepare;
import static java.util.stream.Collectors.*;
import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.METHOD;

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

        List<TypeElement> sortedPackets = elegantPackets.stream().sorted(Comparator.comparing(e -> e.getQualifiedName().toString())).collect(Collectors.toList());
        for (int i = 0; i < sortedPackets.size(); i++) {
            int packetId = i + 1;
            TypeElement packet = sortedPackets.get(i);
            try {
                DataClassRepr classRepr = prepare(packet);
                /*typeElement.getFields().forEach(f -> note(f.getName() + f.getModifiers()));
                typeElement.getMethods().forEach(f -> note(f.toString()));
                typeElement.getConstructors().forEach(f -> note(f.toString()));*/

                Set<ClassRepr> serializableTypes = getAllSerializableTypes(classRepr).collect(toSet());

                note("Required to serializer follow types: \n\n\n" + serializableTypes.stream().map(Objects::toString).collect(joining("\n")));

                generateSerializerSource(classRepr, serializableTypes, packetId)
                        .writeTo(filer);
            } catch (Exception e) {
                error(packet, "Failure on building serializer: \n" + e + "\n" + Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(joining("\n")));
            }
        }
        return false;
    }


    private static Stream<ClassRepr> getAllSerializableTypes(ClassRepr classRepr) {
        return Stream.concat(Stream.of(classRepr),
                (classRepr instanceof DataClassRepr) ?
                        CodeGenerator.onlySerializableFields(((DataClassRepr) classRepr).getFields())
                                .stream()
                                .map(FieldRepr::getType)
                                .filter(t -> nonExistsMethod(elementUtils.getTypeElement("hohserg.elegant.networking.impl.ISerializer"), t))
                                .flatMap(ElegantPacketProcessor::getAllSerializableTypes)
                        : Stream.empty());
    }


    private static boolean nonExistsMethod(TypeElement holder, ClassRepr type) {
        String methodName = "serialize" + type.getSimpleName() + "Generic";


        boolean b = elementUtils.getAllMembers(holder).stream().noneMatch(m -> m.getKind() == METHOD && m.getSimpleName().toString().equals(methodName));
        note("nonExistsMethod " + methodName + " noneMatch " + b);
        return b;
    }

    private static boolean havePacketInterfaces(TypeElement typeElement) {
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
