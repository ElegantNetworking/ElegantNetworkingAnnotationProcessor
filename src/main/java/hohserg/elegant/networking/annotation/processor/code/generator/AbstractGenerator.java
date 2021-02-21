package hohserg.elegant.networking.annotation.processor.code.generator;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import hohserg.elegant.networking.annotation.processor.ElegantSerializerProcessor;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface AbstractGenerator extends MethodNames {

    default void getAllSerializableTypes(ElegantSerializerProcessor processor, DeclaredType type, Map<TypeMirror, List<? extends TypeMirror>> types) {
        types.put(type, Collections.emptyList());
        type.getTypeArguments().forEach(t -> processor.getAllSerializableTypes(t, types));
    }

    void generateSerializer(MethodSpec.Builder builder, DeclaredType type);

    void generateUnserializer(MethodSpec.Builder builder, DeclaredType type);

    default MethodSpec generateSerializer1(DeclaredType type) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(getGenericSerializeMethodName(type))
                .returns(void.class)
                .addParameter(TypeName.get(type), "value")
                .addParameter(byteBuf, "acc");

        generateSerializer(builder, type);

        return builder.build();

    }

    default MethodSpec generateUnserializer1(DeclaredType type) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(getGenericUnserializeMethodName(type))
                .returns(TypeName.get(type))
                .addParameter(byteBuf, "buf");

        generateUnserializer(builder, type);

        return builder.build();
    }

    default Stream<MethodSpec> generateMethodsForType(TypeMirror type, List<? extends TypeMirror> implementations) {
        return Stream.of(generateSerializer1(((DeclaredType) type)), generateUnserializer1(((DeclaredType) type)));
    }

}
