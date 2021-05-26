package hohserg.elegant.networking.annotation.processor;

import com.google.common.collect.ImmutableSet;
import hohserg.elegant.networking.utils.PrintUtils;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;

public abstract class BaseProcessor extends AbstractProcessor {
    public static String printDetailsOption = "elegantnetworking.printDetails";
    public static String disablePrintElementNameOption = "elegantnetworking.disablePrintElementName";

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
            errorAPException(e);

        } catch (Throwable e) {
            String unexpectedMsg = "Unexpected error. Please, report to https://github.com/ElegantNetworking/ElegantNetworkingAnnotationProcessor/issues";
            error(unexpectedMsg, e);
            throw new IllegalStateException(unexpectedMsg + " \n", e);
        }
    }

    public void errorAPException(AnnotationProcessorException e) {
        error(e.element, prepareMsg(e));
    }

    private String prepareMsg(AnnotationProcessorException e) {
        if (options.containsKey(disablePrintElementNameOption))
            return e.msg;
        else
            return "[" + getFullElementName(e.element) + "] " + e.msg;
    }

    private String getFullElementName(Element element) {
        if (element instanceof TypeElement)
            return ((TypeElement) element).getQualifiedName().toString();
        else {
            Element enclosingElement = element.getEnclosingElement();
            return (enclosingElement != null ? getFullElementName(enclosingElement) + "." : "") + element.getSimpleName();
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

    public void error(String msg, Throwable e) {
        PrintWriter writer = PrintUtils.getWriterForStringConsumer(this::error);
        writer.println(msg);
        writer.println("Caused by");
        e.printStackTrace(writer);
        writer.flush();
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
        return ImmutableSet.of(printDetailsOption, disablePrintElementNameOption);
    }
}
