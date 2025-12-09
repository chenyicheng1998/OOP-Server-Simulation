# 📄 如何将 JavaDoc 导出到 Word 文档

## ✅ 第一步：代码中已添加 JavaDoc 注释

所有主要类已经添加了完整的 JavaDoc 注释，包括：
- ✅ SimulationEngine - 核心仿真引擎
- ✅ Task - 任务类
- ✅ Event - 事件类
- ✅ EventList - 事件列表
- ✅ Clock - 时钟类
- ✅ SimulationConfig - 配置类
- ✅ SimulationResults - 结果统计类
- ✅ ServicePoint - 服务点类
- ✅ EventType, TaskType, UserType - 枚举类
- ✅ RandomGenerator - 随机数生成器
- ✅ SimulatorApp - 应用程序主类

---

## 🔧 第二步：生成 HTML JavaDoc（已完成）

JavaDoc HTML 已经生成在：
```
target/site/apidocs/index.html
```

如果需要重新生成，运行：
```bash
mvn javadoc:javadoc
```

---

## 📝 第三步：导出到 Word 文档

### **方法 1：直接复制粘贴（最简单推荐）**

1. **打开生成的 JavaDoc HTML**：
   ```bash
   open target/site/apidocs/index.html
   ```
   或者在 Finder 中找到 `target/site/apidocs/index.html` 双击打开

2. **在浏览器中浏览 JavaDoc**：
   - 左侧是类列表
   - 右侧是详细文档
   - 点击类名查看详细信息

3. **复制内容到 Word**：
   - **方式 A - 复制整个页面**：
     - 在浏览器中按 `Cmd+A` (Mac) 或 `Ctrl+A` (Windows) 全选
     - 按 `Cmd+C` 复制
     - 打开 Word，按 `Cmd+V` 粘贴
     - Word 会自动保留格式（标题、代码块、列表等）
   
   - **方式 B - 选择性复制**：
     - 只选择需要的类和方法的文档
     - 逐个复制粘贴到 Word
     - 可以更好地控制内容

4. **调整 Word 格式**：
   - 代码块可能需要设置为等宽字体（Courier New, 10pt）
   - 标题使用 Word 的标题样式（标题1、标题2等）
   - 调整间距和缩进

---

### **方法 2：使用 Word 的"打开"功能**

1. **在 Word 中**：
   - 文件 → 打开
   - 选择 `target/site/apidocs/index.html`
   - Word 会自动转换 HTML 格式

2. **保存为 Word 文档**：
   - 文件 → 另存为
   - 选择 `.docx` 格式

---

### **方法 3：使用在线转换工具**

1. 访问在线 HTML 转 Word 工具：
   - https://www.zamzar.com/convert/html-to-docx/
   - https://convertio.co/html-docx/

2. 上传 `target/site/apidocs/index.html`

3. 下载转换后的 Word 文档

---

## 📋 建议的 Word 文档结构

### **封面页**
- 项目名称：Cloud Computing Service Queue Simulation
- 文档类型：JavaDoc API Documentation
- 日期、小组成员

### **目录**
- 使用 Word 的自动目录功能（引用 → 目录）

### **主要内容**

#### **1. 概述**
- 项目简介
- JavaDoc 说明

#### **2. 核心类文档**
按包组织：

**com.simulation.model 包：**
- SimulationEngine
- Task
- Event
- EventList
- Clock
- SimulationConfig
- SimulationResults
- ServicePoint

**枚举类型：**
- EventType
- TaskType
- UserType

**com.simulation.util 包：**
- RandomGenerator

**com.simulation 包：**
- SimulatorApp

**com.simulation.controller 包：**
- SimulationController

---

## 💡 格式建议

### **标题样式**
- **一级标题**：类名（如：`SimulationEngine`）
- **二级标题**：方法分类（如：构造方法、公共方法、私有方法）

### **代码格式**
- 使用等宽字体：`Courier New` 或 `Consolas`
- 字号：10pt
- 背景色：浅灰色（可选）

### **表格格式**
- 方法参数表格：参数名 | 类型 | 说明
- 返回值表格：返回值类型 | 说明

### **页面设置**
- 页边距：标准（2.54cm）
- 字体：正文使用宋体或 Times New Roman
- 行距：1.5倍

---

## 🎯 快速操作步骤

```bash
# 1. 生成 JavaDoc（如果还没有）
mvn javadoc:javadoc

# 2. 打开 HTML 文件
open target/site/apidocs/index.html

# 3. 在浏览器中选择内容并复制
# 4. 粘贴到 Word
# 5. 调整格式
# 6. 保存为 .docx
```

---

## ⚠️ 注意事项

1. **代码块格式**：JavaDoc 中的代码示例可能需要手动调整格式
2. **链接**：HTML 中的链接在 Word 中可能失效，需要手动处理
3. **图片**：如果有 UML 图或其他图片，需要单独插入
4. **完整性**：确保所有重要的类和方法都包含在文档中
5. **一致性**：保持整个文档的格式风格一致

---

## 📚 示例：如何组织 Word 文档

```
封面页
├── 项目名称
├── JavaDoc 文档
└── 日期、作者

目录（自动生成）

1. 概述
   └── 项目简介和 JavaDoc 说明

2. 核心类文档
   ├── 2.1 SimulationEngine
   │   ├── 类描述
   │   ├── 构造方法
   │   ├── 公共方法
   │   └── 内部接口
   ├── 2.2 Task
   │   ├── 类描述
   │   ├── 构造方法
   │   └── 方法列表
   └── ...

3. 枚举类型
   ├── EventType
   ├── TaskType
   └── UserType

4. 工具类
   └── RandomGenerator

5. 应用程序类
   └── SimulatorApp
```

---

## ✅ 完成检查清单

- [ ] JavaDoc HTML 已生成
- [ ] 已打开 HTML 文件查看
- [ ] 已复制内容到 Word
- [ ] 已调整格式（字体、间距、代码块）
- [ ] 已添加封面页和目录
- [ ] 已检查所有类是否包含
- [ ] 已保存为 .docx 格式

---

**提示**：如果 Word 文档太大，可以考虑只包含核心类的文档，或者分成多个文档。

