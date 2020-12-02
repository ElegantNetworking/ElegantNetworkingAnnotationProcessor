package hohserg.elegant.networking.annotation.processor;

import hohserg.elegant.networking.annotation.processor.dom.ClassRepr;
import hohserg.elegant.networking.annotation.processor.dom.DataClassRepr;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hohserg.elegant.networking.annotation.processor.ElegantPacketProcessor.*;
import static java.util.stream.Collectors.toList;


public class InheritanceUtils {
    public static Set<DataClassRepr> getAllSealedImplementations(DataClassRepr classRepr) {
        TypeMirror baseClass = classRepr.getElement().asType();

        Set<DataClassRepr> r = classRepr.getElement().getEnclosingElement().getEnclosedElements()
                .stream()
                .filter(e -> e.getKind() == ElementKind.CLASS)
                .map(e -> ((TypeElement) e))
                .filter(e -> !e.getModifiers().contains(Modifier.ABSTRACT))
                .filter(e -> typeUtils.directSupertypes(e.asType()).contains(baseClass))
                .map(Element::asType)
                .map(ClassRepr::typeRepresentation)
                .map(e -> (DataClassRepr) e)
                .collect(Collectors.toSet());


        if (!classRepr.getModifiers().contains(Modifier.ABSTRACT))
            r.add(classRepr);

        return r;
    }

    public static Stream<? extends TypeMirror> getAllInterfaces(DataClassRepr type) {
        return getAllInterfaces(type.getOriginal());
    }

    private static Stream<TypeMirror> getAllInterfaces(TypeMirror type){
        return Stream.concat(Stream.of(type),((TypeElement) typeUtils.asElement(type)).getInterfaces().stream().flatMap(InheritanceUtils::getAllInterfaces));
    }
}
