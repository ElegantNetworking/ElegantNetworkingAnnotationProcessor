package hohserg.elegant.networking.annotation.processor;

import hohserg.elegant.networking.annotation.processor.dom.FieldRepr;
import lombok.Value;

public interface GetAccessor {

    @Value
    class Getter implements GetAccessor {
        FieldRepr field;
    }

    @Value
    class ItselfAccess implements GetAccessor {
        FieldRepr field;
    }
}
