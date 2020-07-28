package ozomorph.ozocodegenerator;

import org.jdom2.Element;
import ozomorph.actions.*;

/**
 * Factory that creates block of Ozocode that calls Ozocode procedure corresponding to given {@code Action}.
 */
public class ProcedureCallFactory {
    /**
     * Creates block of Ozocode that calls Ozocode procedure corresponding to given {@code Action}.
     * @param action Action to call.
     * @return Ozocode call of corresponding procedure.
     */
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

    /**
     * Creates XML element corresponding to call of parameter-less Ozocode procedure.
     * @param procedureName Name of the Ozocode procedure.
     * @return XML element corresponding to call of the procedure.
     */
    private Element createCallNoArg(String procedureName) {
        Element block = new Element("block")
                .setAttribute("type", "procedures_callnoreturn")
                .addContent(
                        new Element("mutation")
                                .setAttribute("name", procedureName)
                );
        return block;
    }

    /**
     * Creates XML element corresponding to call of Ozocode procedure with one integer argument.
     * @param procedureName Name of the Ozocode procedure.
     * @param argumentName Name of the argument (as in Declaration of the procedure in Ozocode).
     * @param argumentValue Value of the argument.
     * @return XML element corresponding to call of the procedure with given value of argument.
     */
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
