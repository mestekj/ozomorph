package ozobotscpf.ozocodegenerator;

import org.jdom2.Attribute;
import org.jdom2.Element;
import ozobotscpf.actions.*;

import java.lang.annotation.ElementType;

public class ProcedureCallFactory {
    public Element createCall(Action action) {
        if (action instanceof TurnLeftAction)
            return createCallNoArg("turnLeft");
        else if (action instanceof TurnRightAction)
            return createCallNoArg("turnRight");
        else if (action instanceof MoveAction)
            return createCallNoArg("moveForward");
        else if (action instanceof WaitAction)
            return createCallOneIntArg("wait", "time_100ms", (byte) (action.getDuration() * 10));
        else
            throw new UnsupportedOperationException("Unknown Action type: " + action.getClass().getTypeName());
    }

    private Element createCallNoArg(String procedureName) {
        Element block = new Element("block")
                .setAttribute("type", "procedures_callnoreturn")
                .addContent(
                        new Element("mutation")
                                .setAttribute("name", procedureName)
                );
        return block;
    }

    private Element createCallOneIntArg(String procedureName, String argumentName, byte argumentValue) {
        Element block = new Element("block")
                .setAttribute("type", "procedures_callnoreturn")
                .addContent(
                        new Element("mutation")
                                .setAttribute("name", procedureName)
                                .addContent(new Element("arg")
                                        .setAttribute("name", argumentName)
                                ))
                .addContent(
                        new Element("value")
                                .setAttribute("name", "ARG0")
                                .addContent(
                                        new Element("block")
                                                .setAttribute("type", "math_number")
                                                .addContent(
                                                        new Element("field")
                                                                .setAttribute("name", "NUM")
                                                                .setText(String.valueOf(argumentValue))
                                                )
                                )
                );
        return block;
    }
}
