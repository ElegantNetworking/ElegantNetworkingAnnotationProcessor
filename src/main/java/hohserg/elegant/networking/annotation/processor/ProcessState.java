package hohserg.elegant.networking.annotation.processor;

import lombok.Value;

import javax.lang.model.element.TypeElement;
import java.util.Set;

@Value
public class ProcessState {
    String annotation;
    String interfaceName;
    Set<TypeElement> existingTypes;
}
