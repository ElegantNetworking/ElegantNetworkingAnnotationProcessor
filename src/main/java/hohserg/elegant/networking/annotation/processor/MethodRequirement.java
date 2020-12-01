package hohserg.elegant.networking.annotation.processor;

import com.squareup.javapoet.MethodSpec;
import hohserg.elegant.networking.annotation.processor.dom.DataClassRepr;
import hohserg.elegant.networking.annotation.processor.dom.EnumClassRepr;
import hohserg.elegant.networking.annotation.processor.dom.containers.ArrayClassRepr;
import hohserg.elegant.networking.annotation.processor.dom.containers.CollectionClassRepr;
import hohserg.elegant.networking.annotation.processor.dom.containers.MapClassRepr;
import lombok.Value;

import java.util.List;
import java.util.stream.Stream;

import static hohserg.elegant.networking.annotation.processor.CodeGenerator.*;

public interface MethodRequirement {

    default Stream<MethodSpec> generateMethods() {
        throw new UnsupportedOperationException(toString());
    }

    @Value
    class GenericMethod implements MethodRequirement {
        DataClassRepr forType;
        List<DataClassRepr> sealedImplementations;

        @Override
        public Stream<MethodSpec> generateMethods() {
            return Stream.of(
                    generateGenericSerializer(this),
                    generateGenericUnserializer(this)
            );
        }
    }

    @Value
    class EnumMethod implements MethodRequirement {
        EnumClassRepr forType;

        @Override
        public Stream<MethodSpec> generateMethods() {
            return Stream.of(
                    generateEnumSerializer(this),
                    generateEnumUnserializer(this)
            );
        }
    }

    @Value
    class ConcreticMethod implements MethodRequirement {
        DataClassRepr forType;

        @Override
        public Stream<MethodSpec> generateMethods() {
            return Stream.of(
                    generateConcreticSerializer(forType),
                    generateConcreticUnserializer(forType)
            );
        }
    }

    @Value
    class MapMethod implements MethodRequirement {
        MapClassRepr forType;

        @Override
        public Stream<MethodSpec> generateMethods() {
            return Stream.of(
                    generateMapSerializer(this),
                    generateMapUnserializer(this)
            );
        }
    }

    @Value
    class CollectionMethod implements MethodRequirement {
        CollectionClassRepr forType;

        @Override
        public Stream<MethodSpec> generateMethods() {
            return Stream.of(
                    generateCollectionSerializer(this),
                    generateCollectionUnserializer(this)
            );
        }
    }

    @Value
    class ArrayMethod implements MethodRequirement {
        ArrayClassRepr forType;

        @Override
        public Stream<MethodSpec> generateMethods() {
            return Stream.of(
                    generateArraySerializer(this),
                    generateArrayUnserializer(this)
            );
        }
    }
}
