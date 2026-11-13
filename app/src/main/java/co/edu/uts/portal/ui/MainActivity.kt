package co.edu.uts.portal.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uts.portal.viewmodel.PortalViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = PortalColors) {
                Portal(viewModel())
            }
        }
    }
}

private val PortalColors = lightColorScheme(
    primary = Color(0xFF245C36),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6EDCF),
    onPrimaryContainer = Color(0xFF112D19),
    secondaryContainer = Color(0xFFE2E9D5),
    onSecondaryContainer = Color(0xFF1B2818),
    surface = Color(0xFFFAFAF4),
    surfaceVariant = Color(0xFFE6EBDF),
    surfaceContainer = Color(0xFFF0F2E8),
    onSurface = Color(0xFF1A2118),
    onSurfaceVariant = Color(0xFF424C40),
)

private enum class Section(val label: String, val icon: ImageVector) {
    PROFILE("Perfil", Icons.Filled.AccountCircle),
    TUITION("Matrícula", Icons.Filled.ShoppingCart),
    HISTORY("Historial", Icons.AutoMirrored.Filled.List),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Portal(model: PortalViewModel) {
    var section by rememberSaveable { mutableStateOf(Section.PROFILE) }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Servicios UTS") })
        },
        bottomBar = {
            NavigationBar {
                Section.entries.forEach { item ->
                    NavigationBarItem(
                        selected = section == item,
                        onClick = { section = item },
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(item.label) },
                    )
                }
            }
        },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (section) {
            Section.PROFILE -> ProfileScreen(model, modifier)
            Section.TUITION -> TuitionScreen(model, modifier, openProfile = { section = Section.PROFILE })
            Section.HISTORY -> HistoryScreen(model, modifier, openTuition = { section = Section.TUITION })
        }
    }
}
