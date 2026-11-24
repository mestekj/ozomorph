# OzoMorph
OzoMorph is a Java program for formulating Colored
MAPF problems, their solving by translation to SAT, and
demonstrating the plans either by simulation on a computer
screen or by execution on robots Ozobot Evo.

OzoMorph runs on Linux, Mac OS, and Windows. 
_Please note, that solving Colored
MAPF problems on Windows is slower because SAT solver that is used by OzoMorph is not working on this platform._

## How to install and run
### Prerequisities
1. Install (download and extract) Picat. Available from: http://picat-lang.org/download.html
2. (Optional) Add path to the folder containing `picat` executable to your environment variable `PATH`. (Otherwise, OzoMorph will ask you for the location of the  executable.)

### Using precompiled binaries
1. From [Releases](https://github.com/mestekj/ozomorph/releases) section,  download and extract appropriate `image-*.zip` based on your platform.
2. Navigate to the `bin` folder and run the `OzoMorph` script  (or `OzoMorph.bat` on Windows).

To run OzoMorph, it is _not_ required to have Java runtime installed.

### Compiling from source code
The actual Java application is a standard Gradle project (located in the `OzoMorph` folder), therefore it can be easily compiled and run from command-line or IDE using the `gradlew run` command.

#### Portable JDK option
Download the Windows x64 JDK 11 ZIP,  e.g., from [Eclipse Temurin](https://adoptium.net/temurin/releases/?arch=x64&os=windows&package=jdk&version=11), and extract it anywhere convenient. No installer is needed. Then run in PowerShell:
```powershell
$env:JAVA_HOME = "D:\ProgramsPortable\jdk-11.0.33+10\"
Set-Location D:\Data\dev\ozomorph\OzoMorph
.\gradlew.bat -PmoduleName=ozomorph run
```

## Usage
1. Enter the desired size of a map (number of nodes) and press **Create**.
2. Allocate robots on their initial and target positions. Each color represents a different group of robots.
3. Press **Morph**. The software finds plans for the robots and opens a new window.
4. There, you can  **Run** the simulation to visualize the plans.
5. Or, you can generate programs for Ozobots by pressing the **Generate** button. OzoMorph asks you for a template of the Ozobot program. Prepared templates are in the `ozocode_templates` folder. `python_template.py` runs each robot's plan independently; `python_template_sync.py` waits for all robots to finish each step before starting the next. Generated programs are stored in the `ozocodes` folder.
6. Run the generated program in the [Ozobot Editor](https://editor.ozobot.com) with your Ozobots Evo connected. The templates use the current Ozobot Python API and refer to robots by names such as `Evo_0` and `Evo_1`.
7. In the simulation window of OzoMorph, switch the mode from **Simulation** to **OnScreen** (for running Ozobots on your display ) or **OnBoard** (for running on printed map).
8. Place Ozobots on their initial positions and press the  **Run** button.

### Depot mode
Select **Depot mode** to start agents in ID order from the bottom-left grid position, filling each row left to right and then moving upward. Agents travel to the selected initial positions, then to target positions of their group, and finally return to their own depot positions. Depot mode and circular plans cannot be selected together.

The generated plans contain `atStart` and `atGoal` actions at the stage boundaries. In simulation each pauses for one second. Use `python_template_depot.py` for independent execution or `python_template_depot_sync.py` for step-synchronized execution. These templates use the agent's group color: only its front-center LED is lit initially, all LEDs light at the start, and only the front-center LED remains lit after reaching the goal. XML Ozocode generation does not support depot plans. Stage actions are aligned by action index, but existing execution modes do not guarantee simultaneous arrival when action durations differ.

All supplied Python templates share the action definitions in `ozocode_templates/actions.py`. During generation, a line such as `# ozomorph: include actions.py` is replaced with that file's contents, producing a standalone program. Include files must be `.py` files in the template's directory; duplicate and nested includes are not supported. Custom templates without include directives continue to work.