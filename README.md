# Cangjie DevEco Plugins

Cangjie language DevEco Studio plugin suite providing comprehensive IDE support for HarmonyOS application development.

## Overview

This project is Huawei's Cangjie language plugin collection for DevEco Studio (based on IntelliJ IDEA), offering the following core features:

- **Coding Assistance**: Code completion, syntax highlighting, find usages, rename, and more
- **Debug**: Debug Cangjie HarmonyOS applications
- **Project Management**: Create and manage Cangjie HarmonyOS example projects
- **Build**: Build Cangjie projects to HAP/APP
- **Code Tools**: Code formatting, static analysis

## Plugin Modules

| Module | Description |
|--------|-------------|
| `support` | Main plugin entry point, defines plugin metadata |
| `lsp-client` | Language Server Protocol client, provides code completion, semantic highlighting, goto definition, and other language features |
| `project-mgmt` | Project management module, handles Cangjie project sync, template rendering, package name configuration |
| `sdk-mgmt` | SDK management module, handles Cangjie SDK sync and build filtering |
| `dap-client` | Debug Adapter Protocol client, supports line breakpoints, function breakpoints, data breakpoints, and other debugging features |
| `profiler` | Profiling service module, provides performance analysis support |
| `tools-extension/cjlint` | Static code analysis tool |
| `tools-extension/cjformat` | Code formatting tool |
| `tools-extension/cangjie-test-framework` | Test framework module |

## Architecture

### Plugin Dependencies

```
support (Main Plugin)
├── lsp-client (Language Service)
├── project-mgmt (Project Management)
├── sdk-mgmt (SDK Management)
├── dap-client (Debugger)
├── profiler (Profiler)
├── cjformat (Formatter)
│   └── cjlint (Linter)
└── cangjie-test-framework (Test Framework)
```

### Supported File Types

- `.cj` - Cangjie source code files
- `.cj.d` - Cangjie declaration files
- `macrocall` - Cangjie macro call files

## Features

### Coding Assistance (lsp-client)

- Code Completion
- Syntax Highlighting
- Semantic Highlighting
- Code Folding
- Brace Matching
- Comment Handling
- Signature Help
- Type Hierarchy
- Call Hierarchy
- Find Usages
- Rename Refactoring
- Documentation Provider
- Live Templates

### Debugging (dap-client)

- Source Breakpoints
- Function Breakpoints
- Data Breakpoints (read/write access, value change)
- Instruction Breakpoints
- LLDB Debug Console

### Project Management (project-mgmt)

- Cangjie Project Template Rendering
- Module Synchronization
- DTS to Cangjie Conversion Sync
- Project Upgrade Checks
- Package Name Configuration
- Optimization Profile Generation

### SDK Management (sdk-mgmt)

- Cangjie SDK Synchronization
- Build Filters
- Build Environment Extensions

## Keyboard Shortcuts

| Feature | Shortcut |
|---------|----------|
| Reformat Code | Ctrl+Alt+L |
| Reformat File | Ctrl+Alt+Shift+L |
| Go to Implementation | Ctrl+Alt+B |
| Override Methods | Ctrl+O |
| Extract Variable | Ctrl+Alt+V |
| Extract Interface | Ctrl+Alt+I |
| Introduce Constant | Ctrl+Alt+C |
| Introduce Field | Ctrl+Alt+F |
| Introduce Parameter | Ctrl+Alt+P |

## Build Requirements

- JDK 17+
- IntelliJ IDEA 2024.3+ (since-build: 243)
- DevEco Studio corresponding version

## Development Build

```bash
# Build project
./gradlew build

# Run tests
./gradlew test

# Package plugin
./gradlew jar
```

## License

This project is open source under Apache-2.0 with Runtime Library Exception license.

See [LICENSE](LICENSE) file for details.

## Contributing

Please refer to the official Cangjie contributing guidelines.

## Contact

- Website: https://cangjie-lang.cn
- Support: support@huawei.com