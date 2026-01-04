# 项目结构速览

> 位置：`app/src/main/java/com/example/computer/`

## 根目录

- **MainActivity.kt** —— 应用入口，负责 NavigationSuite 的顶层导航。
- **ui.theme/**
    - **Color.kt / Theme.kt / Type.kt** —— 全局配色、主题与字体设定。

## app/navigation

- **AppDestination.kt** —— 描述顶层导航项（首页 / 学生 / 教师 / 家长）的数据结构。
- **StudentDestination.kt** —— 预留学生子导航的目的地定义（如需扩展内部导航时使用）。

## components

- （预留）通用 Compose 组件放置点，当前暂无实现。

## core

### core/config
- （预留）应用配置、常量或 BuildConfig 相关封装。

### core/designsystem
- （预留）可共享的设计体系实现，例如统一的组件样式或 spacing 约定。

### core/network
- **NetworkClient.kt** —— OkHttp 客户端及网络配置统一出口。

### core/ocr
- （规划中）OCR 能力封装，后续将承载 `recognizeTextFromImage` 等实现。

### core/stt
- （规划中）语音识别（STT）能力封装，计划迁移 `IatSttClient` 等类。

### core/util
- （预留）全局工具函数或扩展方法。

## data

### data/model
- **Assignment.kt** —— 作业数据模型。
- **ChildInfo.kt** —— 孩子信息模型（家长端）。
- **LearningData.kt** —— 学习仪表盘展示的数据结构。

### data/repository
- **AssignmentRepository.kt** —— 作业仓库接口定义。
- **InMemoryAssignmentRepository.kt** —— 作业仓库的内存实现（示例 / 临时数据源）。

## feature

### feature/common
- **domain/AiSuggestionUseCase.kt** —— AI 学习建议的用例封装。
- **presentation/LearningDashboard.kt** —— 学习仪表盘通用 UI 组件。

### feature/errorbook.presentation
- （规划中）错题本功能的独立 UI 模块，后续从学生端拆分迁移。

### feature/home.presentation
- （规划中）首页相关 UI 组件。

### feature/noteassistant.presentation
- （规划中）Note Assistant 功能界面与逻辑。

### feature/parent.presentation
- **model/ParentFeatureUiModel.kt** —— 家长功能菜单项的数据模型。
- **ParentScreen.kt** —— 家长端主界面。

### feature/student.presentation
- **model/StudentFeatureUiModel.kt** —— 学生功能菜单项的数据模型。
- **StudentViewModel.kt** —— 管理学生作业与提交状态。
- **StudentScreen.kt** —— 学生端主界面（含 Note Assistant、错题本入口等）。

### feature/teacher.presentation
- **model/TeacherFeatureUiModel.kt** —— 教师功能菜单项的数据模型。
- **TeacherViewModel.kt** —— 管理教师端作业发布状态。
- **TeacherScreen.kt** —— 教师端主界面。

### feature/screens
- **parent / student / teacher** —— 迁移前遗留的界面目录，准备逐步淘汰或清理。
- 