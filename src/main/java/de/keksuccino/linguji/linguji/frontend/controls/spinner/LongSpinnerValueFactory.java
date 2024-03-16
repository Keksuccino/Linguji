package de.keksuccino.linguji.linguji.frontend.controls.spinner;

import javafx.scene.control.SpinnerValueFactory;
import javafx.util.converter.LongStringConverter;

public class LongSpinnerValueFactory extends SpinnerValueFactory<Long> {

    protected long minValue;
    protected long maxValue;
    protected int stepsPerIncreaseDecrease;

    public LongSpinnerValueFactory(long min, long max) {
        this(min, max, min);
    }

    public LongSpinnerValueFactory(long min, long max, long initialValue) {
        this(min, max, initialValue, 1);
    }

    public LongSpinnerValueFactory(long min, long max, long initialValue, int stepsPerIncreaseDecrease) {
        if (min >= max) throw new RuntimeException("Min value needs to be smaller than max value!");
        this.minValue = min;
        this.maxValue = max;
        this.stepsPerIncreaseDecrease = stepsPerIncreaseDecrease;
        this.setConverter(new LongStringConverter());
        this.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue < this.getMin()) {
                this.setValue(getMin());
            } else if (newValue > this.getMax()) {
                this.setValue(getMax());
            }
        });
        this.setValue(((initialValue >= min) && (initialValue <= max)) ? initialValue : min);
    }

    public final long getMin() {
        return this.minValue;
    }

    public final long getMax() {
        return this.maxValue;
    }

    public final long getStepsPerIncreaseDecrease() {
        return this.stepsPerIncreaseDecrease;
    }

    @Override
    public void decrement(int steps) {
        final long min = getMin();
        final long max = getMax();
        final long newIndex = getValue() - steps * this.getStepsPerIncreaseDecrease();
        setValue(newIndex >= min ? newIndex : (isWrapAround() ? wrapValue(newIndex, min, max) + 1 : min));
    }

    @Override
    public void increment(int steps) {
        final long min = getMin();
        final long max = getMax();
        final long currentValue = getValue();
        final long newIndex = currentValue + steps * this.getStepsPerIncreaseDecrease();
        setValue(newIndex <= max ? newIndex : (isWrapAround() ? wrapValue(newIndex, min, max) - 1 : max));
    }

    protected static long wrapValue(long value, long min, long max) {
        if (max == 0) throw new RuntimeException();
        long r = value % max;
        if (r > min && max < min) {
            r = r + max - min;
        } else if (r < min && max > min) {
            r = r + max - min;
        }
        return r;
    }

}
