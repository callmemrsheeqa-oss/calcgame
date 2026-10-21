package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.calculator.CalculatorViewModel
import com.example.calculator.ui.CalculatorScreen
import com.example.settings.SettingsManager
import com.example.settings.ui.SettingsScreen
import com.example.snake.SnakeViewModel
import com.example.snake.ui.SnakeScreen
import com.example.ui.theme.*

enum class AppTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    CALCULATOR("Calculator", Icons.Default.Calculate),
    SNAKE("Snake Game", Icons.Default.SportsEsports),
    SETTINGS("Settings", Icons.Default.Settings)
}

@Composable
fun CalcSnakeApp(
    calculatorViewModel: CalculatorViewModel,
    snakeViewModel: SnakeViewModel,
    settingsManager: SettingsManager
) {
    var currentTab by remember { mutableStateOf(AppTab.CALCULATOR) }
    val settings by settingsManager.settings.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(currentTab) {
        val activity = context as? android.app.Activity
        if (currentTab == AppTab.SNAKE) {
            activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    // Handle system back button: return to Calculator if on Snake or Settings
    BackHandler(enabled = currentTab != AppTab.CALCULATOR) {
        currentTab = AppTab.CALCULATOR
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DeepCharcoal,
        topBar = {
            if (currentTab != AppTab.SNAKE) {
                CalcSnakeTopBar(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it }
                )
            }
        },
        bottomBar = {
            if (currentTab != AppTab.SNAKE) {
                CalcSnakeBottomNav(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (currentTab == AppTab.SNAKE) PaddingValues(0.dp) else innerPadding)
                .then(if (currentTab == AppTab.SNAKE) Modifier.safeDrawingPadding() else Modifier)
        ) {
            when (currentTab) {
                AppTab.CALCULATOR -> {
                    CalculatorScreen(
                        viewModel = calculatorViewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppTab.SNAKE -> {
                    SnakeScreen(
                        viewModel = snakeViewModel,
                        settings = settings,
                        onNavigateToCalculator = { currentTab = AppTab.CALCULATOR },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AppTab.SETTINGS -> {
                    SettingsScreen(
                        settingsManager = settingsManager,
                        settings = settings,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun CalcSnakeTopBar(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DeepCharcoal,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Brand Logo & Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PrimaryYellow),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "∑",
                            color = DeepCharcoal,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Calc",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Snake",
                                color = PrimaryYellow,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Text(
                            text = "Scientific Calculator & Motion Game",
                            color = LightGrey,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Quick Mode Pill Toggle
                Surface(
                    onClick = {
                        onTabSelected(if (currentTab == AppTab.CALCULATOR) AppTab.SNAKE else AppTab.CALCULATOR)
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = if (currentTab == AppTab.SNAKE) SnakeRed.copy(alpha = 0.2f) else PrimaryYellow.copy(alpha = 0.2f),
                    modifier = Modifier.testTag("btn_quick_mode_toggle")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (currentTab == AppTab.SNAKE) SnakeRed else PrimaryYellow)
                        )
                        Text(
                            text = if (currentTab == AppTab.SNAKE) "Snake Active" else "Calc Active",
                            color = if (currentTab == AppTab.SNAKE) SnakeRed else PrimaryYellow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalcSnakeBottomNav(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit
) {
    NavigationBar(
        containerColor = DarkGrey,
        contentColor = Color.White,
        tonalElevation = 6.dp
    ) {
        AppTab.values().forEach { tab ->
            val isSelected = currentTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = if (isSelected) DeepCharcoal else LightGrey
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        color = if (isSelected) PrimaryYellow else LightGrey,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = PrimaryYellow
                ),
                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
            )
        }
    }
}
