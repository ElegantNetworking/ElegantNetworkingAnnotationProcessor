package hohserg.elegant.networking.annotation.processor.dom;

import com.google.common.collect.ImmutableSet;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import java.util.Set;

import static hohserg.elegant.networking.annotation.processor.ElegantPacketProcessor.elementUtils;
import static hohserg.elegant.networking.annotation.processor.ElegantPacketProcessor.typeUtils;

public class VoidClassRepr implements ClassRepr {
    public static final VoidClassRepr instance = new VoidClassRepr();

    private VoidClassRepr() {
    }

    @Override
    public String getName() {
        return "void";
    }

    @Override
    public String getSimpleName() {
        return "void";
    }

    @Override
    public Set<ClassRepr> getEnclosingTypes() {
        return ImmutableSet.of();
    }

    @Override
    public TypeMirror getOriginal() {
        return elementUtils.getTypeElement("void").asType();
    }

    @Override
    public Set<Modifier> getModifiers() {
        return ImmutableSet.of();
    }
}
