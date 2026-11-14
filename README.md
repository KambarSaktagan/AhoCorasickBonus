# Aho-Corasick Algorithm 

## About this project

This is my Java implementation of the **Aho-Corasick algorithm**. It finds several words (patterns) in a text at the same time. I also implemented **KMP** so I could compare their running times.

The program finds all matches, even when they overlap. Everything is written in Java, without extra libraries.

## Project files

- `src/AhoCorasick.java` — Aho-Corasick algorithm
- `src/KMP.java` — KMP algorithm
- `src/Tests.java` — tests to check that the results are correct
- `src/Benchmark.java` — runs the speed experiments
- `results/benchmark.csv` — saved experiment results

## How to run

Use **Java 17 or newer**. Open a terminal in the project folder and run:

```bash
javac -d out src/*.java
java -cp out Tests
java -cp out Benchmark
```

The last command runs the experiments again and updates `results/benchmark.csv`.

## How Aho-Corasick works

The algorithm has four main parts:

1. **Trie:** First, all patterns are added to a tree. Patterns that start with the same letters can share part of the tree.
2. **Failure links:** These links tell us where to continue if the next character does not match. They are built using BFS (breadth-first search).
3. **Output links:** They help find extra matches when one pattern ends inside another pattern.
4. **Search:** The algorithm goes through the text and reports every pattern it finds, including its starting position.

For example, if the patterns are `he`, `she`, `his`, and `hers`, searching `ahishers` finds:

- `his` at index 1
- `she` at index 3
- `he` at index 4
- `hers` at index 4

Positions start at **0**.

## Time and space complexity

Let:

- `L` = total length of all patterns
- `n` = length of the text
- `p` = number of patterns
- `d` = length of the longest pattern
- `z` = number of matches

For **Aho-Corasick**:

- Building the trie takes about **O(L)** time.
- Building failure links is usually fast; this implementation has a conservative worst-case bound of **O(L × d)**.
- Searching takes **O(n + z)** expected time.
- The algorithm needs **O(L + p)** memory for the trie, links, and patterns (not counting stored search results).

For **KMP**, searching separately for every pattern takes **O(L + p × n)** total time. Its extra working memory is **O(d)** when patterns are processed one by one.

The main difference is simple: **Aho-Corasick processes the text once for all patterns**, while **KMP processes the text again for each pattern**.

## Experiments

I tested 3 numbers of patterns (**10, 100, 1,000**) and 3 text lengths (**1,000, 10,000, 100,000** characters). That gives **9 test cases**.

For each case, the program generates test data, does one warm-up run, and measures both algorithms **5 times** with `System.nanoTime()`. The table shows the average running time, including building the data structures and searching. Both algorithms are checked to make sure they find the same number of matches.

These are the results from one run on **Java 21 (Linux)**. Times can be different on another computer.

| Patterns | Text length | Aho-Corasick (ms) | KMP (ms) |
|---:|---:|---:|---:|
| 10 | 1,000 | 0.658 | 1.025 |
| 10 | 10,000 | 1.134 | 1.803 |
| 10 | 100,000 | 2.388 | 4.574 |
| 100 | 1,000 | 0.542 | 0.405 |
| 100 | 10,000 | 0.731 | 4.478 |
| 100 | 100,000 | 3.376 | 46.361 |
| 1,000 | 1,000 | 2.733 | 4.442 |
| 1,000 | 10,000 | 1.217 | 47.030 |
| 1,000 | 100,000 | 4.787 | 443.829 |

## What I noticed

When there are only a few patterns, both algorithms are quite fast. Sometimes KMP is a little faster because it does not need to build a trie.

But when the number of patterns increases, **Aho-Corasick is much faster**. For example, with 1,000 patterns and a text of 100,000 characters, Aho-Corasick took about **4.8 ms**, while KMP took about **444 ms**.

**Aho-Corasick** is a good choice when we need to search for many patterns at once. Its downside is that it needs more memory and takes some time to build the trie and links. **KMP** is simpler and works well for one pattern or just a few patterns, but it gets slower when it has to search the same text many times.

## Submission

The repository contains the Java code, tests, benchmark, saved results, and this README. The separate one-page handwritten explanation must be written independently, as required by the assignment.
