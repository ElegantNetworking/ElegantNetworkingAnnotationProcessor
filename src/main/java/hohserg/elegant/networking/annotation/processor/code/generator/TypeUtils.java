package hohserg.elegant.networking.annotation.processor.code.generator;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public interface TypeUtils {
    default ImmutableMap<TypeMirror, TypeMirror> getRawParametersMappings(DeclaredType type) {
        ImmutableMap.Builder<TypeMirror, TypeMirror> toRawTypeParameter = ImmutableMap.builder();
        List<? extends TypeMirror> rawParameters = getRawType(type).getTypeArguments();
        List<? extends TypeMirror> concreteParameters = type.getTypeArguments();
        for (int i = 0; i < rawParameters.size(); i++)
            toRawTypeParameter.put(rawParameters.get(i), concreteParameters.get(i));

        return toRawTypeParameter.build();
    }

    default DeclaredType getRawType(DeclaredType type) {
        return (DeclaredType) type.asElement().asType();
    }

    default Function<TypeMirror, TypeMirror> refineParameterizedTypes(DeclaredType type) {
        ImmutableMap<TypeMirror, TypeMirror> toConcreteTypeParameter = getRawParametersMappings(type);
        return t -> {
            if (t.getKind() == TypeKind.TYPEVAR)
                return toConcreteTypeParameter.get(t);
            else
                return t;
        };
    }


    default boolean typeEquals(TypeMirror a, TypeMirror b) {
        if (a == b)
            return true;
        else {
            if (a instanceof DeclaredType && b instanceof DeclaredType)
                return getRawType((DeclaredType) a).equals(getRawType((DeclaredType) b)) &&
                        ((DeclaredType) a).getTypeArguments().equals(((DeclaredType) b).getTypeArguments());
            else
                return a.equals(b);
        }
    }

    default int uniqueHash(TypeMirror t) {
        if (t instanceof DeclaredType) {
            return new HashCodeBuilder()
                    .append(getRawType((DeclaredType) t))
                    .append(((DeclaredType) t).getTypeArguments().stream().map(this::uniqueHash).collect(toList()))
                    .build();
        }
        return t.hashCode();
    }
}
