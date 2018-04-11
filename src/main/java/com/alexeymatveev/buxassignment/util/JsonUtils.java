package com.alexeymatveev.buxassignment.util;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

/**
 * Created by Alexey Matveev on 4/10/2018.
 */
public class JsonUtils {

    public static Optional<JsonNode> tryGetString(JsonNode node, String key) {
        if (node != null) {
            JsonNode valueNode = node.get(key);
            if (valueNode != null && valueNode.isTextual() && !valueNode.isNull()) {
                return Optional.of(valueNode);
            }
        }
        return Optional.empty();
    }
}
