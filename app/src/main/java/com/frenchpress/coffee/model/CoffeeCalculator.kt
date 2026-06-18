package com.frenchpress.coffee.model

data class CoffeeResult(
    val waterMl: Double,
    val coffeeG: Double,
    val servings: Double
) {
    val waterFormatted: String
        get() {
            val ml = waterMl.toInt()
            return if (ml >= 1000) String.format("%.1f L", waterMl / 1000.0) else "$ml ml"
        }

    val coffeeFormatted: String
        get() = String.format("%.0f g", coffeeG)
}

enum class Intensity(val label: String, val ratio: Int, val emoji: String) {
    SUAVE("Suave", 17, "\uD83C\uDF3E"),
    MEDIO("Medio", 15, "\u2615"),
    FUERTE("Fuerte", 13, "\uD83D\uDD25");

    companion object {
        fun fromRatio(ratio: Int): Intensity = when {
            ratio >= 16 -> SUAVE
            ratio >= 14 -> MEDIO
            else -> FUERTE
        }
    }
}

data class CoffeeSettings(
    val servings: Double = 1.0,
    val mlPerServing: Int = 170,
    val intensity: Intensity = Intensity.MEDIO
) {
    val ratio: Int
        get() = intensity.ratio

    companion object {
        const val MIN_SERVINGS = 0.5
        const val MAX_SERVINGS = 10.0
        const val SERVING_STEP = 0.5
        const val DEFAULT_ML_PER_SERVING = 170
    }
}

object CoffeeCalculator {

    fun calculate(settings: CoffeeSettings): CoffeeResult {
        val totalWaterMl = settings.servings * settings.mlPerServing
        val totalCoffeeG = totalWaterMl / settings.ratio

        return CoffeeResult(
            waterMl = totalWaterMl,
            coffeeG = totalCoffeeG,
            servings = settings.servings
        )
    }
}
