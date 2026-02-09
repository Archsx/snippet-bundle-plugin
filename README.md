# SnippetBundle

<div align="center">

<!-- 截图/GIF 位置 -->
![Demo](docs/example.png)

**A IntelliJ IDEA plugin for bundling project files into Markdown**

[License](LICENSE) | [GitHub](https://github.com/Archsx/snippet-bundle-plugin)

</div>

---

## 中文说明

### 使用场景

当你使用的 IDE 没有 AI 功能，或者不想安装 IDE 内的 AI 插件，但需要将代码片段发送给网页端的大模型（如 ChatGPT、Claude 等）时，这个插件可以帮你解决频繁复制粘贴或上传文件的繁琐问题。

通过简单的拖拽操作，就能把多个文件的内容整理成一个规范的 Markdown 文本，一键复制到网页端使用。

### 功能特点

- 拖拽文件到工具窗口，直观便捷
- 智能过滤构建目录和二进制文件
- 自动检测代码语言并生成语法高亮
- 文件大小限制保护（512KB），防止内容过大
- 树形结构显示，清晰展示文件层级

### 安装

1. 在 IntelliJ IDEA 中打开 `Settings` / `Preferences`
2. 进入 `Plugins` → `Marketplace`
3. 搜索 **SnippetBundle**
4. 点击 `Install` 安装

### 使用方法

1. 打开 SnippetBundle 工具窗口：`View` → `Tool Windows` → `SnippetBundle`
2. 从项目视图拖拽文件/文件夹到工具窗口
3. 点击 **Copy Files (Markdown)** 按钮复制到剪贴板

### 忽略规则

默认忽略以下目录和文件：
- `.git`, `.idea`, `.vscode`
- `node_modules`, `.gradle`, `target`, `build`
- `.DS_Store`, `*.iml`

---

## English

### Use Case

When your IDE doesn't have built-in AI features, or you prefer not to install AI plugins within your IDE, but you still need to send code snippets to web-based LLMs (like ChatGPT, Claude, etc.), this plugin solves the tedious problem of frequently copying/pasting or uploading files.

With simple drag-and-drop, you can organize multiple files into a well-formatted Markdown text and copy it to your browser with one click.

### Features

- Drag and drop files to tool window
- Smart filtering of build directories and binary files
- Automatic language detection for syntax highlighting
- File size limit protection (512KB)
- Tree structure display for clear file hierarchy

### Installation

1. Open `Settings` / `Preferences` in IntelliJ IDEA
2. Go to `Plugins` → `Marketplace`
3. Search **SnippetBundle**
4. Click `Install`

### Usage

1. Open SnippetBundle tool window: `View` → `Tool Windows` → `SnippetBundle`
2. Drag files/folders from project view to the tool window
3. Click **Copy Files (Markdown)** button to copy to clipboard

### Ignore Rules

Default ignores:
- `.git`, `.idea`, `.vscode`
- `node_modules`, `.gradle`, `target`, `build`
- `.DS_Store`, `*.iml`

---

## Contributing

欢迎提交 Issue 和 Pull Request！

Welcome to submit Issues and Pull Requests!

## License

[MIT License](LICENSE)
