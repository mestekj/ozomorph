package ozomorph.ozocodegenerator;

import javafx.scene.paint.Color;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ozomorph.actions.AtGoalAction;
import ozomorph.actions.StartAction;
import ozomorph.nodes.AgentMapNode;
import ozomorph.nodes.Group;
import ozomorph.nodes.PositionMapNode;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class DepotGenerationTest {
    @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void emitsPythonMarkersAndPreservesSavedActions() throws Exception {
        AgentMapNode agent = agent();
        File template = temporaryFolder.newFile("template.py");
        Files.writeString(template.toPath(), "agents = ...\n", StandardCharsets.UTF_8);
        String program = new PythonGenerator(temporaryFolder.getRoot())
                .generateOzocodes(Collections.singletonList(agent), template);
        assertTrue(program.contains("['atStart', 'atGoal']"));

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(agent);
        }
        AgentMapNode restored;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            restored = (AgentMapNode) in.readObject();
        }
        assertTrue(restored.getPlan().get(0) instanceof StartAction);
        assertTrue(restored.getPlan().get(1) instanceof AtGoalAction);
        assertEquals(1.0, restored.getPlan().get(0).getDuration(), 0.0);
        assertEquals(1.0, restored.getPlan().get(1).getDuration(), 0.0);
        restored.move(2);
        assertEquals(0.0, restored.getX(), 0.0);
        assertEquals(0.0, restored.getY(), 0.0);
    }

    @Test
    public void rejectsXmlBeforeReadingTemplateOrWritingFiles() throws Exception {
        File missingTemplate = new File(temporaryFolder.getRoot(), "missing.ozocode");
        try {
            new OzocodeGenerator().generateOzocodes(Collections.singletonList(agent()), missingTemplate);
            fail("Expected unsupported depot actions");
        } catch (UnsupportedOperationException expected) {
            assertTrue(expected.getMessage().contains("Depot"));
        }
    }

    private AgentMapNode agent() {
        PositionMapNode node = new PositionMapNode(0, 0);
        node.setGroup(new Group(Color.RED));
        return new AgentMapNode(node, 0, Arrays.asList(new StartAction(), new AtGoalAction()));
    }
}
