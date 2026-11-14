import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

public final class Benchmark {
    private static volatile long checksum;
    private static final int RUNS = 5;

    private Benchmark() { }

    private record CaseData(String text, List<String> patterns) { }

    private static CaseData makeData(int patternCount, int textLength) {
        Random random = new Random(2026L + patternCount * 31L + textLength);
        StringBuilder text = new StringBuilder(textLength);
        for (int i = 0; i < textLength; i++) text.append((char) ('a' + random.nextInt(6)));
        String input = text.toString();
        List<String> patterns = new ArrayList<>();
        Set<String> unique = new HashSet<>();
        while (patterns.size() < patternCount) {
            int length = 5 + random.nextInt(8);
            String pattern;
            if (patterns.size() % 5 == 0 && length <= textLength) {
                int start = random.nextInt(textLength - length + 1);
                pattern = input.substring(start, start + length);
            } else {
                StringBuilder p = new StringBuilder(length);
                for (int i = 0; i < length; i++) p.append((char) ('a' + random.nextInt(6)));
                pattern = p.toString();
            }
            if (unique.add(pattern)) patterns.add(pattern);
        }
        return new CaseData(input, patterns);
    }

    private static long measureAC(CaseData data) {
        long start = System.nanoTime();
        AhoCorasick ac = new AhoCorasick(data.patterns());
        long found = ac.countMatches(data.text());
        long elapsed = System.nanoTime() - start;
        checksum = found;
        return elapsed;
    }

    private static long measureKMP(CaseData data) {
        long start = System.nanoTime();
        long found = KMP.countMatches(data.text(), data.patterns());
        long elapsed = System.nanoTime() - start;
        if (found != checksum) throw new AssertionError("Different match counts!");
        return elapsed;
    }

    public static void main(String[] args) throws IOException {
        StringBuilder csv = new StringBuilder("patterns,text_length,aho_avg_ms,kmp_avg_ms,matches,runs\n");
        System.out.println("All times include preprocessing/building AND searching.");
        System.out.println("Patterns | Text chars | Aho avg (ms) | KMP avg (ms) | Matches");
        for (int count : new int[]{10, 100, 1000}) {
            for (int length : new int[]{1000, 10000, 100000}) {
                CaseData data = makeData(count, length);
                measureAC(data);
                measureKMP(data);
                long acNanos = 0, kmpNanos = 0;
                for (int run = 0; run < RUNS; run++) {
                    acNanos += measureAC(data);
                    kmpNanos += measureKMP(data);
                }
                double acMs = acNanos / (double) RUNS / 1_000_000.0;
                double kmpMs = kmpNanos / (double) RUNS / 1_000_000.0;
                System.out.printf(Locale.US, "%8d | %10d | %12.3f | %12.3f | %d%n",
                        count, length, acMs, kmpMs, checksum);
                csv.append(String.format(Locale.US, "%d,%d,%.6f,%.6f,%d,%d%n",
                        count, length, acMs, kmpMs, checksum, RUNS));
            }
        }
        Path result = Path.of("results", "benchmark.csv");
        Files.createDirectories(result.getParent());
        Files.writeString(result, csv.toString());
        System.out.println("Saved: " + result);
    }
}
