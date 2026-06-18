package com.frenchpress.coffee.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.frenchpress.coffee.model.CoffeeCalculator
import com.frenchpress.coffee.model.CoffeeResult
import com.frenchpress.coffee.model.CoffeeSettings
import com.frenchpress.coffee.model.Intensity
import androidx.lifecycle.ViewModel
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

    private val _timerRunning = MutableStateFlow(false)
    val timerRunning: StateFlow<Boolean> = _timerRunning.asStateFlow()

    private val _timerSeconds = MutableStateFlow(240)
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()

    private var timerThread: Thread? = null

    fun setServings(servings: Double) {
        val clamped = servings.coerceIn(CoffeeSettings.MIN_SERVINGS, CoffeeSettings.MAX_SERVINGS)
        _settings.update { it.copy(servings = clamped) }
        recalculate()
    }

    fun incrementServings() {
        setServings(_settings.value.servings + CoffeeSettings.SERVING_STEP)
    }

    fun decrementServings() {
        setServings(_settings.value.servings - CoffeeSettings.SERVING_STEP)
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

    fun toggleCustomize() {
        _showCustomize.update { !it }
    }

    fun reset() {
        _settings.value = CoffeeSettings()
        _result.value = CoffeeCalculator.calculate(CoffeeSettings())
        _showCustomize.value = false
        stopTimer()
    }

    fun toggleTimer() {
        if (_timerRunning.value) {
            stopTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        _timerSeconds.value = 240
        _timerRunning.value = true
        timerThread = Thread {
            try {
                while (_timerRunning.value && _timerSeconds.value > 0) {
                    Thread.sleep(1000)
                    _timerSeconds.update { it - 1 }
                }
                _timerRunning.value = false
                _timerSeconds.value = 240
            } catch (_: InterruptedException) {
                _timerRunning.value = false
            }
        }.also { it.start() }
    }

    private fun stopTimer() {
        _timerRunning.value = false
        _timerSeconds.value = 240
        timerThread?.interrupt()
        timerThread = null
    }

    private fun recalculate() {
        _result.value = CoffeeCalculator.calculate(_settings.value)
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}
