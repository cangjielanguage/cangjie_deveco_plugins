# Cangjie DevEco Plugins

仓颉语言 DevEco Studio 插件集，为鸿蒙应用开发提供全面的 IDE 支持。

## 项目概述

本项目是华为仓颉语言在 DevEco Studio（基于 IntelliJ IDEA）上的插件集合，提供以下核心功能：

- **编码辅助**：代码补全、语法高亮、查找引用、重命名等
- **调试**：仓颉鸿蒙应用调试支持
- **项目管理**：创建和管理仓颉鸿蒙应用示例项目
- **构建**：构建仓颉项目为 HAP/APP
- **代码工具**：代码格式化、静态检查

## 插件模块

| 模块 | 描述 |
|------|------|
| `support` | 主插件入口，定义插件基本信息 |
| `lsp-client` | 语言服务器协议客户端，提供代码补全、语义高亮、跳转定义等语言功能 |
| `project-mgmt` | 项目管理模块，处理仓颉项目同步、模板渲染、包名配置等 |
| `sdk-mgmt` | SDK 管理模块，处理仓颉 SDK 同步和构建过滤 |
| `dap-client` | 调试适配协议客户端，支持行断点、函数断点、数据断点等调试功能 |
| `profiler` | 调优服务模块，提供性能分析支持 |
| `tools-extension/cjlint` | 静态代码检查工具 |
| `tools-extension/cjformat` | 代码格式化工具 |
| `tools-extension/cangjie-test-framework` | 测试框架模块 |

## 技术架构

### 插件依赖关系

```
support (主插件)
├── lsp-client (语言服务)
├── project-mgmt (项目管理)
├── sdk-mgmt (SDK管理)
├── dap-client (调试)
├── profiler (调优)
├── cjformat (格式化)
│   └── cjlint (静态检查)
└── cangjie-test-framework (测试框架)
```

### 文件类型支持

- `.cj` - 仓颉源代码文件
- `.cj.d` - 仓颉声明文件
- `macrocall` - 仓颉宏调用文件

## 功能详情

### 编码辅助 (lsp-client)

- 代码补全（Completion Contributor）
- 语法高亮（Syntax Highlighting）
- 语义高亮（Semantic Highlighting）
- 代码折叠（Folding Builder）
- 括号匹配（Brace Matcher）
- 注释处理（Commenter）
- 签名帮助（Signature Help）
- 类型层级（Type Hierarchy）
- 调用层级（Call Hierarchy）
- 查找引用（Find Usages）
- 重命名重构（Rename Handler）
- 文档查看（Documentation Provider）
- 实时模板（Live Templates）

### 调试功能 (dap-client)

- 源码断点
- 函数断点
- 数据断点（读写访问/值变化）
- 指令断点
- LLDB 调试控制台

### 项目管理 (project-mgmt)

- 仓颉项目模板渲染
- 模块同步处理
- DTS 到仓颉的转换同步
- 项目升级检查
- 包名配置管理
- 优化配置文件生成

### SDK 管理 (sdk-mgmt)

- 仓颉 SDK 同步
- 构建过滤器
- 构建环境扩展

## 快捷键

| 功能 | 快捷键 |
|------|--------|
| 格式化代码 | Ctrl+Alt+L |
| 格式化文件 | Ctrl+Alt+Shift+L |
| 跳转实现 | Ctrl+Alt+B |
| 重写方法 | Ctrl+O |
| 提取变量 | Ctrl+Alt+V |
| 提取接口 | Ctrl+Alt+I |
| 引入常量 | Ctrl+Alt+C |
| 引入字段 | Ctrl+Alt+F |
| 引入参数 | Ctrl+Alt+P |

## 构建要求

- JDK 17+
- IntelliJ IDEA 2024.3+ (since-build: 243)
- DevEco Studio 配套版本

## 开发构建

```bash
# 构建项目
./gradlew build

# 运行测试
./gradlew test

# 打包插件
./gradlew jar
```

## 许可证

本项目基于 Apache-2.0 with Runtime Library Exception 许可证开源。

详见 [LICENSE](LICENSE) 文件。

## 参与贡献

请参阅仓颉官方贡献指南。

## 联系方式

- 官网：https://cangjie-lang.cn
- 技术支持：support@huawei.com