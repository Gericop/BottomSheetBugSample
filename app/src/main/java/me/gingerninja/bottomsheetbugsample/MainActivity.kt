package me.gingerninja.bottomsheetbugsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Surface
import androidx.compose.material.SwipeableDefaults
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import me.gingerninja.bottomsheetbugsample.ui.theme.BottomSheetBugSampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BottomSheetBugSampleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MyApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalMaterialApi::class)
@Composable
fun MyApp() {
    var autoDismiss by rememberSaveable { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        animationSpec = SwipeableDefaults.AnimationSpec
    )

    val bottomSheetNavigator = remember { BottomSheetNavigator(sheetState) }
    val navController = rememberNavController(bottomSheetNavigator)

    LaunchedEffect(Unit) {
        snapshotFlow {
            bottomSheetNavigator.navigatorSheetState.targetValue
        }
            .filter { it != ModalBottomSheetValue.Hidden && autoDismiss }
            .collectLatest {
                // the delay could be removed and it will still reproduce the issue
                // it's just nice to see the animation happening
                delay(100)

                sheetState.hide()
            }
    }

    ModalBottomSheetLayout(bottomSheetNavigator) {
        NavHost(
            modifier = Modifier.fillMaxSize(),
            navController = navController,
            startDestination = "home"
        ) {
            composable(route = "home") {
                val entry by navController.currentBackStackEntryAsState()
                val dest = entry?.destination?.route

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            navController.navigate("sheet") {
                                launchSingleTop = true
                            }
                        }
                    ) {
                        Text("Tap me repeatedly")
                    }

                    Text("Back stack last: $dest")

                    Spacer(Modifier.height(100.dp))

                    Row(
                        modifier = Modifier.clickable {
                            autoDismiss = !autoDismiss
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(checked = autoDismiss, onCheckedChange = { autoDismiss = it })
                        Text("Dismiss automatically")
                    }
                }
            }
            bottomSheet(route = "sheet") {
                Box(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 40.dp)
                ) {
                    Text("This is a cool bottom sheet!")
                }
            }
        }
    }
}