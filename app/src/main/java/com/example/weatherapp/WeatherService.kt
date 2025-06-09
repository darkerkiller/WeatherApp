package com.example.weatherapp

import com.example.weatherapp.model.WeatherResponse
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class WeatherService {

    companion object {
        private const val BASE_URL = "https://api.openweathermap.org/data/2.5"
        // 使用自己的API Key
        private const val API_KEY = "8dbbda1cb4b53bba52a462c4b38c9dbf"

        // 中文城市名称转换
        private val cityNameMap = mapOf(
            "北京" to "Beijing",
            "上海" to "Shanghai",
            "广州" to "Guangzhou",
            "深圳" to "Shenzhen",
            "杭州" to "Hangzhou",
            "南京" to "Nanjing",
            "武汉" to "Wuhan",
            "成都" to "Chengdu",
            "重庆" to "Chongqing",
            "西安" to "Xi'an",
            "天津" to "Tianjin",
            "青岛" to "Qingdao",
            "大连" to "Dalian",
            "厦门" to "Xiamen",
            "苏州" to "Suzhou",
            "长沙" to "Changsha",
            "郑州" to "Zhengzhou",
            "济南" to "Jinan",
            "哈尔滨" to "Harbin",
            "沈阳" to "Shenyang",
            "长春" to "Changchun",
            "石家庄" to "Shijiazhuang",
            "太原" to "Taiyuan",
            "合肥" to "Hefei",
            "南昌" to "Nanchang",
            "福州" to "Fuzhou",
            "昆明" to "Kunming",
            "贵阳" to "Guiyang",
            "兰州" to "Lanzhou",
            "银川" to "Yinchuan",
            "西宁" to "Xining",
            "乌鲁木齐" to "Urumqi",
            "拉萨" to "Lhasa",
            "呼和浩特" to "Hohhot",
            "南宁" to "Nanning",
            "海口" to "Haikou",
            "三亚" to "Sanya"
        )
    }

    private val gson = Gson()

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .retryOnConnectionFailure(true)
        .build()

    sealed class WeatherResult {
        data class Success(val data: WeatherResponse) : WeatherResult()
        data class Error(val message: String) : WeatherResult()
        object Loading : WeatherResult()
    }

    suspend fun getCurrentWeather(cityName: String): WeatherResult {
        return withContext(Dispatchers.IO) {
            try {
                val processedCityName = processCityName(cityName)

                val encodedCityName = URLEncoder.encode(processedCityName, "UTF-8")
                val url = "$BASE_URL/weather?q=$encodedCityName&appid=$API_KEY&units=metric"

                android.util.Log.d("WeatherService", "请求URL: $url")
                android.util.Log.d("WeatherService", "处理后的城市名: $processedCityName")

                val request = Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("User-Agent", "WeatherApp/1.0")
                    .build()

                val response = client.newCall(request).execute()

                response.use { resp ->
                    val responseBody = resp.body?.string() ?: ""
                    android.util.Log.d("WeatherService", "响应代码: ${resp.code}")
                    android.util.Log.d("WeatherService", "响应内容: $responseBody")

                    if (resp.isSuccessful) {
                        if (responseBody.isEmpty()) {
                            WeatherResult.Error("服务器返回空数据")
                        } else {
                            try {
                                val weatherResponse = gson.fromJson(responseBody, WeatherResponse::class.java)
                                WeatherResult.Success(weatherResponse)
                            } catch (e: JsonSyntaxException) {
                                android.util.Log.e("WeatherService", "JSON解析错误", e)
                                WeatherResult.Error("数据解析失败: ${e.message}")
                            }
                        }
                    } else {
                        val errorMessage = try {
                            val errorResponse = gson.fromJson(responseBody, ErrorResponse::class.java)
                            when {
                                errorResponse.message.contains("city not found", ignoreCase = true) -> {
                                    "找不到城市「$cityName」，请检查城市名称是否正确"
                                }
                                else -> errorResponse.message
                            }
                        } catch (e: Exception) {
                            when (resp.code) {
                                401 -> "API密钥无效，请检查配置"
                                404 -> "找不到城市「$cityName」，请尝试输入英文城市名或检查拼写"
                                429 -> "请求过于频繁，请稍后重试"
                                500, 502, 503 -> "服务器暂时不可用，请稍后重试"
                                else -> "网络请求失败 (${resp.code}): ${resp.message}"
                            }
                        }

                        WeatherResult.Error(errorMessage)
                    }
                }
            } catch (e: IOException) {
                android.util.Log.e("WeatherService", "网络错误", e)
                WeatherResult.Error("网络连接失败，请检查网络连接")
            } catch (e: Exception) {
                android.util.Log.e("WeatherService", "请求错误", e)
                WeatherResult.Error("请求过程中发生错误: ${e.message}")
            }
        }
    }
    private fun processCityName(cityName: String): String {
        val trimmedName = cityName.trim()
        return cityNameMap[trimmedName] ?: run {
            if (containsChinese(trimmedName)) {
                throw IllegalArgumentException("暂不支持城市「$trimmedName」，请尝试输入英文城市名")
            } else {
                trimmedName
            }
        }
    }
    private fun containsChinese(str: String): Boolean {
        return str.any { char ->
            char.code in 0x4E00..0x9FFF
        }
    }
    data class ErrorResponse(
        val cod: String = "",
        val message: String = ""
    )
}