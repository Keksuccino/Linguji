package de.keksuccino.polyglot.polyglot.frontend.util;

import de.keksuccino.polyglot.polyglot.backend.util.MathUtils;
import de.keksuccino.polyglot.polyglot.frontend.controls.spinner.LongSpinnerValueFactory;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.function.UnaryOperator;

public class SpinnerUtils {

    @SuppressWarnings("all")
    public static void prepareIntegerSpinner(@NotNull Spinner<Integer> spinner, int minValue, int maxValue, int selected, @Nullable IntegerSpinnerChangeListener changeListener) {

        if (minValue >= maxValue) throw new RuntimeException("Min value needs to be smaller than max value!");
        if (selected < minValue) selected = minValue;
        if (selected > maxValue) selected = maxValue;

        SpinnerValueFactory.IntegerSpinnerValueFactory valFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(minValue, maxValue);
        valFactory.setConverter(new StringConverter<>() {
            @Override
            public String toString(Integer integer) {
                return integer.toString();
            }
            @Override
            public Integer fromString(String s) {
                if (MathUtils.isInteger(s)) return Integer.parseInt(s);
                return minValue;
            }
        });
        valFactory.setValue(selected);
        spinner.setValueFactory(valFactory);
        UnaryOperator<TextFormatter.Change> integerFilter = change -> {
            String newVal = change.getControlNewText();
            if (newVal.isEmpty()) return change;
            if (MathUtils.isInteger(newVal)) {
                int parsed = Integer.parseInt(newVal);
                if ((parsed >= minValue) && (parsed <= maxValue)) return change;
            }
            return null;
        };
        spinner.getEditor().setTextFormatter(new TextFormatter<String>(integerFilter));

        //If unfocus/focus and value is empty, set minVal
        spinner.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (spinner.getEditor().getText().trim().isEmpty() || (spinner.getValue() == minValue)) {
                valFactory.setValue(minValue);
                spinner.getEditor().setText("" + minValue);
            }
        });

        if (changeListener != null) {
            spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
                if ((oldValue != null) && (newValue != null)) changeListener.onChange(oldValue, newValue);
            });
        }

    }

    @SuppressWarnings("all")
    public static void prepareLongSpinner(@NotNull Spinner<Long> spinner, long minValue, long maxValue, long selected, @Nullable LongSpinnerChangeListener changeListener) {

        if (minValue >= maxValue) throw new RuntimeException("Min value needs to be smaller than max value!");
        if (selected < minValue) selected = minValue;
        if (selected > maxValue) selected = maxValue;

        LongSpinnerValueFactory valFactory = new LongSpinnerValueFactory(minValue, maxValue);
        valFactory.setConverter(new StringConverter<>() {
            @Override
            public String toString(Long l) {
                return l.toString();
            }
            @Override
            public Long fromString(String s) {
                if (MathUtils.isLong(s)) return Long.parseLong(s);
                return minValue;
            }
        });
        valFactory.setValue(selected);
        spinner.setValueFactory(valFactory);
        UnaryOperator<TextFormatter.Change> integerFilter = change -> {
            String newVal = change.getControlNewText();
            if (newVal.isEmpty()) return change;
            if (MathUtils.isLong(newVal)) {
                long parsed = Long.parseLong(newVal);
                if ((parsed >= minValue) && (parsed <= maxValue)) return change;
            }
            return null;
        };
        spinner.getEditor().setTextFormatter(new TextFormatter<String>(integerFilter));

        //If unfocus/focus and value is empty, set minVal
        spinner.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (spinner.getEditor().getText().trim().isEmpty() || (spinner.getValue() == minValue)) {
                valFactory.setValue(minValue);
                spinner.getEditor().setText("" + minValue);
            }
        });

        if (changeListener != null) {
            spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
                if ((oldValue != null) && (newValue != null)) changeListener.onChange(oldValue, newValue);
            });
        }

    }

    @FunctionalInterface
    public interface IntegerSpinnerChangeListener {
        void onChange(int oldValue, int newValue);
    }

    @FunctionalInterface
    public interface LongSpinnerChangeListener {
        void onChange(long oldValue, long newValue);
    }

}
