package hohserg.elegant.networking.annotation.processor;

import com.google.common.collect.ImmutableList;
import hohserg.elegant.networking.utils.ServiceUtils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.*;

import static hohserg.elegant.networking.Refs.*;
import static java.util.stream.Collectors.toSet;

@SupportedAnnotationTypes({PacketProviderMark_name, SerializerMark_name})
public class ElegantServiceProcessor extends BaseProcessor {

    public InheritanceUtils inheritanceUtils;

    private List<ProcessState> states;

    private boolean isFirstRun = false;
    boolean timeOffsetAlreadyWritten = false;

    private long startTime;

    private Thread hack;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        handleUnexpectedErrors(this::loadExistingServices);
        inheritanceUtils = new InheritanceUtils(typeUtils);
        handleUnexpectedErrors(this::deferredInit);
    }

    private void deferredInit() {
        Optional<Long> timeOffsetBetweenInitAndProcess = loadTimeOffsetBetweenInitAndProcess();
        isFirstRun = !timeOffsetBetweenInitAndProcess.isPresent();

        if (isFirstRun)
            startTime = System.currentTimeMillis();
        else {
            hack = new Thread(() -> {
                try {
                    Thread.sleep(timeOffsetBetweenInitAndProcess.get() * 2);
                    synchronized (ElegantServiceProcessor.this) {
                        states.forEach(state -> saveService(state.getInterfaceName(), state.getExistingTypes()));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            hack.start();
        }
    }


    public final String timeOffsetFileLocation = tmpFolder + "timeOffsetBetweenInitAndProcess.txt";

    private Optional<Long> loadTimeOffsetBetweenInitAndProcess() {
        try {
            FileObject resourceForRead = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", timeOffsetFileLocation);

            try (
                    InputStream inputStream = resourceForRead.openInputStream();
                    Scanner s = new Scanner(inputStream).useDelimiter("\\A")) {
                return s.hasNext() ? Optional.of(Long.parseLong(s.next())) : Optional.empty();
            }
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private void writeTimeOffsetBetweenInitAndProcess(long timeOffsetBetweenInitAndProcess) {
        noteDetailed("Writing time offset between initPackets and process file " + timeOffsetBetweenInitAndProcess);
        try {
            FileObject resourceForWrite = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", timeOffsetFileLocation);

            try (Writer writer = resourceForWrite.openWriter()) {
                writer.write("" + timeOffsetBetweenInitAndProcess);
                writer.flush();
            }
        } catch (IOException e) {
            error("Cannot write timeOffset file", e);
        }
    }

    private void loadExistingServices() {
        states = ImmutableList.of(
                initProcessState(PacketProviderMark_name, IPacketProvider_name),
                initProcessState(SerializerMark_name, ISerializerBase_name)
        );
    }

    private ProcessState initProcessState(String annotation, String interfaceName) {
        return new ProcessState(annotation, interfaceName, loadTypesFromService(getServicePath(interfaceName), annotation));
    }

    private Set<TypeElement> loadTypesFromService(String path, String annotatedWith) {
        try {
            FileObject resourceForRead = filer.getResource(StandardLocation.CLASS_OUTPUT, "", path);

            try (InputStream inputStream = resourceForRead.openInputStream()) {

                List<String> fromService = ServiceUtils.loadClassNamesFromService(inputStream);
                return fromService.stream()
                        .map(elementUtils::getTypeElement)
                        .filter(Objects::nonNull)
                        .filter(e -> isMarkedBy(e, annotatedWith))
                        .collect(toSet());
            }
        } catch (FileNotFoundException e) {
            noteDetailed(path + " not found, its a first compilation");
            return new HashSet<>();
        } catch (IOException e) {
            error("Unable to load service file " + path, e);
            return new HashSet<>();
        }
    }

    private boolean isMarkedBy(TypeElement e, String annotatedWith) {
        return e.getAnnotationMirrors().stream().anyMatch(am -> am.getAnnotationType().asElement().toString().equals(annotatedWith));
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        warn("ElegantServiceProcessor#process/annotations " + annotations);
        handleUnexpectedErrors(() -> {
            if (isFirstRun && !timeOffsetAlreadyWritten) {
                timeOffsetAlreadyWritten = true;
                writeTimeOffsetBetweenInitAndProcess(System.currentTimeMillis() - startTime);
            }
            if (hack != null)
                hack.interrupt();

            for (ProcessState state : states) {
                TypeElement annotationElement = elementUtils.getTypeElement(state.getAnnotation());
                if (annotations.contains(annotationElement))
                    processService(roundEnv, annotationElement, state.getInterfaceName(), state.getExistingTypes());
            }
            if (roundEnv.processingOver())
                states.forEach(state -> saveService(state.getInterfaceName(), state.getExistingTypes()));
        });

        return false;
    }

    private void saveService(String interfaceName, Set<TypeElement> content) {
        writeServiceFile(content, getServicePath(interfaceName));
    }

    private void processService(RoundEnvironment roundEnv, TypeElement annotation, String interfaceName, Set<TypeElement> existingTypes) {
        Set<TypeElement> fromRound =
                roundEnv.getElementsAnnotatedWith(annotation)
                        .stream()
                        .filter(e -> e instanceof TypeElement)
                        .map(e -> (TypeElement) e)
                        .filter(e -> isImplements(e, interfaceName))
                        .collect(toSet());

        noteDetailed("Found follow elements in current round: " + fromRound);

        existingTypes.addAll(fromRound);
        noteDetailed("full marked class set: " + existingTypes);

    }

    private boolean isImplements(TypeElement e, String interfaceName) {
        TypeElement typeElement = elementUtils.getTypeElement(interfaceName);
        return inheritanceUtils.getAllInterfaces(e.asType()).anyMatch(t -> typeUtils.asElement(t) == typeElement);
    }

    private void writeServiceFile(Set<TypeElement> content, String path) {
        noteDetailed("Writing service file");
        try {
            FileObject resourceForWrite = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", path, content.toArray(new TypeElement[0]));

            try (Writer writer = resourceForWrite.openWriter()) {
                for (TypeElement typeElement : content)
                    writer.write(typeElement.getQualifiedName() + "\n");
                writer.flush();
            }
        } catch (IOException e) {
            error("Unable to write service file " + path + "\nTip: close file in all programs or remove file (will be re-writed)", e);
        }
    }
}
