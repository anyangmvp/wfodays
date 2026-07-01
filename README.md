# WFODays - 办公位置记录助手

<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="120" height="120" alt="WFODays Logo">
</p>

<p align="center">
  <b>智能记录您的办公位置，轻松管理 WFO/WFH 出勤</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Android-26%2B-brightgreen.svg" alt="Android 26+">
  <img src="https://img.shields.io/badge/Kotlin-2.0-blue.svg" alt="Kotlin 2.0">
  <img src="https://img.shields.io/badge/Jetpack%20Compose-2.0-purple.svg" alt="Jetpack Compose">
  <img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License: MIT">
</p>

---

## ✨ 功能特性

### 📍 智能位置检测
- **自动检测**：进入公司范围自动记录 WFO，超出范围自动记录 WFH
- **原生 GPS**：使用 Android 原生 LocationManager，无需 Google Play 服务
- **后台定位**：通过 WorkManager 在工作日自动执行位置检测（10:30 / 16:00）
- **可配距离**：办公室半径可在设置页面动态调整（默认 800 米）

### 📝 灵活记录方式
- **自动记录**：基于位置自动判断 WFO/WFH
- **手动记录**：点击今日状态卡片弹出面板，可手动选择 WFO/WFH/请假
- **日历补录**：支持在日历页面为任意日期手动补录

### 📊 出勤统计
- **实时统计**：首页仪表盘展示本月 WFO/WFH/休假天数
- **月度合规环图**：多段环形图直观展示办公室出勤比例
- **目标追踪**：可在设置中自定义 WFO 月度目标百分比（默认 30%），自动计算剩余需到岗天数
- **洞察提示**：根据当前进度智能提示是否达标
- **趋势图表**：查看历史月份出勤趋势
- **日历视图**：按月查看每日出勤记录

### 🔔 智能提醒
- **每日检测**：工作日上午和下午各自动检测一次并推送通知
- **测试通知**：设置页面可手动触发位置检测测试，显示距离和状态

---

## 🚀 快速开始

### 系统要求
- Android 8.0 (API 26) 或更高版本
- 位置权限（精确定位）
- 后台位置权限（可选，用于自动检测）
- 通知权限（Android 13+，用于推送提醒）

### 安装

#### 方式一：直接安装 APK
从 [Releases](../../releases) 页面下载最新版本的 APK 文件安装。

#### 方式二：自行编译
```bash
# 克隆仓库
git clone https://github.com/anyangmvp/wfodays.git
cd wfodays

# 编译 Debug 版本
./gradlew :app:assembleDebug

# 编译 Release 版本
./gradlew :app:assembleRelease
```

### 首次使用
1. 打开应用，通过引导页完成初始设置
2. 授权位置权限（精确定位）
3. 应用会自动根据当前位置检测并记录今日状态
4. 点击首页今日状态卡片可手动切换 WFO/WFH/请假
5. 在设置页面可调整 WFO 目标和办公室半径

---

## 📱 应用截图

<p align="center">
  <img src="screenshots/onboarding.png" width="200" alt="引导页">
  <img src="screenshots/home.png" width="200" alt="首页">
  <img src="screenshots/calendar.png" width="200" alt="日历">
  <img src="screenshots/stats.png" width="200" alt="统计">
  <img src="screenshots/settings.png" width="200" alt="设置">
</p>

---

## 🛠 技术栈

- **语言**: Kotlin 2.0
- **UI 框架**: Jetpack Compose 2.0
- **架构**: MVVM + Repository 模式
- **本地存储**: Room 数据库 + DataStore Preferences
- **后台任务**: WorkManager
- **位置服务**: Android 原生 LocationManager

---

## 📁 项目结构

```
app/src/main/java/me/anyang/wfodays/
├── data/
│   ├── database/          # Room 数据库 (AttendanceDao, AppDatabase)
│   ├── entity/            # 数据实体 (AttendanceRecord)
│   ├── local/             # PreferencesManager (DataStore)
│   └── repository/        # 数据仓库 (AttendanceRepository)
├── location/              # 位置服务模块
│   ├── NativeLocationManager.kt      # 原生 GPS 定位管理
│   ├── DailyCheckScheduler.kt        # 每日检测调度器
│   ├── DailyLocationCheckWorker.kt   # WorkManager Worker
│   ├── GeofenceManager.kt            # 地理围栏管理
│   └── LocationBasedAttendanceRecorder.kt  # 基于位置的考勤记录器
├── notification/          # 通知服务 (NotificationHelper)
├── ui/
│   ├── components/        # 可复用组件 (MultiSegmentDonutChart 等)
│   ├── screens/           # 页面 (Home, Stats, Calendar, Location, Settings, Onboarding)
│   ├── theme/             # 主题配色
│   └── viewmodel/         # ViewModel (HomeViewModel)
└── utils/                 # 工具类 (Constants, LanguageManager)
```

---

## ⚙️ 配置说明

### 办公室位置
办公室坐标在 `Constants.kt` 中定义，当前默认为「GLP I-PARK XI,AN」：
```kotlin
const val OFFICE_LATITUDE = 34.211892
const val OFFICE_LONGITUDE = 108.834240
```

### 可动态配置项
以下配置可在应用内 **设置页面** 直接调整，无需修改代码：

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| WFO 目标百分比 | 30% | 月度办公室出勤目标，范围 10%-100% |
| 办公室半径 | 800m | 判定 WFO 的距离阈值 |

### 工作时间与检测时段
- 工作时间：9:00 - 18:30
- 自动检测：工作日 10:30 和 16:00 各执行一次
- 调试模式可在设置中开启，按自定义间隔（默认 10 分钟）执行检测

---

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

---

## 📄 开源协议

本项目基于 [MIT](LICENSE) 协议开源。

---

## 👨‍💻 作者

**Stephen Y An**

---

## 🙏 致谢

- [Jetpack Compose](https://developer.android.com/jetpack/compose) - 现代 Android UI 工具包
- [Room](https://developer.android.com/training/data-storage/room) - 本地数据库
- [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) - 后台任务调度
- [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) - 键值对存储

---

<p align="center">
  <b>⭐ 如果这个项目对你有帮助，请给它一个 Star！</b>
</p>
