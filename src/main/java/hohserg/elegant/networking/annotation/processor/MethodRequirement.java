package hohserg.elegant.networking.annotation.processor;

import hohserg.elegant.networking.annotation.processor.dom.ClassRepr;
import hohserg.elegant.networking.annotation.processor.dom.CollectionClassRepr;
import hohserg.elegant.networking.annotation.processor.dom.DataClassRepr;
import hohserg.elegant.networking.annotation.processor.dom.MapClassRepr;
import lombok.Value;

import java.util.List;
import java.util.Set;

public interface MethodRequirement {

    @Value
    class GenericMethod implements MethodRequirement {
        DataClassRepr forType;
        List<DataClassRepr> sealedImplementations;
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
}
