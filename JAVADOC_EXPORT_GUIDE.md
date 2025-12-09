# JavaDoc 导出到 Word 文档指南

## 📋 概述

本指南说明如何将项目的 JavaDoc 注释导出到 Word 文档中，用于课程报告。

---

## ✅ 第一步：代码中已添加 JavaDoc 注释

所有主要类已经添加了完整的 JavaDoc 注释：
- ✅ SimulationEngine
- ✅ Task
- ✅ Event
- ✅ EventList
- ✅ Clock
- ✅ SimulationConfig
- ✅ SimulationResults
- ✅ ServicePoint
- ✅ EventType, TaskType, UserType (枚举)
- ✅ RandomGenerator
- ✅ SimulatorApp

---

## 🔧 第二步：生成 HTML JavaDoc

### 方法 1：使用 Maven（推荐）

1. **添加 JavaDoc 插件到 pom.xml**（如果还没有）：

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-javadoc-plugin</artifactId>
    <version>3.6.0</version>
    <configuration>
        <source>17</source>
        <target>17</target>
        <encoding>UTF-8</encoding>
        <doclint>none</doclint>
    </configuration>
</plugin>
```

2. **生成 JavaDoc HTML**：

```bash
cd /Users/liulu/Desktop/Oop/OOP-Server-Simulation
mvn javadoc:javadoc
```

3. **JavaDoc 将生成在**：
   ```
   target/site/apidocs/index.html
   ```

### 方法 2：使用 IDE（IntelliJ IDEA / Eclipse）

**IntelliJ IDEA:**
1. 右键点击项目根目录
2. 选择 `Generate JavaDoc...`
3. 设置输出目录（例如：`target/javadoc`）
4. 点击 `OK`

**Eclipse:**
1. 右键点击项目
2. 选择 `Export` → `Java` → `Javadoc`
3. 选择要导出的包
4. 设置输出目录
5. 点击 `Finish`

---

## 📄 第三步：将 HTML 转换为 Word

### 方法 1：直接复制粘贴（最简单）

1. **打开生成的 HTML 文件**：
   ```bash
   open target/site/apidocs/index.html
   ```

2. **在浏览器中查看 JavaDoc**：
   - 浏览各个类和方法的文档
   - 选择需要的部分（Ctrl+A 全选，或选择特定部分）

3. **复制到 Word**：
   - 在浏览器中：`Ctrl+C` (Windows) 或 `Cmd+C` (Mac)
   - 打开 Word 文档
   - 粘贴：`Ctrl+V` (Windows) 或 `Cmd+V` (Mac)
   - Word 会自动保留格式（标题、代码块、列表等）

4. **调整格式**：
   - 可能需要调整字体和间距
   - 代码块可能需要设置为等宽字体（Courier New）

### 方法 2：使用 Pandoc 转换（高级）

如果你安装了 Pandoc：

```bash
# 安装 Pandoc（如果还没有）
# macOS: brew install pandoc
# Windows: 下载安装包

# 转换 HTML 到 Word
pandoc target/site/apidocs/index.html -o javadoc.docx
```

### 方法 3：使用在线转换工具

1. 访问在线 HTML 转 Word 工具（如：https://www.zamzar.com/convert/html-to-docx/）
2. 上传 `target/site/apidocs/index.html`
3. 下载转换后的 Word 文档

---

## 📝 第四步：整理 Word 文档

### 建议的文档结构：

```
1. 封面页
   - 项目名称
   - JavaDoc 文档
   - 日期

2. 目录
   - 自动生成（Word 的"引用" → "目录"）

3. 概述
   - 项目简介
   - JavaDoc 说明

4. 核心类文档
   - SimulationEngine
   - Task
   - Event
   - EventList
   - Clock
   - SimulationConfig
   - SimulationResults
   - ServicePoint

5. 枚举类型
   - EventType
   - TaskType
   - UserType

6. 工具类
   - RandomGenerator

7. 应用程序类
   - SimulatorApp
```

### 格式建议：

- **标题**：使用 Word 的标题样式（标题1、标题2等）
- **代码块**：使用等宽字体（Courier New, 10pt）
- **类名**：加粗
- **方法签名**：使用代码格式
- **参数说明**：使用表格或列表

---

## 🎯 快速导出步骤总结

```bash
# 1. 生成 JavaDoc HTML
mvn javadoc:javadoc

# 2. 打开 HTML 文件
open target/site/apidocs/index.html

# 3. 在浏览器中选择内容并复制到 Word
# 4. 调整格式并保存
```

---

## 💡 提示

1. **选择性导出**：不需要导出所有类，只选择核心类和重要方法
2. **截图补充**：可以添加代码截图或 UML 图
3. **交叉引用**：在 Word 中使用交叉引用链接相关类
4. **页码**：添加页码和页眉页脚
5. **样式统一**：保持整个文档的格式一致

---

## 📦 如果遇到问题

### 问题：JavaDoc 生成失败
- 检查 Java 版本（需要 Java 17+）
- 检查 pom.xml 配置
- 查看错误信息

### 问题：格式丢失
- 使用"保留源格式"粘贴选项
- 手动调整格式
- 使用"选择性粘贴" → "带格式文本"

### 问题：代码块格式不对
- 手动设置代码块为等宽字体
- 使用 Word 的"代码"样式（如果有）
- 或者使用文本框包含代码

---

## 📚 参考

- [Oracle JavaDoc 指南](https://www.oracle.com/technical-resources/articles/java/javadoc-tool.html)
- [Maven JavaDoc Plugin](https://maven.apache.org/plugins/maven-javadoc-plugin/)

