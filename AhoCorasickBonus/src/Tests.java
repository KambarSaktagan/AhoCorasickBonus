import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class Tests {
    private Tests() { }

    private static List<String> findAC(String text, List<String> patterns) {
        List<String> result = new ArrayList<>();
        new AhoCorasick(patterns).search(text, (id, pos) -> result.add(id + "@" + pos));
        Collections.sort(result);
        return result;
    }

    private static List<String> findKMP(String text, List<String> patterns) {
        List<String> result = new ArrayList<>();
        KMP.searchAll(text, patterns, (id, pos) -> result.add(id + "@" + pos));
        Collections.sort(result);
        return result;
    }

    private static void check(String text, List<String> patterns, String... expected) {
        List<String> actual = findAC(text, patterns);
        List<String> wanted = new ArrayList<>(Arrays.asList(expected));
        Collections.sort(wanted);
        if (!actual.equals(wanted)) {
            throw new AssertionError("Expected " + wanted + " but got " + actual);
        }
        if (!actual.equals(findKMP(text, patterns))) {
            throw new AssertionError("Aho-Corasick and KMP disagree");
        }
    }

    public static void main(String[] args) {
        check("ahishers", List.of("he", "she", "his", "hers"),
                "2@1", "1@3", "0@4", "3@4");
        check("aaaa", List.of("a", "aa", "aaa"),
                "0@0", "0@1", "0@2", "0@3", "1@0", "1@1", "1@2", "2@0", "2@1");
        check("abc", List.of("xyz"));
        check("", List.of("abc"));
        check("ababa", List.of("aba", "aba"), "0@0", "0@2", "1@0", "1@2");
        check("ABCabc", List.of("ABC", "abc"), "0@0", "1@3");
        check("abc", List.of());

        Random random = new Random(2026);
        for (int trial = 0; trial < 100; trial++) {
            StringBuilder text = new StringBuilder();
            for (int i = 0; i < 80; i++) text.append((char) ('a' + random.nextInt(4)));
            List<String> patterns = new ArrayList<>();
            for (int p = 0; p < 15; p++) {
                StringBuilder s = new StringBuilder();
                int length = 1 + random.nextInt(6);
                for (int j = 0; j < length; j++) s.append((char) ('a' + random.nextInt(4)));
                patterns.add(s.toString());
            }
            if (!findAC(text.toString(), patterns).equals(findKMP(text.toString(), patterns))) {
                throw new AssertionError("Random test failed: " + trial);
            }
        }
        System.out.println("All 107 correctness tests passed.");
    }
}
