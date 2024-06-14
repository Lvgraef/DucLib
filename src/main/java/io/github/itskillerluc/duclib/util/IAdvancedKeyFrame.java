package io.github.itskillerluc.duclib.util;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;

import java.util.function.DoubleSupplier;

public interface IAdvancedKeyFrame {
    void setFunctionX(Double2DoubleFunction supplier);
    void setFunctionY(Double2DoubleFunction supplier);
    void setFunctionZ(Double2DoubleFunction supplier);
    void setTimeSupplier(DoubleSupplier supplier);
}
