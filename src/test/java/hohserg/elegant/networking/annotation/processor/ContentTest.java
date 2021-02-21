package hohserg.elegant.networking.annotation.processor;

import com.google.common.collect.ImmutableList;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.testing.compile.CompilationSubject.assertThat;

public class ContentTest extends BaseCompilationTest {
    @TestFactory
    Collection<DynamicContainer> dynamicTests() throws IOException {
        List<String> rootFiles = IOUtils.readLines(getClass().getClassLoader().getResourceAsStream("in/test"), StandardCharsets.UTF_8);

        return rootFiles.stream().map(f -> DynamicContainer.dynamicContainer(f, createTests("test/" + f))).collect(Collectors.toList());
    }

    private List<DynamicNode> createTests(String folder) {
        System.out.println("folder " + folder);
        String inPath = "in/" + folder;
        String commonPath = "common";

        List<JavaFileObject> commonFiles = getAllFiles(Stream.of(commonPath)).map(JavaFileObjects::forResource).collect(Collectors.toList());
        return getAllFiles(Stream.of(inPath))
                .map(resourceName -> {
                    String testName = resourceName.substring(inPath.length() + 1, resourceName.length() - 5);
                    return DynamicTest.dynamicTest(testName, () -> {
                        Compilation compilation = Compiler.javac().withProcessors(new ElegantSerializerProcessor(), new ElegantServiceProcessor())
                                .withClasspath(ImmutableList.of(
                                        //uh-oh: todo: wut?
                                        new File("C:\\Users\\hohserg\\.gradle\\caches\\modules-2\\files-2.1\\io.netty\\netty-all\\4.1.25.Final\\d0626cd3108294d1d58c05859add27b4ef21f83b\\netty-all-4.1.25.Final.jar"),
                                        new File("C:\\Users\\hohserg\\.gradle\\caches\\modules-2\\files-2.1\\com.google.guava\\guava\\30.0-jre\\8ddbc8769f73309fe09b54c5951163f10b0d89fa\\guava-30.0-jre.jar"),
                                        new File("C:\\Users\\hohserg\\Documents\\GitHub\\ElegantNetworking1\\ElegantNetworkingCommon\\build\\libs\\ElegantNetworkingCommon-1.0.jar")
                                ))
                                .compile(
                                        ImmutableList.<JavaFileObject>builder()
                                                .add(JavaFileObjects.forResource(resourceName))
                                                .addAll(commonFiles)
                                                .build()
                                );
                        if (testName.endsWith("_Failure"))
                            assertThat(compilation).hadErrorCount(1);
                        else {
                            assertThat(compilation).succeededWithoutWarnings();

                            String compilationLocation = resourceName.substring("in/".length()).replace(".java", "Serializer").replace("/", ".");
                            String outLocation = "out/" + resourceName.substring("in/".length()).replace(".java", "Serializer.java");

                            System.out.println("compilationLocation " + compilationLocation);
                            System.out.println("outLocation " + outLocation);

                            assertThat(compilation)
                                    .generatedSourceFile(compilationLocation)
                                    .hasSourceEquivalentTo(getExpectedClass(outLocation));
                        }
                    });
                })
                .collect(Collectors.toList());
    }
}
