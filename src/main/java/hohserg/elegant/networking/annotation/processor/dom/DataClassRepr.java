package hohserg.elegant.networking.annotation.processor.dom;

import hohserg.elegant.networking.annotation.processor.CodeGenerator;
import hohserg.elegant.networking.annotation.processor.InheritanceUtils;
import hohserg.elegant.networking.annotation.processor.MethodRequirement;
import lombok.Value;
import lombok.experimental.Wither;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static hohserg.elegant.networking.annotation.processor.ElegantPacketProcessor.*;
import static hohserg.elegant.networking.annotation.processor.LombokUtils.correctFieldModifiers;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Value
@Wither
public class DataClassRepr implements ClassRepr {
    TypeElement element;
    List<? extends AnnotationMirror> annotations;
    String name;
    String simpleName;
    Set<Modifier> modifiers;
    List<FieldRepr> fields;
    List<MethodRepr> methods;
    List<MethodRepr> constructors;

    public static DataClassRepr prepare(TypeElement typeElement) {
        List<? extends Element> allSubElements = getAllSubElements(typeElement).collect(toList());
        return correctFieldModifiers(new DataClassRepr(
                typeElement,
                typeElement.getAnnotationMirrors(),
                typeElement.getQualifiedName().toString(),
                typeElement.getSimpleName().toString(),
                typeElement.getModifiers(),
                allSubElements.stream().filter(e -> e.getKind() == ElementKind.FIELD).map(e -> ((VariableElement) e)).map(FieldRepr::prepare).collect(toList()),
                allSubElements.stream().filter(e -> e.getKind() == ElementKind.METHOD).map(e -> ((ExecutableElement) e)).map(MethodRepr::prepare).collect(toList()),
                allSubElements.stream().filter(e -> e.getKind() == ElementKind.CONSTRUCTOR).map(e -> ((ExecutableElement) e)).map(MethodRepr::prepare).collect(toList())
        ));
    }

    private static Stream<? extends Element> getAllSubElements(TypeElement typeElement) {
        if (typeElement == null)
            return Stream.empty();

        TypeMirror superclass = typeElement.getSuperclass();
        if (superclass == elementUtils.getTypeElement(Object.class.getName()).asType())
            return typeElement.getEnclosedElements().stream();
        else
            return Stream.concat(getAllSubElements((TypeElement) typeUtils.asElement(superclass)), typeElement.getEnclosedElements().stream());
    }

    @Override
    public Set<ClassRepr> getEnclosingTypes() {
        return CodeGenerator.onlySerializableFields(fields).stream().map(FieldRepr::getType).collect(toSet());
    }

    @Override
    public TypeMirror getOriginal() {
        return element.asType();
    }

    @Override
    public Stream<MethodRequirement> getRequirementMethods() {
        List<DataClassRepr> sealedImplementations = InheritanceUtils.getAllSealedImplementations(this)
                .stream().sorted(Comparator.comparing(DataClassRepr::getName)).collect(toList());
        if (options.containsKey(printDetailsOption) || sealedImplementations.size() > 1)
            note(getName() + " have implementations: " + sealedImplementations.stream().map(ClassRepr::getName).collect(toSet()));
        if (sealedImplementations.isEmpty())
            warn(getName() + " have no implementations! Is it a bug? ");

        return Stream.concat(
                Stream.of(new MethodRequirement.GenericMethod(this, sealedImplementations)),
                sealedImplementations.stream().map(MethodRequirement.ConcreticMethod::new)
        );
    }
}
