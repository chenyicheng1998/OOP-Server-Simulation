# Scene Builder Guide

## What is Scene Builder?

Scene Builder is a visual JavaFX UI design tool that allows you to design user interfaces via drag-and-drop and generates FXML files.

---

## Download and Installation

Download link:
[https://gluonhq.com/products/scene-builder/](https://gluonhq.com/products/scene-builder/)

Select the version suitable for your operating system, download, and install.

---

## Open Project FXML in Scene Builder

1. Launch Scene Builder
2. Click **File → Open**
3. Navigate to project directory:
   `C:\Users\cheny\IdeaProjects\OOP-Server-Simulation\src\main\resources\`
4. Open `simulation_view.fxml`

---

## FXML File Structure

Our `simulation_view.fxml` uses the following layout:

```
BorderPane (Root Container)
├── Top: VBox (Title and Control Panel)
│   ├── Title Label
│   └── Control Buttons HBox (Start, Pause, Stop, etc.)
├── Center: VBox (Visualization Canvas)
│   └── Canvas (Simulation animation rendering)
├── Right: VBox (Statistics Panel)
│   └── ScrollPane
│       └── Multiple TitledPane (Overall stats, Queue stats, User stats, etc.)
└── Bottom: VBox (Configuration Panel)
    ├── Config Buttons HBox (Save, Load, View History)
    └── GridPane (Parameter input fields)
```

---

## How to Modify UI in Scene Builder

### 1. Modify Component Properties

* Select a component → right panel shows its properties:

    * **Properties**: Text, size, alignment, etc.
    * **Layout**: Margins, spacing, constraints
    * **Code**: fx:id, event handlers

Example: Change button text

* Select "Start" button
* In **Properties**, change `Text` to "Start" or other label

---

### 2. Add New Components

* Drag components from left panel:

    * **Containers**: VBox, HBox, GridPane, BorderPane
    * **Controls**: Button, Label, TextField, ComboBox
    * **Shapes**: Rectangle, Circle, Line

Example: Add a new button

* Drag "Button" from **Controls** → drop in target container
* Set `fx:id` and `onAction` properties

---

### 3. Set Event Handlers

* Select button → **Code** panel → `On Action` → enter method name (e.g., `handleStart`)
* Method must exist in Controller with `@FXML` annotation:

```java
@FXML
private void handleStart() {
    // handle start event
}
```

---

### 4. Set fx:id

* Select component → **Code** panel → `fx:id` → enter ID (e.g., `startButton`)
* Declare in Controller:

```java
@FXML private Button startButton;
```

---

### 5. Apply CSS Styles

* **Option 1 – Style Class**

    * Select component → **Properties → Style Class** → add class name (e.g., `control-button`)
    * Define style in `styles.css`
* **Option 2 – Inline Style**

    * Select component → **Properties → Style** → enter CSS directly (e.g., `-fx-background-color: blue;`)

---

## Important Project Components and fx:id

**Control Buttons**

| fx:id        | Description   |
| ------------ | ------------- |
| startButton  | Start button  |
| pauseButton  | Pause button  |
| resumeButton | Resume button |
| stopButton   | Stop button   |
| resetButton  | Reset button  |

**Display Labels**

| fx:id               | Description         |
| ------------------- | ------------------- |
| timeLabel           | Simulation time     |
| speedLabel          | Speed multiplier    |
| arrivedTasksLabel   | Tasks arrived       |
| completedTasksLabel | Tasks completed     |
| avgSystemTimeLabel  | Average system time |
| throughputLabel     | Throughput          |
| dbStatusLabel       | Database status     |

**Input Fields**

| fx:id                | Description          |
| -------------------- | -------------------- |
| arrivalIntervalField | Arrival interval     |
| simulationTimeField  | Simulation time      |
| cpuNodesField        | CPU nodes            |
| gpuNodesField        | GPU nodes            |
| cpuProbabilityField  | CPU task probability |

**Other Components**

| fx:id               | Description          |
| ------------------- | -------------------- |
| speedSlider         | Speed slider         |
| visualizationCanvas | Visualization canvas |

---

## Best Practices for Modifying UI

1. Keep Controller bindings:

    * Ensure all components needing program access have fx:id
    * Ensure all buttons have event methods bound
    * fx:controller points to correct Controller class:
      `fx:controller="com.simulation.controller.SimulationController"`
2. Use CSS instead of inline styles for easier maintenance
3. Maintain clear layout hierarchy:

    * VBox → vertical
    * HBox → horizontal
    * GridPane → grid
    * BorderPane → top, bottom, left, right, center
4. Set reasonable constraints:

    * `prefWidth/prefHeight`, `minWidth/minHeight`, `maxWidth/maxHeight`
    * `HBox.hgrow` / `VBox.vgrow` → growth priority

---

## Common Scene Builder Issues

**Issue 1: Preview shows blank**

* Solution: Click **Preview → Show Preview in Window**, check FXML syntax, check Scene Builder console

**Issue 2: App fails after saving**

* Solution: Ensure fx:id exists in Controller, onAction methods implemented, FXML namespace correct

**Issue 3: Custom CSS not applied**

* Solution: Ensure `stylesheets="@styles.css"` exists, CSS in resources, add in Scene Builder Preview → Scene Style Sheets

---

## Integrate Scene Builder with IntelliJ IDEA

* Open Settings/Preferences → search "JavaFX" → set path to Scene Builder executable

    * Example: `C:\Program Files\SceneBuilder\SceneBuilder.exe`
* Right-click FXML → Open in Scene Builder → edit directly

---

## Recommended UI Design Workflow

1. Design layout in Scene Builder

    * Drag & drop components
    * Adjust size and layout
    * Set basic properties
    * Set fx:id and event handlers
2. Implement logic in Controller

    * Declare fx:id fields
    * Implement event methods
3. Apply CSS in `styles.css`
4. Preview in Scene Builder
5. Run and test: `mvn javafx:run`

---

## Suggested UI Extensions

* Charts:

    * `LineChart` → performance curves
    * `BarChart` → comparison statistics
    * `PieChart` → task distribution
* Table views:

    * `TableView` → task list, historical records
* Menu bar:

    * `MenuBar` → File (export/import), View (theme/layout), Help
* Dialogs:

    * Configuration editing, detailed statistics, history viewer

---

## Reference Resources

* Scene Builder official: [https://docs.gluonhq.com/scenebuilder/](https://docs.gluonhq.com/scenebuilder/)
* JavaFX CSS reference: [https://openjfx.io/javadoc/20/javafx.graphics/javafx/scene/doc-files/cssref.html](https://openjfx.io/javadoc/20/javafx.graphics/javafx/scene/doc-files/cssref.html)
* JavaFX Controls: [https://openjfx.io/javadoc/20/](https://openjfx.io/javadoc/20/)

**Tip:** After modifying FXML, save and rerun app. If UI not updating, run `mvn clean compile`.
