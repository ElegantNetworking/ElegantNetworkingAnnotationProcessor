package hohserg.elegant.networking.annotation.processor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static java.util.stream.Collectors.toList;

public class ServiceUtils {

    private static String removeComment(String line) {
        int commentStart = line.indexOf('#');
        if (commentStart >= 0)
            return line.substring(0, commentStart);
        else
            return line;
    }

    private static boolean isClassName(String line) {
        return line.matches("[A-z][A-z.0-9]+");
    }

    public static List<String> loadClassNamesFromService(InputStream inputStream) {
        List<String> r = new ArrayList<>();
        try (Scanner s = new Scanner(inputStream)/*.useDelimiter("\\A")*/) {
            while (s.hasNextLine())
                r.add(s.nextLine());


            return r
                    .stream()
                    .map(ServiceUtils::removeComment)
                    .map(String::trim)
                    .filter(ServiceUtils::isClassName)
                    .collect(toList());
        }
    }

}
