package ozomorph.ozocodegenerator;

import org.jdom2.filter.ElementFilter;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ozomorph.actions.Action;
import ozomorph.nodes.AgentMapNode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jdom2.*;

/**
 * Generator of programs for Ozobots.
 * Programs are written in Ozocode language which is a XML.
 */
public class OzocodeGenerator {
    private static final Logger logger = LoggerFactory.getLogger(OzocodeGenerator.class);

    /**
     * Directory where create programs will be saved.
     */
    private String ozocodesDir = "../ozocodes";

    /**
     * Generates programs for Ozobots according to plans of given agents.
     * @param agents Agents containing plans.
     * @param templateFile Ozocode template of generated programs.
     * @throws JDOMException Error while reading template or writing generated programs.
     * @throws IOException Error while reading template or writing generated programs.
     * @throws MissingDeclarationException Required Ozocode procedure is not declared in given template.
     */
    public void generateOzocodes(List<AgentMapNode> agents, File templateFile) throws JDOMException, IOException, MissingDeclarationException {
        try {
            //load and parse template
            Document template = loadTemplate(templateFile);
            Element executeActionsDefinition = getProcedureDefinition(template, "executeActions");
            Element setColorDefinition = getProcedureDefinition(template,"setVariables");

            //initialize outputter
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getPrettyFormat());

            File ozoDir = new File(ozocodesDir);
            ozoDir.mkdirs();


            //process agents
            for (AgentMapNode agent : agents) {
                //clean template
                executeActionsDefinition.removeContent();
                setColorDefinition.removeContent();

                //set agent's color
                var color = agent.getGroup().getColor();
                var rgbSet = generateSetVariables(color.getRed(),color.getGreen(),color.getBlue(),agent.getId(),agents.size());
                setColorDefinition.addContent(rgbSet);


                //inject action procedure calls
                var plan = agent.getPlan();
                if (!plan.isEmpty()) {
                    var calls = generateCalls(plan);
                    executeActionsDefinition.addContent(calls);
                }

                //output Ozocode
                FileWriter writer = new FileWriter(ozocodesDir + "/" + "agent_" + agent.getId() + ".ozocode", StandardCharsets.UTF_8);
                outputter.output(template, writer);
            }
        }
        catch (Exception e){
            //just log it
            logger.error("Error while generating ozocodes.",e);
            throw e;
        }
    }

    /**
     * Parse Ozocode template (which is internally a XML file).
     * @param templateFile Template to parse.
     * @return Parsed template.
     * @throws JDOMException Error while parsing the template.
     * @throws IOException Error while reading the template.
     */
    private Document loadTemplate(File templateFile) throws JDOMException, IOException {
        SAXBuilder saxBuilder = JdomHelper.getSAXBuilder(); //SAXBuilder that ignores namespaces
        Document template = saxBuilder.build(templateFile);
        return template;
    }

    /**
     * Returns XML element {@code <statement name="STACK">} representing definition of Ozocode procedure (declared in the template).
     * @param template Parsed Ozocode template.
     * @param procedureName Name of the procedure
     * @return Definition of the procedure (not declaration! i.e. the element whose value is the actual sequence of function calls).
     * @throws MissingDeclarationException The procedure is not declared in given template.
     */
    private Element getProcedureDefinition(Document template, String procedureName) throws MissingDeclarationException {
        String query = "/xml/block[@type='procedures_defnoreturn' and field[@name='NAME']='" + procedureName +"']";
        XPathExpression<Element> xpe = XPathFactory.instance().compile(query, Filters.element());

        Element procedure_def = xpe.evaluateFirst(template); //Assuming that there is exactly one such element

        if(procedure_def == null)
            throw new MissingDeclarationException(procedureName);

        //remove definition (if exists)
        procedure_def.removeContent(new ElementFilter("statement"));

        //add new STACK and return them
        Element statement_stack = new Element("statement");
        statement_stack.setAttribute("name","STACK");
        procedure_def.addContent(statement_stack);

        return statement_stack;
    }

    /**
     * Generates XML element representing sequence of procedure calls according to given list of actions.
     * @param actions List of actions whose corresponding procedures will be called.
     * @return XML element representing sequence of procedure calls.
     */
    private Element generateCalls(List<Action> actions) {
        if(actions.isEmpty())
            throw new IllegalArgumentException("No actions to generate calls.");

        ProcedureCallFactory pcf = new ProcedureCallFactory();
        List<Element> actionCalls = actions.stream().map(pcf::createCall).collect(Collectors.toList());
        Element root = generateSequence(actionCalls);
        return root;
    }

    /**
     * Generates XML elements representing Ozocode that sets Ozocode variables "r","g","b","id", "agentsCount".
     * Components of color will be converted to range used by Ozobots (which is integer 0...127).
     * @param red Red component of agents color, in [0,1].
     * @param green Green component of agents color, in [0,1].
     * @param blue Blue component of agents color, in [0,1].
     * @param id ID of agent.
     * @param agentsCount Total number of agents.
     * @return XML elements representing Ozocode that sets the Ozocode variables
     */
    private Element generateSetVariables(double red, double green, double blue, int id, int agentsCount) {
        List<Element> varsSets = new ArrayList<>();
        varsSets.add(generateSetVariable("r",(byte)Math.round(red*127)));
        varsSets.add(generateSetVariable("g",(byte)Math.round(green*127)));
        varsSets.add(generateSetVariable("b",(byte)Math.round(blue*127)));
        varsSets.add(generateSetVariable("id",(byte)id));
        varsSets.add(generateSetVariable("agentsCount",(byte)agentsCount));

        Element sequenceRoot = generateSequence(varsSets);
        return sequenceRoot;
    }

    /**
     * Generates XML element representing sequence of Ozocode commands.
     * @param blocks XML elements representing the commands.
     * @return XML element representing sequence of Ozocode commands
     * @throws IllegalArgumentException No commands to generate.
     */
    private Element generateSequence(List<Element> blocks){
        if(blocks.isEmpty())
            throw new IllegalArgumentException("No blocks to generate sequence.");

        Element root = blocks.get(0);
        Element previous = root;

        for (int i = 1; i < blocks.size(); i++) {
            Element next = new Element("next");
            Element block = blocks.get(i);
            next.addContent(block);
            previous.addContent(next);
            previous = block;
        }
        return root;
    }

    /**
     * Generates XML element representing seter of an Ozocode variable.
     * @param variableName Name of the variable.
     * @param value Value of the variable.
     * @return XML element representing seter of the Ozocode variable.
     */
    private Element generateSetVariable(String variableName, byte value) {
        Element block = new Element("block")
                .setAttribute("type", "variables_set")
                .addContent(
                        new Element("field")
                                .setText(variableName)
                                .setAttribute("name", "VAR")
                                .setAttribute("variabletype", "")

                ).addContent(
                        new Element("value")
                                .setAttribute("name", "VALUE")
                                .addContent(
                                        new Element("block")
                                                .setAttribute("type", "math_number")
                                                .addContent(
                                                        new Element("field")
                                                                .setText(String.valueOf(value))
                                                                .setAttribute("name", "NUM")
                                                )
                                )
                );
        return block;
    }
}
