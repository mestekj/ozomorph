package ozomorph.ozocodegenerator;

/**
 * Exception indicating that required procedure is not declared in given Ozocode template.
 */
public class MissingDeclarationException extends Exception {
    protected String missingProcedureName;

    /**
     * Creates new MissingDeclarationException indicating that
     * required procedure is not declared in given Ozocode template.
     * @param missingProcedureName Name of the missing procedure.
     */
    public MissingDeclarationException(String missingProcedureName) {
        super("Procedure " + missingProcedureName + " is not declared in selected template.");
        this.missingProcedureName = missingProcedureName;
    }

    /**
     * Gets name of the required procedure that is not declared in given Ozocode template.
     * @return Name of the undeclared required procedure.
     */
    public String getMissingProcedureName() {
        return missingProcedureName;
    }
}
