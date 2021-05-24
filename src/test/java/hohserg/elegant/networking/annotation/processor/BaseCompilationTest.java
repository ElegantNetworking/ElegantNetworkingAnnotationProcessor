package hohserg.elegant.networking.annotation.processor;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.apache.commons.io.IOUtils;

import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.testing.compile.CompilationSubject.assertThat;

abstract class BaseCompilationTest {

    void doTest(String name) throws IOException {

        String inPath = "tests/"+name + "/in";
        String outPath = "tests/"+name + "/out";
        String commonPath = "common";

        List<JavaFileObject> inFiles = getAllFiles(Stream.of(inPath)).map(JavaFileObjects::forResource).collect(Collectors.toList());
        List<JavaFileObject> commonPathFiles = getAllFiles(Stream.of(commonPath)).map(JavaFileObjects::forResource).collect(Collectors.toList());
        List<String> outFiles = getAllFiles(Stream.of(outPath)).collect(Collectors.toList());

        Compilation compilation = Compiler.javac().withProcessors(new ElegantSerializerProcessor(), new ElegantServiceProcessor())
                .withClasspath(ImmutableList.of(
                        new File("C:\\Users\\hohserg\\.gradle\\caches\\modules-2\\files-2.1\\io.netty\\netty-all\\4.1.25.Final\\d0626cd3108294d1d58c05859add27b4ef21f83b\\netty-all-4.1.25.Final.jar"),
                        new File("C:\\Users\\hohserg\\.gradle\\caches\\modules-2\\files-2.1\\com.google.guava\\guava\\30.0-jre\\8ddbc8769f73309fe09b54c5951163f10b0d89fa\\guava-30.0-jre.jar"),
                        new File("C:\\Users\\hohserg\\Documents\\GitHub\\ElegantNetworking1\\ElegantNetworkingCommon\\build\\libs\\ElegantNetworkingCommon-1.0.jar")
                ))
                .compile(
                        ImmutableList.<JavaFileObject>builder()
                                .addAll(inFiles)
                                .addAll(commonPathFiles)
                                .build()
                );
        assertThat(compilation).succeededWithoutWarnings();

        for (String outFile : outFiles) {
            String compilationLocation = outFile.substring(outPath.length() + 1);
            if (outFile.endsWith(".java"))
                assertThat(compilation)
                        .generatedSourceFile(compilationLocation.substring(0, compilationLocation.length() - 5).replace("/", "."))
                        .hasSourceEquivalentTo(getExpectedClass(outFile));
            else
                assertThat(compilation)
                        .generatedFile(StandardLocation.CLASS_OUTPUT, compilationLocation)
                        .hasContents(getExpectedService(outFile));
        }
    }

    Stream<String> getAllFiles(Stream<String> basePathes) {
        return basePathes.flatMap(e -> {
            if (e.contains("."))
                return Stream.of(e);
            else {
                try {
                    return getAllFiles(IOUtils.readLines(getClass().getClassLoader().getResourceAsStream(e), StandardCharsets.UTF_8).stream()
                            .map(e1 -> e + "/" + e1));
                } catch (Exception e1) {
                    e1.printStackTrace();
                    return Stream.empty();
                }
            }
        });
    }

    protected JavaFileObject getExpectedClass(String resourceName) {
        return JavaFileObjects.forResource(resourceName);
    }

    protected ByteSource getExpectedService(String resourceName) throws IOException {
        return ByteSource.wrap(
                IOUtils.toString(
                        Resources.getResource(resourceName).openStream(),
                        StandardCharsets.UTF_8
                ).replace("" + Character.valueOf((char) 13), "").getBytes()
        );
    }
}
