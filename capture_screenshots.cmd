@echo off
chcp 65001 >nul
echo ==========================================
echo WFODays 截图工具
echo ==========================================
echo.
echo 提示：按回车键截图，按其他键跳过
echo.

REM 创建截图目录
if not exist screenshots mkdir screenshots

set /p capture1="1. 引导页 (Onboarding) - 按回车截图，其他键跳过: "
if "%capture1%"=="" (
    echo 正在截取引导页...
    adb shell screencap -p /sdcard/screen_onboarding.png
    adb pull /sdcard/screen_onboarding.png screenshots/onboarding.png
    echo 引导页截图已保存到 screenshots/onboarding.png
) else (
    echo 已跳过引导页截图
)
echo.

set /p capture2="2. 首页 (Home) - 按回车截图，其他键跳过: "
if "%capture2%"=="" (
    echo 正在截取首页...
    adb shell screencap -p /sdcard/screen_home.png
    adb pull /sdcard/screen_home.png screenshots/home.png
    echo 首页截图已保存到 screenshots/home.png
) else (
    echo 已跳过首页截图
)
echo.

set /p capture3="3. 日历 (Calendar) - 按回车截图，其他键跳过: "
if "%capture3%"=="" (
    echo 正在截取日历...
    adb shell screencap -p /sdcard/screen_calendar.png
    adb pull /sdcard/screen_calendar.png screenshots/calendar.png
    echo 日历截图已保存到 screenshots/calendar.png
) else (
    echo 已跳过日历截图
)
echo.

set /p capture4="4. 统计 (Stats) - 按回车截图，其他键跳过: "
if "%capture4%"=="" (
    echo 正在截取统计...
    adb shell screencap -p /sdcard/screen_stats.png
    adb pull /sdcard/screen_stats.png screenshots/stats.png
    echo 统计截图已保存到 screenshots/stats.png
) else (
    echo 已跳过统计截图
)
echo.

set /p capture5="5. 设置 (Settings) - 按回车截图，其他键跳过: "
if "%capture5%"=="" (
    echo 正在截取设置...
    adb shell screencap -p /sdcard/screen_settings.png
    adb pull /sdcard/screen_settings.png screenshots/settings.png
    echo 设置截图已保存到 screenshots/settings.png
) else (
    echo 已跳过设置截图
)
echo.

echo ==========================================
echo 截图任务完成！
echo 截图保存在 screenshots/ 目录
echo ==========================================
pause
