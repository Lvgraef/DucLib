package io.github.itskillerluc.duclib.data.animation.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public record KeyFrame(List<Either<Double, Double2DoubleFunction>> pre, List<Either<Double, Double2DoubleFunction>> post, String lerpMode) {
    private static final Pattern splitter = Pattern.compile("(?<=[0-9.])(?![0-9a-z.])");
    private static final Pattern stripper = Pattern.compile("\"-\\\\+\"");
    private static final Pattern finder = Pattern.compile("(?<=anim_time\\*)[0-9]+");
    public static KeyFrame deserialize(JsonElement json) throws JsonParseException {
        if (!json.isJsonObject()) {
            List<Either<Double, Double2DoubleFunction>> array = new ArrayList<>();
            if (json.isJsonArray()) {
                var jsonArray = json.getAsJsonArray();
                for (JsonElement jsonElement : jsonArray.asList()) {
                    var string = jsonElement.getAsString();
                    array.add(createFrame(string));
                }
            } else {
                array.add(createFrame(json.getAsString()));
                array.add(createFrame(json.getAsString()));
                array.add(createFrame(json.getAsString()));
            }
            return new KeyFrame(null, array, "linear");
        }
        JsonElement preJson = json.getAsJsonObject().get("pre");
        List<Either<Double, Double2DoubleFunction>> pre = new ArrayList<>();
        if (preJson == null) {
            pre = null;
        } else {
            pre = getEithers(preJson, pre);
        }
        JsonElement postJson = json.getAsJsonObject().get("post");
        List<Either<Double, Double2DoubleFunction>> post = new ArrayList<>();
        if (postJson == null) {
            if (json.getAsJsonObject().has("vector")) {
                for (JsonElement vector : json.getAsJsonObject().get("vector").getAsJsonArray()) {
                    var string = vector.getAsString();
                    post.add(createFrame(string));
                }
            } else {
                post = null;
            }
        } else {
            post = getEithers(postJson, post);
        }
        JsonElement lerpModeJson = json.getAsJsonObject().get("lerp_mode");
        String lerpMode = lerpModeJson != null ? lerpModeJson.getAsString() : "linear";
        return new KeyFrame(pre, post, lerpMode);
    }

    @Nullable
    private static List<Either<Double, Double2DoubleFunction>> getEithers(JsonElement postJson, List<Either<Double, Double2DoubleFunction>> post) {
        if (postJson.isJsonArray()) {
            for (JsonElement jsonElement : postJson.getAsJsonArray()) {
                var string = jsonElement.getAsString();
                post.add(createFrame(string));
            }
        } else if (postJson.isJsonObject()) {
            for (JsonElement vector : postJson.getAsJsonObject().get("vector").getAsJsonArray()) {
                var string = vector.getAsString();
                post.add(createFrame(string));
            }
        } else {
            post = null;
        }
        return post;
    }

    private static Either<Double, Double2DoubleFunction> createFrame(String string) {
        if (NumberUtils.isCreatable(string)) {
            return Either.left(NumberUtils.createDouble(string));
        } else {
            Double2DoubleFunction function;
            if (string.contains("math.cos(")) {
                var split = splitter.split(string.replace("math.cos(", ""));
                function = getDouble2DoubleFunction(split, Operation.COS);
            } else if (string.contains("math.sin(")) {
                var split = splitter.split(string.replace("math.sin(", ""));
                function = getDouble2DoubleFunction(split, Operation.SIN);
            } else {
                throw new IllegalArgumentException("\"" + string + "\" Is not a valid math expression (you can only use sin or cos)");
            }
            return Either.right(function);
        }
    }

    @NotNull
    private static Double2DoubleFunction getDouble2DoubleFunction(String[] split, Operation operation) {
        Double2DoubleFunction function;
        float add;
        float timeMultiply;
        float multiplier;
        int stringStart = 0;
        int stringEnd = split.length -1;
        if (NumberUtils.isCreatable(split[0])) {
            if (split[split.length - 1].charAt(0) == '+' | split[split.length - 1].charAt(0) == '-') {
                add = NumberUtils.createFloat(split[0]) + NumberUtils.createFloat(stripper.matcher(split[split.length - 1]).replaceAll(""));
                stringEnd -= 1;
            } else {
                add = 0;
            }
            stringStart = 1;
        } else {
            add = 0;
        }
        var matcher = finder.matcher(split[stringStart]);
        String timeMult;
        if (matcher.find()) {
            timeMult = matcher.group();
        } else {
            timeMult = null;
        }
        if (NumberUtils.isCreatable(timeMult)) {
            timeMultiply = NumberUtils.createFloat(timeMult);
        } else {
            timeMultiply = 1;
        }
        var mult = split[stringEnd].replace(" ", "").replace(")*", "");
        if (NumberUtils.isCreatable(mult)) {
            multiplier = NumberUtils.createFloat(mult);
        } else {
            multiplier = 1;
        }
        if (operation == Operation.SIN) {
            function = (time) -> Math.sin(Math.toRadians(time * timeMultiply)) * multiplier + add;
        } else {
            function = (time) -> Math.cos(Math.toRadians(time * timeMultiply)) * multiplier + add;
        }
        return function;
    }

    enum Operation {
        SIN,
        COS
    }
}
