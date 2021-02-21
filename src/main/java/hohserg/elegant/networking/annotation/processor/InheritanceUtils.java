package hohserg.elegant.networking.annotation.processor;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.stream.Stream;


public class InheritanceUtils {
    private Types typeUtils;

    public InheritanceUtils(Types typeUtils) {
        this.typeUtils = typeUtils;
    }

    public Stream<TypeMirror> getAllInterfaces(TypeMirror type) {
        if (!(type instanceof NoType)) {
            TypeElement typeElement = (TypeElement) typeUtils.asElement(type);
            Stream<TypeMirror> implementsRecursive = typeElement.getInterfaces().stream().flatMap(this::getAllInterfaces);
            Stream<TypeMirror> extendsRecursive = getAllInterfaces(typeElement.getSuperclass());

            if (typeElement.getKind() == ElementKind.INTERFACE)
                return Stream.concat(Stream.concat(Stream.of(type), implementsRecursive), extendsRecursive);
            else
                return Stream.concat(implementsRecursive, extendsRecursive);
        } else
            return Stream.empty();
    }

    public boolean isImplements(TypeMirror type, TypeElement interfaceElement) {
        return getAllInterfaces(type).anyMatch(t -> typeUtils.asElement(t) == interfaceElement);
    }
}
