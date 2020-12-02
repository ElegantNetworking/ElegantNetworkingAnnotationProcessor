package hohserg.elegant.networking.annotation.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import hohserg.elegant.networking.annotation.processor.dom.DataClassRepr;
import hohserg.elegant.networking.annotation.processor.dom.EnumClassRepr;
import hohserg.elegant.networking.annotation.processor.dom.FieldRepr;
import hohserg.elegant.networking.annotation.processor.dom.containers.ArrayClassRepr;
import hohserg.elegant.networking.annotation.processor.dom.containers.CollectionClassRepr;
import hohserg.elegant.networking.annotation.processor.dom.containers.MapClassRepr;
import lombok.Value;

import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static hohserg.elegant.networking.annotation.processor.CodeGenerator.*;
import static hohserg.elegant.networking.annotation.processor.ElegantPacketProcessor.*;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.ElementKind.CONSTRUCTOR;
import static javax.lang.model.element.Modifier.FINAL;

public interface MethodRequirement {

    default MethodSpec generateSerializer() {
        throw new UnsupportedOperationException(toString());
    }

    default MethodSpec generateUnserializer() {
        throw new UnsupportedOperationException(toString());
    }

    String serialize_Prefix = "serialize_";
    String unserialize_Prefix = "unserialize_";
    String Generic_Suffix = "_Generic";
    String Concretic_Suffix = "_Concretic";

    @Value
    class GenericMethod implements MethodRequirement {
        DataClassRepr forType;
        List<DataClassRepr> sealedImplementations;

        @Override
        public MethodSpec generateSerializer() {
            List<DataClassRepr> implementations = getSealedImplementations();

            MethodSpec.Builder builder = MethodSpec.methodBuilder(serialize_Prefix + forType.getSimpleName() + Generic_Suffix)
                    .returns(void.class)
                    .addParameter(ClassName.get(forType.getOriginal()), "value")
                    .addParameter(byteBuf, "acc");

            builder.beginControlFlow("if (false) ");
            builder.addStatement("return");

            for (int concreteIndex = 0; concreteIndex < implementations.size(); concreteIndex++) {
                DataClassRepr concreteType = implementations.get(concreteIndex);
                builder.nextControlFlow("else if (value instanceof $T)", concreteType.getOriginal());
                builder.addStatement("acc.writeByte($L)", concreteIndex);
                builder.addStatement(serialize_Prefix + concreteType.getSimpleName() + Concretic_Suffix + "(value, acc)");
            }

            builder.nextControlFlow("else");
            builder.addStatement("throw new IllegalStateException(\"Unexpected implementation of $L: \"+value.getClass().getName())", forType.getName());
            builder.endControlFlow();

            return builder.build();
        }

        @Override
        public MethodSpec generateUnserializer() {
            List<DataClassRepr> implementations = getSealedImplementations();

            MethodSpec.Builder builder = MethodSpec.methodBuilder(unserialize_Prefix + forType.getSimpleName() + Generic_Suffix)
                    .returns(TypeName.get(forType.getOriginal()))
                    .addParameter(byteBuf, "buf");

            builder.addStatement("byte concreteIndex = buf.readByte()");

            builder.beginControlFlow("if (false) ");
            builder.addStatement("return null");

            for (int concreteIndex = 0; concreteIndex < implementations.size(); concreteIndex++) {
                DataClassRepr concreteType = implementations.get(concreteIndex);
                builder.nextControlFlow("else if (concreteIndex == $L)", concreteIndex);
                builder.addStatement("return " + unserialize_Prefix + concreteType.getSimpleName() + Concretic_Suffix + "(buf)");
            }

            builder.nextControlFlow("else");
            builder.addStatement("throw new IllegalStateException(\"Unexpected implementation of $L: concreteIndex = \"+concreteIndex)", forType.getName());
            builder.endControlFlow();

            return builder.build();
        }
    }

    @Value
    class EnumMethod implements MethodRequirement {
        EnumClassRepr forType;

        @Override
        public MethodSpec generateSerializer() {
            return MethodSpec.methodBuilder(serialize_Prefix + forType.getSimpleName() + Generic_Suffix)
                    .returns(void.class)
                    .addParameter(TypeName.get(forType.getOriginal()), "value")
                    .addParameter(byteBuf, "acc")
                    .addStatement("acc.writeByte(value.ordinal())")
                    .build();
        }

        @Override
        public MethodSpec generateUnserializer() {
            MethodSpec.Builder builder = MethodSpec.methodBuilder(unserialize_Prefix + forType.getSimpleName() + Generic_Suffix)
                    .returns(TypeName.get(forType.getOriginal()))
                    .addParameter(byteBuf, "buf");

            builder.addStatement("return $T.values()[buf.readByte()]", forType.getOriginal());
            return builder.build();
        }
    }

    @Value
    class ConcreticMethod implements MethodRequirement {
        DataClassRepr forType;

        @Override
        public MethodSpec generateSerializer() {
            MethodSpec.Builder builder = MethodSpec.methodBuilder(serialize_Prefix + forType.getSimpleName() + Concretic_Suffix)
                    .returns(void.class)
                    .addParameter(ClassName.get(forType.getOriginal()), "value")
                    .addParameter(byteBuf, "acc");

            if (haveSerializationOverride())
                builder.addStatement("value.serialize(acc)");
            else
                for (FieldRepr field : onlySerializableFields(forType.getFields()))
                    builder.addStatement(serialize_Prefix + field.getType().getSimpleName() + Generic_Suffix + "($L, acc)", getFieldGetAccess(forType, field));

            return builder.build();
        }

        private boolean haveSerializationOverride() {
            return InheritanceUtils.getAllInterfaces(forType).anyMatch(e -> e == elementUtils.getTypeElement("hohserg.elegant.networking.api.IByteBufSerializable").asType()) &&
                    elementUtils.getAllMembers(forType.getElement())
                            .stream().anyMatch(e -> {
                        if (e.getKind() == CONSTRUCTOR) {
                            List<? extends TypeMirror> parameterTypes = ((ExecutableType) e.asType()).getParameterTypes();
                            return (parameterTypes.size() == 1 && parameterTypes.get(0) == elementUtils.getTypeElement(byteBuf.canonicalName()).asType());
                        } else
                            return false;
                    });
        }

        @Override
        public MethodSpec generateUnserializer() {
            Map<Boolean, List<FieldRepr>> collect2 = onlySerializableFields(forType.getFields()).stream().collect(Collectors.partitioningBy(f -> f.getModifiers().contains(FINAL)));
            List<FieldRepr> finalFields = collect2.get(true);
            List<FieldRepr> mutableFields = collect2.get(false);

            MethodSpec.Builder builder = MethodSpec.methodBuilder(unserialize_Prefix + forType.getSimpleName() + Concretic_Suffix)
                    .returns(TypeName.get(forType.getOriginal()))
                    .addParameter(byteBuf, "buf");

            if (haveSerializationOverride())
                builder.addStatement("return new $T(buf)", forType.getOriginal());
            else {
                if (forType.getConstructors().stream().noneMatch(c -> c.getArguments().equals(finalFields.stream().map(FieldRepr::getType).collect(toList()))))
                    throw new IllegalStateException("Constructor for final fields not found");

                builder.addCode("$T value = new $T(", forType.getOriginal(), forType.getOriginal());
                for (int i = 0; i < finalFields.size(); i++) {
                    builder.addCode(unserialize_Prefix + finalFields.get(i).getType().getSimpleName() + Generic_Suffix + "(buf)");
                    if (i < finalFields.size() - 1)
                        builder.addCode(", ");
                }
                builder.addCode(");\n");

                for (FieldRepr field : mutableFields)
                    builder.addStatement(getFieldSetAccess(forType, field), unserialize_Prefix + field.getType().getSimpleName() + Generic_Suffix + "(buf)");

                builder.addStatement("return value");
            }

            return builder.build();
        }
    }

    @Value
    class MapMethod implements MethodRequirement {
        MapClassRepr forType;

        @Override
        public MethodSpec generateSerializer() {
            MethodSpec.Builder builder = MethodSpec.methodBuilder(serialize_Prefix + forType.getSimpleName() + Generic_Suffix)
                    .returns(void.class)
                    .addParameter(TypeName.get(forType.getOriginal()), "value")
                    .addParameter(byteBuf, "acc");

            builder.addStatement("acc.writeInt(value.size())");

            TypeName keyTypeName = TypeName.get(forType.getKeyType().getOriginal());
            TypeName valueTypeName = TypeName.get(forType.getValueType().getOriginal());

            builder.beginControlFlow("for (Map.Entry<$T, $T> entry :value.entrySet())", keyTypeName, valueTypeName);
            builder.addStatement("$T k = entry.getKey()", keyTypeName);
            builder.addStatement("$T v = entry.getValue()", valueTypeName);
            builder.addStatement(serialize_Prefix + forType.getKeyType().getSimpleName() + Generic_Suffix + "(k,acc)");
            builder.addStatement(serialize_Prefix + forType.getValueType().getSimpleName() + Generic_Suffix + "(v,acc)");
            builder.endControlFlow();

            return builder.build();
        }

        @Override
        public MethodSpec generateUnserializer() {
            MethodSpec.Builder builder = MethodSpec.methodBuilder(unserialize_Prefix + forType.getSimpleName() + Generic_Suffix)
                    .returns(TypeName.get(forType.getOriginal()))
                    .addParameter(byteBuf, "buf");

            builder.addStatement("int size = buf.readInt()");
            builder.addStatement(forType.getConcreteBuilder());

            builder.beginControlFlow("for (int i=0;i<size;i++)");
            builder.addStatement("$T k = " + unserialize_Prefix + forType.getKeyType().getSimpleName() + Generic_Suffix + "(buf)", TypeName.get(forType.getKeyType().getOriginal()));
            builder.addStatement("$T v = " + unserialize_Prefix + forType.getValueType().getSimpleName() + Generic_Suffix + "(buf)", TypeName.get(forType.getValueType().getOriginal()));
            builder.addStatement("value.put(k, v)");
            builder.endControlFlow();

            builder.addStatement("return " + forType.getConcreteFinalizer());

            return builder.build();
        }
    }

    @Value
    class CollectionMethod implements MethodRequirement {
        CollectionClassRepr forType;

        @Override
        public MethodSpec generateSerializer() {
            MethodSpec.Builder builder = MethodSpec.methodBuilder(serialize_Prefix + forType.getSimpleName() + Generic_Suffix)
                    .returns(void.class)
                    .addParameter(TypeName.get(forType.getOriginal()), "value")
                    .addParameter(byteBuf, "acc");

            builder.addStatement("acc.writeInt(value.size())");

            TypeName elementTypeName = TypeName.get(forType.getElementType().getOriginal());
            builder.beginControlFlow("for ($T e :value)", elementTypeName);
            builder.addStatement(serialize_Prefix + forType.getElementType().getSimpleName() + Generic_Suffix + "(e,acc)");
            builder.endControlFlow();

            return builder.build();
        }

        @Override
        public MethodSpec generateUnserializer() {
            MethodSpec.Builder builder = MethodSpec.methodBuilder(unserialize_Prefix + forType.getSimpleName() + Generic_Suffix)
                    .returns(TypeName.get(forType.getOriginal()))
                    .addParameter(byteBuf, "buf");

            builder.addStatement("int size = buf.readInt()");
            builder.addStatement(forType.getConcreteBuilder());

            builder.beginControlFlow("for (int i=0;i<size;i++)");
            builder.addStatement("$T e = " + unserialize_Prefix + forType.getElementType().getSimpleName() + Generic_Suffix + "(buf)", TypeName.get(forType.getElementType().getOriginal()));
            builder.addStatement("value.add(e)");
            builder.endControlFlow();

            builder.addStatement("return " + forType.getConcreteFinalizer());

            return builder.build();
        }
    }

    @Value
    class ArrayMethod implements MethodRequirement {
        ArrayClassRepr forType;

        @Override
        public MethodSpec generateSerializer() {
            MethodSpec.Builder builder = MethodSpec.methodBuilder(serialize_Prefix + forType.getSimpleName() + Generic_Suffix)
                    .returns(void.class)
                    .addParameter(TypeName.get(forType.getOriginal()), "value")
                    .addParameter(byteBuf, "acc");

            builder.addStatement("acc.writeInt(value.length)");

            TypeName elementTypeName = TypeName.get(forType.getElementType().getOriginal());
            builder.beginControlFlow("for ($T e :value)", elementTypeName);
            builder.addStatement(serialize_Prefix + forType.getElementType().getSimpleName() + Generic_Suffix + "(e,acc)");
            builder.endControlFlow();

            return builder.build();
        }

        @Override
        public MethodSpec generateUnserializer() {
            MethodSpec.Builder builder = MethodSpec.methodBuilder(unserialize_Prefix + forType.getSimpleName() + Generic_Suffix)
                    .returns(TypeName.get(forType.getOriginal()))
                    .addParameter(byteBuf, "buf");

            TypeName elementTypeName = TypeName.get(forType.getElementType().getOriginal());

            builder.addStatement("int size = buf.readInt()");
            builder.addStatement("$T[] value = new  $T[size]", elementTypeName, elementTypeName);

            builder.beginControlFlow("for (int i=0;i<size;i++)");
            builder.addStatement("$T e = " + unserialize_Prefix + forType.getElementType().getSimpleName() + Generic_Suffix + "(buf)", TypeName.get(forType.getElementType().getOriginal()));
            builder.addStatement("value[i] = e");
            builder.endControlFlow();

            builder.addStatement("return value");

            return builder.build();
        }
    }
}
