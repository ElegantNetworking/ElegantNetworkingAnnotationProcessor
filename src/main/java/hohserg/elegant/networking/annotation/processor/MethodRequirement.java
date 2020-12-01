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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static hohserg.elegant.networking.annotation.processor.CodeGenerator.*;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.FINAL;

public interface MethodRequirement {

    default MethodSpec generateSerializer() {
        throw new UnsupportedOperationException(toString());
    }

    default MethodSpec generateUnserializer() {
        throw new UnsupportedOperationException(toString());
    }

    @Value
    class GenericMethod implements MethodRequirement {
        DataClassRepr forType;
        List<DataClassRepr> sealedImplementations;

        @Override
        public MethodSpec generateSerializer() {
            List<DataClassRepr> implementations = getSealedImplementations();

            MethodSpec.Builder builder = MethodSpec.methodBuilder("serialize" + forType.getSimpleName() + "Generic")
                    .returns(void.class)
                    .addParameter(ClassName.get(forType.getOriginal()), "value")
                    .addParameter(byteBuf, "acc");

            builder.beginControlFlow("if (false) ");
            builder.addStatement("return");

            for (int concreteIndex = 0; concreteIndex < implementations.size(); concreteIndex++) {
                DataClassRepr concreteType = implementations.get(concreteIndex);
                builder.nextControlFlow("else if (value instanceof $L)", concreteType.getName());
                builder.addStatement("acc.writeByte($L)", concreteIndex);
                builder.addStatement("serialize" + concreteType.getSimpleName() + "Concretic(value, acc)");
            }

            builder.nextControlFlow("else");
            builder.addStatement("throw new IllegalStateException(\"Unexpected implementation of $L: \"+value.getClass().getName())", forType.getName());
            builder.endControlFlow();

            return builder.build();
        }

        @Override
        public MethodSpec generateUnserializer() {
            List<DataClassRepr> implementations = getSealedImplementations();

            MethodSpec.Builder builder = MethodSpec.methodBuilder("unserialize" + forType.getSimpleName() + "Generic")
                    .returns(TypeName.get(forType.getOriginal()))
                    .addParameter(byteBuf, "buf");

            builder.addStatement("byte concreteIndex = buf.readByte()");

            builder.beginControlFlow("if (false) ");
            builder.addStatement("return null");

            for (int concreteIndex = 0; concreteIndex < implementations.size(); concreteIndex++) {
                DataClassRepr concreteType = implementations.get(concreteIndex);
                builder.nextControlFlow("else if (concreteIndex == $L)", concreteIndex);
                builder.addStatement("return unserialize" + concreteType.getSimpleName() + "Concretic(buf)");
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
            return MethodSpec.methodBuilder("serialize" + forType.getSimpleName() + "Generic")
                    .returns(void.class)
                    .addParameter(TypeName.get(forType.getOriginal()), "value")
                    .addParameter(byteBuf, "acc")
                    .addStatement("acc.writeByte(value.ordinal())")
                    .build();
        }

        @Override
        public MethodSpec generateUnserializer() {
            MethodSpec.Builder builder = MethodSpec.methodBuilder("unserialize" + forType.getSimpleName() + "Generic")
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
            MethodSpec.Builder builder = MethodSpec.methodBuilder("serialize" + forType.getSimpleName() + "Concretic")
                    .returns(void.class)
                    .addParameter(ClassName.get(forType.getOriginal()), "value")
                    .addParameter(byteBuf, "acc");

            for (FieldRepr field : onlySerializableFields(forType.getFields()))
                builder.addStatement("serialize" + field.getType().getSimpleName() + "Generic($L, acc)", getFieldGetAccess(forType, field));

            return builder.build();
        }

        @Override
        public MethodSpec generateUnserializer() {
            Map<Boolean, List<FieldRepr>> collect2 = onlySerializableFields(forType.getFields()).stream().collect(Collectors.partitioningBy(f -> f.getModifiers().contains(FINAL)));
            List<FieldRepr> finalFields = collect2.get(true);
            List<FieldRepr> mutableFields = collect2.get(false);

            MethodSpec.Builder builder = MethodSpec.methodBuilder("unserialize" + forType.getSimpleName() + "Concretic")
                    .returns(TypeName.get(forType.getOriginal()))
                    .addParameter(byteBuf, "buf");

            if (forType.getConstructors().stream().noneMatch(c -> c.getArguments().equals(finalFields.stream().map(FieldRepr::getType).collect(toList()))))
                throw new IllegalStateException("Constructor for final fields not found");

            builder.addCode(forType.getName() + " value = new " + forType.getName() + "(");
            for (int i = 0; i < finalFields.size(); i++) {
                builder.addCode("unserialize" + finalFields.get(i).getType().getSimpleName() + "Generic(buf)");
                if (i < finalFields.size() - 1)
                    builder.addCode(", ");
            }
            builder.addCode(");\n");

            for (FieldRepr field : mutableFields)
                builder.addStatement(getFieldSetAccess(forType, field), "unserialize" + field.getType().getSimpleName() + "Generic(buf)");

            builder.addStatement("return value");

            return builder.build();
        }
    }

    @Value
    class MapMethod implements MethodRequirement {
        MapClassRepr forType;

        @Override
        public MethodSpec generateSerializer() {
            MethodSpec.Builder builder = MethodSpec.methodBuilder("serialize" + forType.getSimpleName() + "Generic")
                    .returns(void.class)
                    .addParameter(TypeName.get(forType.getOriginal()), "value")
                    .addParameter(byteBuf, "acc");

            builder.addStatement("acc.writeInt(value.size())");

            builder.beginControlFlow("for (Map.Entry<String, Integer> entry :value.entrySet())");
            builder.addStatement("$T k = entry.getKey()", TypeName.get(forType.getKeyType().getOriginal()));
            builder.addStatement("$T v = entry.getValue()", TypeName.get(forType.getValueType().getOriginal()));
            builder.addStatement("serialize" + forType.getKeyType().getSimpleName() + "Generic(k,acc)");
            builder.addStatement("serialize" + forType.getValueType().getSimpleName() + "Generic(v,acc)");
            builder.endControlFlow();

            return builder.build();
        }

        @Override
        public MethodSpec generateUnserializer() {
            MethodSpec.Builder builder = MethodSpec.methodBuilder("unserialize" + forType.getSimpleName() + "Generic")
                    .returns(TypeName.get(forType.getOriginal()))
                    .addParameter(byteBuf, "buf");

            builder.addStatement("int size = buf.readInt()");
            builder.addStatement(forType.getConcreteBuilder());

            builder.beginControlFlow("for (int i=0;i<size;i++)");
            builder.addStatement("$T k = unserialize" + forType.getKeyType().getSimpleName() + "Generic(buf)", TypeName.get(forType.getKeyType().getOriginal()));
            builder.addStatement("$T v = unserialize" + forType.getValueType().getSimpleName() + "Generic(buf)", TypeName.get(forType.getValueType().getOriginal()));
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
            MethodSpec.Builder builder = MethodSpec.methodBuilder("serialize" + forType.getSimpleName() + "Generic")
                    .returns(void.class)
                    .addParameter(TypeName.get(forType.getOriginal()), "value")
                    .addParameter(byteBuf, "acc");

            builder.addStatement("acc.writeInt(value.size())");

            TypeName elementTypeName = TypeName.get(forType.getElementType().getOriginal());
            builder.beginControlFlow("for ($T e :value)", elementTypeName);
            builder.addStatement("serialize" + forType.getElementType().getSimpleName() + "Generic(e,acc)");
            builder.endControlFlow();

            return builder.build();
        }

        @Override
        public MethodSpec generateUnserializer() {
            MethodSpec.Builder builder = MethodSpec.methodBuilder("unserialize" + forType.getSimpleName() + "Generic")
                    .returns(TypeName.get(forType.getOriginal()))
                    .addParameter(byteBuf, "buf");

            builder.addStatement("int size = buf.readInt()");
            builder.addStatement(forType.getConcreteBuilder());

            builder.beginControlFlow("for (int i=0;i<size;i++)");
            builder.addStatement("$T e = unserialize" + forType.getElementType().getSimpleName() + "Generic(buf)", TypeName.get(forType.getElementType().getOriginal()));
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
            MethodSpec.Builder builder = MethodSpec.methodBuilder("serialize" + forType.getSimpleName() + "Generic")
                    .returns(void.class)
                    .addParameter(TypeName.get(forType.getOriginal()), "value")
                    .addParameter(byteBuf, "acc");

            builder.addStatement("acc.writeInt(value.length)");

            TypeName elementTypeName = TypeName.get(forType.getElementType().getOriginal());
            builder.beginControlFlow("for ($T e :value)", elementTypeName);
            builder.addStatement("serialize" + forType.getElementType().getSimpleName() + "Generic(e,acc)");
            builder.endControlFlow();

            return builder.build();
        }

        @Override
        public MethodSpec generateUnserializer() {
            MethodSpec.Builder builder = MethodSpec.methodBuilder("unserialize" + forType.getSimpleName() + "Generic")
                    .returns(TypeName.get(forType.getOriginal()))
                    .addParameter(byteBuf, "buf");

            TypeName elementTypeName = TypeName.get(forType.getElementType().getOriginal());

            builder.addStatement("int size = buf.readInt()");
            builder.addStatement("$T[] value = new  $T[size]", elementTypeName, elementTypeName);

            builder.beginControlFlow("for (int i=0;i<size;i++)");
            builder.addStatement("$T e = unserialize" + forType.getElementType().getSimpleName() + "Generic(buf)", TypeName.get(forType.getElementType().getOriginal()));
            builder.addStatement("value[i] = e");
            builder.endControlFlow();

            builder.addStatement("return value");

            return builder.build();
        }
    }
}
