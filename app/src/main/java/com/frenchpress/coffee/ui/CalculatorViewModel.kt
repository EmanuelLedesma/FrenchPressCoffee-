package com.frenchpress.coffee.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.frenchpress.coffee.model.CoffeeCalculator
import com.frenchpress.coffee.model.CoffeeResult
import com.frenchpress.coffee.model.CoffeeSettings
import com.frenchpress.coffee.model.DrinkType
import com.frenchpress.coffee.model.Intensity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                CalculatorViewModel(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application)
            }
        }
    }

    private val _settings = MutableStateFlow(CoffeeSettings())
    val settings: StateFlow<CoffeeSettings> = _settings.asStateFlow()

    private val _result = MutableStateFlow(CoffeeCalculator.calculate(CoffeeSettings()))
    val result: StateFlow<CoffeeResult> = _result.asStateFlow()

    private val _showCustomize = MutableStateFlow(false)
    val showCustomize: StateFlow<Boolean> = _showCustomize.asStateFlow()

    private val _showServingsSheet = MutableStateFlow(false)
    val showServingsSheet: StateFlow<Boolean> = _showServingsSheet.asStateFlow()

    private val _showMlSheet = MutableStateFlow(false)
    val showMlSheet: StateFlow<Boolean> = _showMlSheet.asStateFlow()

    private val _timer4Running = MutableStateFlow(false)
    val timer4Running: StateFlow<Boolean> = _timer4Running.asStateFlow()

    private val _timer4Seconds = MutableStateFlow(240)
    val timer4Seconds: StateFlow<Int> = _timer4Seconds.asStateFlow()

    private val _timer5Running = MutableStateFlow(false)
    val timer5Running: StateFlow<Boolean> = _timer5Running.asStateFlow()

    private val _timer5Seconds = MutableStateFlow(300)
    val timer5Seconds: StateFlow<Int> = _timer5Seconds.asStateFlow()

    private var timer4Thread: Thread? = null
    private var timer5Thread: Thread? = null

    fun setServings(servings: Int) {
        val clamped = servings.coerceIn(CoffeeSettings.MIN_SERVINGS, CoffeeSettings.MAX_SERVINGS)
        _settings.update { it.copy(servings = clamped) }
        recalculate()
    }

    fun incrementServings() {
        setServings(_settings.value.servings + 1)
    }

    fun decrementServings() {
        setServings(_settings.value.servings - 1)
    }

    fun openServingsSheet() {
        _showServingsSheet.value = true
    }

    fun dismissServingsSheet() {
        _showServingsSheet.value = false
    }

    fun setIntensity(intensity: Intensity) {
        _settings.update { it.copy(intensity = intensity) }
        recalculate()
    }

    fun setMlPerServing(ml: Int) {
        val clamped = ml.coerceIn(50, 500)
        _settings.update { it.copy(mlPerServing = clamped) }
        recalculate()
    }

    fun openMlSheet() {
        _showMlSheet.value = true
    }

    fun dismissMlSheet() {
        _showMlSheet.value = false
    }

    fun setDrinkType(drinkType: DrinkType) {
        _settings.update { it.copy(drinkType = drinkType) }
        recalculate()
    }

    fun setMilkPercentage(percentage: Int) {
        _settings.update { it.copy(milkPercentage = percentage) }
        recalculate()
    }

    fun toggleCustomize() {
        _showCustomize.update { !it }
    }

    fun toggleTimer4() {
        if (_timer4Running.value) {
            stopTimer4()
        } else {
            startTimer4()
        }
    }

    fun toggleTimer5() {
        if (_timer5Running.value) {
            stopTimer5()
        } else {
            startTimer5()
        }
    }

    private fun startTimer4() {
        _timer4Seconds.value = 240
        _timer4Running.value = true
        timer4Thread = Thread {
            try {
                while (_timer4Running.value && _timer4Seconds.value > 0) {
                    Thread.sleep(1000)
                    _timer4Seconds.update { it - 1 }
                }
                _timer4Running.value = false
                _timer4Seconds.value = 240
            } catch (_: InterruptedException) {
                _timer4Running.value = false
            }
        }.also { it.start() }
    }

    private fun stopTimer4() {
        _timer4Running.value = false
        _timer4Seconds.value = 240
        timer4Thread?.interrupt()
        timer4Thread = null
    }

    private fun startTimer5() {
        _timer5Seconds.value = 300
        _timer5Running.value = true
        timer5Thread = Thread {
            try {
                while (_timer5Running.value && _timer5Seconds.value > 0) {
                    Thread.sleep(1000)
                    _timer5Seconds.update { it - 1 }
                }
                _timer5Running.value = false
                _timer5Seconds.value = 300
            } catch (_: InterruptedException) {
                _timer5Running.value = false
            }
        }.also { it.start() }
    }

    private fun stopTimer5() {
        _timer5Running.value = false
        _timer5Seconds.value = 300
        timer5Thread?.interrupt()
        timer5Thread = null
    }

    fun reset() {
        _settings.value = CoffeeSettings()
        _result.value = CoffeeCalculator.calculate(CoffeeSettings())
        _showCustomize.value = false
        stopTimer4()
        stopTimer5()
    }

    private fun recalculate() {
        _result.value = CoffeeCalculator.calculate(_settings.value)
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer4()
        stopTimer5()
    }
}
