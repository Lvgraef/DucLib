package io.github.itskillerluc.duclib.client.model;

import io.github.itskillerluc.duclib.client.animation.AnimationHolder;
import io.github.itskillerluc.duclib.client.animation.DucAnimation;
import io.github.itskillerluc.duclib.entity.Animatable;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Create the animation in here and specify the model.
 * just use this one, it's the best, and you can't animate the others.
 */
public class AnimatableDucModel <T extends Entity & Animatable<?>> extends HierarchicalModel<T> {
    Ducling root;

    public AnimatableDucModel(Ducling ducling, Function<ResourceLocation, RenderType> renderType) {
        super(renderType);
        this.root = ducling;
    }

    /**
     * @return the root Ducling
     */
    @Override
    public @NotNull Ducling root() {
        if(root == null){
            throw new NullPointerException("model does not contain a root.");
        }
        return root;
    }

    /**
     * Override this and return any animation keys of animations you don't want to be automatically registered.
     */
    protected Set<String> excludeAnimations(){
        return Collections.emptySet();
    }

    /**
     * Automatically register the animations
     */
    @Override
    public void setupAnim(@NotNull T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        for (Map.Entry<String, AnimationState> stringAnimationStateEntry : pEntity.getAnimations().get().entrySet()) {
            if (!excludeAnimations().contains(stringAnimationStateEntry.getKey())) {
                AnimationHolder animation = pEntity.getAnimation().getAnimations().get(stringAnimationStateEntry.getKey());
                this.animate(stringAnimationStateEntry.getValue(), animation.animation(), pAgeInTicks, animation.speed());
                if (!animation.animation().looping() && stringAnimationStateEntry.getValue().isStarted() && stringAnimationStateEntry.getValue().getAccumulatedTime() > animation.animation().lengthInSeconds() * 1000) {
                    stringAnimationStateEntry.getValue().stop();
                }
            }
        }
    }

    /**
     * Create a map of the animation states automatically.
     * @param animation The animation
     * @return a map of animation states and keys.
     */
    public static Map<String, AnimationState> createStateMap(DucAnimation animation){
        Map<String, AnimationState> states = new HashMap<>();
        for (String key : animation.getAnimations().keySet()) {
            states.put(key, new AnimationState());
        }
        return states;
    }
}
