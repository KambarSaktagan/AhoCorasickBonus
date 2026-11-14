# Bonus Task: Aho–Corasick Algorithm (Java)

## What this project does
This program searches for many words (patterns) in one text. Aho–Corasick checks the text once using a **trie** and **failure links**. It finds all matches, including overlapping matches. A separate **KMP** implementation checks each pattern one at a time for comparison.

No external libraries are needed. All source files are Java.

## Files
- `src/AhoCorasick.java` — builds a trie, computes failure/output links, searches for every occurrence.
- `src/KMP.java` — searches the same text once per pattern.
- `src/Tests.java` — known examples, edge cases and 100 deterministic randomized comparisons.
- `src/Benchmark.java` — nine experiments, five timed runs each and CSV output.
- `results/benchmark.csv` — measured timing results from a sample run.

## How to run
Install JDK 17 or later. From the project folder:

```bash
javac -d out src/*.java
java -cp out Tests
java -cp out Benchmark
```

On Windows (PowerShell), the same commands work when Java is installed and on `PATH`. The benchmark overwrites `results/benchmark.csv` with results from your computer.

## Aho–Corasick: how it works
1. **Trie**: Insert each pattern letter by letter. Patterns with the same prefix share trie nodes. At terminal nodes we store pattern IDs.
2. **Failure links**: Use breadth-first search (BFS) to connect each node to the longest proper suffix of its current prefix that is also a trie prefix. If the next text letter has no edge, follow failure links instead of starting over.
3. **Output links**: Some failure ancestors also finish patterns. An output link skips to the closest suffix node that ends a pattern. This lets the search report shorter matching patterns without copying match lists to every state.
4. **Search**: Read each text character, change the current trie node, and report all patterns ending there and at output-linked nodes. Each match includes its **zero-based starting index**.

For example, in `ahishers` with patterns `he`, `she`, `his`, `hers`, the results are `his@1`, `she@3`, `he@4`, and `hers@4` (where `@` means 'starts at index').

## Complexity
Let:
- `L` = total number of characters in all patterns;
- `n` = text length;
- `d` = maximum pattern length;
- `z` = number of matches found;
- `p` = number of patterns.

**Aho–Corasick:**
- Building the trie: expected `O(L)` time using hash-map transitions.
- Building failure links: often close to `O(L)` in practice. This straightforward Java version walks failure links during BFS, so a conservative worst-case bound is `O(L * d)`.
- Searching: expected `O(n + z)` with hash-map lookups, using failure and output links; each match must be reported.
- Memory: `O(L + p)` for nodes, edges, terminal pattern IDs, failure/output pointers, and stored patterns, plus memory used by any results the caller chooses to store. Here, the benchmark counts matches without keeping a large result list.

**KMP run separately for every pattern:**
- For each pattern, building its prefix table and searching takes `O(length of pattern + n)` time.
- For all patterns, total time is `O(L + p * n)`.
- KMP only needs `O(d)` extra working memory per pattern when executed sequentially, excluding the input text and pattern collection.

The key advantage of Aho–Corasick is that the text is processed once for all patterns. KMP searches the whole text separately for every pattern. For one pattern, KMP is easier to write and has low setup cost. Aho–Corasick uses more memory for the trie and needs preprocessing. If patterns change constantly or there are very few patterns, KMP may be practical.

## Experiments
The benchmark tests every combination of:
- Pattern counts: **10, 100, 1,000**.
- Text lengths: **1,000, 10,000, 100,000**.

For each of the 9 combinations:
1. Generate a fixed-seed random text (alphabet `a` to `f`) and unique patterns of length 5–12. About one fifth of the patterns are sampled from the text so there are some guaranteed matches.
2. Perform one untimed warm-up execution for each algorithm.
3. Repeat **five measured executions** per algorithm with `System.nanoTime()`.
4. Measure **preprocessing + full search** for both algorithms to make the total-work comparison consistent.
5. Divide accumulated nanoseconds by five and convert to milliseconds. Confirm that both algorithms return the same number of matches.

Results from a sample run (JDK 21, Linux container; values will vary by machine, JVM warm-up, and system load):

| Patterns | Text characters | Aho–Corasick avg (ms) | KMP avg (ms) | Matches |
|---:|---:|---:|---:|---:|
| 10 | 1,000 | 0.658 | 1.025 | 2 |
| 10 | 10,000 | 1.134 | 1.803 | 5 |
| 10 | 100,000 | 2.388 | 4.574 | 15 |
| 100 | 1,000 | 0.542 | 0.405 | 25 |
| 100 | 10,000 | 0.731 | 4.478 | 39 |
| 100 | 100,000 | 3.376 | 46.361 | 198 |
| 1,000 | 1,000 | 2.733 | 4.442 | 221 |
| 1,000 | 10,000 | 1.217 | 47.030 | 372 |
| 1,000 | 100,000 | 4.787 | 443.829 | 2,063 |

### Discussion
The results show a strong advantage for Aho–Corasick when there are many patterns and the text is long. For 1,000 patterns and 100,000 characters, the sample run measured around 4.8 ms for Aho–Corasick compared with 443.8 ms for KMP. For very small cases, the difference can be small, and KMP can occasionally win because it requires less setup. Tiny measurements can be noisy, so rerun the benchmark on your own machine to confirm trends.

