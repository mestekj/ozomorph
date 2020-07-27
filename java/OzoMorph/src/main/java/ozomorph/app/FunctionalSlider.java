package ozomorph.app;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Slider;
import javafx.util.StringConverter;

public class FunctionalSlider extends Slider {
    private ReadOnlyDoubleWrapper functionValue = new ReadOnlyDoubleWrapper();

    public FunctionalSlider() {
        this.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                functionValue.set(Math.pow(2, getValue()));
            }
        });

        this.setLabelFormatter(new StringConverter<Double>() {

            @Override
            public Double fromString(String string) {
                return 0.0;
            }

            @Override
            public String toString(Double object) {
                if(object>= 0)
                    return String.format("%1$.0f",Math.pow(2,object));
                else
                    return String.format("1/%1$.0f",Math.pow(2,-object));
            }
        });

        //initial value
        functionValue.set(Math.pow(2, getValue()));
    }

    public double getFunctionValue() {
        return functionValue.get();
    }

    public ReadOnlyDoubleProperty functionValueProperty() {
        return functionValue.getReadOnlyProperty();
    }

    public void setFuctionalValue(double functionalValue){
        this.setValue(Math.log(functionalValue)/Math.log(2));
    }
}
