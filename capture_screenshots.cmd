@echo off
chcp 65001 >nul
echo ==========================================
echo WFODays 截图工具
echo ==========================================
echo.

REM 创建截图目录
if not exist screenshots mkdir screenshots

echo 请按顺序切换到以下界面，按任意键截图：
echo 1. 引导页 (Onboarding)
echo 2. 首页 (Home)
echo 3. 日历 (Calendar)
echo 4. 统计 (Stats)
echo 5. 设置 (Settings)
echo.

REM 截图1: 引导页
pause >nul
echo 正在截取引导页...
adb shell screencap -p /sdcard/screen_onboarding.png
adb pull /sdcard/screen_onboarding.png screenshots/onboarding.png
echo 引导页截图已保存到 screenshots/onboarding.png
echo.

REM 截图2: 首页
pause >nul
echo 正在截取首页...
adb shell screencap -p /sdcard/screen_home.png
adb pull /sdcard/screen_home.png screenshots/home.png
echo 首页截图已保存到 screenshots/home.png
echo.

REM 截图3: 日历
pause >nul
echo 正在截取日历...
adb shell screencap -p /sdcard/screen_calendar.png
adb pull /sdcard/screen_calendar.png screenshots/calendar.png
echo 日历截图已保存到 screenshots/calendar.png
echo.

REM 截图4: 统计
pause >nul
echo 正在截取统计...
adb shell screencap -p /sdcard/screen_stats.png
adb pull /sdcard/screen_stats.png screenshots/stats.png
echo 统计截图已保存到 screenshots/stats.png
echo.

REM 截图5: 设置
pause >nul
echo 正在截取设置...
adb shell screencap -p /sdcard/screen_settings.png
adb pull /sdcard/screen_settings.png screenshots/settings.png
echo 设置截图已保存到 screenshots/settings.png
echo.

echo ==========================================
echo 所有截图已完成！
echo 截图保存在 screenshots/ 目录
echo ==========================================
pause >nul
