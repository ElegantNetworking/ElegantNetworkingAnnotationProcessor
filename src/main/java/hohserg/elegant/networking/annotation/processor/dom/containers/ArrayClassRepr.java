package hohserg.elegant.networking.annotation.processor.dom.containers;

import com.google.common.collect.ImmutableSet;
import hohserg.elegant.networking.annotation.processor.MethodRequirement;
import hohserg.elegant.networking.annotation.processor.dom.ClassRepr;
import lombok.Value;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import java.util.Set;
import java.util.stream.Stream;

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
        return "Array" + elementType.getSimpleName();
    }

    @Override
    public Set<ClassRepr> getEnclosingTypes() {
        return ImmutableSet.of(elementType);
    }

    @Override
    public Set<Modifier> getModifiers() {
        return ImmutableSet.of();
    }

    @Override
    public Stream<MethodRequirement> getRequirementMethods() {
        return Stream.of(new MethodRequirement.ArrayMethod(this));
    }
}
