package ozomorph.pathfinder;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ozomorph.actions.Action;
import ozomorph.actions.ActionFactory;
import ozomorph.actions.ActionSettings;
import ozomorph.nodes.AgentMapNode;
import ozomorph.nodes.Group;
import ozomorph.nodes.PositionMapNode;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Solves given {@link ProblemInstance}.
 */
public class PathFinder {
    private static final Logger logger = LoggerFactory.getLogger(PathFinder.class);
    private ActionFactory actionFactory;
    private GetPathCallback getPicatExec;
    private boolean isPicatRunning;
    private Process picatProcess;

    /**
     * Creates new PathFinder.
     * @param settings Duration of actions.
     * @param getPicatExec Callback to get path to executable of Picat runtime. Used when not found in system PATH.
     */
    public PathFinder(ActionSettings settings, GetPathCallback getPicatExec) {
        actionFactory = new ActionFactory(settings);
        this.getPicatExec = getPicatExec;
    }

    /**
     * Returns solution to given problemInstance in form of list agents having the found plans.
     * @param problemInstance Problem (initial and target configuration) to solve.
     * @return Agents with already set plans.
     * @throws IOException IO error.
     * @throws InterruptedException Picat runtime interupted.
     * @throws NoPlansFoundException Given problem was not solved (maybe solver terminated?).
     * @throws PicatNotFoundException Executable of Picat runtime not found.
     */
    public List<AgentMapNode> findPaths(ProblemInstance problemInstance) throws IOException, InterruptedException, NoPlansFoundException {
        List<PositionMapNode> agentsLinearOrdering = new ArrayList<>();
        String picatInput = translateToPicatInput(problemInstance, agentsLinearOrdering);
        logger.info("Instance of problem (Picat): " + picatInput);


        File problemInstanceFile = createProblemInstanceFile(picatInput);
        logger.info("Instance of problem being written to: " + problemInstanceFile.getCanonicalPath());

        String picatOutput;
        try{
            picatOutput = runPicat(problemInstanceFile,"picat");
            logger.info("Plans from Picat: " + picatOutput);
        } catch(PicatNotFoundException e){
            logger.warn("Cannot get picat executable from PATH, trying getting the path from user.");
            picatOutput = runPicat(problemInstanceFile, getPicatExec.getPath());
            logger.info("Plans from Picat: " + picatOutput);
        }

        return parsePlans(picatOutput,agentsLinearOrdering);
    }

    /**
     * Creates Picat source file containing the instance of problem as input to solver.
     * @param problemInstance Instance of problem, in Picat language.
     * @return File containing the instance of problem as input to solver.
     * @throws IOException Error while creating the file / writing to it.
     */
    private File createProblemInstanceFile(String problemInstance) throws IOException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date date = new Date();

        File file = new File("../workdir/" + formatter.format(date)  + ".pi");
        file.getParentFile().mkdirs();
        file.createNewFile();
        FileWriter fileWriter = new FileWriter(file);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.printf("getProblemInstance() = PI =>\n" +
                "    PI = %s.", problemInstance);
        printWriter.close();
        return file;
    }

    /**
     * Runs solver in Picat.
     * @param problemInstanceFile Picat source file containing the problem instance (input for solver).
     * @param picatExecPath Path to executable of Picat runtime.
     * @return Plans returned by solver, in Picat language.
     * @throws PicatNotFoundException Executable of Picat runtime not found.
     * @throws IOException IO error.
     * @throws InterruptedException Picat runtime interupted.
     */
    private String runPicat(File problemInstanceFile, String picatExecPath) throws IOException, InterruptedException, NoPlansFoundException {
        String picatMain = "../picat/solve.pi"; // "C:\\Users\\jakub\\OneDrive\\02_mff\\05\\bp\\picat\\solve.pi";

        ProcessBuilder builder = new ProcessBuilder(picatExecPath, picatMain, problemInstanceFile.getCanonicalPath());
        builder.directory(new File("."));

        logger.info("Starting picat as: " + String.join(" ",builder.command()) + "\nPicat process working directory is: " + builder.directory().getCanonicalPath());

        try{
            picatProcess  = builder.start();
            isPicatRunning = true;
        } catch (IOException e){
            throw new PicatNotFoundException(e);
        }

        picatProcess.waitFor();
        isPicatRunning = false;

        byte[] errOut = picatProcess.getErrorStream().readAllBytes();
        if(errOut != null && errOut.length > 0)
            logger.warn("Picat error output: \n" + new String(errOut));

        StringBuilder out = new StringBuilder();
        String plans = null;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(picatProcess.getInputStream()))) {
            //debug only
            String line = null;
            while ((line = reader.readLine()) != null) {
                //debug only
                out.append(line);
                out.append("\n");

                //real code
                if(line.trim().charAt(0) == '[')
                    plans = line;
            }
        } catch (IOException e) {
            throw new IOException("Reading of Picat output failed.", e);
        }



        logger.info("Full picat output: \n"+ out.toString());

        if(plans != null)
            return plans;
        else
            throw new NoPlansFoundException();
    }

    /**
     * Translates {@link ProblemInstance} to corresponding Picat predicate.
     * @param problemInstance Instance of problem to translate.
     * @param agentsLinOrdering Output argument, linear ordering of occupied positions in initial configuration (= starting positions of agets).
     * @return Picat predicate corresponding to given {@link ProblemInstance}.
     */
    private String translateToPicatInput(ProblemInstance problemInstance, List<PositionMapNode> agentsLinOrdering){
        agentsLinOrdering.clear();
        StringBuilder input = new StringBuilder();
        input.append("$problem(");
        input.append(problemInstance.getAgentsCount());
        input.append(",");
        input.append(translateGroups(problemInstance, agentsLinOrdering));
        input.append(",");
        input.append(problemInstance.getWidth());
        input.append(",");
        input.append(problemInstance.getHeight());
        input.append(")");
        return input.toString();
    }

    /**
     * Translates groups used in ProblemInstance to sequence of corresponding Picat predicates.
     * @param problemInstance ProblemInstance to translate.
     * @param agentsLinOrdering Output argument, linear ordering of occupied positions in initial configuration (= starting positions of agets).
     * @return Groups used in ProblemInstance to sequence of corresponding Picat predicates.
     */
    private String translateGroups(ProblemInstance problemInstance, List<PositionMapNode> agentsLinOrdering){
        StringBuilder groupsList = new StringBuilder();
        int firstAgentNumber = 1; //number of first agent in the group
        groupsList.append("[");
        String prefix = "";
        for (Map.Entry<Group, Set<PositionMapNode>> initialsEntry : problemInstance.getInitialPositions().entrySet()) {
            var initials = new ArrayList<>(initialsEntry.getValue());
            agentsLinOrdering.addAll(initials);
            var targets = problemInstance.getTargetPositions().get(initialsEntry.getKey());
            groupsList.append(prefix);
            prefix = ",";
            groupsList.append("$group(");
            groupsList.append(firstAgentNumber);
            groupsList.append(",");
            groupsList.append(translateNodeCollection(problemInstance,initials));
            groupsList.append(",");
            groupsList.append(translateNodeCollection(problemInstance,targets));
            groupsList.append(")");
            firstAgentNumber += initials.size();
        }
        groupsList.append("]");
        return groupsList.toString();
    }

    /**
     * Translates collection of map nodes (vertices) to corresponding Picat predicate.
     * @param problemInstance Instance of problem.
     * @param nodes Collection of nodes to translate.
     * @return Picat predicate corresponding to the map nodes.
     */
    private String translateNodeCollection(ProblemInstance problemInstance, Collection<PositionMapNode> nodes){
        StringBuilder collection = new StringBuilder();
        collection.append("[");
        String prefix = "";
        for (PositionMapNode node : nodes) {
            collection.append(prefix);
            prefix = ",";
            collection.append(getVertexLinIdx(problemInstance,node));
        }
        collection.append("]");
        return collection.toString();
    }

    /**
     * Computes linear index of given map node (vertex of grid).
     * @param problemInstance Instance of problem.
     * @param node Map node.
     * @return Linear index of the map node
     */
    private int getVertexLinIdx(ProblemInstance problemInstance, PositionMapNode node){
        return node.getGridY()*problemInstance.getWidth() + node.getGridX()+1;
    }

    /**
     * Parses plans from Picat output.
     * @param picatOutput Output of solver, in Picat language.
     * @param agentsLinOrdering Linear ordering of occupied positions in initial configuration (= starting positions of agets).
     * @return
     */
    private List<AgentMapNode> parsePlans(String picatOutput, List<PositionMapNode> agentsLinOrdering){
        List<AgentMapNode> agents = new ArrayList<>();
        String[] plans = removeParentheses(picatOutput).split(",(?![^\\[\\]]*\\])"); //split on comma not inside brackets
        for (int i = 0; i < agentsLinOrdering.size(); i++) {
            var parsedPlan = parsePlan(plans[i]);
            agents.add(new AgentMapNode(agentsLinOrdering.get(i), i, parsedPlan));
        }
        return agents;
    }

    /**
     * Remove brackets ("[]") from both sides of given string.
     * @param s String surrounded by brackets.
     * @return String without the brackets.
     */
    private String removeParentheses(String s){
        if(s.charAt(0) == '[' && s.charAt(s.length()-1) == ']')
            return s.substring(1,s.length()-1);
    else
        throw new IllegalArgumentException(String.format("String %s does not start with [ or end with ].", s));
    }

    /**
     * Parse plan to list of {@link Action}s.
     * @param plan Plan for one agent, list of actions in Picat language.
     * @return List of {@link Action}s
     */
    private List<Action> parsePlan(String plan){
        List<Action> parsedPlan = new ArrayList<>();

        String[] picatActions = removeParentheses(plan).split(",");
        for (String picatAction : picatActions) {
            parsedPlan.add(actionFactory.createAction(picatAction));
        }
        return parsedPlan;
    }

    /**
     * Terminates solver (if running).
     */
    public void stop() {
        if(isPicatRunning)
            picatProcess.destroy();
    }
}
