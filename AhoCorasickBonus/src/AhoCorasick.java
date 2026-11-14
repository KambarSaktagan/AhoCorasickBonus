import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public final class AhoCorasick {
    @FunctionalInterface
    public interface MatchHandler {
        void onMatch(int patternIndex, int startIndex);
    }

    private static final class Node {
        final Map<Character, Node> children = new HashMap<>();
        final List<Integer> endings = new ArrayList<>();
        Node failure;
        Node outputLink;
    }

    private final Node root = new Node();
    private final List<String> patterns;

    public AhoCorasick(List<String> patterns) {
        if (patterns == null) throw new IllegalArgumentException("Patterns cannot be null");
        this.patterns = List.copyOf(patterns);
        for (int i = 0; i < patterns.size(); i++) {
            String pattern = patterns.get(i);
            if (pattern == null || pattern.isEmpty()) {
                throw new IllegalArgumentException("Patterns must be non-null and non-empty");
            }
            Node node = root;
            for (int j = 0; j < pattern.length(); j++) {
                node = node.children.computeIfAbsent(pattern.charAt(j), c -> new Node());
            }
            node.endings.add(i);
        }
        buildFailureLinks();
    }

    private void buildFailureLinks() {
        root.failure = root;
        Queue<Node> queue = new ArrayDeque<>();
        for (Node child : root.children.values()) {
            child.failure = root;
            queue.add(child);
        }
        while (!queue.isEmpty()) {
            Node current = queue.remove();
            for (Map.Entry<Character, Node> edge : current.children.entrySet()) {
                char letter = edge.getKey();
                Node child = edge.getValue();
                Node fallback = current.failure;
                while (fallback != root && !fallback.children.containsKey(letter)) {
                    fallback = fallback.failure;
                }
                child.failure = fallback.children.getOrDefault(letter, root);
                child.outputLink = !child.failure.endings.isEmpty()
                        ? child.failure : child.failure.outputLink;
                queue.add(child);
            }
        }
    }

    public long search(String text, MatchHandler handler) {
        if (text == null || handler == null) {
            throw new IllegalArgumentException("Text and handler cannot be null");
        }
        Node node = root;
        long count = 0;
        for (int i = 0; i < text.length(); i++) {
            char letter = text.charAt(i);
            while (node != root && !node.children.containsKey(letter)) {
                node = node.failure;
            }
            node = node.children.getOrDefault(letter, root);
            for (Node terminal = node; terminal != null; terminal = terminal.outputLink) {
                for (int id : terminal.endings) {
                    handler.onMatch(id, i - patterns.get(id).length() + 1);
                    count++;
                }
            }
        }
        return count;
    }

    public long countMatches(String text) {
        return search(text, (pattern, start) -> { });
    }
}
