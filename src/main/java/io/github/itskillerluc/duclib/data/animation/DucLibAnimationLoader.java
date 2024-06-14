package io.github.itskillerluc.duclib.data.animation;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import io.github.itskillerluc.duclib.data.animation.serializers.AnimationHolder;
import io.github.itskillerluc.duclib.data.animation.serializers.Bone;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * load the animations into the game.
 */
public class DucLibAnimationLoader extends SimpleJsonResourceReloadListener {
    public static final Gson GSON = new GsonBuilder().registerTypeAdapter(Bone.class, new Bone.adapter()).setPrettyPrinting().disableHtmlEscaping().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    private static final Map<ResourceLocation, AnimationHolder> ANIMATIONS = new HashMap<>();

    public static AnimationHolder getAnimation(ResourceLocation key){
        for (Map.Entry<ResourceLocation, AnimationHolder> resourceLocationAnimationHolderEntry : ANIMATIONS.entrySet()) {
            if(resourceLocationAnimationHolderEntry.getKey().equals(key)){
                return resourceLocationAnimationHolderEntry.getValue();
            }
        }
        return null;
    }

    public DucLibAnimationLoader() {
        super(GSON, "duclings/animations");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, @NotNull ResourceManager pResourceManager, @NotNull ProfilerFiller pProfiler) {
        for (Map.Entry<ResourceLocation, JsonElement> resourceLocationJsonElementEntry : pObject.entrySet()) {
            AnimationHolder holder = GsonHelper.fromJson(GSON, resourceLocationJsonElementEntry.getValue().toString(), AnimationHolder.class);
            ANIMATIONS.put(ResourceLocation.tryBuild(resourceLocationJsonElementEntry.getKey().getNamespace(), resourceLocationJsonElementEntry.getKey().getPath().replace(".animation", "")), holder);
        }
    }
}
