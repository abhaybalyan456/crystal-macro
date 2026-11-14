package com.crystalmacro.gui;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class DoubleSliderWidget extends SliderWidget {
    private final String prefix;
    private final String suffix;
    private final double min;
    private final double max;
    private final Consumer<Double> onValueChange;
    private final boolean isInteger;

    public DoubleSliderWidget(int x, int y, int width, int height, String prefix, String suffix, double min, double max, double initialValue, boolean isInteger, Consumer<Double> onValueChange) {
        super(x, y, width, height, Text.empty(), (initialValue - min) / (max - min));
        this.prefix = prefix;
        this.suffix = suffix;
        this.min = min;
        this.max = max;
        this.isInteger = isInteger;
        this.onValueChange = onValueChange;
        this.updateMessage();
    }

    @Override
    protected void updateMessage() {
        double currentVal = min + (this.value * (max - min));
        String valStr = isInteger ? String.valueOf((int) Math.round(currentVal)) : String.format("%.1f", currentVal);
        this.setMessage(Text.literal(prefix + ": " + valStr + (suffix.isEmpty() ? "" : " " + suffix)));
    }

    @Override
    protected void applyValue() {
        double currentVal = min + (this.value * (max - min));
        if (isInteger) {
            currentVal = Math.round(currentVal);
        }
        if (onValueChange != null) {
            onValueChange.accept(currentVal);
        }
    }
}
