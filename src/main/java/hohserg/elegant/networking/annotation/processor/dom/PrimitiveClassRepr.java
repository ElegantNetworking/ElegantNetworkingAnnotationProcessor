package hohserg.elegant.networking.annotation.processor.dom;

import com.google.common.collect.ImmutableSet;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Set;

@Value
public class PrimitiveClassRepr implements ClassRepr {
    PrimitiveKind kind;
    TypeMirror original;

    @Override
    public String getName() {
        return kind.name();
    }

    @Override
    public String getSimpleName() {
        return StringUtils.capitalize(getName().toLowerCase());
    }

    @Override
    public Set<ClassRepr> getEnclosingTypes() {
        return ImmutableSet.of();
    }

    @Override
    public Set<Modifier> getModifiers() {
        return ImmutableSet.of();
    }

    public enum PrimitiveKind {
        BOOLEAN,
        BYTE,
        SHORT,
        INT,
        LONG,
        CHAR,
        FLOAT,
        DOUBLE
    }

    static Set<String> boxedPrimitives = ImmutableSet.of(
            Boolean.class.getName(),
            Byte.class.getName(),
            Short.class.getName(),
            Integer.class.getName(),
            Long.class.getName(),
            Character.class.getName(),
            Float.class.getName(),
            Double.class.getName()
    );
}
