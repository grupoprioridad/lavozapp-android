package com.lavozapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.lavozapp.ui.radio.RadioScreen
import com.lavozapp.ui.socio.SocioScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation() {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Radio, contentDescription = null) },
                    label = { Text("La Radio") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Socio LVP") }
                )
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> RadioScreen(modifier = Modifier.padding(padding))
            1 -> SocioScreen(modifier = Modifier.padding(padding))
        }
    }
}
