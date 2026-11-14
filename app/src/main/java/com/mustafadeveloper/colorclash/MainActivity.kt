package com.mustafadeveloper.colorclash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mustafadeveloper.colorclash.splash.AnimatedSplashScreen
import com.mustafadeveloper.colorclash.stroop.StroopGameScreen
import com.mustafadeveloper.colorclash.stroop.StroopViewModel
import com.mustafadeveloper.colorclash.ui.theme.MustafaTheme

class MainActivity : ComponentActivity() {

    private val stroopViewModel: StroopViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MustafaTheme {
                var isSplashFinished by remember { mutableStateOf(false) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Crossfade(
                        targetState = isSplashFinished,
                        animationSpec = tween(durationMillis = 600),
                        label = "splash_crossfade"
                    ) { splashDone ->
                        if (!splashDone) {
                            AnimatedSplashScreen(
                                onSplashFinished = { isSplashFinished = true },
                                modifier = Modifier.padding(innerPadding)
                            )
                        } else {
                            StroopGameScreen(
                                viewModel = stroopViewModel,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}
