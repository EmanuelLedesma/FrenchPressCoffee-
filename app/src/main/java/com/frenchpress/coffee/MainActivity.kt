package com.frenchpress.coffee

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.frenchpress.coffee.ui.CalculatorScreen
import com.frenchpress.coffee.ui.CalculatorViewModel
import com.frenchpress.coffee.ui.theme.FrenchPressTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FrenchPressTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CalculatorScreen(
                        viewModel = viewModel(factory = CalculatorViewModel.Factory),
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
