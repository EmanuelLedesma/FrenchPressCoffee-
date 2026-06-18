@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.frenchpress.coffee.ui

import android.widget.NumberPicker
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.frenchpress.coffee.model.CoffeeSettings
import com.frenchpress.coffee.model.Intensity
import kotlinx.coroutines.launch

@Composable
fun CalculatorScreen(viewModel: CalculatorViewModel) {
    val settings by viewModel.settings.collectAsState()
    val result by viewModel.result.collectAsState()
    val showCustomize by viewModel.showCustomize.collectAsState()
    val showServingsSheet by viewModel.showServingsSheet.collectAsState()
    val showMlSheet by viewModel.showMlSheet.collectAsState()
    val timer4Running by viewModel.timer4Running.collectAsState()
    val timer4Seconds by viewModel.timer4Seconds.collectAsState()
    val timer5Running by viewModel.timer5Running.collectAsState()
    val timer5Seconds by viewModel.timer5Seconds.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Header()

        Spacer(modifier = Modifier.height(28.dp))

        ServingsSelector(
            servings = settings.servings,
            onIncrement = viewModel::incrementServings,
            onDecrement = viewModel::decrementServings,
            onClick = viewModel::openServingsSheet
        )

        Spacer(modifier = Modifier.height(16.dp))

        ResultCard(
            waterFormatted = result.waterFormatted,
            coffeeFormatted = result.coffeeFormatted,
            servings = settings.servings,
            intensity = settings.intensity,
            coffeePerPersonG = settings.intensity.coffeePerPersonG
        )

        Spacer(modifier = Modifier.height(16.dp))

        IntensitySelector(
            selected = settings.intensity,
            onSelect = viewModel::setIntensity
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomizeSection(
            showCustomize = showCustomize,
            onToggle = viewModel::toggleCustomize,
            mlPerServing = settings.mlPerServing,
            onMlChange = viewModel::setMlPerServing,
            onMlClick = viewModel::openMlSheet
        )

        Spacer(modifier = Modifier.height(16.dp))

        TimerSection(
            timer4Running = timer4Running,
            timer4Seconds = timer4Seconds,
            timer5Running = timer5Running,
            timer5Seconds = timer5Seconds,
            onTimer4Toggle = viewModel::toggleTimer4,
            onTimer5Toggle = viewModel::toggleTimer5
        )

        Spacer(modifier = Modifier.height(16.dp))

        TipSection()

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = viewModel::reset,
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Restaurar valores")
        }
    }

    if (showServingsSheet) {
        ServingsBottomSheet(
            currentServings = settings.servings,
            onDismiss = viewModel::dismissServingsSheet,
            onConfirm = { viewModel.setServings(it) }
        )
    }

    if (showMlSheet) {
        MlBottomSheet(
            currentMl = settings.mlPerServing,
            onDismiss = viewModel::dismissMlSheet,
            onConfirm = { viewModel.setMlPerServing(it) }
        )
    }
}

@Composable
private fun ServingsBottomSheet(
    currentServings: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var selectedValue by remember { mutableStateOf(currentServings) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Cantidad de personas",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            AndroidView(
                factory = { context ->
                    NumberPicker(context).apply {
                        minValue = CoffeeSettings.MIN_SERVINGS
                        maxValue = CoffeeSettings.MAX_SERVINGS
                        value = currentServings
                        setOnValueChangedListener { _, _, newVal ->
                            selectedValue = newVal
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = {
                    onConfirm(selectedValue)
                    scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                }) {
                    Text("Aceptar")
                }
            }
        }
    }
}

@Composable
private fun MlBottomSheet(
    currentMl: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var selectedValue by remember { mutableStateOf(currentMl) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Mililitros por taza",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            AndroidView(
                factory = { context ->
                    NumberPicker(context).apply {
                        minValue = 50
                        maxValue = 500
                        value = currentMl
                        setOnValueChangedListener { _, _, newVal ->
                            selectedValue = newVal
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = {
                    onConfirm(selectedValue)
                    scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                }) {
                    Text("Aceptar")
                }
            }
        }
    }
}

@Composable
private fun Header() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "\u2615", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Prensa Francesa",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Calculadora de cafe",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ServingsSelector(
    servings: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Personas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onDecrement,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Menos personas",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                Text(
                    text = servings.toString(),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 56.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .width(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onClick() }
                        .padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.width(24.dp))

                IconButton(
                    onClick = onIncrement,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Mas personas",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Toca el numero para elegir",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun ResultCard(
    waterFormatted: String,
    coffeeFormatted: String,
    servings: Int,
    intensity: Intensity,
    coffeePerPersonG: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ResultItem(
                    emoji = "\uD83D\uDCA7",
                    label = "Agua",
                    value = waterFormatted
                )
                ResultItem(
                    emoji = "\u2615",
                    label = "Cafe",
                    value = coffeeFormatted
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${intensity.emoji} ${intensity.label} \u00B7 ${coffeePerPersonG.toInt()}g/persona",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun ResultItem(emoji: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, fontSize = 32.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun IntensitySelector(
    selected: Intensity,
    onSelect: (Intensity) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Intensity.entries.forEach { intensity ->
            val isSelected = intensity == selected

            FilledTonalButton(
                onClick = { onSelect(intensity) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isSelected)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = intensity.label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun CustomizeSection(
    showCustomize: Boolean,
    onToggle: () -> Unit,
    mlPerServing: Int,
    onMlChange: (Int) -> Unit,
    onMlClick: () -> Unit
) {
    Column {
        OutlinedButton(
            onClick = onToggle,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = if (showCustomize) "Ocultar opciones" else "Personalizar taza")
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = if (showCustomize) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null
            )
        }

        AnimatedVisibility(
            visible = showCustomize,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .animateContentSize(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Mililitros por taza de cafe deseada",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$mlPerServing ml",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onMlClick() }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = mlPerServing.toFloat(),
                        onValueChange = { onMlChange((it / 10).toInt() * 10) },
                        valueRange = 50f..500f,
                        steps = 44,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("50 ml", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("500 ml", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun TimerSection(
    timer4Running: Boolean,
    timer4Seconds: Int,
    timer5Running: Boolean,
    timer5Seconds: Int,
    onTimer4Toggle: () -> Unit,
    onTimer5Toggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = onTimer4Toggle,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (timer4Running)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (timer4Running) {
                    "${timer4Seconds / 60}:${String.format("%02d", timer4Seconds % 60)}"
                } else "Timer 4 min"
            )
        }

        Button(
            onClick = onTimer5Toggle,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (timer5Running)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (timer5Running) {
                    "${timer5Seconds / 60}:${String.format("%02d", timer5Seconds % 60)}"
                } else "Timer 5 min"
            )
        }
    }
}

@Composable
private fun TipSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Preparacion",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "1. Calentar agua a 93\u00B0C (no hirviendo)\n" +
                        "2. Verter agua sobre el cafe molido grueso\n" +
                        "3. Revolver suavemente y tapar\n" +
                        "4. Esperar 4 minutos y retirar la espuma\n" +
                        "5. Esperar 5 minutos adicionales\n" +
                        "6. Presionar el piston despacio y servir",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )
        }
    }
}
