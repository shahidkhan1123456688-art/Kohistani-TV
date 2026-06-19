package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.KohistaniTvViewModel
import com.example.ui.KohistaniTvViewModelFactory
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FeedScreen
import com.example.ui.screens.LiveStreamScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.KohistaniTvTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewModel: KohistaniTvViewModel by viewModels {
            KohistaniTvViewModelFactory(application)
        }

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

            KohistaniTvTheme(darkTheme = isDarkMode) {
                MainLayout(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainLayout(viewModel: KohistaniTvViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            TikTokBottomNavigationBar(
                activeTab = currentTab,
                onTabSelected = { tab -> viewModel.setTab(tab) }
            )
        },
        contentWindowInsets = WindowInsets.navigationBars
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()) // Account for bottom bar
        ) {
            // High-fidelity tab transitions
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "ScreenTransitions"
            ) { targetTab ->
                when (targetTab) {
                    "feed" -> FeedScreen(viewModel = viewModel)
                    "dashboard" -> DashboardScreen(viewModel = viewModel)
                    "live" -> LiveStreamScreen(viewModel = viewModel)
                    "notifications" -> NotificationsScreen(viewModel = viewModel)
                    "profile" -> ProfileScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun TikTokBottomNavigationBar(
    activeTab: String,
    onTabSelected: (String) -> Unit
) {
    // Pure dark visual container matching the Elegant Dark theme background
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF010101))
            .navigationBarsPadding() // Handle notch and gesture pill overlaps gracefully!
    ) {
        Divider(color = Color.White.copy(alpha = 0.08f), thickness = 0.8.dp)
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home / Feed Tab
            NavBarItem(
                label = "ہوم",
                iconSelected = Icons.Filled.Home,
                iconUnselected = Icons.Outlined.Home,
                isSelected = (activeTab == "feed"),
                onSelect = { onTabSelected("feed") },
                modifier = Modifier.testTag("tab_feed")
            )

            // Discover / Dashboard
            NavBarItem(
                label = "ڈیش بورڈ",
                iconSelected = Icons.Filled.Explore,
                iconUnselected = Icons.Outlined.Explore,
                isSelected = (activeTab == "dashboard"),
                onSelect = { onTabSelected("dashboard") },
                modifier = Modifier.testTag("tab_dashboard")
            )

            // Center custom live neon button
            Box(
                modifier = Modifier
                    .size(width = 54.dp, height = 34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF25F4EE), // Cyan neon edge
                                Color(0xFFFE2C55)  // Crimson neon edge
                            )
                        )
                    )
                    .padding(1.5.dp) // Symmetrical outline border
                    .clickable { onTabSelected("live") }
                    .testTag("tab_live"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Podcasts,
                        contentDescription = "LIVE",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Notifications
            NavBarItem(
                label = "الرٹس",
                iconSelected = Icons.Filled.Notifications,
                iconUnselected = Icons.Outlined.Notifications,
                isSelected = (activeTab == "notifications"),
                onSelect = { onTabSelected("notifications") },
                modifier = Modifier.testTag("tab_notifications")
            )

            // User Profile
            NavBarItem(
                label = "پروفائل",
                iconSelected = Icons.Filled.Person,
                iconUnselected = Icons.Outlined.Person,
                isSelected = (activeTab == "profile"),
                onSelect = { onTabSelected("profile") },
                modifier = Modifier.testTag("tab_profile")
            )
        }
    }
}

@Composable
fun RowScope.NavBarItem(
    label: String,
    iconSelected: androidx.compose.ui.graphics.vector.ImageVector,
    iconUnselected: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(onClick = onSelect),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isSelected) iconSelected else iconUnselected,
            contentDescription = label,
            tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f)
        )
    }
}
