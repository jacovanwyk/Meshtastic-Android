/*
 * Copyright (c) 2025 Meshtastic LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.geeksville.mesh.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Forward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PermScanWifi
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.geeksville.mesh.MeshProtos.DeviceMetadata
import com.geeksville.mesh.R
import com.geeksville.mesh.android.Logging
import com.geeksville.mesh.model.MetricsViewModel
import com.geeksville.mesh.model.RadioConfigViewModel
import com.geeksville.mesh.ui.components.BaseScaffold
import com.geeksville.mesh.ui.components.DeviceMetricsScreen
import com.geeksville.mesh.ui.components.EnvironmentMetricsScreen
import com.geeksville.mesh.ui.components.NodeMapScreen
import com.geeksville.mesh.ui.components.PositionLogScreen
import com.geeksville.mesh.ui.components.SignalMetricsScreen
import com.geeksville.mesh.ui.components.TracerouteLogScreen
import com.geeksville.mesh.ui.components.config.AmbientLightingConfigScreen
import com.geeksville.mesh.ui.components.config.AudioConfigScreen
import com.geeksville.mesh.ui.components.config.BluetoothConfigScreen
import com.geeksville.mesh.ui.components.config.CannedMessageConfigScreen
import com.geeksville.mesh.ui.components.config.ChannelConfigScreen
import com.geeksville.mesh.ui.components.config.DetectionSensorConfigScreen
import com.geeksville.mesh.ui.components.config.DeviceConfigScreen
import com.geeksville.mesh.ui.components.config.DisplayConfigScreen
import com.geeksville.mesh.ui.components.config.ExternalNotificationConfigScreen
import com.geeksville.mesh.ui.components.config.LoRaConfigScreen
import com.geeksville.mesh.ui.components.config.MQTTConfigScreen
import com.geeksville.mesh.ui.components.config.NeighborInfoConfigScreen
import com.geeksville.mesh.ui.components.config.NetworkConfigScreen
import com.geeksville.mesh.ui.components.config.PaxcounterConfigScreen
import com.geeksville.mesh.ui.components.config.PositionConfigScreen
import com.geeksville.mesh.ui.components.config.PowerConfigScreen
import com.geeksville.mesh.ui.components.config.RangeTestConfigScreen
import com.geeksville.mesh.ui.components.config.RemoteHardwareConfigScreen
import com.geeksville.mesh.ui.components.config.SecurityConfigScreen
import com.geeksville.mesh.ui.components.config.SerialConfigScreen
import com.geeksville.mesh.ui.components.config.StoreForwardConfigScreen
import com.geeksville.mesh.ui.components.config.TelemetryConfigScreen
import com.geeksville.mesh.ui.components.config.UserConfigScreen
import com.geeksville.mesh.ui.theme.AppTheme
import com.geeksville.mesh.util.UiText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable

internal fun FragmentManager.navigateToNavGraph(
    destNum: Int? = null,
    startDestination: String = "RadioConfig",
) {
    val radioConfigFragment = NavGraphFragment().apply {
        arguments = bundleOf("destNum" to destNum, "startDestination" to startDestination)
    }
    beginTransaction()
        .replace(R.id.mainActivityLayout, radioConfigFragment)
        .addToBackStack(null)
        .commit()
}

@AndroidEntryPoint
class NavGraphFragment : ScreenFragment("NavGraph"), Logging {

    private val model: RadioConfigViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        @Suppress("DEPRECATION")
        val destNum = arguments?.getSerializable("destNum") as? Int
        val startDestination: Any = when (arguments?.getString("startDestination")) {
            "NodeDetails" -> Route.NodeDetail(destNum!!)
            else -> Route.RadioConfig(destNum)
        }

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val node by model.destNode.collectAsStateWithLifecycle()

                AppTheme {
                    val navController: NavHostController = rememberNavController()
                    BaseScaffold(
                        title = node?.user?.longName
                            ?: stringResource(R.string.unknown_username),
                        canNavigateBack = true,
                        navigateUp = {
                            if (navController.previousBackStackEntry != null) {
                                navController.navigateUp()
                            } else {
                                parentFragmentManager.popBackStack()
                            }
                        },
                    ) {
                        NavGraph(
                            navController = navController,
                            startDestination = startDestination,
                        )
                    }
                }
            }
        }
    }
}

enum class AdminRoute(@StringRes val title: Int) {
    REBOOT(R.string.reboot),
    SHUTDOWN(R.string.shutdown),
    FACTORY_RESET(R.string.factory_reset),
    NODEDB_RESET(R.string.nodedb_reset),
}

sealed interface Route {
    @Serializable
    data class Messages(val contactKey: String, val message: String = "") : Route
    @Serializable
    data class Share(val message: String) : Route

    @Serializable
    data class RadioConfig(val destNum: Int? = null) : Route
    @Serializable data object User : Route
    @Serializable data object Channels : Route
    @Serializable data object Device : Route
    @Serializable data object Position : Route
    @Serializable data object Power : Route
    @Serializable data object Network : Route
    @Serializable data object Display : Route
    @Serializable data object LoRa : Route
    @Serializable data object Bluetooth : Route
    @Serializable data object Security : Route

    @Serializable data object MQTT : Route
    @Serializable data object Serial : Route
    @Serializable data object ExtNotification : Route
    @Serializable data object StoreForward : Route
    @Serializable data object RangeTest : Route
    @Serializable data object Telemetry : Route
    @Serializable data object CannedMessage : Route
    @Serializable data object Audio : Route
    @Serializable data object RemoteHardware : Route
    @Serializable data object NeighborInfo : Route
    @Serializable data object AmbientLighting : Route
    @Serializable data object DetectionSensor : Route
    @Serializable data object Paxcounter : Route

    @Serializable
    data class NodeDetail(val destNum: Int) : Route
    @Serializable data object DeviceMetrics : Route
    @Serializable data object NodeMap : Route
    @Serializable data object PositionLog : Route
    @Serializable data object EnvironmentMetrics : Route
    @Serializable data object SignalMetrics : Route
    @Serializable data object TracerouteLog : Route
}

// Config (type = AdminProtos.AdminMessage.ConfigType)
enum class ConfigRoute(val title: String, val route: Route, val icon: ImageVector?, val type: Int = 0) {
    USER("User", Route.User, Icons.Default.Person, 0),
    CHANNELS("Channels", Route.Channels, Icons.AutoMirrored.Default.List, 0),
    DEVICE("Device", Route.Device, Icons.Default.Router, 0),
    POSITION("Position", Route.Position, Icons.Default.LocationOn, 1),
    POWER("Power", Route.Power, Icons.Default.Power, 2),
    NETWORK("Network", Route.Network, Icons.Default.Wifi, 3),
    DISPLAY("Display", Route.Display, Icons.Default.DisplaySettings, 4),
    LORA("LoRa", Route.LoRa, Icons.Default.CellTower, 5),
    BLUETOOTH("Bluetooth", Route.Bluetooth, Icons.Default.Bluetooth, 6),
    SECURITY("Security", Route.Security, Icons.Default.Security, type = 7),
    ;

    companion object {
        fun filterExcludedFrom(metadata: DeviceMetadata?): List<ConfigRoute> = entries.filter {
            when {
                metadata == null -> true
                it == BLUETOOTH -> metadata.hasBluetooth
                it == NETWORK -> metadata.hasWifi || metadata.hasEthernet
                else -> true // Include all other routes by default
            }
        }
    }
}

// ModuleConfig (type = AdminProtos.AdminMessage.ModuleConfigType)
enum class ModuleRoute(val title: String, val route: Route, val icon: ImageVector?, val type: Int = 0) {
    MQTT("MQTT", Route.MQTT, Icons.Default.Cloud, 0),
    SERIAL("Serial", Route.Serial, Icons.Default.Usb, 1),
    EXT_NOTIFICATION("External Notification", Route.ExtNotification, Icons.Default.Notifications, 2),
    STORE_FORWARD("Store & Forward", Route.StoreForward, Icons.AutoMirrored.Default.Forward, 3),
    RANGE_TEST("Range Test", Route.RangeTest, Icons.Default.Speed, 4),
    TELEMETRY("Telemetry", Route.Telemetry, Icons.Default.DataUsage, 5),
    CANNED_MESSAGE("Canned Message", Route.CannedMessage, Icons.AutoMirrored.Default.Message, 6),
    AUDIO("Audio", Route.Audio, Icons.AutoMirrored.Default.VolumeUp, 7),
    REMOTE_HARDWARE("Remote Hardware", Route.RemoteHardware, Icons.Default.SettingsRemote, 8),
    NEIGHBOR_INFO("Neighbor Info", Route.NeighborInfo, Icons.Default.People, 9),
    AMBIENT_LIGHTING("Ambient Lighting", Route.AmbientLighting, Icons.Default.LightMode, 10),
    DETECTION_SENSOR("Detection Sensor", Route.DetectionSensor, Icons.Default.Sensors, 11),
    PAXCOUNTER("Paxcounter", Route.Paxcounter, Icons.Default.PermScanWifi, 12),
    ;

    val bitfield: Int get() = 1 shl ordinal

    companion object {
        fun filterExcludedFrom(metadata: DeviceMetadata?): List<ModuleRoute> = entries.filter {
            when (metadata) {
                null -> true
                else -> metadata.excludedModules and it.bitfield == 0
            }
        }
    }
}

/**
 * Generic sealed class defines each possible state of a response.
 */
sealed class ResponseState<out T> {
    data object Empty : ResponseState<Nothing>()
    data class Loading(var total: Int = 1, var completed: Int = 0) : ResponseState<Nothing>()
    data class Success<T>(val result: T) : ResponseState<T>()
    data class Error(val error: UiText) : ResponseState<Nothing>()

    fun isWaiting() = this !is Empty
}

@Suppress("LongMethod")
@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: Any,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable<Route.NodeDetail> {
            NodeDetailScreen { navController.navigate(route = it) }
        }
        composable<Route.DeviceMetrics> {
            val parentEntry = remember { navController.getBackStackEntry<Route.NodeDetail>() }
            DeviceMetricsScreen(hiltViewModel<MetricsViewModel>(parentEntry))
        }
        composable<Route.NodeMap> {
            val parentEntry = remember { navController.getBackStackEntry<Route.NodeDetail>() }
            NodeMapScreen(hiltViewModel<MetricsViewModel>(parentEntry))
        }
        composable<Route.PositionLog> {
            val parentEntry = remember { navController.getBackStackEntry<Route.NodeDetail>() }
            PositionLogScreen(hiltViewModel<MetricsViewModel>(parentEntry))
        }
        composable<Route.EnvironmentMetrics> {
            val parentEntry = remember { navController.getBackStackEntry<Route.NodeDetail>() }
            EnvironmentMetricsScreen(hiltViewModel<MetricsViewModel>(parentEntry))
        }
        composable<Route.SignalMetrics> {
            val parentEntry = remember { navController.getBackStackEntry<Route.NodeDetail>() }
            SignalMetricsScreen(hiltViewModel<MetricsViewModel>(parentEntry))
        }
        composable<Route.TracerouteLog> {
            val parentEntry = remember { navController.getBackStackEntry<Route.NodeDetail>() }
            TracerouteLogScreen(hiltViewModel<MetricsViewModel>(parentEntry))
        }
        composable<Route.RadioConfig> {
            RadioConfigScreen { navController.navigate(route = it) }
        }
        composable<Route.User> {
            val parentEntry = remember { navController.getBackStackEntry<Route.RadioConfig>() }
            UserConfigScreen(hiltViewModel<RadioConfigViewModel>(parentEntry))
        }
        composable<Route.Channels> {
            val parentEntry = remember { navController.getBackStackEntry<Route.RadioConfig>() }
            ChannelConfigScreen(hiltViewModel<RadioConfigViewModel>(parentEntry))
        }
        composable<Route.Device> {
            val parentEntry = remember { navController.getBackStackEntry<Route.RadioConfig>() }
            DeviceConfigScreen(hiltViewModel<RadioConfigViewModel>(parentEntry))
        }
        composable<Route.Position> {
            val parentEntry = remember { navController.getBackStackEntry<Route.RadioConfig>() }
            PositionConfigScreen(hiltViewModel<RadioConfigViewModel>(parentEntry))
        }
        composable<Route.Power> {
            val parentEntry = remember { navController.getBackStackEntry<Route.RadioConfig>() }
            PowerConfigScreen(hiltViewModel<RadioConfigViewModel>(parentEntry))
        }
        composable<Route.Network> {
            val parentEntry = remember { navController.getBackStackEntry<Route.RadioConfig>() }
            NetworkConfigScreen(hiltViewModel<RadioConfigViewModel>(parentEntry))
        }
        composable<Route.Display> {
            val parentEntry = remember { navController.getBackStackEntry<Route.RadioConfig>() }
            DisplayConfigScreen(hiltViewModel<RadioConfigViewModel>(parentEntry))
        }
        composable<Route.LoRa> {
            val parentEntry = remember { navController.getBackStackEntry<Route.RadioConfig>() }
            LoRaConfigScreen(hiltViewModel<RadioConfigViewModel>(parentEntry))
        }
        composable<Route.Bluetooth> {
            val parentEntry = remember { navController.getBackStackEntry<Route.RadioConfig>() }
            BluetoothConfigScreen(hiltViewModel<RadioConfigViewModel>(parentEntry))
        }
        composable<Route.Security> {
            val parentEntry = remember { navController.getBackStackEntry<Route.RadioConfig>() }
            SecurityConfigScreen(hiltViewModel<RadioConfigViewModel>(parentEntry))
        }
        composable<Route.MQTT> {
            val parentEntry = remember { navController.getBackStackEntry<Route.RadioConfig>() }
            MQTTConfigScreen(hiltViewModel<RadioConfigViewModel>(parentEntry))
        }
        composable<Route.Serial> {
            val parentEntry = remember { navController.getBackStackEntry<Route.RadioConfig>() }
            SerialConfigScreen(hiltViewModel<RadioConfigViewModel>(parentEntry))
        }
        composable<Route.ExtNotification> {
            val parentEntry = remember { navController.getBackStackEntry<Route.RadioConfig>() }
            ExternalNotificationConfigScreen(hiltViewModel<RadioConfigViewModel>(parentEntry))
        }
        composable<Route.StoreForward> {
            val parentEntry = remember { navController.getBackStackEntry<Route.RadioConfig>() }
            StoreForwardConfigScreen(hiltViewModel<RadioConfigViewModel>(parentEntry))
        }
        composable<Route.RangeTest> {
            val parentEntry = remember { navController.getBackStackEntry<Route.RadioConfig>() }
            RangeTestConfigScreen(hiltViewModel<RadioConfigViewModel>(parentEntry))
        }
        composable<Route.Telemetry> {
            val parentEntry = remember { navController.getBackStackEntry<Route.RadioConfig>() }
            TelemetryConfigScreen(hiltViewModel<RadioConfigViewModel>(parentEntry))
        }
        composable<Route.CannedMessage> {
            val parentEntry = remember { navController.getBackStackEntry<Route.RadioConfig>() }
            CannedMessageConfigScreen(hiltViewModel<RadioConfigViewModel>(parentEntry))
        }
        composable<Route.Audio> {
            val parentEntry = remember { navController.getBackStackEntry<Route.RadioConfig>() }
            AudioConfigScreen(hiltViewModel<RadioConfigViewModel>(parentEntry))
        }
        composable<Route.RemoteHardware> {
            val parentEntry = remember { navController.getBackStackEntry<Route.RadioConfig>() }
            RemoteHardwareConfigScreen(hiltViewModel<RadioConfigViewModel>(parentEntry))
        }
        composable<Route.NeighborInfo> {
            val parentEntry = remember { navController.getBackStackEntry<Route.RadioConfig>() }
            NeighborInfoConfigScreen(hiltViewModel<RadioConfigViewModel>(parentEntry))
        }
        composable<Route.AmbientLighting> {
            val parentEntry = remember { navController.getBackStackEntry<Route.RadioConfig>() }
            AmbientLightingConfigScreen(hiltViewModel<RadioConfigViewModel>(parentEntry))
        }
        composable<Route.DetectionSensor> {
            val parentEntry = remember { navController.getBackStackEntry<Route.RadioConfig>() }
            DetectionSensorConfigScreen(hiltViewModel<RadioConfigViewModel>(parentEntry))
        }
        composable<Route.Paxcounter> {
            val parentEntry = remember { navController.getBackStackEntry<Route.RadioConfig>() }
            PaxcounterConfigScreen(hiltViewModel<RadioConfigViewModel>(parentEntry))
        }
        composable<Route.Share> { backStackEntry ->
            val message = backStackEntry.toRoute<Route.Share>().message
            ShareScreen(
                navigateUp = navController::navigateUp,
            ) {
                navController.navigate(Route.Messages(it, message)) {
                    popUpTo<Route.Share> { inclusive = true }
                }
            }
        }
    }
}
