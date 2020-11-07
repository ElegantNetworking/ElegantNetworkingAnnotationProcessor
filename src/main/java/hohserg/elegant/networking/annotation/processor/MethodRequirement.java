package hohserg.elegant.networking.annotation.processor;

import hohserg.elegant.networking.annotation.processor.dom.*;
import hohserg.elegant.networking.annotation.processor.dom.containers.*;
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
