package hohserg.elegant.networking.annotation.processor;

import com.google.common.collect.ImmutableSet;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.Map;
import java.util.Set;

public abstract class BaseProcessor extends AbstractProcessor {
    public static String printDetailsOption = "elegantnetworking.printDetails";

    public Filer filer;
    public Elements elementUtils;
    public Messager messager;
    public Map<String, String> options;
    public Types typeUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        elementUtils = processingEnv.getElementUtils();
        messager = processingEnv.getMessager();
        options = processingEnv.getOptions();
        typeUtils = processingEnv.getTypeUtils();
    }

    protected void handleUnexpectedErrors(RunWithException f) {
        try {
            f.run();
        } catch (AnnotationProcessorException e) {
            error(e.element, e.msg);

        } catch (Throwable e) {
            /*
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "Unexpected error. Please, report to https://github.com/ElegantNetworking/ElegantNetworkingAnnotationProcessor/issues \n"
                            + e.toString() + "\n"
                            + Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n")));*/
            throw new IllegalStateException("Unexpected error. Please, report to https://github.com/ElegantNetworking/ElegantNetworkingAnnotationProcessor/issues \n", e);
        }
    }

    public void noteDetailed(Element e, String msg) {
        if (options.containsKey(printDetailsOption))
            note(e, msg);
    }

    public void noteDetailed(String msg) {
        if (options.containsKey(printDetailsOption))
            note(msg);
    }

    public void note(String msg) {
        messager.printMessage(Diagnostic.Kind.NOTE, msg);
    }

    public void note(Element e, String msg) {
        messager.printMessage(Diagnostic.Kind.NOTE, msg, e);
    }

    public void warn(String msg) {
        messager.printMessage(Diagnostic.Kind.WARNING, msg);
    }

    public void warn(Element e, String msg) {
        messager.printMessage(Diagnostic.Kind.WARNING, msg, e);
    }

    public void error(String msg) {
        messager.printMessage(Diagnostic.Kind.ERROR, msg);
    }

    public void error(Element e, String msg) {
        messager.printMessage(Diagnostic.Kind.ERROR, msg, e);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedOptions() {
        return ImmutableSet.of("elegantnetworking.printDetails");
    }
}
