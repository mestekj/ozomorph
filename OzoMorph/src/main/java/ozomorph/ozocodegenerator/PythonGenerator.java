package ozomorph.ozocodegenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ozomorph.actions.Action;
import ozomorph.nodes.AgentMapNode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class PythonGenerator {

    private static final Logger logger = LoggerFactory.getLogger(OzocodeGenerator.class);

    /**
     * Directory where create programs will be saved.
     */
    private String ozocodesDir = "../ozocodes";

    /**
     * Generates programs for Ozobots according to plans of given agents.
     * @param agents Agents containing plans.
     * @param templateFile Python template file.
     * @throws IOException Error while reading template or writing generated programs.
     * @throws MissingDeclarationException Required agents declaration is not found in given template.
     */
    public void generateOzocodes(List<AgentMapNode> agents, File templateFile) throws IOException, MissingDeclarationException {
        // Read the Python template file
        List<String> lines = Files.readAllLines(templateFile.toPath(), StandardCharsets.UTF_8);
        
        // Find the line with "agents = ..."
        int agentsLineIndex = -1;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.startsWith("agents") && line.contains("=")) {
                agentsLineIndex = i;
                break;
            }
        }
        
        if (agentsLineIndex == -1) {
            throw new MissingDeclarationException("Declaration of 'agents' variable not found in template file");
        }
        
        // Build the agents dictionary string
        StringBuilder agentsDict = new StringBuilder("agents = {");
        
        for (int i = 0; i < agents.size(); i++) {
            AgentMapNode agent = agents.get(i);
            var color = agent.getGroup().getColor();
            
            if (i > 0) {
                agentsDict.append(", ");
            }
            
            // Format: agent_id: ((r, g, b), [action_list])
            agentsDict.append(agent.getId()).append(": (");
            
            // RGB tuple
            agentsDict.append("(")
                    .append((int)(color.getRed() * 255)).append(", ")
                    .append((int)(color.getGreen() * 255)).append(", ")
                    .append((int)(color.getBlue() * 255))
                    .append("), ");
            
            // Action list
            agentsDict.append("[");
            List<Action> plan = agent.getPlan();
            for (int j = 0; j < plan.size(); j++) {
                if (j > 0) {
                    agentsDict.append(", ");
                }
                agentsDict.append("'").append(getActionName(plan.get(j))).append("'");
            }
            agentsDict.append("])");
        }
        
        agentsDict.append("}");
        
        // Replace the agents line
        lines.set(agentsLineIndex, agentsDict.toString());
        
        // Create output directory
        File ozoDir = new File(ozocodesDir);
        ozoDir.mkdirs();
        
        // Write the updated Python program
        File outputFile = new File(ozocodesDir + "/robot_program.py");
        try (FileWriter writer = new FileWriter(outputFile, StandardCharsets.UTF_8)) {
            for (String line : lines) {
                writer.write(line);
                writer.write(System.lineSeparator());
            }
        }
        
        logger.info("Generated Python program for {} agents at {}", agents.size(), outputFile.getAbsolutePath());
    }
    
    /**
     * Gets the action name based on the action class type.
     * @param action The action instance.
     * @return The action name as a string.
     */
    private String getActionName(Action action) {
        String className = action.getClass().getSimpleName();
        
        // Map class names to action names
        switch (className) {
            case "MoveAction":
                return "goAhead";
            case "TurnLeftAction":
                return "turnLeft";
            case "TurnRightAction":
                return "turnRight";
            case "WaitAction":
                return "wait";
            default:
                return className.replace("Action", "").toLowerCase();
        }
    }

}
