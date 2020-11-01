package hohserg.elegant.networking.annotation.processor.dom;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static hohserg.elegant.networking.annotation.processor.ElegantPacketProcessor.note;
import static hohserg.elegant.networking.annotation.processor.ElegantPacketProcessor.typeUtils;
import static hohserg.elegant.networking.annotation.processor.dom.PrimitiveClassRepr.boxedPrimitives;

public interface ClassRepr {
    String getName();

    String getSimpleName();

    Set<ClassRepr> getEnclosingTypes();

    TypeMirror getOriginal();

    Set<Modifier> getModifiers();


    class CacheHolder {
        private static Map<String, ClassRepr> cache = new HashMap<>();
    }

    static ClassRepr typeRepresentation(TypeMirror type) {
        //note("test " + type + " " + (type instanceof DeclaredType));
        return CacheHolder.cache.computeIfAbsent(type.toString(), __ -> {
            if (isPrimitive(type))
                return new PrimitiveClassRepr(PrimitiveClassRepr.PrimitiveKind.valueOf(unboxIfPossible(type).toString().toUpperCase()), type);


            else if (type.getKind() == TypeKind.ARRAY)
                return new ArrayClassRepr(typeRepresentation(((ArrayType) type).getComponentType()), type);

            else if (type.getKind() == TypeKind.VOID)
                return VoidClassRepr.instance;

            else {
                return CollectionClassRepr.prepare(type)
                        .orElseGet(() -> MapClassRepr.prepare(type)
                                .orElseGet(() -> DataClassRepr.prepare((TypeElement) typeUtils.asElement(type))));
            }
        });
    }

    static TypeMirror unboxIfPossible(TypeMirror type) {
        return type.getKind().isPrimitive() ? type : typeUtils.unboxedType(type);
    }

    static boolean isPrimitive(TypeMirror type) {
        return type.getKind().isPrimitive() || boxedPrimitives.contains(type.toString());
    }
}
