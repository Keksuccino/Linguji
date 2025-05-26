package de.keksuccino.linguji.linguji.frontend.util;

import de.keksuccino.linguji.linguji.backend.lib.MathUtils;
import de.keksuccino.linguji.linguji.frontend.controls.spinner.LongSpinnerValueFactory;
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

    @SuppressWarnings("all")
    public static void prepareDoubleSpinner(@NotNull Spinner<Double> spinner, double minValue, double maxValue, double selected, @Nullable DoubleSpinnerChangeListener changeListener) {

        if (minValue >= maxValue) throw new RuntimeException("Min value needs to be smaller than max value!");
        if (selected < minValue) selected = minValue;
        if (selected > maxValue) selected = maxValue;

        SpinnerValueFactory.DoubleSpinnerValueFactory valFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(minValue, maxValue);
        valFactory.setConverter(new StringConverter<>() {
            @Override
            public String toString(Double d) {
                return d.toString();
            }
            @Override
            public Double fromString(String s) {
                if (MathUtils.isDouble(s)) return Double.parseDouble(s);
                return minValue;
            }
        });
        valFactory.setValue(selected);
        valFactory.setAmountToStepBy(0.1D);
        spinner.setValueFactory(valFactory);
        UnaryOperator<TextFormatter.Change> doubleFilter = change -> {
            String newVal = change.getControlNewText();
            if (newVal.isEmpty()) return change;
            if (MathUtils.isDouble(newVal)) {
                double parsed = Double.parseDouble(newVal);
                if ((parsed >= minValue) && (parsed <= maxValue)) return change;
            }
            return null;
        };
        spinner.getEditor().setTextFormatter(new TextFormatter<String>(doubleFilter));

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

//    @SuppressWarnings("all")
//    public static void prepareDoubleSpinner(@NotNull Spinner<Double> spinner, double minValue, double maxValue, double selected, @Nullable DoubleSpinnerChangeListener changeListener) {
//
//        if (minValue >= maxValue) throw new RuntimeException("Min value needs to be smaller than max value!");
//        if (selected < minValue) selected = minValue;
//        if (selected > maxValue) selected = maxValue;
//
//        SpinnerValueFactory.DoubleSpinnerValueFactory valFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(minValue, maxValue);
//        valFactory.setConverter(new StringConverter<>() {
//            @Override
//            public String toString(Double d) {
//                if (d == null) return "0.0";
//                return String.format("%.1f", d);
//            }
//            @Override
//            public Double fromString(String s) {
//                if (s == null || s.trim().isEmpty()) {
//                    return spinner.getValue() != null ? spinner.getValue() : minValue;
//                }
//                try {
//                    // Replace comma with dot for locales that use comma as decimal separator
//                    s = s.replace(',', '.');
//                    // Remove any non-numeric characters except dot and minus
//                    s = s.replaceAll("[^0-9.-]", "");
//                    if (s.isEmpty() || s.equals("-") || s.equals(".")) {
//                        return spinner.getValue() != null ? spinner.getValue() : minValue;
//                    }
//                    double val = Double.parseDouble(s);
//                    if (val < minValue) val = minValue;
//                    if (val > maxValue) val = maxValue;
//                    return val;
//                } catch (Exception e) {
//                    return spinner.getValue() != null ? spinner.getValue() : minValue;
//                }
//            }
//        });
//        valFactory.setValue(selected);
//        valFactory.setAmountToStepBy(0.1); // Set increment for double spinner
//        spinner.setValueFactory(valFactory);
//        spinner.setEditable(true); // Ensure spinner is editable
//
//        // Simplified text formatter
//        spinner.getEditor().textProperty().addListener((obs, oldText, newText) -> {
//            if (newText != null && !newText.isEmpty()) {
//                // Allow typing decimal numbers
//                if (!newText.matches("-?\\d*\\.?\\d*")) {
//                    spinner.getEditor().setText(oldText);
//                }
//            }
//        });
//
//        //Handle focus lost
//        spinner.focusedProperty().addListener((observable, oldValue, newValue) -> {
//            if (!newValue) { // Lost focus
//                try {
//                    Double value = valFactory.getConverter().fromString(spinner.getEditor().getText());
//                    valFactory.setValue(value);
//                } catch (Exception e) {
//                    valFactory.setValue(spinner.getValue());
//                }
//            }
//        });
//
//        if (changeListener != null) {
//            spinner.valueProperty().addListener((observable, oldValue, newValue) -> {
//                if ((oldValue != null) && (newValue != null) && !oldValue.equals(newValue)) {
//                    changeListener.onChange(oldValue, newValue);
//                }
//            });
//        }
//
//    }

    @FunctionalInterface
    public interface IntegerSpinnerChangeListener {
        void onChange(int oldValue, int newValue);
    }

    @FunctionalInterface
    public interface LongSpinnerChangeListener {
        void onChange(long oldValue, long newValue);
    }

    @FunctionalInterface
    public interface DoubleSpinnerChangeListener {
        void onChange(double oldValue, double newValue);
    }

}
