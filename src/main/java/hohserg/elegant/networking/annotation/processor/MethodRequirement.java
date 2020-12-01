package hohserg.elegant.networking.annotation.processor;

import hohserg.elegant.networking.annotation.processor.dom.DataClassRepr;
import hohserg.elegant.networking.annotation.processor.dom.EnumClassRepr;
import hohserg.elegant.networking.annotation.processor.dom.containers.ArrayClassRepr;
import hohserg.elegant.networking.annotation.processor.dom.containers.CollectionClassRepr;
import hohserg.elegant.networking.annotation.processor.dom.containers.MapClassRepr;
import lombok.Value;

import java.util.List;

public interface MethodRequirement {

    @Value
    class GenericMethod implements MethodRequirement {
        DataClassRepr forType;
        List<DataClassRepr> sealedImplementations;
    }

    @Value
    class EnumMethod implements MethodRequirement {
        EnumClassRepr forType;
    }

    @Value
    class ConcreticMethod implements MethodRequirement {
        DataClassRepr forType;
    }

    @Value
    class MapMethod implements MethodRequirement {
        MapClassRepr forType;
    }

    @Value
    class CollectionMethod implements MethodRequirement {
        CollectionClassRepr forType;
    }

    @Value
    class ArrayMethod implements MethodRequirement {
        ArrayClassRepr forType;
    }
}
