package com.thriic.itchwatch.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import com.thriic.itchwatch.ui.theme.ItchWatchTheme
import com.thriic.itchwatch.ui.detail.DetailScreen
import com.thriic.itchwatch.ui.detail.DetailViewModel
import com.thriic.itchwatch.ui.nav.AppNavHost
import com.thriic.itchwatch.ui.utils.DevicePreviews
import com.thriic.itchwatch.ui.utils.WatchLayout
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var navigator: Navigator
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ItchWatchTheme {
                ItchWatchApp(navigator, currentWindowAdaptiveInfo().windowSizeClass)
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ItchWatchApp(
    navigator: Navigator,
    windowSize: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
) {
    val layoutType = when (windowSize.windowWidthSizeClass) {
        WindowWidthSizeClass.COMPACT -> NavigationSuiteType.NavigationBar
        WindowWidthSizeClass.MEDIUM -> NavigationSuiteType.NavigationRail
        WindowWidthSizeClass.EXPANDED -> NavigationSuiteType.NavigationRail
        else -> NavigationSuiteType.NavigationBar
    }
    val layout = when (windowSize.windowWidthSizeClass) {
        WindowWidthSizeClass.COMPACT -> WatchLayout.Compact
        WindowWidthSizeClass.MEDIUM -> WatchLayout.Medium
        WindowWidthSizeClass.EXPANDED -> WatchLayout.Expanded
        else -> WatchLayout.Medium
    }
    SharedTransitionLayout {
        val navController = rememberNavController()
        navigator.setController(navController)
        NavHost(
            navController = navController,
            startDestination = "nav"
        ) {
            composable("nav") {
                AppNavHost(
                    modifier = Modifier,
                    layoutType = layoutType,
                    layout = layout,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this@composable
                )
            }
            composable(
                "detail?url={url}&id={id}",
                arguments = listOf(
                    navArgument("url") { type = NavType.StringType },
                    navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                val id =
                    backStackEntry.arguments?.getString("id") ?: throw Exception("id is null")
                DetailScreen(
                    id = id,
                    animatedContentScope = this@composable,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    viewModel = hiltViewModel<DetailViewModel>(backStackEntry)
                )
            }
            composable("tag") {
            }
        }
    }
}

@DevicePreviews
@Composable
fun NavigationPreView() {
    ItchWatchApp(Navigator())
}
