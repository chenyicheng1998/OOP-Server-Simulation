# 云计算服务排队模拟系统

## 项目简介
这是一个基于Java和JavaFX的离散事件模拟系统，模拟云计算服务提供商的任务处理流程。

## 系统特性
- **三类用户**：普通用户、个人VIP、企业VIP（不同优先级）
- **两种任务类型**：CPU任务和GPU任务  
- **服务流程**：任务到达 → 数据存储 → 分类 → 排队(CPU/GPU) → 计算节点执行 → 结果存储 → 返回用户
- **MVC架构**：Model-View-Controller设计模式
- **可视化界面**：实时显示系统状态和统计数据
- **可配置参数**：到达率、服务时间、资源数量等

## 项目结构
```
src/main/java/com/simulation/
├── model/                      # 模型层
│   ├── EventType.java         # 事件类型枚举
│   ├── TaskType.java          # 任务类型枚举  
│   ├── UserType.java          # 用户类型枚举
│   ├── Task.java              # 任务类
│   ├── Event.java             # 事件类
│   ├── EventList.java         # 事件列表
│   ├── Clock.java             # 仿真时钟(单例)
│   ├── ServicePoint.java      # 服务点类
│   ├── SimulationConfig.java  # 配置类
│   ├── SimulationResults.java # 结果统计类
│   └── SimulationEngine.java  # 仿真引擎(线程)
├── controller/                 # 控制层
│   └── SimulationController.java
├── view/                       # 视图层
│   └── SimulationView.java
├── util/                       # 工具类
│   └── RandomGenerator.java
└── SimulatorApp.java          # 主程序入口
```

## 技术栈
- **Java 17+**
- **Maven 3.6+**
- **JavaFX 20.0.1** (Controls, FXML, Graphics)
- **MariaDB** + JDBC Driver
- **HikariCP** - 数据库连接池
- **Scene Builder** - FXML可视化设计工具

## 前置要求
1. **JDK 17** 或更高版本
2. **Maven 3.6+**
3. **MariaDB** 数据库服务器
4. **HeidiSQL** 或其他数据库管理工具（推荐）
5. **Scene Builder** (可选，用于UI设计)

## 快速开始

### 1. 设置数据库
```bash
# 在MariaDB中执行SQL脚本
mysql -u root -p < src/main/resources/database_schema.sql
```
或者使用HeidiSQL打开 `src/main/resources/database_schema.sql` 并执行。

详细步骤请参考：[DATABASE_SETUP.md](DATABASE_SETUP.md)

### 2. 配置数据库连接
编辑 `src/main/resources/database.properties`：
```properties
db.url=jdbc:mariadb://localhost:3306/cloud_simulation
db.username=root
db.password=你的密码
```

### 3. 编译和运行
```bash
# 清理并编译
mvn clean compile

# 运行应用
mvn javafx:run
```

## 配置参数（可在GUI中调整）
- 平均到达间隔：2.0秒（指数分布）
- CPU计算节点数：2个
- GPU计算节点数：1个
- 仿真时间：1000秒
- 任务类型分布：70% CPU, 30% GPU
- 用户类型分布：60%普通，30%个人VIP，10%企业VIP

## 主要功能

### 1. 可视化界面 (JavaFX + FXML)
- ✅ 实时仿真动画
- ✅ 动态统计信息显示
- ✅ 可调节的仿真速度（0.1x - 10x）
- ✅ 暂停/继续/单步执行
- ✅ 使用Scene Builder可视化设计

### 2. 数据库集成 (MariaDB)
- ✅ 保存和加载仿真配置
- ✅ 记录所有仿真运行结果
- ✅ 存储详细的任务数据
- ✅ 统计分析（按用户类型、任务类型、服务点）
- ✅ 历史记录查询
- ✅ HikariCP连接池管理

### 3. 仿真功能
- ✅ 三类用户优先级调度（企业VIP > 个人VIP > 普通用户）
- ✅ 两种任务类型（CPU/GPU）
- ✅ 多服务点流程（数据存储→分类→队列→计算→结果存储）
- ✅ 基于概率的到达过程和服务时间
- ✅ 实时性能指标计算

## 核心算法
### 离散事件仿真（Three-Phase Approach）
1. **A阶段**：时钟前进到下一个事件时间
2. **B阶段**：执行到期的事件（条件事件）
3. **C阶段**：检查并启动可以开始的活动（绑定事件）

### 优先级调度
- 企业VIP > 个人VIP > 普通用户
- 同等优先级按FIFO（先到先服务）

## 性能指标
- 平均系统时间
- 吞吐量（任务/秒）
- 队列长度统计
- 服务点利用率
- 按用户类型和任务类型的统计

## 使用Scene Builder编辑UI
详细说明请参考：[SCENE_BUILDER_GUIDE.md](SCENE_BUILDER_GUIDE.md)

1. 下载Scene Builder: https://gluonhq.com/products/scene-builder/
2. 打开FXML文件: `src/main/resources/simulation_view.fxml`
3. 可视化编辑界面布局
4. 所有修改会自动反映到应用中

## 文档生成
```bash
mvn javadoc:javadoc
```
生成的文档位于: `target/site/apidocs/`

## 数据库管理

### 查看仿真历史
```sql
SELECT * FROM simulation_runs ORDER BY start_time DESC LIMIT 10;
```

### 清空所有数据
```sql
DELETE FROM tasks;
DELETE FROM simulation_runs;
```

### 备份数据库
```bash
mysqldump -u root -p cloud_simulation > backup.sql
```

更多数据库操作请参考：[DATABASE_SETUP.md](DATABASE_SETUP.md)

## 项目结构说明

```
src/main/
├── java/com/simulation/
│   ├── SimulatorApp.java           # 应用入口（使用FXML加载）
│   ├── controller/
│   │   └── SimulationController.java  # FXML控制器（处理UI事件）
│   ├── model/                      # 业务逻辑层
│   ├── view/                       # 视图层（已废弃，使用FXML代替）
│   ├── database/                   # 数据库访问层
│   │   ├── DatabaseManager.java   # 连接池管理
│   │   ├── SimulationConfigDAO.java   # 配置DAO
│   │   └── SimulationResultsDAO.java  # 结果DAO
│   └── util/                       # 工具类
└── resources/
    ├── simulation_view.fxml        # UI布局文件（可用Scene Builder编辑）
    ├── styles.css                  # CSS样式
    ├── database.properties         # 数据库配置
    └── database_schema.sql         # 数据库建表脚本
```

## 扩展功能建议

1. **可视化增强**
   - 添加实时图表（JavaFX Charts）
   - 任务流动动画
   - 热力图显示队列状态

2. **统计分析**
   - 导出Excel报告
   - 多次运行对比分析
   - 参数敏感性分析

3. **高级功能**
   - 多场景仿真
   - 实时参数调整
   - 自动优化算法

## 常见问题

### Q1: 数据库连接失败
A: 检查MariaDB服务是否运行，配置文件是否正确，参考 [DATABASE_SETUP.md](DATABASE_SETUP.md)

### Q2: FXML加载失败
A: 确保 `simulation_view.fxml` 在resources目录下，Controller类路径正确

### Q3: Maven编译错误
A: 运行 `mvn clean install` 重新下载依赖

## 作者
Cloud Simulation Team - Metropolia UAS

## 许可证
Educational Use - Metropolia University of Applied Sciences

