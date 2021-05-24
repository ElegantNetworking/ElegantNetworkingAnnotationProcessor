package hohserg.elegant.networking.annotation.processor.code.generator;

import com.squareup.javapoet.ClassName;

import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public interface ICodeGenerator {
    Types getTypeUtils();

    Elements getElementUtils();

    ClassName byteBuf = ClassName.get("io.netty.buffer", "ByteBuf");
}
