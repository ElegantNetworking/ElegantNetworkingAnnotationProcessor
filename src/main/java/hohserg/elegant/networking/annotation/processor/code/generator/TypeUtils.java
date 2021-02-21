package hohserg.elegant.networking.annotation.processor.code.generator;

import com.google.common.collect.ImmutableMap;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.function.Function;

public interface TypeUtils {
    default ImmutableMap<TypeMirror, TypeMirror> getRawParametersMappings(DeclaredType type) {
        ImmutableMap.Builder<TypeMirror, TypeMirror> toRawTypeParameter = ImmutableMap.builder();
        List<? extends TypeMirror> rawParameters = ((DeclaredType) type.asElement().asType()).getTypeArguments();
        List<? extends TypeMirror> concreteParameters = type.getTypeArguments();
        for (int i = 0; i < rawParameters.size(); i++)
            toRawTypeParameter.put(rawParameters.get(i), concreteParameters.get(i));

        return toRawTypeParameter.build();
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
}
