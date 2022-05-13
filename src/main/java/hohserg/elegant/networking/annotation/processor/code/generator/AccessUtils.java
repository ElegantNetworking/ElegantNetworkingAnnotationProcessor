package hohserg.elegant.networking.annotation.processor.code.generator;

import hohserg.elegant.networking.annotation.processor.AnnotationProcessorException;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;

import static javax.lang.model.element.Modifier.*;
import static javax.lang.model.type.TypeKind.BOOLEAN;

public interface AccessUtils extends MemberUtils, TypeUtils {


    default String getFieldGetAccess(DeclaredType type, VariableElement field) throws AnnotationProcessorException {
        return isPrivate(field) ? getterAccess(type, field) : "value." + field.getSimpleName();
    }

    default String getFieldSetAccess(DeclaredType type, VariableElement field) throws AnnotationProcessorException {
        return isPrivate(field) ? setterAccess(type, field) : "value." + field.getSimpleName() + " = $L";
    }

    default String getterAccess(DeclaredType type, VariableElement field) throws AnnotationProcessorException {
        TypeElement element = (TypeElement) type.asElement();
        String capitalized = StringUtils.capitalize(field.getSimpleName().toString());

        boolean get_accessor = getMethods(element).anyMatch(m ->
                m.getSimpleName().toString().equals("get" + capitalized) &&
                        typeEquals(m.getReturnType(), field.asType()) && m.getParameters().isEmpty());

        if (get_accessor)
            return "value." + "get" + capitalized + "()";
        else if (field.asType().getKind() == BOOLEAN) {
            boolean is_accessor = getMethods(element).anyMatch(m ->
                    m.getSimpleName().toString().equals("is" + capitalized) &&
                            typeEquals(m.getReturnType(), field.asType()) && m.getParameters().isEmpty());
            if (is_accessor)
                return "value." + "is" + capitalized + "()";
            else
                throw new AnnotationProcessorException(field, "Private fields must have getters: " + element.getSimpleName() + "#" + field.getSimpleName());
        } else
            throw new AnnotationProcessorException(field, "Private fields must have getters: " + element.getSimpleName() + "#" + field.getSimpleName());
    }

    default String setterAccess(DeclaredType type, VariableElement field) throws AnnotationProcessorException {
        TypeElement element = (TypeElement) type.asElement();
        String capitalized = StringUtils.capitalize(field.getSimpleName().toString());

        boolean set_accessor = getMethods(element).anyMatch(m ->
                m.getSimpleName().toString().equals("set" + capitalized) &&
                        m.getParameters().size() == 1 && typeEquals(m.getParameters().get(0).asType(), field.asType()));
        if (set_accessor)
            return "value." + "set" + capitalized + "($L)";
        else
            throw new AnnotationProcessorException(field, "Private non-final fields must have setters: " + element.getSimpleName() + "#" + field.getSimpleName());
    }

    default boolean isPrivate(VariableElement f) {
        return f.getModifiers().contains(PRIVATE) ||
                !f.getModifiers().contains(PUBLIC) &&
                        !f.getModifiers().contains(PROTECTED) &&
                        isLombokValue(f);
    }

    default boolean isFinal(VariableElement f) {
        return f.getModifiers().contains(FINAL) || isLombokValue(f);
    }

    default boolean isLombokValue(VariableElement f) {
        return f.getEnclosingElement().getAnnotationMirrors().stream().anyMatch(a -> a.getAnnotationType().toString().equals("lombok.Value"));
    }
}
