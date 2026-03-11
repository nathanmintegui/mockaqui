package com.sicredi.poc.mockaqui.shared.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class RadixTree implements IRadixTree {

    private Node root = new Node();

    private RadixTree() {
    }

    public static RadixTree getInstance() {
        return RadixTreeInner.INSTANCE;
    }

    public static RadixTree create() {
        return new RadixTree();
    }

    @Override
    public int insert(String key, Endpoint value) {
        try {
            Node current = root;

            String[] parts = key.split("/");
            String[] modifiedArray = parts;
            if (Objects.equals(parts[0], "")) {
                modifiedArray = Arrays.copyOfRange(parts, 1, parts.length);
            }

            for (String part : modifiedArray) {
                if (part.charAt(0) == ':') {
                    var node = Node.builder()
                            .parameter(new Parameter(":id", "Number"))
                            .build();
                    current = current.children.computeIfAbsent(part, c -> node);
                } else {
                    current = current.children.computeIfAbsent(part, c -> new Node());
                }
            }
            current.isEndOfWord = true;
            current.value = value;

            return 0;
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public Endpoint search(String key) {
        Node current = root;
        String[] parts = key.split("/");
        for (String part : parts) {
            if (current == null) {
                return null;
            }
            Node nextNode = current.getChildren().get(part);
            if (nextNode == null) {
                for (Node childNode : current.getChildren().values()) {
                    if (childNode.parameter != null) {
                        nextNode = childNode;
                        break;
                    }
                }
            }
            if (nextNode == null) {
                return null;
            }
            current = nextNode;
        }
        if (current != null && current.isEndOfWord()) {
            return current.value;
        }
        return null;
    }

    @Override
    public int delete(String key) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    @Override
    public void clear() {
        this.root = null;
    }

    private static class RadixTreeInner {
        private static final RadixTree INSTANCE = new RadixTree();
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    static class Node {
        private final Map<String, Node> children = new HashMap<>();
        private boolean isEndOfWord;
        private Endpoint value;
        private Parameter parameter;
    }

    @Getter
    @AllArgsConstructor
    static class Parameter {
        private String value;
        private Object type;
    }
}
