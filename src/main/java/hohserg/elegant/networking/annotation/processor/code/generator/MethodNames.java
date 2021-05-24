package hohserg.elegant.networking.annotation.processor.code.generator;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Set;
import java.util.function.BiFunction;

public interface MethodNames extends ICodeGenerator {

    default String getGenericSerializeMethodName(TypeMirror type) {
        return serialize_Prefix + getTypeSimpleName(type) + Generic_Suffix;
    }

    default String getConcreticSerializeMethodName(TypeMirror type) {
        return serialize_Prefix + getTypeSimpleName(type) + Concretic_Suffix;
    }

    default String getGenericUnserializeMethodName(TypeMirror type) {
        return unserialize_Prefix + getTypeSimpleName(type) + Generic_Suffix;
    }

    default String getConcreticUnserializeMethodName(TypeMirror type) {
        return unserialize_Prefix + getTypeSimpleName(type) + Concretic_Suffix;
    }

    String serialize_Prefix = "serialize_";
    String unserialize_Prefix = "unserialize_";
    String Generic_Suffix = "_Generic";
    String Concretic_Suffix = "_Concretic";

    default String getTypeSimpleName(TypeMirror type) {
        if (isPrimitive(type))
            return StringUtils.capitalize(unboxIfPossible(type).toString().toLowerCase());
        else if (type instanceof DeclaredType) {
            DeclaredType declaredType = (DeclaredType) type;
            String baseName = declaredType.asElement().getSimpleName().toString();
            if (declaredType.getTypeArguments().size() > 0)
                return baseName + declaredType.getTypeArguments().stream().reduce("_of", (BiFunction<String, TypeMirror, String>) (s, typeMirror) -> s + "_" + getTypeSimpleName(typeMirror), (a, b) -> a + b);
            return baseName;
        } else if (type instanceof ArrayType)
            return "Array" + getTypeSimpleName(((ArrayType) type).getComponentType());
        else
            return "";
    }

    default TypeMirror unboxIfPossible(TypeMirror type) {
        if (type == null)
            throw new RuntimeException("test1");
        if (type.getKind() == null)
            throw new RuntimeException("test2");
        if (getTypeUtils() == null)
            throw new RuntimeException("test3");

        return type.getKind().isPrimitive() ? type : getTypeUtils().unboxedType(type);
    }

    default boolean isPrimitive(TypeMirror type) {
        return type.getKind().isPrimitive() || boxedPrimitives.contains(type.toString());
    }

    Set<String> boxedPrimitives = ImmutableSet.of(
            Boolean.class.getName(),
            Byte.class.getName(),
            Short.class.getName(),
            Integer.class.getName(),
            Long.class.getName(),
            Character.class.getName(),
            Float.class.getName(),
            Double.class.getName()
    );
}
