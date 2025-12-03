# Scene Builder 使用指南 (Scene Builder Guide)

## 什么是Scene Builder？

Scene Builder是一个可视化的JavaFX UI设计工具，可以通过拖放方式设计用户界面，并生成FXML文件。

## 下载和安装

### 下载地址：
https://gluonhq.com/products/scene-builder/

选择适合你操作系统的版本下载并安装。

## 在Scene Builder中打开项目的FXML文件

1. 启动Scene Builder
2. 点击 "File" → "Open"
3. 导航到项目目录：
   ```
   C:\Users\cheny\IdeaProjects\OOP-Server-Simulation\src\main\resources\
   ```
4. 打开 `simulation_view.fxml`

## FXML文件结构说明

我们的 `simulation_view.fxml` 使用以下布局结构：

```
BorderPane (根容器)
├── Top: VBox (标题和控制面板)
│   ├── 标题Label
│   └── 控制按钮HBox (启动、暂停、停止等)
├── Center: VBox (可视化Canvas)
│   └── Canvas (绘制仿真动画)
├── Right: VBox (统计面板)
│   └── ScrollPane
│       └── 多个TitledPane (总体统计、队列统计、用户统计等)
└── Bottom: VBox (配置参数面板)
    ├── 配置按钮HBox (保存、加载、查看历史)
    └── GridPane (参数输入框)
```

## 如何在Scene Builder中修改UI

### 1. 修改组件属性

**选中一个组件后，右侧面板显示其属性：**
- **Properties（属性）**：文本内容、尺寸、对齐方式等
- **Layout（布局）**：边距、间距、约束等
- **Code（代码）**：fx:id、事件处理器等

**示例：修改按钮文本**
1. 选中"启动"按钮
2. 在Properties面板找到"Text"
3. 修改为"开始"或其他文本

### 2. 添加新组件

**从左侧面板拖放组件：**
- **Containers（容器）**：VBox, HBox, GridPane, BorderPane等
- **Controls（控件）**：Button, Label, TextField, ComboBox等
- **Shapes（图形）**：Rectangle, Circle, Line等

**示例：添加一个新按钮**
1. 从左侧"Controls"中拖动"Button"
2. 放到目标容器中
3. 设置fx:id和onAction属性

### 3. 设置事件处理器

**为按钮添加点击事件：**
1. 选中按钮
2. 在Code面板找到"On Action"
3. 输入方法名，如：`handleStart`
4. 这个方法必须在Controller中定义（带@FXML注解）

```java
@FXML
private void handleStart() {
    // 处理启动事件
}
```

### 4. 设置fx:id

**让Controller可以访问UI组件：**
1. 选中组件
2. 在Code面板找到"fx:id"
3. 输入ID，如：`startButton`
4. 在Controller中声明对应的字段：

```java
@FXML
private Button startButton;
```

### 5. 应用CSS样式

**方法1：使用样式类**
1. 选中组件
2. 在Properties面板找到"Style Class"
3. 添加类名，如：`control-button`
4. 在 `styles.css` 中定义样式

**方法2：内联样式**
1. 选中组件
2. 在Properties面板找到"Style"
3. 直接输入CSS，如：`-fx-background-color: blue;`

## 项目中的重要组件及其fx:id

### 控制按钮
- `startButton` - 启动按钮
- `pauseButton` - 暂停按钮
- `resumeButton` - 继续按钮
- `stopButton` - 停止按钮
- `resetButton` - 重置按钮

### 显示标签
- `timeLabel` - 仿真时间
- `speedLabel` - 速度倍率
- `arrivedTasksLabel` - 已到达任务数
- `completedTasksLabel` - 已完成任务数
- `avgSystemTimeLabel` - 平均系统时间
- `throughputLabel` - 吞吐量
- `dbStatusLabel` - 数据库状态

### 输入字段
- `arrivalIntervalField` - 到达间隔
- `simulationTimeField` - 仿真时间
- `cpuNodesField` - CPU节点数
- `gpuNodesField` - GPU节点数
- `cpuProbabilityField` - CPU任务概率

### 其他组件
- `speedSlider` - 速度滑块
- `visualizationCanvas` - 可视化画布

## 修改UI的最佳实践

### 1. 保持Controller绑定

修改UI后，确保：
- 所有需要程序访问的组件都有fx:id
- 所有按钮事件都绑定到Controller方法
- fx:controller属性指向正确的Controller类

```xml
fx:controller="com.simulation.controller.SimulationController"
```

### 2. 使用CSS而非内联样式

推荐在 `styles.css` 中定义样式，而不是在FXML中使用内联样式。这样便于维护和主题切换。

### 3. 保持布局层次清晰

使用合适的容器：
- **VBox**：垂直排列
- **HBox**：水平排列
- **GridPane**：网格布局
- **BorderPane**：五区域布局（top, bottom, left, right, center）

### 4. 设置合理的约束

为组件设置合适的：
- `prefWidth` / `prefHeight`：首选尺寸
- `minWidth` / `minHeight`：最小尺寸
- `maxWidth` / `maxHeight`：最大尺寸
- `HBox.hgrow` / `VBox.vgrow`：增长优先级

## 常见Scene Builder问题

### 问题1：预览显示空白

**解决方案：**
- 点击"Preview" → "Show Preview in Window"
- 检查FXML语法是否正确
- 查看Scene Builder控制台是否有错误

### 问题2：保存后应用无法运行

**解决方案：**
- 确保所有fx:id在Controller中都有对应声明
- 确保所有onAction方法在Controller中都已实现
- 检查FXML文件中的namespace是否正确

### 问题3：自定义CSS不生效

**解决方案：**
1. 确认stylesheets属性正确：
```xml
stylesheets="@styles.css"
```
2. CSS文件必须在resources目录下
3. 在Scene Builder中点击"Preview" → "Scene Style Sheets" → 添加CSS文件

## 在IntelliJ IDEA中集成Scene Builder

### 设置方法：
1. 打开 Settings/Preferences
2. 搜索 "JavaFX"
3. 在 "Path to SceneBuilder" 中设置Scene Builder可执行文件路径
   - Windows: `C:\Program Files\SceneBuilder\SceneBuilder.exe`
   - 或者你安装的位置

### 使用方法：
1. 在项目中右键点击FXML文件
2. 选择 "Open in Scene Builder"
3. 直接在IDE中编辑UI

## 推荐的UI设计流程

1. **在Scene Builder中设计布局**
   - 拖放组件
   - 调整布局和尺寸
   - 设置基本属性

2. **设置fx:id和事件处理器**
   - 为需要程序访问的组件设置fx:id
   - 为交互组件设置onAction等事件

3. **在Controller中实现逻辑**
   - 声明所有fx:id对应的字段
   - 实现所有事件处理方法

4. **应用CSS美化**
   - 在styles.css中定义样式
   - 在Scene Builder中预览效果

5. **运行测试**
   - 使用 `mvn javafx:run` 运行应用
   - 测试所有交互功能

## 扩展UI功能建议

### 可以添加的新组件：

1. **图表显示**
   - 使用 LineChart 显示性能曲线
   - 使用 BarChart 显示统计对比
   - 使用 PieChart 显示任务分布

2. **表格视图**
   - 使用 TableView 显示任务列表
   - 显示历史运行记录

3. **菜单栏**
   - 添加 MenuBar 提供更多功能
   - 文件菜单：导出报告、导入配置
   - 视图菜单：切换主题、调整布局
   - 帮助菜单：关于、使用说明

4. **对话框**
   - 配置编辑对话框
   - 详细统计对话框
   - 历史记录查看器

## 参考资源

- **Scene Builder官方文档**：https://docs.gluonhq.com/scenebuilder/
- **JavaFX CSS参考**：https://openjfx.io/javadoc/20/javafx.graphics/javafx/scene/doc-files/cssref.html
- **JavaFX控件文档**：https://openjfx.io/javadoc/20/

---

**提示：** 每次在Scene Builder中修改FXML后，记得保存文件，然后重新运行应用查看效果。如果UI没有更新，尝试使用 `mvn clean compile` 清理重新编译。

