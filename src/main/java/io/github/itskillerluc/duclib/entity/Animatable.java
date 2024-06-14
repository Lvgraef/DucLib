package io.github.itskillerluc.duclib.entity;

import io.github.itskillerluc.duclib.client.animation.DucAnimation;
import io.github.itskillerluc.duclib.client.model.AnimatableDucModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.util.Lazy;
import org.apache.logging.log4j.LogManager;

import java.util.Map;
import java.util.Optional;

/**
 * For proper instructions on how to use, check out the wiki on GitHub!
 */
public interface Animatable <T extends AnimatableDucModel<?>> {
    /**
     * @return the model location
     */
    ResourceLocation getModelLocation();

    /**
     * @return the DucAnimation
     */
    DucAnimation getAnimation();

    /**
     * @return Get a lazy with all animations and their keys.
     */
    Lazy<Map<String, AnimationState>> getAnimations();

    /**
     * @return true if the entity is moving.
     */
    default boolean isMoving(LivingEntity entity) {
        return entity.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6D;
    }

    /**
     * Start the animation if it's not already playing
     */
    default void playAnimation(String animation){
        getAnimationState(animation).ifPresentOrElse(animationState -> animationState.startIfStopped(tickCount()), () -> LogManager.getLogger().warn("Could not find animation: " + animation + " for entity"));
    }

    /**
     * Start regardless of if it's already playing.
     */
    default void replayAnimation(String animation){
        getAnimationState(animation).ifPresentOrElse(animationState -> animationState.start(tickCount()), () -> LogManager.getLogger().warn("Couldn't not find animation: " + animation + " for entity"));
    }

    /**
     * Animate when the condition is true
     */
    default void animateWhen(String animation, boolean condition){
        getAnimationState(animation).ifPresentOrElse(animationState -> animationState.animateWhen(condition, tickCount()), () -> LogManager.getLogger().warn("Could not find animation: " + animation + " for entity"));
    }

    /**
     * stop an animation
     */
    default void stopAnimation(String animation){
        getAnimationState(animation).ifPresentOrElse(AnimationState::stop, () -> LogManager.getLogger().warn("Couldn't find animation: " + animation + " for entity"));
    }

    /**
     * get the animation state for a key.
     * @param animation animation key
     * @return the animation state corresponding to the key.
     */
    Optional<AnimationState> getAnimationState(String animation);

    int tickCount();
}
