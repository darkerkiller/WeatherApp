package com.example.weatherapp

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.weatherapp.model.WeatherDisplayData
import com.example.weatherapp.utils.NetworkUtil
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var etCityName: EditText
    private lateinit var btnSearch: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var weatherCard: MaterialCardView
    private lateinit var tvCityName: TextView
    private lateinit var tvTemperature: TextView
    private lateinit var tvWeatherCondition: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvHumidity: TextView
    private lateinit var tvTempRange: TextView
    private lateinit var tvError: TextView
    private lateinit var tvCityTips: TextView

    private val weatherService = WeatherService()

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_main)

            initViews()
            setupListeners()

            // 显示输入
            showCityInputTips()

            // 默认北京
            etCityName.setText("北京")
            searchWeather()

        } catch (e: Exception) {
            handleInitializationError(e)
        }
    }

    private fun initViews() {
        try {
            etCityName = findViewById(R.id.etCityName)
            btnSearch = findViewById(R.id.btnSearch)
            progressBar = findViewById(R.id.progressBar)
            weatherCard = findViewById(R.id.weatherCard)
            tvCityName = findViewById(R.id.tvCityName)
            tvTemperature = findViewById(R.id.tvTemperature)
            tvWeatherCondition = findViewById(R.id.tvWeatherCondition)
            tvDescription = findViewById(R.id.tvDescription)
            tvHumidity = findViewById(R.id.tvHumidity)
            tvTempRange = findViewById(R.id.tvTempRange)
            tvError = findViewById(R.id.tvError)

            tvCityTips = try {
                findViewById(R.id.tvCityTips)
            } catch (e: Exception) {
                TextView(this)
            }

        } catch (e: Exception) {
            throw IllegalStateException("视图初始化失败: ${e.message}", e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setupListeners() {
        btnSearch.setOnClickListener {
            hideKeyboard()
            searchWeather()
        }

        etCityName.setOnEditorActionListener { _, _, _ ->
            hideKeyboard()
            searchWeather()
            true
        }
    }

    private fun showCityInputTips() {
        val tips = """
            支持的中文城市：北京、上海、广州、深圳、杭州、南京、武汉、成都等
            也可以输入英文城市名：Beijing、Shanghai、New York、London等
        """.trimIndent()
        
        if (tvCityTips.parent != null) {
            tvCityTips.text = tips
            tvCityTips.visibility = View.VISIBLE
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun searchWeather() {
        val cityName = etCityName.text.toString().trim()

        if (cityName.isEmpty()) {
            showError("请输入城市名称")
            return
        }

        if (!NetworkUtil.isNetworkAvailable(this)) {
            showError("网络连接不可用，请检查网络设置")
            return
        }

        lifecycleScope.launch {
            showLoading(true)

            try {
                when (val result = weatherService.getCurrentWeather(cityName)) {
                    is WeatherService.WeatherResult.Success -> {
                        val displayData = convertToDisplayData(result.data)
                        showWeatherData(displayData)
                    }
                    is WeatherService.WeatherResult.Error -> {
                        showError(result.message)
                    }
                    is WeatherService.WeatherResult.Loading -> {
                    }
                }
            } catch (e: IllegalArgumentException) {
                showError(e.message ?: "城市名称不支持")
            } catch (e: Exception) {
                showError("查询过程中发生错误: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun convertToDisplayData(weatherResponse: com.example.weatherapp.model.WeatherResponse): WeatherDisplayData {
        val weather = weatherResponse.weather.firstOrNull()

        // 中英文
        val weatherConditionMap = mapOf(
            "Clear" to "晴天",
            "Clouds" to "多云",
            "Rain" to "雨天",
            "Drizzle" to "毛毛雨",
            "Thunderstorm" to "雷暴",
            "Snow" to "雪天",
            "Mist" to "薄雾",
            "Fog" to "雾",
            "Haze" to "霾"
        )

        val weatherDescriptionMap = mapOf(
            "clear sky" to "晴空万里",
            "few clouds" to "少云",
            "scattered clouds" to "多云",
            "broken clouds" to "阴天",
            "overcast clouds" to "阴云密布",
            "light rain" to "小雨",
            "moderate rain" to "中雨",
            "heavy rain" to "大雨",
            "light snow" to "小雪",
            "moderate snow" to "中雪",
            "heavy snow" to "大雪"
        )

        val mainWeather = weather?.main ?: "未知"
        val description = weather?.description ?: "暂无描述"

        return WeatherDisplayData(
            cityName = weatherResponse.cityName,
            temperature = "${weatherResponse.main.temperature.toInt()}°C",
            weatherCondition = weatherConditionMap[mainWeather] ?: mainWeather,
            description = weatherDescriptionMap[description] ?: description,
            humidity = "湿度: ${weatherResponse.main.humidity}%",
            tempRange = "${weatherResponse.main.tempMin.toInt()}° ~ ${weatherResponse.main.tempMax.toInt()}°"
        )
    }

    private fun showWeatherData(data: WeatherDisplayData) {
        if (!::weatherCard.isInitialized) {
            showError("界面初始化失败")
            return
        }

        hideError()
        hideCityTips()

        tvCityName.text = data.cityName
        tvTemperature.text = data.temperature
        tvWeatherCondition.text = data.weatherCondition
        tvDescription.text = data.description
        tvHumidity.text = data.humidity
        tvTempRange.text = data.tempRange

        weatherCard.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        runOnUiThread {
            try {
                if (::weatherCard.isInitialized) {
                    weatherCard.visibility = View.GONE
                }

                if (::tvError.isInitialized) {
                    tvError.text = message
                    tvError.visibility = View.VISIBLE
                } else {
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }

                showCityInputTips()

            } catch (e: Exception) {
                Toast.makeText(this, "错误: $message", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun hideError() {
        if (::tvError.isInitialized) {
            tvError.visibility = View.GONE
        }
    }

    private fun hideCityTips() {
        if (tvCityTips.parent != null) {
            tvCityTips.visibility = View.GONE
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (::progressBar.isInitialized) {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        if (::btnSearch.isInitialized) {
            btnSearch.isEnabled = !isLoading
            btnSearch.text = if (isLoading) "查询中..." else "查询"
        }

        if (::etCityName.isInitialized) {
            etCityName.isEnabled = !isLoading
        }

        if (isLoading) {
            hideError()
            hideCityTips()
            if (::weatherCard.isInitialized) {
                weatherCard.visibility = View.GONE
            }
        }
    }

    private fun hideKeyboard() {
        try {
            val inputManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            currentFocus?.let { view ->
                inputManager.hideSoftInputFromWindow(view.windowToken, 0)
            }
        } catch (e: Exception) {
        }
    }

    private fun handleInitializationError(e: Exception) {
        val errorMessage = "应用初始化失败: ${e.message}"
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        android.util.Log.e("MainActivity", "Initialization error", e)
    }
}