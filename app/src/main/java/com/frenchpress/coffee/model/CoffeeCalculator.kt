package com.frenchpress.coffee.model

data class CoffeeResult(
    val waterMl: Double,
    val coffeeG: Double,
    val servings: Int
) {
    val waterFormatted: String
        get() {
            val ml = waterMl.toInt()
            return if (ml >= 1000) String.format("%.1f L", waterMl / 1000.0) else "$ml ml"
        }

    val coffeeFormatted: String
        get() = String.format("%.0f g", coffeeG)
}

enum class Intensity(val label: String, val coffeePerPersonG: Double, val emoji: String) {
    SUAVE("Suave", 8.0, "\uD83C\uDF3E"),
    MEDIO("Medio", 10.0, "\u2615"),
    FUERTE("Fuerte", 12.0, "\uD83D\uDD25");
}

data class CoffeeSettings(
    val servings: Int = 1,
    val mlPerServing: Int = 150,
    val intensity: Intensity = Intensity.MEDIO
) {
    companion object {
        const val MIN_SERVINGS = 1
        const val MAX_SERVINGS = 10
        const val DEFAULT_ML_PER_SERVING = 150
    }
}

object CoffeeCalculator {

    fun calculate(settings: CoffeeSettings): CoffeeResult {
        val coffeePerPerson = settings.intensity.coffeePerPersonG
        val totalCoffeeG = settings.servings * coffeePerPerson
        val totalWaterMl = settings.servings * (settings.mlPerServing + coffeePerPerson * 3.0)

        return CoffeeResult(
            waterMl = totalWaterMl,
            coffeeG = totalCoffeeG,
            servings = settings.servings
        )
    }
}
