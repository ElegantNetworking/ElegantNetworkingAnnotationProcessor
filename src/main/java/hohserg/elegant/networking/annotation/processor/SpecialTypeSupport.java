package hohserg.elegant.networking.annotation.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import hohserg.elegant.networking.annotation.processor.code.generator.AbstractGenerator;
import lombok.Value;
import org.apache.commons.lang3.tuple.Pair;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class SpecialTypeSupport {

    public static AbstractGenerator empty = new AbstractGenerator() {
        @Override
        public Types getTypeUtils() {
            return null;
        }

        @Override
        public Elements getElementUtils() {
            return null;
        }

        @Override
        public void getAllSerializableTypes(ElegantSerializerProcessor elegantSerializerProcessor, DeclaredType type, Map<TypeMirror, List<? extends TypeMirror>> types) {
        }

        @Override
        public void generateSerializer(MethodSpec.Builder builder, DeclaredType type) {

        }

        @Override
        public void generateUnserializer(MethodSpec.Builder builder, DeclaredType type) {

        }

        @Override
        public Stream<MethodSpec> generateMethodsForType(TypeMirror type, List<? extends TypeMirror> implementations) {
            return Stream.empty();
        }
    };

    public static CollectionTypeSupport immutableCollectionSpecial(Types typeUtils, Elements elementUtils, String collectionType) {
        return commonCollectionSpecial(typeUtils, elementUtils, collectionType + ".Builder", __ -> collectionType + ".builder()", "$L.add(e)", "$L.build()");
    }

    public static CollectionTypeSupport mutableCollectionSpecial(Types typeUtils, Elements elementUtils, String collectionType) {
        return commonCollectionSpecial(typeUtils, elementUtils, collectionType, __ -> "new " + collectionType + "()", "$L.add(e)", "$L");
    }

    public static CollectionTypeSupport commonCollectionSpecial(Types typeUtils, Elements elementUtils, String collectionType, Function<DeclaredType, String> createBuilderStatement, String addStatement, String finalizeStatement) {
        return new CollectionTypeSupport(typeUtils, elementUtils, collectionType, createBuilderStatement, addStatement, finalizeStatement);
    }

    public static MapTypeSupport immutableMapSpecial(Types typeUtils, Elements elementUtils, String collectionType) {
        return commonMapSpecial(typeUtils, elementUtils, collectionType + ".Builder", __ -> collectionType + ".builder()", "$L.put($L,$L)", "$L.build()");
    }

    public static MapTypeSupport mutableMapSpecial(Types typeUtils, Elements elementUtils, String collectionType) {
        return commonMapSpecial(typeUtils, elementUtils, collectionType, __ -> "new " + collectionType + "()", "$L.put($L,$L)", "$L");
    }

    public static MapTypeSupport commonMapSpecial(Types typeUtils, Elements elementUtils, String collectionType, Function<DeclaredType, String> createBuilderStatement, String addStatement, String finalizeStatement) {
        return new MapTypeSupport(typeUtils, elementUtils, collectionType, createBuilderStatement, addStatement, finalizeStatement);
    }

    public static AbstractGenerator getCustomSpecialGenerator(Types typeUtils, Elements elementUtils, Map.Entry<DeclaredType, AbstractGenerator> baseSpecial, DeclaredType customSpecialType) {
        AbstractGenerator baseGenerator = baseSpecial.getValue();
        if (baseGenerator instanceof SpecialTypeSupport.MapTypeSupport) {
            return mutableMapSpecial(typeUtils, elementUtils, customSpecialType.toString());

        } else if (baseGenerator instanceof SpecialTypeSupport.CollectionTypeSupport) {
            return mutableCollectionSpecial(typeUtils, elementUtils, customSpecialType.toString());

        } else if (baseGenerator instanceof SpecialTypeSupport.PairSupport) {
            return new SpecialTypeSupport.PairSupport(typeUtils, elementUtils, customSpecialType.toString());
        } else
            throw new AnnotationProcessorException(customSpecialType.asElement(), "Unsupported custom special type: " + customSpecialType.toString() + " with " + baseGenerator.getClass().getSimpleName());
    }

    //todo
    /*
    @Value
    public static class LinkedMapTypeSupport implements AbstractGenerator {
        Types typeUtils;
        Elements elementUtils;
        MapTypeSupport base = commonMapSpecial(typeUtils, elementUtils, LinkedHashMap.class.getCanonicalName(), __ -> "new " + LinkedHashMap.class.getCanonicalName() + "(size, 0.75f, accessOrder)", "$L.put($L,$L)", "$L");

        @Override
        public void getAllSerializableTypes(ElegantSerializerProcessor processor, DeclaredType type, Map<TypeMirror, List<? extends TypeMirror>> types) {
            base.getAllSerializableTypes(processor, type, types);
        }

        @Override
        public void generateSerializer1(MethodSpec.Builder builder, DeclaredType type) {
            LinkedHashMap t = new LinkedHashMap();
            builder.addStatement("acc.writeBoolean(value.accessOrder");//private field. todo?
            base.generateSerializer1(builder, type);
        }

        @Override
        public void generateUnserializer1(MethodSpec.Builder builder, DeclaredType type) {
            builder.addStatement("boolean accessOrder = buf.readBoolean()");
            base.generateUnserializer1(builder, type);
        }

        @Override
        public Types getTypeUtils() {
            return null;
        }

        @Override
        public Elements getElementUtils() {
            return null;
        }
    }*/

    //Map           Сериализация: запись размера коллекции, итерация коллекции с вызовом функции сериализации для каждого ключа и значения,
    //              Десериализация: чтение размера коллекции, создания экземпляра HashMap(размер), последовательные вызовы методов десериализации ключа и значения с вставкой в мапу
    @Value
    public static class MapTypeSupport implements AbstractGenerator {
        Types typeUtils;
        Elements elementUtils;
        String collectionType;
        Function<DeclaredType, String> createBuilderStatement;
        String addStatement;
        String finalizeStatement;


        @Override
        public void generateSerializer(MethodSpec.Builder builder, DeclaredType type) {
            TypeMirror keyType = type.getTypeArguments().get(0);
            TypeMirror valueType = type.getTypeArguments().get(1);

            builder.addStatement("acc.writeInt(value.size())");

            TypeName keyTypeName = TypeName.get(keyType);
            TypeName valueTypeName = TypeName.get(valueType);
            TypeName entryName = ParameterizedTypeName.get(ClassName.get(elementUtils.getTypeElement("java.util.Map.Entry")), keyTypeName, valueTypeName);

            builder.beginControlFlow("for ($T entry :value.entrySet())", entryName);
            builder.addStatement("$T k = entry.getKey()", keyTypeName);
            builder.addStatement("$T v = entry.getValue()", valueTypeName);
            builder.addStatement(getGenericSerializeMethodName(keyType) + "(k,acc)");
            builder.addStatement(getGenericSerializeMethodName(valueType) + "(v,acc)");
            builder.endControlFlow();
        }

        @Override
        public void generateUnserializer(MethodSpec.Builder builder, DeclaredType type) {
            TypeMirror keyType = type.getTypeArguments().get(0);
            TypeMirror valueType = type.getTypeArguments().get(1);

            builder.addStatement("int size = buf.readInt()");
            builder.addStatement(collectionType + " value = " + createBuilderStatement.apply(type));

            builder.beginControlFlow("for (int i=0;i<size;i++)");
            builder.addStatement("$T k = " + getGenericUnserializeMethodName(keyType) + "(buf)", TypeName.get(keyType));
            builder.addStatement("$T v = " + getGenericUnserializeMethodName(valueType) + "(buf)", TypeName.get(valueType));
            builder.addStatement(addStatement, "value", "k", "v");
            builder.endControlFlow();

            builder.addStatement("return " + finalizeStatement, "value");
        }
    }

    //Iterable    Сериализация: запись размера коллекции, итерация коллекции с вызовом функции сериализации для каждого элемента,
    //            Десериализация: чтение размера коллекции, создания экземпляра ArrayList(размер), последовательные вызовы методов десериализации с вставкой в лист
    @Value
    public static class CollectionTypeSupport implements AbstractGenerator {
        Types typeUtils;
        Elements elementUtils;
        public String collectionType;
        public Function<DeclaredType, String> createBuilderStatement;
        public String addStatement;
        public String finalizeStatement;

        @Override
        public void generateSerializer(MethodSpec.Builder builder, DeclaredType type) {
            builder.addStatement("acc.writeInt(value.size())");

            TypeName elementTypeName = TypeName.get(type.getTypeArguments().get(0));
            builder.beginControlFlow("for ($T e :value)", elementTypeName);
            builder.addStatement(getGenericSerializeMethodName(type.getTypeArguments().get(0)) + "(e,acc)");
            builder.endControlFlow();
        }

        @Override
        public void generateUnserializer(MethodSpec.Builder builder, DeclaredType type) {
            builder.addStatement("int size = buf.readInt()");
            builder.addStatement(collectionType + " value = " + createBuilderStatement.apply(type));

            builder.beginControlFlow("for (int i=0;i<size;i++)");
            builder.addStatement("$T e = " + getGenericUnserializeMethodName(type.getTypeArguments().get(0)) + "(buf)", TypeName.get(type.getTypeArguments().get(0)));
            builder.addStatement(addStatement, "value");
            builder.endControlFlow();

            builder.addStatement("return " + finalizeStatement, "value");
        }
    }

    @Value
    public static class OptionalSupport implements AbstractGenerator {
        Types typeUtils;
        Elements elementUtils;

        @Override
        public void generateSerializer(MethodSpec.Builder builder, DeclaredType type) {
            builder.beginControlFlow("if(value.isPresent())");
            builder.addStatement("acc.writeBoolean(true)");
            builder.addStatement(getGenericSerializeMethodName(type.getTypeArguments().get(0)) + "(value.get(),acc)");
            builder.nextControlFlow("else");
            builder.addStatement("acc.writeBoolean(false)");
            builder.endControlFlow();
        }

        @Override
        public void generateUnserializer(MethodSpec.Builder builder, DeclaredType type) {
            builder.addStatement("boolean isPresent = buf.readBoolean()");
            builder.beginControlFlow("if(isPresent)");
            builder.addStatement("return Optional.of(" + getGenericUnserializeMethodName(type.getTypeArguments().get(0)) + "(buf))");
            builder.nextControlFlow("else");
            builder.addStatement("return Optional.empty()");
            builder.endControlFlow();
        }
    }

    @Value
    public static class PairSupport implements AbstractGenerator {
        Types typeUtils;
        Elements elementUtils;

        @Override
        public void generateSerializer(MethodSpec.Builder builder, DeclaredType type) {
            builder.addStatement(getGenericSerializeMethodName(type.getTypeArguments().get(0)) + "(value.getLeft(),acc)");
            builder.addStatement(getGenericSerializeMethodName(type.getTypeArguments().get(1)) + "(value.getRight(),acc)");
        }

        @Override
        public void generateUnserializer(MethodSpec.Builder builder, DeclaredType type) {
            builder.addStatement("return " + Pair.class.getCanonicalName() + ".of(" +
                    getGenericUnserializeMethodName(type.getTypeArguments().get(0)) + "(buf), " +
                    getGenericUnserializeMethodName(type.getTypeArguments().get(1)) + "(buf))");
        }
    }
}
