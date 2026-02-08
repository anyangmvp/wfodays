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
- **自动检测**：进入公司 800 米范围自动记录 WFO
- **原生 GPS**：支持原生 GPS 定位，无需 Google Play 服务
- **后台定位**：即使 App 关闭也能检测到公司位置

### 📝 灵活记录方式
- **自动记录**：基于位置自动判断 WFO/WFH
- **手动记录**：支持任意日期手动补录 WFO/WFH/休假
- **长按切换**：长按今日状态卡片快速切换工作模式

### 📊 出勤统计
- **实时统计**：本月 WFO/WFH/休假天数实时统计
- **目标追踪**：自动计算 60% WFO 达标进度
- **历史记录**：查看历史月份出勤统计

### 🔔 智能提醒
- **每日提醒**：上午自动检测并记录位置
- **通知推送**：位置更新时推送通知提醒

---

## 🚀 快速开始

### 系统要求
- Android 8.0 (API 26) 或更高版本
- 位置权限（精确定位）
- 后台位置权限（可选，用于自动检测）

### 安装

#### 方式一：直接安装 APK
从 [Releases](../../releases) 页面下载最新版本的 APK 文件安装。

#### 方式二：自行编译
```bash
# 克隆仓库
git clone https://github.com/yourusername/wfodays.git
cd wfodays

# 编译 Debug 版本
./gradlew :app:assembleDebug

# 编译 Release 版本
./gradlew :app:assembleRelease
```

### 首次使用
1. 打开应用，授权位置权限
2. 应用会自动根据当前位置记录今日状态
3. 长按今日状态卡片可手动切换 WFO/WFH/休假

---

## 📱 应用截图

<p align="center">
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
- **依赖注入**: Hilt
- **本地存储**: Room 数据库
- **后台任务**: WorkManager
- **位置服务**: Android 原生 LocationManager

---

## 📁 项目结构

```
app/src/main/java/me/anyang/wfodays/
├── data/
│   ├── database/          # Room 数据库
│   ├── entity/            # 数据实体
│   ├── local/             # 本地数据源
│   └── repository/        # 数据仓库
├── location/              # 位置服务
│   ├── NativeLocationManager.kt
│   ├── DailyLocationCheckWorker.kt
│   └── GeofenceManager.kt
├── notification/          # 通知服务
├── ui/
│   ├── components/        # 可复用组件
│   ├── screens/           # 页面
│   ├── theme/             # 主题
│   └── viewmodel/         # ViewModel
└── utils/                 # 工具类
```

---

## ⚙️ 配置说明

### 公司位置配置
在 `NativeLocationManager.kt` 中修改公司坐标：

```kotlin
companion object {
    const val OFFICE_LATITUDE = 34.2098056   // 公司纬度
    const val OFFICE_LONGITUDE = 108.8379444 // 公司经度
    const val OFFICE_RADIUS_METERS = 800f    // 检测范围（米）
    const val OFFICE_NAME = "中软国际"        // 公司名称
}
```

### 通知时间配置
在 `DailyCheckScheduler.kt` 中修改每日检测时间：

```kotlin
// 设置为每天上午 10:30 执行
calendar.set(Calendar.HOUR_OF_DAY, 10)
calendar.set(Calendar.MINUTE, 30)
```

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
- [Hilt](https://dagger.dev/hilt/) - 依赖注入框架
- [Room](https://developer.android.com/training/data-storage/room) - 本地数据库
- [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) - 后台任务调度

---

<p align="center">
  <b>⭐ 如果这个项目对你有帮助，请给它一个 Star！</b>
</p>
