package hohserg.elegant.networking.annotation.processor;

import hohserg.elegant.networking.annotation.processor.dom.FieldRepr;
import lombok.Value;

public interface SetAccessor {

    @Value
    class Setter implements SetAccessor {
        FieldRepr field;
    }

    @Value
    class ItselfAccess implements SetAccessor {
        FieldRepr field;
    }
}
