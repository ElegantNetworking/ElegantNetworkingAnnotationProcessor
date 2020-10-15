package hohserg.elegant.networking.annotation.processor.dom;

import com.google.common.collect.ImmutableSet;
import lombok.Value;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import java.util.Set;

@Value
public class ArrayClassRepr implements ClassRepr {
    ClassRepr elementType;
    TypeMirror original;

    @Override
    public String getName() {
        return elementType.getName() + "[]";
    }

    @Override
    public String getSimpleName() {
        return "Array" + elementType.getName();
    }

    @Override
    public Set<ClassRepr> getEnclosingTypes() {
        return ImmutableSet.of(elementType);
    }

    @Override
    public Set<Modifier> getModifiers() {
        return ImmutableSet.of();
    }
}
