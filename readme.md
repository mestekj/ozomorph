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

## Usage
1. Enter the desired size of a map (number of nodes) and press **Create**.
2. Allocate robots on their initial and target positions. Each color represents a different group of robots.
3. Press **Morph**. The software finds plans for the robots and opens a new window.
4. There, you can  **Run** the simulation to visualize the plans.
5. Or, you can generate programs for Ozobots by pressing the **Generate** button. OzoMorph asks you for a template of the Ozobot program. Prepared templates are in `ozocode_template` folder. Generated programs are stored in the `ozocodes` folder.
6. Upload programs to [ozoblockly.com](https://ozoblockly.com/editor?robot=evo&mode=5) and run them on your Ozobots Evo.
7. In the simulation window of OzoMorph, switch the mode from **Simulation** to **OnScreen** (for running Ozobots on your display ) or **OnBoard** (for running on printed map).
8. Place Ozobots on their initial positions and press the  **Run** button.