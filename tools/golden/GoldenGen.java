import com.dexetra.securekeyslib.util.Securekeys;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs the legacy SecureKeysLib.aar on the JVM and writes golden vectors.
 * Usage: GoldenGen phrases.txt out.tsv. One phrase per line, with \t, \n and \\ escaped;
 * lines starting with # are comments. Output: phrase, length, mixedCase, special, numbers, expected,
 * for lengths 1-25 and all 8 flag combinations, phrase escaped the same way.
 */
public class GoldenGen {
    public static void main(String[] args) throws Exception {
        List<String> phrases = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]), StandardCharsets.UTF_8))) {
            for (String line; (line = r.readLine()) != null; ) {
                if (!line.isEmpty() && !line.startsWith("#")) phrases.add(unescape(line));
            }
        }
        Securekeys lib = new Securekeys();
        boolean[] tf = {false, true};
        try (PrintStream out = new PrintStream(new FileOutputStream(args[1]), false, "UTF-8")) {
            for (String p : phrases)
                for (int len = 1; len <= 25; len++)
                    for (boolean mixed : tf)
                        for (boolean special : tf)
                            for (boolean numbers : tf)
                                out.print(escape(p) + "\t" + len + "\t" + mixed + "\t" + special + "\t" + numbers + "\t"
                                        + lib.getPassPhrase(p, len, mixed, special, numbers) + "\n");
        }
    }

    static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n");
    }

    static String unescape(String s) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char n = s.charAt(++i);
                b.append(n == 't' ? '\t' : n == 'n' ? '\n' : n == '\\' ? '\\' : fail(s));
            } else {
                b.append(c);
            }
        }
        return b.toString();
    }

    private static char fail(String s) {
        throw new IllegalArgumentException("bad escape in " + s);
    }
}
