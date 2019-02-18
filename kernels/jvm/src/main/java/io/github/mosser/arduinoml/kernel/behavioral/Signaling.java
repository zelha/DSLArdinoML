package io.github.mosser.arduinoml.kernel.behavioral;

import io.github.mosser.arduinoml.kernel.generator.Visitable;
import io.github.mosser.arduinoml.kernel.generator.Visitor;
import io.github.mosser.arduinoml.kernel.structural.Actuator;
import io.github.mosser.arduinoml.kernel.structural.BEEP;

public class Signaling implements Visitable {

    private int numberOfBeeps = 0;
    private BEEP beepSize;
    private Actuator actuator;

    public int getNumberOfBeeps() {
        return numberOfBeeps;
    }

    public void setNumberOfBeeps(int numberOfBeeps) {
        this.numberOfBeeps = numberOfBeeps;
    }

    public BEEP getBeepSize() {
        return beepSize;
    }

    public void setBeepSize(BEEP beepSize) {
        this.beepSize = beepSize;
    }

    public Actuator getActuator() {
        return actuator;
    }

    public void setActuator(Actuator actuator) {
        this.actuator = actuator;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}