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
import java.nio.file.Path;
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

    @Test
    public void expandsSharedActionsInAllBundledTemplates() throws Exception {
        Path templates = Path.of("..", "ozocode_templates");
        String fragment = Files.readString(templates.resolve("actions.py"), StandardCharsets.UTF_8);
        String[] names = {"python_template.py", "python_template_sync.py",
                "python_template_depot.py", "python_template_depot_sync.py"};
        for (String name : names) {
            String program = new PythonGenerator(temporaryFolder.getRoot())
                    .generateOzocodes(Collections.singletonList(agent()), templates.resolve(name).toFile());
            String expected = String.join("\n", Files.readAllLines(templates.resolve(name), StandardCharsets.UTF_8))
                    .replace("agents = ...", "agents = {0: ((255, 0, 0), ['atStart', 'atGoal'])}")
                    .replace("# ozomorph: include actions.py", fragment.replace("\r\n", "\n").stripTrailing())
                    .replace("\n", System.lineSeparator()) + System.lineSeparator();
            assertEquals(name, expected, program);
            assertTrue(name, program.contains(fragment.replace("\r\n", "\n")
                    .replace("\n", System.lineSeparator()).trim()));
            assertEquals(name, program.indexOf("async def execute_action"), program.lastIndexOf("async def execute_action"));
            assertFalse(name, program.contains("# ozomorph: include"));
            assertFalse(name, program.contains("import actions"));
            assertTrue(name, program.contains("agents = {0:"));
            assertTrue(name, program.contains(name.contains("depot") ? "DEPOT_MODE = True" : "DEPOT_MODE = False"));
            assertTrue(name, program.contains(name.contains("depot")
                    ? "await set_front_center_color(r, g, b)"
                    : "await set_light_color(r, g, b)"));
            assertTrue(name, program.contains(name.contains("sync")
                    ? "await execute_action(action, agents[agent][0])"
                    : "await execute_action(action, (r, g, b))"));
            assertEquals(program, Files.readString(temporaryFolder.getRoot().toPath().resolve("robot_program.py"), StandardCharsets.UTF_8));
        }
    }

    @Test
    public void rejectsMissingInvalidDuplicateAndNestedIncludesWithoutWritingOutput() throws Exception {
        File template = temporaryFolder.newFile("include-template.py");
        File fragment = temporaryFolder.newFile("fragment.py");
        File previousOutput = temporaryFolder.newFile("robot_program.py");
        Files.writeString(previousOutput.toPath(), "existing program");
        String[] directives = {
                "# ozomorph: include missing.py",
                "# ozomorph: include ../fragment.py",
                "# ozomorph: include fragment.py\n# ozomorph: include fragment.py",
                "# ozomorph: include fragment.py"
        };
        for (int i = 0; i < directives.length; i++) {
            Files.writeString(fragment.toPath(), i == 3 ? "# ozomorph: include nested.py\n" : "pass\n");
            Files.writeString(template.toPath(), "agents = ...\n" + directives[i] + "\n");
            try {
                new PythonGenerator(temporaryFolder.getRoot())
                        .generateOzocodes(Collections.singletonList(agent()), template);
                fail("Expected include error: " + directives[i]);
            } catch (IOException expected) {
                assertTrue(expected.getMessage().contains("include"));
                assertEquals("existing program", Files.readString(previousOutput.toPath()));
            }
        }
    }

    @Test
    public void resolvesIncludesBesideCustomTemplate() throws Exception {
        File directory = temporaryFolder.newFolder("custom templates");
        File template = new File(directory, "custom.py");
        Files.writeString(template.toPath(), "agents = ...\n# ozomorph: include shared actions.py\n");
        Files.writeString(new File(directory, "shared actions.py").toPath(), "async def custom_action():\n    pass\n");
        String program = new PythonGenerator(temporaryFolder.getRoot())
                .generateOzocodes(Collections.singletonList(agent()), template);
        assertTrue(program.contains("async def custom_action():"));
        assertFalse(program.contains("# ozomorph: include"));
    }

    private AgentMapNode agent() {
        PositionMapNode node = new PositionMapNode(0, 0);
        node.setGroup(new Group(Color.RED));
        return new AgentMapNode(node, 0, Arrays.asList(new StartAction(), new AtGoalAction()));
    }
}
