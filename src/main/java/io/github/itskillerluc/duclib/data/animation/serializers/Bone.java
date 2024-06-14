package io.github.itskillerluc.duclib.data.animation.serializers;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public record Bone(Map<String, KeyFrame> rotation, Map<String, KeyFrame> position, Map<String, KeyFrame> scale) {
    public static class adapter implements JsonDeserializer<Bone> {

        @Override
        public Bone deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Map<String, KeyFrame> rotations = new HashMap<>();
            Map<String, KeyFrame> positions = new HashMap<>();
            Map<String, KeyFrame> scales = new HashMap<>();

            if (json.getAsJsonObject().get("rotation") != null) {
                if (json.getAsJsonObject().get("rotation").isJsonObject()) {
                    for (Map.Entry<String, JsonElement> rotation : json.getAsJsonObject().get("rotation").getAsJsonObject().asMap().entrySet()) {
                        rotations.put(rotation.getKey().equals("vector") ? "0.0" : rotation.getKey(), KeyFrame.deserialize(rotation.getValue()));
                    }
                } else {
                    rotations.put("0.0", KeyFrame.deserialize(json.getAsJsonObject().get("rotation")));
                }
            }
            if (json.getAsJsonObject().get("position") != null) {
                if (json.getAsJsonObject().get("position").isJsonObject()) {
                    for (Map.Entry<String, JsonElement> position : json.getAsJsonObject().get("position").getAsJsonObject().asMap().entrySet()) {
                        positions.put(position.getKey().equals("vector") ? "0.0" : position.getKey(), KeyFrame.deserialize(position.getValue()));
                    }
                } else {
                    rotations.put("0.0", KeyFrame.deserialize(json.getAsJsonObject().get("position")));
                }
            }
            if (json.getAsJsonObject().get("scale") != null) {
                if (json.getAsJsonObject().get("scale").isJsonObject()) {
                    for (Map.Entry<String, JsonElement> scale : json.getAsJsonObject().get("scale").getAsJsonObject().asMap().entrySet()) {
                        scales.put(scale.getKey().equals("vector") ? "0.0" : scale.getKey(), KeyFrame.deserialize(scale.getValue()));
                    }
                } else {
                    rotations.put("0.0", KeyFrame.deserialize(json.getAsJsonObject().get("scale")));
                }
            }
            return new Bone(rotations, positions, scales);
        }
    }
}
