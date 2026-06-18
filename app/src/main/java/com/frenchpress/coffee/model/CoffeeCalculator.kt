package com.frenchpress.coffee.model

enum class DrinkType(val label: String) {
    BLACK("Cafe negro"),
    FLAT_WHITE("Flat White")
}

data class CoffeeResult(
    val waterMl: Double,
    val coffeeG: Double,
    val servings: Int,
    val milkMl: Double = 0.0,
    val hasMilkCompensation: Boolean = false
) {
    val waterFormatted: String
        get() {
            val ml = waterMl.toInt()
            return if (ml >= 1000) String.format("%.1f L", waterMl / 1000.0) else "$ml ml"
        }

    val coffeeFormatted: String
        get() = String.format("%.0f g", coffeeG)

    val milkFormatted: String
        get() {
            val ml = milkMl.toInt()
            return if (ml >= 1000) String.format("%.1f L", milkMl / 1000.0) else "$ml ml"
        }
}

enum class Intensity(val label: String, val coffeePerPersonG: Double, val emoji: String) {
    SUAVE("Suave", 8.0, "\uD83C\uDF3E"),
    MEDIO("Medio", 10.0, "\u2615"),
    FUERTE("Fuerte", 12.0, "\uD83D\uDD25");
}

data class CoffeeSettings(
    val servings: Int = 1,
    val mlPerServing: Int = 150,
    val intensity: Intensity = Intensity.MEDIO,
    val drinkType: DrinkType = DrinkType.BLACK,
    val milkPercentage: Int = 50
) {
    companion object {
        const val MIN_SERVINGS = 1
        const val MAX_SERVINGS = 10
        const val DEFAULT_ML_PER_SERVING = 150
        val MILK_PERCENTAGES = listOf(40, 50, 60)
    }
}

object CoffeeCalculator {

    private const val MILK_COFFEE_COMPENSATION = 1.20

    fun calculate(settings: CoffeeSettings): CoffeeResult {
        val coffeePerPerson = settings.intensity.coffeePerPersonG
        val totalBeverageMl = settings.servings * settings.mlPerServing

        if (settings.drinkType == DrinkType.FLAT_WHITE) {
            val milkMl = totalBeverageMl * settings.milkPercentage / 100.0
            val coffeePreparedMl = totalBeverageMl - milkMl
            val coffeePerPersonCompensated = coffeePerPerson * MILK_COFFEE_COMPENSATION
            val waterForCoffeeMl = settings.servings * (coffeePreparedMl / settings.servings + coffeePerPersonCompensated * 3.0)
            val totalCoffeeG = settings.servings * coffeePerPersonCompensated

            return CoffeeResult(
                waterMl = waterForCoffeeMl,
                coffeeG = totalCoffeeG,
                servings = settings.servings,
                milkMl = milkMl,
                hasMilkCompensation = true
            )
        }

        val totalWaterMl = settings.servings * (settings.mlPerServing + coffeePerPerson * 3.0)
        val totalCoffeeG = settings.servings * coffeePerPerson

        return CoffeeResult(
            waterMl = totalWaterMl,
            coffeeG = totalCoffeeG,
            servings = settings.servings
        )
    }
}
