package hohserg.elegant.networking.annotation.processor;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.function.Consumer;

public class PrintUtils {
    public static PrintWriter getWriterForStringConsumer(Consumer<String> print) {
        return new PrintWriter(new Writer() {
            private String content = "";

            @Override
            public void write(char[] cbuf, int off, int len) {
                content += new String(cbuf, off, len);
            }

            @Override
            public void flush() {
                if (!content.isEmpty()) {
                    print.accept(content.replace('\r', ' '));
                    content = "";
                }
            }

            @Override
            public void close() {
                flush();
            }
        });
    }
}
