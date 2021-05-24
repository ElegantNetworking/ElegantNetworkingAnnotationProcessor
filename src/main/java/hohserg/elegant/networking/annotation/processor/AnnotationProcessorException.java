package hohserg.elegant.networking.annotation.processor;

import javax.lang.model.element.Element;

public class AnnotationProcessorException extends RuntimeException {
    public final Element element;
    public final String msg;

    public AnnotationProcessorException(Element element, String msg) {
        super(msg);
        this.element = element;
        this.msg = msg;
    }
}
