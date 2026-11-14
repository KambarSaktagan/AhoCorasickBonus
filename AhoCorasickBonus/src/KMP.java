import java.util.List;

public final class KMP {
    private KMP() { }

    public static long searchAll(String text, List<String> patterns,
                                 AhoCorasick.MatchHandler handler) {
        if (text == null || patterns == null || handler == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }
        long total = 0;
        for (int id = 0; id < patterns.size(); id++) {
            String pattern = patterns.get(id);
            if (pattern == null || pattern.isEmpty()) {
                throw new IllegalArgumentException("Patterns must be non-null and non-empty");
            }
            int[] prefix = prefixFunction(pattern);
            int matched = 0;
            for (int i = 0; i < text.length(); i++) {
                char letter = text.charAt(i);
                while (matched > 0 && pattern.charAt(matched) != letter) {
                    matched = prefix[matched - 1];
                }
                if (pattern.charAt(matched) == letter) matched++;
                if (matched == pattern.length()) {
                    handler.onMatch(id, i - pattern.length() + 1);
                    total++;
                    matched = prefix[matched - 1];
                }
            }
        }
        return total;
    }

    public static long countMatches(String text, List<String> patterns) {
        return searchAll(text, patterns, (pattern, start) -> { });
    }

    private static int[] prefixFunction(String pattern) {
        int[] prefix = new int[pattern.length()];
        for (int i = 1; i < pattern.length(); i++) {
            int j = prefix[i - 1];
            while (j > 0 && pattern.charAt(i) != pattern.charAt(j)) j = prefix[j - 1];
            if (pattern.charAt(i) == pattern.charAt(j)) j++;
            prefix[i] = j;
        }
        return prefix;
    }
}
