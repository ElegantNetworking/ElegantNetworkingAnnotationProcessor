package hohserg.elegant.networking.annotation.processor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import net.jqwik.api.Arbitraries;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.testing.compile.CompilationSubject.assertThat;

public class UsabilityTest extends BaseCompilationTest {
    @Test
    void doTest() throws IOException, ClassNotFoundException, NoSuchMethodException {
        List<String> inputPackets = getAllFiles(Stream.of("in")).filter(f -> !f.endsWith("_Failure.java")).collect(Collectors.toList());

        List<String> rootFiles = IOUtils.readLines(getClass().getClassLoader().getResourceAsStream("in/test"), StandardCharsets.UTF_8);

        List<String> commonFiles = getAllFiles(Stream.of("common")).collect(Collectors.toList());

        ImmutableList<String> inputFiles = ImmutableList.<String>builder()
                .add("in/test/fields/PacketWith_FinalAndNonFinalFields.java")
                .addAll(commonFiles)
                .build();

        Compilation compilation = Compiler.javac().withProcessors(new ElegantSerializerProcessor(), new ElegantServiceProcessor())
                .withClasspath(ImmutableList.of(
                        //uh-oh: todo: wut?
                        new File("C:\\Users\\hohserg\\.gradle\\caches\\modules-2\\files-2.1\\io.netty\\netty-all\\4.1.25.Final\\d0626cd3108294d1d58c05859add27b4ef21f83b\\netty-all-4.1.25.Final.jar"),
                        new File("C:\\Users\\hohserg\\.gradle\\caches\\modules-2\\files-2.1\\com.google.guava\\guava\\30.0-jre\\8ddbc8769f73309fe09b54c5951163f10b0d89fa\\guava-30.0-jre.jar"),
                        new File("C:\\Users\\hohserg\\Documents\\GitHub\\ElegantNetworking1\\ElegantNetworkingCommon\\build\\libs\\ElegantNetworkingCommon-1.0.jar")
                ))
                .compile(
                        inputFiles.stream().map(JavaFileObjects::forResource).collect(Collectors.toList())
                );
        assertThat(compilation).succeededWithoutWarnings();

        String serializerName = "test.fields.PacketWith_FinalAndNonFinalFieldsSerializer";
        String valueName = "test.fields.PacketWith_FinalAndNonFinalFields";
        ByteClassLoader byteClassLoader = new ByteClassLoader(new URL[0], getClass().getClassLoader(), getBytesForClasses(compilation,
                valueName,
                serializerName,
                "hohserg.elegant.networking.api.ClientToServerPacket",
                "hohserg.elegant.networking.api.ServerToClientPacket",
                "hohserg.elegant.networking.impl.ISerializer",
                "test.SomeEnum",
                "test.SomeKey",
                "test.SomeValue"
        ));
        Class<?> serializerClass = byteClassLoader.findClass(serializerName);
        Class<?> valueClass = byteClassLoader.findClass(valueName);

        Class<?> SomeEnum = byteClassLoader.findClass("test.SomeEnum");
        Class<?> SomeKey = byteClassLoader.findClass("test.SomeKey");
        Class<?> SomeValue = byteClassLoader.findClass("test.SomeValue");

        //System.out.println(Arbitraries.forType(SomeEnum).sample());
        System.out.println(Arbitraries.forType(SomeKey).sample());
        System.out.println(Arbitraries.forType(SomeValue).sample());

        System.out.println(Arbitraries.forType(C.class).sample());
    }

    public static class A {
    }

    public static class B {
    }

    public static class C {
        final A a1;
        final B b1;
        A a2;
        B b2;

        public C(A a1, B b1) {
            this.a1 = a1;
            this.b1 = b1;
        }
    }

    private Map<String, byte[]> getBytesForClasses(Compilation compilation, String... names) throws IOException {
        ImmutableMap.Builder<String, byte[]> r = ImmutableMap.builder();
        for (String name : names) {
            r.put(name, getBytesForClass(compilation, name));
        }
        return r.build();
    }

    private byte[] getBytesForClass(Compilation compilation, String serializerName) throws IOException {
        InputStream inputStream = compilation.generatedFile(StandardLocation.CLASS_OUTPUT, serializerName.replaceAll("\\.", "/") + ".class").get().openInputStream();
        return IOUtils.toByteArray(inputStream);
    }
}
