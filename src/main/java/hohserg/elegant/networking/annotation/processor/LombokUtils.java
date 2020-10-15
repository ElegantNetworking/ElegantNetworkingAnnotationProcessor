package hohserg.elegant.networking.annotation.processor;

import com.google.common.collect.ImmutableSet;
import hohserg.elegant.networking.annotation.processor.dom.DataClassRepr;

import javax.lang.model.element.Modifier;

import java.util.stream.Collectors;

import static javax.lang.model.element.Modifier.*;

public class LombokUtils {
    public static DataClassRepr correctFieldModifiers(DataClassRepr classRepr) {
        if (classRepr.getAnnotations().stream().anyMatch(a -> a.getAnnotationType().toString().equals("lombok.Value")))
            return classRepr.withFields(classRepr.getFields().stream()
                    .map(e ->
                    {
                        if (e.getModifiers().contains(STATIC))
                            return e;
                        else {
                            ImmutableSet.Builder<Modifier> modifierBuilder = ImmutableSet.<Modifier>builder().addAll(e.getModifiers()).add(FINAL);
                            if (!e.getModifiers().contains(PUBLIC) && !e.getModifiers().contains(PROTECTED))
                                modifierBuilder.add(PRIVATE);
                            return e.withModifiers(modifierBuilder.build());
                        }
                    })
                    .collect(Collectors.toList())
            );
        else
            return classRepr;
    }
}
