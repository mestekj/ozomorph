package ozobotscpf.ozocodegenerator;

import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import ozobotscpf.actions.Action;
import ozobotscpf.nodes.AgentMapNode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.jdom2.*;

public class OzocodeGenerator {
    private String ozocodesDir = "./ozocodes"; //TODO set properly
    private String template = "";

    public void generateOzocodes(List<AgentMapNode> agents) throws JDOMException, IOException {
        //load and parse template
        Document template = loadTemplate();
        Element executeActionsDefinition = getProcedureDefinition(template,"executeActions");

        //initialize outputter
        XMLOutputter outputter = new XMLOutputter();
        outputter.setFormat(Format.getPrettyFormat());

        //process agents
        for (AgentMapNode agent : agents) {
            //clean template
            executeActionsDefinition.removeContent();

            //inject action procedure calls
            var plan = agent.getPlan();
            if(!plan.isEmpty()){
                var calls = generateCalls(plan);
                executeActionsDefinition.addContent(calls);
            }

            //output Ozocode
            FileWriter writer = new FileWriter(ozocodesDir +"/" + "agent_" + agent.getId() + ".ozocode", StandardCharsets.UTF_8);
            outputter.output(template,writer);
        }
    }

    private Document loadTemplate() throws JDOMException, IOException {
        File templateXmlFile = new File(template);
        SAXBuilder saxBuilder = new SAXBuilder();
        Document template = saxBuilder.build(templateXmlFile);
        return template;
    }

    //returns element "<statement name="STACK">
    //returns definition, not declaration! i.e. the element whose value is the actual sequence of function calls
    private Element getProcedureDefinition(Document template, String procedureName){
        String query = "/xml/block[@type='procedures_defnoreturn' and field[@name='NAME']='executeActions']/statement[@name='STACK']";
        XPathExpression<Element> xpe = XPathFactory.instance().compile(query, Filters.element());
        Element statement_stack = xpe.evaluateFirst(template); //Assuming that there is exactly one such element
        return statement_stack;
    }

    private Element generateCalls(List<Action> actions) {
        if(actions.isEmpty())
            throw new IllegalArgumentException("No actions to generate calls.");

        ProcedureCallFactory pcf = new ProcedureCallFactory();
        Element root = pcf.createCall(actions.get(0));
        Element previous = root;

        for (int i = 1; i < actions.size(); i++) {
            Element next = new Element("next");
            Element block = pcf.createCall(actions.get(i));
            next.addContent(block);
            previous.addContent(next);
            previous = block;
        }
        return root;
    }
}
