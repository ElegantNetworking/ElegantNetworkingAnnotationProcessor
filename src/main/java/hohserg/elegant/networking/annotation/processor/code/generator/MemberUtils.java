package hohserg.elegant.networking.annotation.processor.code.generator;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.stream.Stream;

public interface MemberUtils {
    default Stream<ExecutableElement> getMethods(TypeElement element) {
        return element.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.METHOD)
                .map(e -> ((ExecutableElement) e));
    }

    default Stream<VariableElement> getFields(TypeElement element) {
        return element.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.FIELD)
                .map(e -> ((VariableElement) e));
    }

    default Stream<ExecutableElement> getConstructors(TypeElement element) {
        return element.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.CONSTRUCTOR)
                .map(e -> ((ExecutableElement) e));
    }
}
