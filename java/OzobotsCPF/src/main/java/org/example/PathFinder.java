package org.example;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class PathFinder {
    private ActionFactory actionFactory = new ActionFactory();
    private static final Logger logger = LoggerFactory.getLogger(PathFinder.class);

    public List<AgentMapNode> findPaths(ProblemInstance problemInstance) throws IOException, InterruptedException {
        List<PositionMapNode> agentsLinearOrdering = new ArrayList<>();
        String picatInput = translateToPicatInput(problemInstance, agentsLinearOrdering);
        logger.info("Instance of problem (Picat): " + picatInput);


        File problemInstanceFile = createProblemInstanceFile(picatInput);
        logger.info("Instance of problem being written to: " + problemInstanceFile.getAbsolutePath());

        String picatOutput = runPicat(problemInstanceFile);
        logger.info("Plans from Picat: " + picatOutput);

        return parsePlans(picatOutput,agentsLinearOrdering);
    }

    private File createProblemInstanceFile(String problemInstance) throws IOException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date date = new Date();

        File file = new File("./workdir/" + formatter.format(date)  + ".pi");
        file.getParentFile().mkdirs();
        file.createNewFile();
        FileWriter fileWriter = new FileWriter(file);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.printf("getProblemInstance() = PI =>\n" +
                "    PI = %s.", problemInstance);
        printWriter.close();
        return file;
    }

    private String runPicat(File problemInstanceFile) throws IOException, InterruptedException {
        String picatMain = "./picat/solve.pi"; // "C:\\Users\\jakub\\OneDrive\\02_mff\\05\\bp\\picat\\solve.pi";
        ProcessBuilder builder = new ProcessBuilder("picat", picatMain, problemInstanceFile.getAbsolutePath()); //TODO use relative path
        Process process = builder.start();
        logger.info("Starting picat with: " );
        process.waitFor();

        byte[] errOut = process.getErrorStream().readAllBytes();
        if(errOut != null && errOut.length > 0)
            logger.warn("Picat error output: \n" + new String(errOut));

        StringBuilder out = new StringBuilder();
        String plans = null;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
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

    private int getVertexLinIdx(ProblemInstance problemInstance, PositionMapNode node){
        return node.getGridY()*problemInstance.getWidth() + node.getGridX()+1;
    }

    private List<AgentMapNode> parsePlans(String picatOutput, List<PositionMapNode> agentsLinOrdering){
        List<AgentMapNode> agents = new ArrayList<>();
        String[] plans = removeParentheses(picatOutput).split(",(?![^\\[\\]]*\\])"); //split on comma not inside brackets
        for (int i = 0; i < agentsLinOrdering.size(); i++) {
            var parsedPlan = parsePlan(plans[i]);
            agents.add(new AgentMapNode(agentsLinOrdering.get(i), i, parsedPlan));
        }
        return agents;
    }

    private String removeParentheses(String s){
        if(s.charAt(0) == '[' && s.charAt(s.length()-1) == ']')
            return s.substring(1,s.length()-1);
    else
        throw new IllegalArgumentException(String.format("String %s does not start with [ or end with ].", s));
    }

    private List<Action> parsePlan(String plan){
        List<Action> parsedPlan = new ArrayList<>();

        String[] picatActions = removeParentheses(plan).split(",");
        for (String picatAction : picatActions) {
            parsedPlan.add(actionFactory.createAction(picatAction));
        }
        return parsedPlan;
    }
}
