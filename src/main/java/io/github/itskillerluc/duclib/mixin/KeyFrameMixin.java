package io.github.itskillerluc.duclib.mixin;

import io.github.itskillerluc.duclib.util.IAdvancedKeyFrame;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import net.minecraft.client.animation.Keyframe;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.function.DoubleSupplier;

@Mixin(Keyframe.class)
public class KeyFrameMixin implements IAdvancedKeyFrame {
    @Nullable
    private Double2DoubleFunction xFunction;
    @Nullable
    private Double2DoubleFunction yFunction;
    @Nullable
    private Double2DoubleFunction zFunction;
    private DoubleSupplier timeSupplier;

    @Shadow
    Vector3f target;
    @Override
    public void setFunctionX(@Nullable Double2DoubleFunction function) {
        xFunction = function;
    }

    @Override
    public void setFunctionY(@Nullable Double2DoubleFunction function) {
        yFunction = function;
    }

    @Override
    public void setFunctionZ(@Nullable Double2DoubleFunction function) {
        zFunction = function;
    }

    @Override
    public void setTimeSupplier(DoubleSupplier supplier) {
        timeSupplier = supplier;
    }

    public Vector3f target() {
        if (xFunction != null) {
            float time = ((float) timeSupplier.getAsDouble());
            target.x = ((float) xFunction.applyAsDouble(time));
        }
        if (yFunction != null) {
            float time = ((float) timeSupplier.getAsDouble());
            target.y = ((float) yFunction.applyAsDouble(time));
        }
        if (zFunction != null) {
            float time = ((float) timeSupplier.getAsDouble());
            target.z = ((float) yFunction.applyAsDouble(time));
        }
        return target;
    }
}
