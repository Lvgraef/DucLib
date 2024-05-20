package io.github.itskillerluc.duclib.client.animation;

import cpw.mods.util.Lazy;
import io.github.itskillerluc.duclib.data.animation.DucLibAnimationLoader;
import io.github.itskillerluc.duclib.data.animation.serializers.Animation;
import io.github.itskillerluc.duclib.data.animation.serializers.Bone;
import io.github.itskillerluc.duclib.data.animation.serializers.KeyFrame;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

import java.util.*;

/**
 * This class generates the animations from json files.
 */
public abstract class DucAnimation {
    private final Lazy<Map<String, AnimationHolder>> ANIMATIONS = Lazy.of(this::createAnimations);

    /**
     * This method converts the serialized json files into java keyframe animations. This is quite resource expensive, so the result gets stored in the lazy above.
     * @return A map of animations with their keys.
     */
    Map<String, AnimationHolder> createAnimations(){
        io.github.itskillerluc.duclib.data.animation.serializers.AnimationHolder holder = DucLibAnimationLoader.getAnimation(getLocation());
        Map<String, AnimationHolder> holderMap = new HashMap<>();
        for (Map.Entry<String, Animation> stringAnimationEntry : Objects.requireNonNull(holder, "Couldn't find animation with name \"" + getLocation().toString() + "\"").animations().entrySet()) {
            AnimationDefinition.Builder builder = AnimationDefinition.Builder.withLength(stringAnimationEntry.getValue().animationLength() == 0 ? 1 : stringAnimationEntry.getValue().animationLength());
            for (Map.Entry<String, Bone> bones : stringAnimationEntry.getValue().bones().entrySet()) {
                List<Keyframe> positions = new ArrayList<>();
                List<Keyframe> rotations = new ArrayList<>();
                List<Keyframe> scales = new ArrayList<>();
                for (int i = 0; i < bones.getValue().position().entrySet().size(); i++) {
                    List<Map.Entry<String, KeyFrame>> entries = bones.getValue().position().entrySet().stream().toList();
                    var pre = createKeyFrameFor(i, entries, true, AnimationChannel.Targets.POSITION);
                    if (pre != null) {
                        positions.add(pre);
                    }
                    positions.add(createKeyFrameFor(i, entries, false, AnimationChannel.Targets.POSITION));
                }
                for (int i = 0; i < bones.getValue().rotation().entrySet().size(); i++) {
                    List<Map.Entry<String, KeyFrame>> entries = bones.getValue().rotation().entrySet().stream().toList();
                    var pre = createKeyFrameFor(i, entries, true, AnimationChannel.Targets.ROTATION);
                    if (pre != null) {
                        rotations.add(pre);
                    }
                    rotations.add(createKeyFrameFor(i, entries, false, AnimationChannel.Targets.ROTATION));
                }
                for (int i = 0; i < bones.getValue().scale().entrySet().size(); i++) {
                    List<Map.Entry<String, KeyFrame>> entries = bones.getValue().scale().entrySet().stream().toList();
                    var pre = createKeyFrameFor(i, entries, true, AnimationChannel.Targets.SCALE);
                    if (pre != null) {
                        scales.add(pre);
                    }
                    scales.add(createKeyFrameFor(i, entries, false, AnimationChannel.Targets.SCALE));
                }

                positions.sort(Comparator.comparingDouble(Keyframe::timestamp));
                rotations.sort(Comparator.comparingDouble(Keyframe::timestamp));
                scales.sort(Comparator.comparingDouble(Keyframe::timestamp));

                AnimationChannel positionChannel = new AnimationChannel(AnimationChannel.Targets.POSITION, positions.toArray(new Keyframe[0]));
                AnimationChannel rotationChannel = new AnimationChannel(AnimationChannel.Targets.ROTATION, rotations.toArray(new Keyframe[0]));
                AnimationChannel scaleChannel = new AnimationChannel(AnimationChannel.Targets.SCALE, scales.toArray(new Keyframe[0]));

                if (positionChannel.keyframes().length > 0){
                    builder.addAnimation(bones.getKey(), positionChannel);
                }
                if (rotationChannel.keyframes().length > 0){
                    builder.addAnimation(bones.getKey(), rotationChannel);
                }
                if (scaleChannel.keyframes().length > 0){
                    builder.addAnimation(bones.getKey(), scaleChannel);

                }
            }
            if (stringAnimationEntry.getValue().loop()){
                builder.looping();
            }
            holderMap.put(stringAnimationEntry.getKey(), new AnimationHolder(stringAnimationEntry.getValue().speed(), builder.build()));
        }
        return holderMap;
    }

    /**
     * helper method for creating keyframes
     * @param i index
     * @param entries all keyframes
     * @param target position to move to
     * @return the keyframe
     */
    private static Keyframe createKeyFrame(int i, List<Map.Entry<String, KeyFrame>> entries, Vector3f target) {
        return new Keyframe(Float.parseFloat(entries.get(i).getKey()),
                target, entries.get(i).getValue().lerpMode() != null && entries.get(i).getValue().lerpMode().equals("catmullrom") ? AnimationChannel.Interpolations.CATMULLROM : AnimationChannel.Interpolations.LINEAR);
    }


    /**
     * a helper method for the helper methods.
     * @param i index
     * @param entries all keyframes
     * @param pre true if this is a pre keyframe
     * @param channel position, rotation or scale
     * @return the keyframe
     */
    private static Keyframe createKeyFrameFor(int i, List<Map.Entry<String, KeyFrame>> entries, boolean pre, AnimationChannel.Target channel){
        if (channel == AnimationChannel.Targets.ROTATION) {
            double[] vector = entries.get(i).getValue().pre();
            if (pre) {
                if (vector != null) {
                    return createPreKeyFrame(i, entries, KeyframeAnimations.degreeVec(((float) entries.get(i).getValue().pre()[0]), (float) entries.get(i).getValue().pre()[1], (float) entries.get(i).getValue().pre()[2]));
                } else {
                    return null;
                }
            } else {
                return createKeyFrame(i, entries, KeyframeAnimations.degreeVec(((float) entries.get(i).getValue().post()[0]), ((float) entries.get(i).getValue().post()[1]), ((float) entries.get(i).getValue().post()[2])));
            }
        }
        if (channel == AnimationChannel.Targets.SCALE){
            double[] vector = entries.get(i).getValue().pre();
            if (pre) {
                if (vector != null) {
                    return createPreKeyFrame(i, entries, KeyframeAnimations.scaleVec(((float) entries.get(i).getValue().pre()[0]), (float) entries.get(i).getValue().pre()[1], (float) entries.get(i).getValue().pre()[2]));
                } else {
                    return null;
                }
            }
            return createKeyFrame(i, entries, KeyframeAnimations.scaleVec(((float) entries.get(i).getValue().post()[0]), ((float) entries.get(i).getValue().post()[1]), ((float) entries.get(i).getValue().post()[2])));
        }
        double[] vector = entries.get(i).getValue().pre();
        if (pre) {
            if (vector != null) {
                return createPreKeyFrame(i, entries, KeyframeAnimations.posVec(((float) entries.get(i).getValue().pre()[0]), (float) entries.get(i).getValue().pre()[1], (float) entries.get(i).getValue().pre()[2]));
            } else {
                return null;
            }
        }
        return createKeyFrame(i, entries, KeyframeAnimations.posVec(((float) entries.get(i).getValue().post()[0]), ((float) entries.get(i).getValue().post()[1]), ((float) entries.get(i).getValue().post()[2])));
    }

    /**
     * helper method for creating keyframes
     * @param i index
     * @param entries all keyframes
     * @param target position to move to
     * @return the keyframe
     */
    private static Keyframe createPreKeyFrame(int i, List<Map.Entry<String, KeyFrame>> entries, Vector3f target){
        double[] vector = entries.get(i).getValue().pre();
        if (vector == null){
            return null;
        }
        return new Keyframe(Float.parseFloat(entries.get(i).getKey()),
                target, AnimationChannel.Interpolations.LINEAR);
    }


    /**
     * This is the proper way of getting the animations through the DucAnimation class.
     * @return A map of animations and their keys.
     */
    public final Map<String, AnimationHolder> getAnimations(){
        return ANIMATIONS.get();
    }

    /**
     * @return The animation's resourceLocation.
     */
    abstract ResourceLocation getLocation();

    /**
     * Use this factory method to create a new DucAnimation instance that reads a json file.
     * @param location The animation's resourceLocation
     * @return a new instance of a DucAnimation.
     */
    public static DucAnimation create(ResourceLocation location){
        return new DucAnimation(){
            @Override
            ResourceLocation getLocation() {
                return location;
            }
        };
    }

    /**
     * Use this factory method to create a new DucAnimation instance using java code.
     * @param animations A map of all animations and their keys.
     * @return a new instance of a DucAnimation.
     */
    public static DucAnimation createWithJava(Map<String, AnimationHolder> animations){
        return new DucAnimation() {
            @Override
            Map<String, AnimationHolder> createAnimations() {
                return new HashMap<>(animations);
            }

            @Override
            ResourceLocation getLocation() {
                return null;
            }
        };
    }
}
