# 🌤️ Android天气查询应用

一个基于Kotlin开发的Android天气查询应用，使用OkHttp进行网络请求，实现单城市单天天气信息展示。

## 📱 应用截图

![应用界面](https://via.placeholder.com/300x600/E3F2FD/1976D2?text=Weather+App)

## ✨ 主要功能

- 🔍 **城市天气查询**: 支持输入城市名称查询实时天气
- 🌡️ **温度显示**: 显示当前温度、体感温度、温度范围
- 🌈 **天气状况**: 展示天气状况描述和图标
- 💧 **详细信息**: 显示湿度、最高最低温度等信息
- 🌐 **网络检测**: 自动检测网络连接状态
- ⚡ **异步处理**: 使用协程处理网络请求，保证UI流畅
- 🎨 **Material Design**: 现代化的界面设计

## 🛠️ 技术栈

- **开发语言**: Kotlin
- **网络请求**: OkHttp3 + Retrofit
- **JSON解析**: Gson
- **异步处理**: Kotlin Coroutines
- **UI框架**: Android原生View + Material Design
- **架构模式**: MVP (Model-View-Presenter)

## 📦 项目结构

```
app/
├── src/main/java/com/example/weatherapp/
│   ├── MainActivity.kt           # 主界面Activity
│   ├── WeatherService.kt        # 天气API服务类
│   ├── model/
│   │   └── WeatherData.kt       # 数据模型类
│   └── utils/
│       └── NetworkUtil.kt       # 网络工具类
├── res/
│   ├── layout/
│   │   └── activity_main.xml    # 主界面布局
│   ├── values/
│   │   ├── strings.xml          # 字符串资源
│   │   └── colors.xml           # 颜色资源
│   └── drawable/
│       └── weather_background.xml # 背景资源
└── AndroidManifest.xml          # 应用清单文件
```

## 🚀 快速开始

### 前置要求

- Android Studio Arctic Fox 或更高版本
- Android SDK API 21 (Android 5.0) 或更高版本
- Kotlin 1.8.0 或更高版本

### 安装步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/yourusername/weather-app.git
   cd weather-app
   ```

2. **获取API密钥**
   - 访问 [OpenWeatherMap](https://openweathermap.org/api)
   - 注册账号并获取免费API密钥
   - 在 `WeatherService.kt` 中替换 `YOUR_API_KEY_HERE`

3. **导入项目**
   - 在Android Studio中选择 "Open an existing project"
   -
