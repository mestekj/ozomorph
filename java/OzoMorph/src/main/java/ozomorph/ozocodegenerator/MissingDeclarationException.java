package ozomorph.ozocodegenerator;

public class MissingDeclarationException extends IllegalArgumentException {
    protected String missingProcedureName;

    public MissingDeclarationException(String missingProcedureName) {
        super("Procedure " + missingProcedureName + " is not declared in selected template.");
        this.missingProcedureName = missingProcedureName;
    }

    public String getMissingProcedureName() {
        return missingProcedureName;
    }
}
