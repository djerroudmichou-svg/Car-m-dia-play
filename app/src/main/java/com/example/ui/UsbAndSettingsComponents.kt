package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.content.Intent
import android.provider.Settings
import android.view.KeyEvent
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.LayoutDirection
import com.example.data.VolumeEntity
import com.example.localization.AppLanguage
import com.example.localization.LocalAppStrings
import com.example.theme.LocalCarColors
import com.example.theme.ThemeMode
import com.example.swc.*
import com.example.telemetry.TempSource
import com.example.telemetry.SpeedSource
import com.example.telemetry.ComprehensiveSensorScanReport
import com.example.telemetry.ScannedSensorInfo
import com.example.telemetry.ScannedSysfsThermalInfo
import com.example.telemetry.ScannedLocationProviderInfo
import java.util.Locale

// ==========================================
// TAB: USB, THEMES, SENSORS & LANGUAGE SETTINGS
// ==========================================
@Composable
fun UsbAndSettingsTabContent(
    volumesList: List<VolumeEntity>,
    isScanning: Boolean,
    scanProgress: String,
    currentLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onResetLanguage: () -> Unit,
    onTriggerScan: () -> Unit,
    themeMode: ThemeMode,
    isLightSensorAvailable: Boolean,
    currentLux: Float,
    isDynamicColorEnabled: Boolean,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onDynamicColorToggle: (Boolean) -> Unit,
    isCompactScreenMode: Boolean = false,
    onCompactScreenModeToggle: (Boolean) -> Unit = {},
    isKeepScreenOn: Boolean = true,
    onKeepScreenOnToggle: (Boolean) -> Unit = {},
    isFullscreenMode: Boolean = true,
    onFullscreenModeToggle: (Boolean) -> Unit = {},
    isAutoLaunchOnUsb: Boolean = true,
    onAutoLaunchOnUsbToggle: (Boolean) -> Unit = {},
    swcConfig: SwcConfig = SwcConfig(),
    onSwcPresetSelected: (SwcPresetProfile) -> Unit = {},
    onSwcDualPressModeSelected: (SwcDualPressMode) -> Unit = {},
    onSwcDualPressWindowChanged: (Long) -> Unit = {},
    onSwcDedicatedPauseToggled: (Boolean) -> Unit = {},
    onSwcLongPressModeSelected: (SwcLongPressMode) -> Unit = {},
    onSwcLongPressThresholdChanged: (Long) -> Unit = {},
    onSwcSeekStepChanged: (Int) -> Unit = {},
    onSwcAcceptExtendedKeysToggled: (Boolean) -> Unit = {},
    onSwcDebounceChanged: (Long) -> Unit = {},
    swcLiveKeyLog: SwcLiveKeyLog? = null,
    screenScale: Float = 1.0f,
    onScreenScaleChanged: (Float) -> Unit = {},
    fontSizeScale: Float = 1.0f,
    onFontSizeScaleChanged: (Float) -> Unit = {},
    onSwcCustomButton1Changed: (Int) -> Unit = {},
    onSwcCustomButton2Changed: (Int) -> Unit = {},
    onSwcCustomPlayPauseChanged: (Int) -> Unit = {},
    onSwcCustomAccelerationChanged: (Int) -> Unit = {},
    showClock: Boolean = true,
    onShowClockToggle: (Boolean) -> Unit = {},
    showDate: Boolean = true,
    onShowDateToggle: (Boolean) -> Unit = {},
    showTemp: Boolean = true,
    onShowTempToggle: (Boolean) -> Unit = {},
    showSpeed: Boolean = true,
    onShowSpeedToggle: (Boolean) -> Unit = {},
    onSwcWheelModeChanged: (CanBusWheelMode) -> Unit = {},
    onSwcProtocolChanged: (CanBusProtocol) -> Unit = {},
    ambientTemp: Int = 25,
    carSpeed: Int = 0,
    useMetricSpeed: Boolean = true,
    onUseMetricSpeedToggle: (Boolean) -> Unit = {},
    useMetricTemp: Boolean = true,
    onUseMetricTempToggle: (Boolean) -> Unit = {},
    tempOffset: Float = 0f,
    onTempOffsetChanged: (Float) -> Unit = {},
    isTempSensorAvailable: Boolean = true,
    isGpsActive: Boolean = false,
    tempSource: TempSource = TempSource.AUTO,
    onTempSourceSelected: (TempSource) -> Unit = {},
    speedSource: SpeedSource = SpeedSource.AUTO_GPS,
    onSpeedSourceSelected: (SpeedSource) -> Unit = {},
    manualTempValue: Float = 24f,
    onManualTempValueChanged: (Float) -> Unit = {},
    simulatedSpeedValue: Int = 0,
    onSimulatedSpeedValueChanged: (Int) -> Unit = {},
    speedMultiplier: Float = 1.0f,
    onSpeedMultiplierChanged: (Float) -> Unit = {},
    ambientHardwareTemp: Float? = null,
    batteryTemp: Float? = null,
    cpuSysfsTemp: Float? = null,
    canBusTemp: Float? = null,
    gpsSpeed: Int? = null,
    networkSpeed: Int? = null,
    canBusSpeed: Int? = null,
    scanReport: ComprehensiveSensorScanReport = ComprehensiveSensorScanReport(),
    liveSensorValues: Map<String, List<Float>> = emptyMap(),
    customTempSensorName: String? = null,
    customSysfsTempPath: String? = null,
    customSpeedSensorName: String? = null,
    onSelectCustomTempSensor: (String) -> Unit = {},
    onSelectCustomSysfsThermal: (String) -> Unit = {},
    onSelectCustomSpeedSensor: (String) -> Unit = {},
    onStartLiveAudit: () -> Unit = {},
    onStopLiveAudit: () -> Unit = {},
    onRequestLocationPermission: () -> Unit = {},
    onRescanSensors: () -> Unit = {},
    onAutoDetect: () -> String = { "" }
) {
    val strings = LocalAppStrings.current
    val colors = LocalCarColors.current

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isNarrow = isCompactScreenMode || maxWidth < 1200.dp
        var selectedSubTab by remember { mutableIntStateOf(0) } // 0: USB, 1: SWC, 2: CAN-Bus, 3: Sensors, 4: App Settings
        var showScannerDialog by remember { mutableStateOf(false) }

        if (showScannerDialog) {
            SensorScannerDialog(
                scanReport = scanReport,
                liveSensorValues = liveSensorValues,
                customTempSensorName = customTempSensorName,
                customSysfsTempPath = customSysfsTempPath,
                customSpeedSensorName = customSpeedSensorName,
                activeTempSource = tempSource,
                activeSpeedSource = speedSource,
                initialCategoryIndex = 0,
                onSelectCustomTempSensor = onSelectCustomTempSensor,
                onSelectCustomSysfsThermal = onSelectCustomSysfsThermal,
                onSelectCustomSpeedSensor = onSelectCustomSpeedSensor,
                onSelectTempSource = onTempSourceSelected,
                onSelectSpeedSource = onSpeedSourceSelected,
                onAutoDetect = onAutoDetect,
                onRequestLocationPermission = onRequestLocationPermission,
                onRescan = {
                    onStartLiveAudit()
                    onRescanSensors()
                },
                onDismiss = {
                    onStopLiveAudit()
                    showScannerDialog = false
                }
            )
        }

        if (isNarrow) {
            val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
            var totalDragX by remember { mutableFloatStateOf(0f) }

            val subTabSwipeModifier = Modifier.pointerInput(selectedSubTab, isRtl) {
                detectHorizontalDragGestures(
                    onDragStart = { totalDragX = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        totalDragX += dragAmount
                    },
                    onDragEnd = {
                        val swipeThreshold = 50.dp.toPx()
                        val isNext = if (isRtl) totalDragX > swipeThreshold else totalDragX < -swipeThreshold
                        val isPrev = if (isRtl) totalDragX < -swipeThreshold else totalDragX > swipeThreshold

                        if (isNext && selectedSubTab < 4) {
                            selectedSubTab += 1
                        } else if (isPrev && selectedSubTab > 0) {
                            selectedSubTab -= 1
                        }
                    }
                )
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Top Segmented Switcher with Circular Sensor Discovery Radar Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surface, RoundedCornerShape(12.dp))
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val mountedCount = volumesList.count { it.isMounted }

                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tab 0: USB Storage
                        SubTabButton(
                            icon = Icons.Filled.Usb,
                            label = "${strings.storageTitle} ($mountedCount)",
                            isSelected = selectedSubTab == 0,
                            onClick = { selectedSubTab = 0 }
                        )

                        // Tab 1: SWC Settings
                        SubTabButton(
                            icon = Icons.Filled.DirectionsCar,
                            label = strings.swcSettingsSectionTitle,
                            isSelected = selectedSubTab == 1,
                            onClick = { selectedSubTab = 1 }
                        )

                        // Tab 2: CAN-Bus Integration
                        SubTabButton(
                            icon = Icons.Filled.Cable,
                            label = strings.canBusSection,
                            isSelected = selectedSubTab == 2,
                            onClick = { selectedSubTab = 2 }
                        )

                        // Tab 3: Sensors & Speed Management
                        SubTabButton(
                            icon = Icons.Filled.Sensors,
                            label = strings.sensorsTabTitle,
                            isSelected = selectedSubTab == 3,
                            onClick = { selectedSubTab = 3 }
                        )

                        // Tab 4: App Settings
                        SubTabButton(
                            icon = Icons.Filled.Settings,
                            label = strings.tabSettings,
                            isSelected = selectedSubTab == 4,
                            onClick = { selectedSubTab = 4 }
                        )
                    }

                    // Circular Sensor Radar Discovery Button (requested by user)
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .size(if (isCompactScreenMode) 34.dp else 40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F172A))
                            .border(1.5.dp, colors.accent, CircleShape)
                            .clickable {
                                onStartLiveAudit()
                                onRescanSensors()
                                showScannerDialog = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Troubleshoot,
                            contentDescription = strings.sensorScannerDialogTitle,
                            tint = colors.accent,
                            modifier = Modifier.size(if (isCompactScreenMode) 18.dp else 22.dp)
                        )
                    }
                }

                // Active SubTab Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .then(subTabSwipeModifier)
                ) {
                    when (selectedSubTab) {
                        0 -> UsbStoragePane(
                            volumesList = volumesList,
                            isScanning = isScanning,
                            scanProgress = scanProgress,
                            onTriggerScan = onTriggerScan,
                            isCompactScreenMode = true,
                            modifier = Modifier.fillMaxSize()
                        )
                        1 -> SwcSettingsPane(
                            swcConfig = swcConfig,
                            onPresetSelected = onSwcPresetSelected,
                            onDualPressModeSelected = onSwcDualPressModeSelected,
                            onDualPressWindowChanged = onSwcDualPressWindowChanged,
                            onDedicatedPauseToggled = onSwcDedicatedPauseToggled,
                            onLongPressModeSelected = onSwcLongPressModeSelected,
                            onLongPressThresholdChanged = onSwcLongPressThresholdChanged,
                            onSeekStepChanged = onSwcSeekStepChanged,
                            onAcceptExtendedKeysToggled = onSwcAcceptExtendedKeysToggled,
                            onDebounceChanged = onSwcDebounceChanged,
                            onCustomButton1Changed = onSwcCustomButton1Changed,
                            onCustomButton2Changed = onSwcCustomButton2Changed,
                            onCustomPlayPauseChanged = onSwcCustomPlayPauseChanged,
                            onCustomAccelerationChanged = onSwcCustomAccelerationChanged,
                            liveKeyLog = swcLiveKeyLog,
                            isCompactScreenMode = true,
                            modifier = Modifier.fillMaxSize()
                        )
                        2 -> CanBusSettingsPane(
                            swcConfig = swcConfig,
                            onWheelModeChanged = onSwcWheelModeChanged,
                            onProtocolChanged = onSwcProtocolChanged,
                            ambientTemp = ambientTemp,
                            carSpeed = carSpeed,
                            useMetricSpeed = useMetricSpeed,
                            useMetricTemp = useMetricTemp,
                            isTempSensorAvailable = isTempSensorAvailable,
                            isGpsActive = isGpsActive,
                            isCompactScreenMode = true,
                            modifier = Modifier.fillMaxSize()
                        )
                        3 -> SensorsSettingsPane(
                            tempSource = tempSource,
                            onTempSourceSelected = onTempSourceSelected,
                            speedSource = speedSource,
                            onSpeedSourceSelected = onSpeedSourceSelected,
                            manualTempValue = manualTempValue,
                            onManualTempValueChanged = onManualTempValueChanged,
                            simulatedSpeedValue = simulatedSpeedValue,
                            onSimulatedSpeedValueChanged = onSimulatedSpeedValueChanged,
                            speedMultiplier = speedMultiplier,
                            onSpeedMultiplierChanged = onSpeedMultiplierChanged,
                            tempOffset = tempOffset,
                            onTempOffsetChanged = onTempOffsetChanged,
                            useMetricSpeed = useMetricSpeed,
                            onUseMetricSpeedToggle = onUseMetricSpeedToggle,
                            useMetricTemp = useMetricTemp,
                            onUseMetricTempToggle = onUseMetricTempToggle,
                            ambientHardwareTemp = ambientHardwareTemp,
                            batteryTemp = batteryTemp,
                            cpuSysfsTemp = cpuSysfsTemp,
                            canBusTemp = canBusTemp,
                            gpsSpeed = gpsSpeed,
                            networkSpeed = networkSpeed,
                            canBusSpeed = canBusSpeed,
                            isGpsActive = isGpsActive,
                            isCompactScreenMode = true,
                            scanReport = scanReport,
                            liveSensorValues = liveSensorValues,
                            customTempSensorName = customTempSensorName,
                            customSysfsTempPath = customSysfsTempPath,
                            customSpeedSensorName = customSpeedSensorName,
                            onSelectCustomTempSensor = onSelectCustomTempSensor,
                            onSelectCustomSysfsThermal = onSelectCustomSysfsThermal,
                            onSelectCustomSpeedSensor = onSelectCustomSpeedSensor,
                            onStartLiveAudit = onStartLiveAudit,
                            onStopLiveAudit = onStopLiveAudit,
                            onRequestLocationPermission = onRequestLocationPermission,
                            onRescanSensors = onRescanSensors,
                            onAutoDetect = onAutoDetect,
                            modifier = Modifier.fillMaxSize()
                        )
                        else -> SettingsPane(
                            currentLanguage = currentLanguage,
                            onLanguageSelected = onLanguageSelected,
                            onResetLanguage = onResetLanguage,
                            themeMode = themeMode,
                            isLightSensorAvailable = isLightSensorAvailable,
                            currentLux = currentLux,
                            isDynamicColorEnabled = isDynamicColorEnabled,
                            onThemeModeSelected = onThemeModeSelected,
                            onDynamicColorToggle = onDynamicColorToggle,
                            isCompactScreenMode = isCompactScreenMode,
                            onCompactScreenModeToggle = onCompactScreenModeToggle,
                            isKeepScreenOn = isKeepScreenOn,
                            onKeepScreenOnToggle = onKeepScreenOnToggle,
                            isFullscreenMode = isFullscreenMode,
                            onFullscreenModeToggle = onFullscreenModeToggle,
                            isAutoLaunchOnUsb = isAutoLaunchOnUsb,
                            onAutoLaunchOnUsbToggle = onAutoLaunchOnUsbToggle,
                            screenScale = screenScale,
                            onScreenScaleChanged = onScreenScaleChanged,
                            fontSizeScale = fontSizeScale,
                            onFontSizeScaleChanged = onFontSizeScaleChanged,
                            showClock = showClock,
                            onShowClockToggle = onShowClockToggle,
                            showDate = showDate,
                            onShowDateToggle = onShowDateToggle,
                            showTemp = showTemp,
                            onShowTempToggle = onShowTempToggle,
                            showSpeed = showSpeed,
                            onShowSpeedToggle = onShowSpeedToggle,
                            useMetricSpeed = useMetricSpeed,
                            onUseMetricSpeedToggle = onUseMetricSpeedToggle,
                            useMetricTemp = useMetricTemp,
                            onUseMetricTempToggle = onUseMetricTempToggle,
                            tempOffset = tempOffset,
                            onTempOffsetChanged = onTempOffsetChanged,
                            isTempSensorAvailable = isTempSensorAvailable,
                            isGpsActive = isGpsActive,
                            onOpenSensorScanner = {
                                onStartLiveAudit()
                                onRescanSensors()
                                showScannerDialog = true
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        } else {
            // Wide Screen: Triple Pane side-by-side (USB, SWC, Settings)
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(if (isCompactScreenMode) 8.dp else 14.dp)
            ) {
                UsbStoragePane(
                    volumesList = volumesList,
                    isScanning = isScanning,
                    scanProgress = scanProgress,
                    onTriggerScan = onTriggerScan,
                    isCompactScreenMode = isCompactScreenMode,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )

                SwcSettingsPane(
                    swcConfig = swcConfig,
                    onPresetSelected = onSwcPresetSelected,
                    onDualPressModeSelected = onSwcDualPressModeSelected,
                    onDualPressWindowChanged = onSwcDualPressWindowChanged,
                    onDedicatedPauseToggled = onSwcDedicatedPauseToggled,
                    onLongPressModeSelected = onSwcLongPressModeSelected,
                    onLongPressThresholdChanged = onSwcLongPressThresholdChanged,
                    onSeekStepChanged = onSwcSeekStepChanged,
                    onAcceptExtendedKeysToggled = onSwcAcceptExtendedKeysToggled,
                    onDebounceChanged = onSwcDebounceChanged,
                    onCustomButton1Changed = onSwcCustomButton1Changed,
                    onCustomButton2Changed = onSwcCustomButton2Changed,
                    onCustomPlayPauseChanged = onSwcCustomPlayPauseChanged,
                    onCustomAccelerationChanged = onSwcCustomAccelerationChanged,
                    liveKeyLog = swcLiveKeyLog,
                    isCompactScreenMode = isCompactScreenMode,
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
                )

                CanBusSettingsPane(
                    swcConfig = swcConfig,
                    onWheelModeChanged = onSwcWheelModeChanged,
                    onProtocolChanged = onSwcProtocolChanged,
                    ambientTemp = ambientTemp,
                    carSpeed = carSpeed,
                    useMetricSpeed = useMetricSpeed,
                    useMetricTemp = useMetricTemp,
                    isTempSensorAvailable = isTempSensorAvailable,
                    isGpsActive = isGpsActive,
                    isCompactScreenMode = isCompactScreenMode,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )

                SensorsSettingsPane(
                    tempSource = tempSource,
                    onTempSourceSelected = onTempSourceSelected,
                    speedSource = speedSource,
                    onSpeedSourceSelected = onSpeedSourceSelected,
                    manualTempValue = manualTempValue,
                    onManualTempValueChanged = onManualTempValueChanged,
                    simulatedSpeedValue = simulatedSpeedValue,
                    onSimulatedSpeedValueChanged = onSimulatedSpeedValueChanged,
                    speedMultiplier = speedMultiplier,
                    onSpeedMultiplierChanged = onSpeedMultiplierChanged,
                    tempOffset = tempOffset,
                    onTempOffsetChanged = onTempOffsetChanged,
                    useMetricSpeed = useMetricSpeed,
                    onUseMetricSpeedToggle = onUseMetricSpeedToggle,
                    useMetricTemp = useMetricTemp,
                    onUseMetricTempToggle = onUseMetricTempToggle,
                    ambientHardwareTemp = ambientHardwareTemp,
                    batteryTemp = batteryTemp,
                    cpuSysfsTemp = cpuSysfsTemp,
                    canBusTemp = canBusTemp,
                    gpsSpeed = gpsSpeed,
                    networkSpeed = networkSpeed,
                    canBusSpeed = canBusSpeed,
                    isGpsActive = isGpsActive,
                    isCompactScreenMode = isCompactScreenMode,
                    scanReport = scanReport,
                    liveSensorValues = liveSensorValues,
                    customTempSensorName = customTempSensorName,
                    customSysfsTempPath = customSysfsTempPath,
                    customSpeedSensorName = customSpeedSensorName,
                    onSelectCustomTempSensor = onSelectCustomTempSensor,
                    onSelectCustomSysfsThermal = onSelectCustomSysfsThermal,
                    onSelectCustomSpeedSensor = onSelectCustomSpeedSensor,
                    onStartLiveAudit = onStartLiveAudit,
                    onStopLiveAudit = onStopLiveAudit,
                    onRequestLocationPermission = onRequestLocationPermission,
                    onRescanSensors = onRescanSensors,
                    onAutoDetect = onAutoDetect,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )

                SettingsPane(
                    currentLanguage = currentLanguage,
                    onLanguageSelected = onLanguageSelected,
                    onResetLanguage = onResetLanguage,
                    themeMode = themeMode,
                    isLightSensorAvailable = isLightSensorAvailable,
                    currentLux = currentLux,
                    isDynamicColorEnabled = isDynamicColorEnabled,
                    onThemeModeSelected = onThemeModeSelected,
                    onDynamicColorToggle = onDynamicColorToggle,
                    isCompactScreenMode = isCompactScreenMode,
                    onCompactScreenModeToggle = onCompactScreenModeToggle,
                    isKeepScreenOn = isKeepScreenOn,
                    onKeepScreenOnToggle = onKeepScreenOnToggle,
                    isFullscreenMode = isFullscreenMode,
                    onFullscreenModeToggle = onFullscreenModeToggle,
                    isAutoLaunchOnUsb = isAutoLaunchOnUsb,
                    onAutoLaunchOnUsbToggle = onAutoLaunchOnUsbToggle,
                    screenScale = screenScale,
                    onScreenScaleChanged = onScreenScaleChanged,
                    fontSizeScale = fontSizeScale,
                    onFontSizeScaleChanged = onFontSizeScaleChanged,
                    showClock = showClock,
                    onShowClockToggle = onShowClockToggle,
                    showDate = showDate,
                    onShowDateToggle = onShowDateToggle,
                    showTemp = showTemp,
                    onShowTempToggle = onShowTempToggle,
                    showSpeed = showSpeed,
                    onShowSpeedToggle = onShowSpeedToggle,
                    useMetricSpeed = useMetricSpeed,
                    onUseMetricSpeedToggle = onUseMetricSpeedToggle,
                    useMetricTemp = useMetricTemp,
                    onUseMetricTempToggle = onUseMetricTempToggle,
                    tempOffset = tempOffset,
                    onTempOffsetChanged = onTempOffsetChanged,
                    isTempSensorAvailable = isTempSensorAvailable,
                    isGpsActive = isGpsActive,
                    onOpenSensorScanner = {
                        onStartLiveAudit()
                        onRescanSensors()
                        showScannerDialog = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }
    }
}

// ==========================================
// USB STORAGE PANE (Scrollable & Responsive)
// ==========================================
@Composable
fun RowScope.SubTabButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalCarColors.current
    Box(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) colors.surfaceSecondary else Color.Transparent)
            .border(
                width = if (isSelected) 1.5.dp else 0.dp,
                color = if (isSelected) colors.accent else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) colors.accent else colors.textSecondary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                color = if (isSelected) colors.textPrimary else colors.textSecondary,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun UsbStoragePane(
    volumesList: List<VolumeEntity>,
    isScanning: Boolean,
    scanProgress: String,
    onTriggerScan: () -> Unit,
    isCompactScreenMode: Boolean,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val colors = LocalCarColors.current

    Column(
        modifier = modifier
            .background(colors.surface, RoundedCornerShape(16.dp))
            .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp))
            .padding(if (isCompactScreenMode) 10.dp else 14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Title & Scan Button Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Icon(
                    imageVector = Icons.Filled.Usb,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = strings.storageTitle,
                    color = colors.textPrimary,
                    fontSize = if (isCompactScreenMode) 13.sp else 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onTriggerScan,
                enabled = !isScanning,
                colors = ButtonDefaults.buttonColors(containerColor = colors.surfaceSecondary),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = strings.storageScanButton,
                    tint = colors.accent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = strings.storageScanButton,
                    color = colors.textPrimary,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }

        if (isScanning) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE5A93C).copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color(0xFFE5A93C),
                    strokeWidth = 2.dp
                )
                Text(
                    text = scanProgress.ifEmpty { strings.scanningStorage },
                    color = Color(0xFFE5A93C),
                    fontSize = 12.sp
                )
            }
        }

        val mountedVolumes = volumesList.filter { it.isMounted }
        if (mountedVolumes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceSecondary, RoundedCornerShape(12.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SdCard,
                        contentDescription = null,
                        tint = colors.textSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(46.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = strings.noUsbFound,
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            mountedVolumes.forEach { vol ->
                val isInternal = vol.volumeId == "internal_storage"
                val displayName = if (isInternal) strings.internalStorage else vol.label
                val icon = if (isInternal) Icons.Filled.Storage else Icons.Filled.Usb

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceSecondary, RoundedCornerShape(12.dp))
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = displayName,
                                color = colors.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = strings.storageMounted,
                            color = colors.accent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${strings.storageRoot}: ${vol.rootPath}",
                        color = colors.textSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ==========================================
// SETTINGS PANE (Themes, Display, Languages)
// ==========================================
@Composable
fun SettingsPane(
    currentLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onResetLanguage: () -> Unit,
    themeMode: ThemeMode,
    isLightSensorAvailable: Boolean,
    currentLux: Float,
    isDynamicColorEnabled: Boolean,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onDynamicColorToggle: (Boolean) -> Unit,
    isCompactScreenMode: Boolean,
    onCompactScreenModeToggle: (Boolean) -> Unit,
    isKeepScreenOn: Boolean,
    onKeepScreenOnToggle: (Boolean) -> Unit,
    isFullscreenMode: Boolean,
    onFullscreenModeToggle: (Boolean) -> Unit,
    isAutoLaunchOnUsb: Boolean,
    onAutoLaunchOnUsbToggle: (Boolean) -> Unit,
    screenScale: Float,
    onScreenScaleChanged: (Float) -> Unit,
    fontSizeScale: Float,
    onFontSizeScaleChanged: (Float) -> Unit,
    showClock: Boolean = true,
    onShowClockToggle: (Boolean) -> Unit = {},
    showDate: Boolean = true,
    onShowDateToggle: (Boolean) -> Unit = {},
    showTemp: Boolean = true,
    onShowTempToggle: (Boolean) -> Unit = {},
    showSpeed: Boolean = true,
    onShowSpeedToggle: (Boolean) -> Unit = {},
    useMetricSpeed: Boolean = true,
    onUseMetricSpeedToggle: (Boolean) -> Unit = {},
    useMetricTemp: Boolean = true,
    onUseMetricTempToggle: (Boolean) -> Unit = {},
    tempOffset: Float = 0f,
    onTempOffsetChanged: (Float) -> Unit = {},
    isTempSensorAvailable: Boolean = true,
    isGpsActive: Boolean = false,
    onOpenSensorScanner: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val colors = LocalCarColors.current

    Column(
        modifier = modifier
            .background(colors.surface, RoundedCornerShape(16.dp))
            .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp))
            .padding(if (isCompactScreenMode) 10.dp else 14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(if (isCompactScreenMode) 10.dp else 14.dp)
    ) {
        // QUICK SENSOR SCANNER & RADAR ACTION (Requested by user)
        Card(
            onClick = { onOpenSensorScanner() },
            colors = CardDefaults.cardColors(containerColor = colors.surfaceSecondary),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, colors.accent.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isCompactScreenMode) 8.dp else 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (isCompactScreenMode) 38.dp else 44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F172A))
                            .border(1.5.dp, colors.accent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Troubleshoot,
                            contentDescription = strings.sensorScannerDialogTitle,
                            tint = colors.accent,
                            modifier = Modifier.size(if (isCompactScreenMode) 20.dp else 24.dp)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = strings.sensorScannerDialogTitle,
                            color = colors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = strings.scanSensorsSubtitle,
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // 1. THEME MODE SECTION (Dark / Light / Auto-Sensor / Auto-Time / Auto-Screen)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = strings.themeSection,
                    color = colors.accent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                // Row 1: Direct Modes & Sensor Mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeModeButton(
                        title = strings.themeDark,
                        icon = Icons.Filled.DarkMode,
                        isSelected = themeMode == ThemeMode.DARK,
                        modifier = Modifier.weight(1f),
                        onClick = { onThemeModeSelected(ThemeMode.DARK) }
                    )
                    ThemeModeButton(
                        title = strings.themeLight,
                        icon = Icons.Filled.LightMode,
                        isSelected = themeMode == ThemeMode.LIGHT,
                        modifier = Modifier.weight(1f),
                        onClick = { onThemeModeSelected(ThemeMode.LIGHT) }
                    )
                    ThemeModeButton(
                        title = strings.themeAutoSensor,
                        icon = Icons.Filled.BrightnessAuto,
                        isSelected = themeMode == ThemeMode.AUTO_SENSOR,
                        modifier = Modifier.weight(1.1f),
                        onClick = { onThemeModeSelected(ThemeMode.AUTO_SENSOR) }
                    )
                }

                // Row 2: Smart Alternative Modes for Vehicles Without Ambient Light Sensor
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeModeButton(
                        title = strings.themeAutoTime,
                        icon = Icons.Filled.Schedule,
                        isSelected = themeMode == ThemeMode.AUTO_TIME,
                        modifier = Modifier.weight(1f),
                        onClick = { onThemeModeSelected(ThemeMode.AUTO_TIME) }
                    )
                    ThemeModeButton(
                        title = strings.themeAutoScreen,
                        icon = Icons.Filled.BrightnessMedium,
                        isSelected = themeMode == ThemeMode.AUTO_SCREEN,
                        modifier = Modifier.weight(1f),
                        onClick = { onThemeModeSelected(ThemeMode.AUTO_SCREEN) }
                    )
                }

                // Ambient Light Sensor Status & Alternatives Badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceSecondary, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isLightSensorAvailable) Icons.Filled.Sensors else Icons.Filled.SensorsOff,
                        contentDescription = null,
                        tint = if (isLightSensorAvailable) colors.accent else colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isLightSensorAvailable) {
                                if (themeMode == ThemeMode.AUTO_SENSOR) {
                                    "${strings.lightSensorStatusAvailable} (${currentLux.toInt()} lux)"
                                } else {
                                    strings.lightSensorStatusAvailable
                                }
                            } else {
                                strings.lightSensorStatusUnavailable
                            },
                            color = colors.textPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 15.sp
                        )
                        if (!isLightSensorAvailable) {
                            Text(
                                text = strings.lightSensorAlternativeNotice,
                                color = colors.accent,
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                // Dynamic Album Art Color Adaptive Theming
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.surfaceSecondary)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Palette,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = strings.dynamicColorTitle,
                                color = colors.textPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = strings.dynamicColorSubtitle,
                                color = colors.textSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Switch(
                        checked = isDynamicColorEnabled,
                        onCheckedChange = onDynamicColorToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.onAccent,
                            checkedTrackColor = colors.accent,
                            uncheckedThumbColor = colors.textSecondary,
                            uncheckedTrackColor = colors.surface
                        )
                    )
                }

                // Compact Screen / Small Display Mode Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.surfaceSecondary)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FitScreen,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = strings.compactScreenTitle,
                                    color = colors.textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isCompactScreenMode) {
                                    Box(
                                        modifier = Modifier
                                            .background(colors.accent.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = strings.compactScreenBadge,
                                            color = colors.accent,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Text(
                                text = strings.compactScreenSubtitle,
                                color = colors.textSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Switch(
                        checked = isCompactScreenMode,
                        onCheckedChange = onCompactScreenModeToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.onAccent,
                            checkedTrackColor = colors.accent,
                            uncheckedThumbColor = colors.textSecondary,
                            uncheckedTrackColor = colors.surface
                        )
                    )
                }

                // Screen Size / UI Scale Slider
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceSecondary, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.AspectRatio, null, tint = colors.accent, modifier = Modifier.size(18.dp))
                            Text(strings.screenSizeTitle, color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("${(screenScale * 100).toInt()}%", color = colors.accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(strings.screenSizeSubtitle, color = colors.textSecondary, fontSize = 10.sp)
                    Slider(
                        value = screenScale,
                        onValueChange = onScreenScaleChanged,
                        valueRange = 0.8f..1.3f,
                        steps = 10
                    )
                }

                // Font Size Scale Slider
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceSecondary, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.FormatSize, null, tint = colors.accent, modifier = Modifier.size(18.dp))
                            Text(strings.fontSizeTitle, color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("${(fontSizeScale * 100).toInt()}%", color = colors.accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(strings.fontSizeSubtitle, color = colors.textSecondary, fontSize = 10.sp)
                    Slider(
                        value = fontSizeScale,
                        onValueChange = onFontSizeScaleChanged,
                        valueRange = 0.85f..1.35f,
                        steps = 10
                    )
                }

                // Keep Screen On / Prevent Sleep Mode Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.surfaceSecondary)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.WbSunny,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = strings.keepScreenOnTitle,
                                    color = colors.textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isKeepScreenOn) {
                                    Box(
                                        modifier = Modifier
                                            .background(colors.accent.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = strings.keepScreenOnBadge,
                                            color = colors.accent,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Text(
                                text = strings.keepScreenOnSubtitle,
                                color = colors.textSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Switch(
                        checked = isKeepScreenOn,
                        onCheckedChange = onKeepScreenOnToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.onAccent,
                            checkedTrackColor = colors.accent,
                            uncheckedThumbColor = colors.textSecondary,
                            uncheckedTrackColor = colors.surface
                        )
                    )
                }

                // Immersive Fullscreen Mode (100% car display)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceSecondary, RoundedCornerShape(12.dp))
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Fullscreen,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = strings.fullscreenTitle,
                                    color = colors.textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isFullscreenMode) {
                                    Box(
                                        modifier = Modifier
                                            .background(colors.accent.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = strings.fullscreenBadge,
                                            color = colors.accent,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Text(
                                text = strings.fullscreenSubtitle,
                                color = colors.textSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Switch(
                        checked = isFullscreenMode,
                        onCheckedChange = onFullscreenModeToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.onAccent,
                            checkedTrackColor = colors.accent,
                            uncheckedThumbColor = colors.textSecondary,
                            uncheckedTrackColor = colors.surface
                        )
                    )
                }

                // Auto-Open App on USB Insertion
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceSecondary, RoundedCornerShape(12.dp))
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Usb,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = strings.autoOpenOnUsbTitle,
                                    color = colors.textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isAutoLaunchOnUsb) {
                                    Box(
                                        modifier = Modifier
                                            .background(colors.accent.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = strings.autoOpenOnUsbBadge,
                                            color = colors.accent,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Text(
                                text = strings.autoOpenOnUsbSubtitle,
                                color = colors.textSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Switch(
                        checked = isAutoLaunchOnUsb,
                        onCheckedChange = onAutoLaunchOnUsbToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.onAccent,
                            checkedTrackColor = colors.accent,
                            uncheckedThumbColor = colors.textSecondary,
                            uncheckedTrackColor = colors.surface
                        )
                    )
                }
            }

            HorizontalDivider(color = colors.cardBorder, thickness = 1.dp)

            // 2. LANGUAGE SETTINGS SECTION
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = strings.languageSection,
                    color = colors.accent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                // 3 Supported Languages
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LanguageSelectCompactButton(
                        title = strings.langArabic,
                        isSelected = currentLanguage == AppLanguage.ARABIC,
                        modifier = Modifier.weight(1f),
                        onClick = { onLanguageSelected(AppLanguage.ARABIC) }
                    )
                    LanguageSelectCompactButton(
                        title = strings.langFrench,
                        isSelected = currentLanguage == AppLanguage.FRENCH,
                        modifier = Modifier.weight(1f),
                        onClick = { onLanguageSelected(AppLanguage.FRENCH) }
                    )
                    LanguageSelectCompactButton(
                        title = strings.langEnglish,
                        isSelected = currentLanguage == AppLanguage.ENGLISH,
                        modifier = Modifier.weight(1f),
                        onClick = { onLanguageSelected(AppLanguage.ENGLISH) }
                    )
                }

                // Default Rule Notice
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceSecondary, RoundedCornerShape(10.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = strings.systemDefaultRuleNotice,
                        color = colors.textSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }

                Button(
                    onClick = onResetLanguage,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.surfaceSecondary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = strings.resetToSystemLanguage,
                        color = colors.accent,
                        fontSize = 12.sp
                    )
                }
            }

            HorizontalDivider(color = colors.cardBorder, thickness = 1.dp)

            // 3. DASHBOARD WIDGETS SECTION
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = strings.widgetsSectionTitle,
                    color = colors.accent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = strings.widgetsSectionSubtitle,
                    color = colors.textSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                SettingsToggleRow(
                    title = strings.showClock,
                    icon = Icons.Filled.Schedule,
                    checked = showClock,
                    onCheckedChange = onShowClockToggle,
                    isCompact = isCompactScreenMode
                )
                SettingsToggleRow(
                    title = strings.showDate,
                    icon = Icons.Filled.CalendarMonth,
                    checked = showDate,
                    onCheckedChange = onShowDateToggle,
                    isCompact = isCompactScreenMode
                )
                SettingsToggleRow(
                    title = strings.showTemp,
                    icon = Icons.Filled.Thermostat,
                    checked = showTemp,
                    onCheckedChange = onShowTempToggle,
                    isCompact = isCompactScreenMode
                )
                SettingsToggleRow(
                    title = strings.showSpeed,
                    icon = Icons.Filled.Speed,
                    checked = showSpeed,
                    onCheckedChange = onShowSpeedToggle,
                    isCompact = isCompactScreenMode
                )

                // Speed Unit (km/h vs mph)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceSecondary, RoundedCornerShape(12.dp))
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                        .padding(if (isCompactScreenMode) 8.dp else 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Speed,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = strings.speedUnit,
                                color = colors.textPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (useMetricSpeed) strings.speedUnit else strings.speedUnitMph,
                                color = colors.textSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(
                            selected = useMetricSpeed,
                            onClick = { onUseMetricSpeedToggle(true) },
                            label = { Text("km/h", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.accent,
                                selectedLabelColor = colors.onAccent
                            )
                        )
                        FilterChip(
                            selected = !useMetricSpeed,
                            onClick = { onUseMetricSpeedToggle(false) },
                            label = { Text("mph", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.accent,
                                selectedLabelColor = colors.onAccent
                            )
                        )
                    }
                }

                // Temperature Unit (°C vs °F)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceSecondary, RoundedCornerShape(12.dp))
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                        .padding(if (isCompactScreenMode) 8.dp else 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Thermostat,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = strings.tempUnit,
                                color = colors.textPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (useMetricTemp) strings.tempUnit else strings.tempUnitFahrenheit,
                                color = colors.textSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(
                            selected = useMetricTemp,
                            onClick = { onUseMetricTempToggle(true) },
                            label = { Text("°C", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.accent,
                                selectedLabelColor = colors.onAccent
                            )
                        )
                        FilterChip(
                            selected = !useMetricTemp,
                            onClick = { onUseMetricTempToggle(false) },
                            label = { Text("°F", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.accent,
                                selectedLabelColor = colors.onAccent
                            )
                        )
                    }
                }

                // Temperature Calibration Offset Slider
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceSecondary, RoundedCornerShape(12.dp))
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.Tune, null, tint = colors.accent, modifier = Modifier.size(18.dp))
                            Text(strings.tempCalibrationLabel, color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        val formattedOffset = if (tempOffset > 0) "+${tempOffset.toInt()}°C" else "${tempOffset.toInt()}°C"
                        Text(formattedOffset, color = colors.accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Slider(
                        value = tempOffset,
                        onValueChange = onTempOffsetChanged,
                        valueRange = -15f..15f,
                        steps = 29
                    )
                }
            }

            HorizontalDivider(color = colors.cardBorder, thickness = 1.dp)

            // 3. ABOUT APPLICATION & DEVELOPER SECTION
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = strings.aboutSectionTitle,
                    color = colors.accent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceSecondary, RoundedCornerShape(12.dp))
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Developer Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Code,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = strings.appDeveloperLabel,
                                color = colors.textSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.accent.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = strings.developerName,
                                color = colors.accent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    HorizontalDivider(color = colors.cardBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

                    // Version Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = strings.appVersionLabel,
                                color = colors.textSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            text = strings.appVersionValue,
                            color = colors.textPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    HorizontalDivider(color = colors.cardBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

                    // Summary of Automotive Capabilities
                    Text(
                        text = strings.appFeaturesSummary,
                        color = colors.textSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }

// ==========================================
// COMPACT LANGUAGE BUTTON
// ==========================================
@Composable
fun LanguageSelectCompactButton(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalCarColors.current

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) colors.surfaceSecondary else colors.surface)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) colors.accent else colors.cardBorder,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = if (isSelected) colors.accent else colors.textPrimary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

// ==========================================
// THEME MODE SELECT BUTTON
// ==========================================
@Composable
fun ThemeModeButton(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalCarColors.current

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) colors.surfaceSecondary else colors.surface)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) colors.accent else colors.cardBorder,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isSelected) colors.accent else colors.textSecondary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = title,
            color = if (isSelected) colors.accent else colors.textPrimary,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1
        )
    }
}

// ==========================================
// SWC SETTINGS PANE & COMPONENTS
// ==========================================
@Composable
fun SwcSettingsPane(
    swcConfig: SwcConfig,
    onPresetSelected: (SwcPresetProfile) -> Unit,
    onDualPressModeSelected: (SwcDualPressMode) -> Unit,
    onDualPressWindowChanged: (Long) -> Unit,
    onDedicatedPauseToggled: (Boolean) -> Unit,
    onLongPressModeSelected: (SwcLongPressMode) -> Unit,
    onLongPressThresholdChanged: (Long) -> Unit,
    onSeekStepChanged: (Int) -> Unit,
    onAcceptExtendedKeysToggled: (Boolean) -> Unit,
    onDebounceChanged: (Long) -> Unit,
    onCustomButton1Changed: (Int) -> Unit = {},
    onCustomButton2Changed: (Int) -> Unit = {},
    onCustomPlayPauseChanged: (Int) -> Unit = {},
    onCustomAccelerationChanged: (Int) -> Unit = {},
    liveKeyLog: SwcLiveKeyLog?,
    isCompactScreenMode: Boolean,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val colors = LocalCarColors.current

    var mappingTarget by remember { mutableIntStateOf(-1) } // -1: none, 0: Next, 1: Prev, 2: PlayPause

    // Auto-map when key is detected if in mapping mode
    LaunchedEffect(liveKeyLog) {
        if (mappingTarget != -1 && liveKeyLog != null) {
            when (mappingTarget) {
                0 -> onCustomButton1Changed(liveKeyLog.keyCode)
                1 -> onCustomButton2Changed(liveKeyLog.keyCode)
                2 -> onCustomPlayPauseChanged(liveKeyLog.keyCode)
            }
            mappingTarget = -1 // Reset after mapping
        }
    }

    Column(
        modifier = modifier
            .background(colors.surface, RoundedCornerShape(16.dp))
            .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp))
            .padding(if (isCompactScreenMode) 10.dp else 14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(if (isCompactScreenMode) 10.dp else 14.dp)
    ) {
        // Header
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.DirectionsCar,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = strings.swcSettingsSectionTitle,
                    color = colors.accent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = strings.swcSettingsSectionSubtitle,
                color = colors.textSecondary,
                fontSize = 11.sp
            )
        }

        // 1. Live Key Monitor
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surfaceSecondary, RoundedCornerShape(12.dp))
                .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Filled.Monitor, null, tint = colors.accent, modifier = Modifier.size(16.dp))
                Text(strings.swcTesterTitle, color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Text(strings.swcTesterSubtitle, color = colors.textSecondary, fontSize = 10.sp)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                if (liveKeyLog == null) {
                    Text(
                        text = strings.swcTesterWaiting,
                        color = colors.textSecondary,
                        fontSize = 11.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${strings.swcTesterKeyDetected}:", color = colors.textSecondary, fontSize = 10.sp)
                            Text("${liveKeyLog.keyName} (${liveKeyLog.keyCode})", color = colors.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${strings.swcTesterAction}:", color = colors.textSecondary, fontSize = 10.sp)
                            Text(liveKeyLog.action, color = colors.textPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${strings.swcTesterMappedFunction}:", color = colors.textSecondary, fontSize = 10.sp)
                            Text(liveKeyLog.mappedFunction, color = colors.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. Native System MediaSession & Steering Wheel Controls Overview
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colors.surfaceSecondary),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, colors.accent.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = strings.mediaSessionActiveTitle,
                        color = colors.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = strings.mediaSessionActiveDesc,
                    color = colors.textSecondary,
                    fontSize = 10.sp,
                    lineHeight = 15.sp
                )
            }
        }

        // 3. Supported Physical Steering Wheel Buttons
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = strings.supportedSwcButtonsTitle,
                color = colors.textPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            val supportedButtons = listOf(
                strings.btnPlayPause to "KEYCODE_MEDIA_PLAY_PAUSE",
                strings.btnNextTrack to "KEYCODE_MEDIA_NEXT",
                strings.btnPrevTrack to "KEYCODE_MEDIA_PREVIOUS",
                strings.btnFastForward to "KEYCODE_MEDIA_FAST_FORWARD",
                strings.btnRewind to "KEYCODE_MEDIA_REWIND",
                strings.btnStop to "KEYCODE_MEDIA_STOP"
            )

            supportedButtons.forEach { (label, codeName) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceSecondary, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, color = colors.textPrimary, fontSize = 10.sp)
                    Text(codeName, color = colors.accent, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // 4. Hardware Audio Focus & Vehicle Optimizations
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colors.surfaceSecondary),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, colors.cardBorder)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.VolumeUp,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = strings.audioFocusTitle,
                        color = colors.textPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = strings.audioFocusDesc,
                    color = colors.textSecondary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun MappingRow(
    label: String,
    currentCode: Int,
    isMapping: Boolean,
    onMapClick: () -> Unit,
    strings: com.example.localization.AppStrings,
    colors: com.example.theme.CarColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isMapping) colors.accent.copy(alpha = 0.1f) else Color.Transparent, RoundedCornerShape(8.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = colors.textPrimary, fontSize = 11.sp)
            Text(
                text = if (isMapping) strings.swcMapWaiting else "Code: $currentCode (${KeyEvent.keyCodeToString(currentCode)})",
                color = if (isMapping) colors.accent else colors.textSecondary,
                fontSize = 10.sp,
                fontWeight = if (isMapping) FontWeight.Bold else FontWeight.Normal
            )
        }
        Button(
            onClick = onMapClick,
            colors = ButtonDefaults.buttonColors(containerColor = if (isMapping) colors.accent else colors.surfaceSecondary),
            shape = RoundedCornerShape(6.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            modifier = Modifier.height(28.dp)
        ) {
            Text(if (isMapping) "..." else "MAP", fontSize = 10.sp, color = if (isMapping) colors.onAccent else colors.textPrimary)
        }
    }
}

@Composable
fun PresetButton(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalCarColors.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) colors.accent.copy(alpha = 0.2f) else colors.surfaceSecondary)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) colors.accent else colors.cardBorder,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) colors.accent else colors.textPrimary,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun DualModeOption(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalCarColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) colors.surfaceSecondary else Color.Transparent)
            .border(
                width = if (isSelected) 1.5.dp else 0.dp,
                color = if (isSelected) colors.accent else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            color = if (isSelected) colors.textPrimary else colors.textSecondary,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
    }
}

@Composable
fun SettingsToggleRow(
    title: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isCompact: Boolean = false
) {
    val colors = LocalCarColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceSecondary, RoundedCornerShape(12.dp))
            .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
            .padding(if (isCompact) 8.dp else 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = title,
                color = colors.textPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.onAccent,
                checkedTrackColor = colors.accent,
                uncheckedThumbColor = colors.textSecondary,
                uncheckedTrackColor = colors.surface
            )
        )
    }
}

@Composable
fun CanBusSettingsPane(
    swcConfig: SwcConfig,
    onWheelModeChanged: (CanBusWheelMode) -> Unit,
    onProtocolChanged: (CanBusProtocol) -> Unit,
    ambientTemp: Int,
    carSpeed: Int,
    useMetricSpeed: Boolean = true,
    useMetricTemp: Boolean = true,
    isTempSensorAvailable: Boolean = true,
    isGpsActive: Boolean = false,
    isCompactScreenMode: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalCarColors.current
    val strings = LocalAppStrings.current

    Column(
        modifier = modifier
            .background(colors.surface, RoundedCornerShape(16.dp))
            .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Filled.DirectionsCar, null, tint = colors.accent, modifier = Modifier.size(24.dp))
            Text(strings.canBusSection, color = colors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. CAR TELEMETRY (SPEED & TEMP)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Speed Card
                    val displaySpeed = if (useMetricSpeed) carSpeed else (carSpeed * 0.621371f).toInt()
                    val speedUnitStr = if (useMetricSpeed) strings.speedUnit else strings.speedUnitMph
                    val speedSourceStr = if (isGpsActive) strings.speedSourceGps else strings.speedSourceCan

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = colors.surfaceSecondary),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, colors.cardBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Filled.Speed, null, tint = colors.accent, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.height(4.dp))
                            Text(strings.carSpeedLabel, color = colors.textSecondary, fontSize = 10.sp)
                            Text("$displaySpeed $speedUnitStr", color = colors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(2.dp))
                            Text(speedSourceStr, color = colors.accent, fontSize = 9.sp)
                        }
                    }
                    // Temp Card
                    val displayTemp = if (ambientTemp <= -100 || ambientTemp == Int.MIN_VALUE || ambientTemp > 100) {
                        "--"
                    } else if (useMetricTemp) {
                        "$ambientTemp"
                    } else {
                        "${((ambientTemp * 9 / 5) + 32)}"
                    }
                    val tempUnitStr = if (useMetricTemp) strings.tempUnit else strings.tempUnitFahrenheit

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = colors.surfaceSecondary),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, colors.cardBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Filled.Thermostat, null, tint = colors.accent, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.height(4.dp))
                            Text(strings.carTempLabel, color = colors.textSecondary, fontSize = 10.sp)
                            Text("$displayTemp$tempUnitStr", color = colors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = if (isTempSensorAvailable) strings.tempSourceCanBus else strings.tempSensorUnavailable,
                                color = if (isTempSensorAvailable) colors.accent else colors.textSecondary,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }

            // 2. VEHICLE CAN-BUS & OS INTEGRATION STATUS
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceSecondary),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, colors.accent.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DirectionsCar,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = strings.canBusIntegrationTitle,
                                color = colors.textPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = strings.canBusIntegrationDesc,
                            color = colors.textSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // 3. SYSTEM PERFORMANCE FLAGS
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceSecondary),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, colors.cardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = strings.systemOptimizationsTitle,
                            color = colors.textPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(strings.optImmersiveFullscreen, color = colors.textSecondary, fontSize = 10.sp)
                            Text(strings.statusActive, color = Color(0xFF4CAF50), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(strings.optKeepScreenOn, color = colors.textSecondary, fontSize = 10.sp)
                            Text(strings.statusActive, color = Color(0xFF4CAF50), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(strings.optBufferControl, color = colors.textSecondary, fontSize = 10.sp)
                            Text("30s - 120s", color = colors.accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SensorsSettingsPane(
    tempSource: TempSource,
    onTempSourceSelected: (TempSource) -> Unit,
    speedSource: SpeedSource,
    onSpeedSourceSelected: (SpeedSource) -> Unit,
    manualTempValue: Float,
    onManualTempValueChanged: (Float) -> Unit,
    simulatedSpeedValue: Int,
    onSimulatedSpeedValueChanged: (Int) -> Unit,
    speedMultiplier: Float,
    onSpeedMultiplierChanged: (Float) -> Unit,
    tempOffset: Float,
    onTempOffsetChanged: (Float) -> Unit,
    useMetricSpeed: Boolean,
    onUseMetricSpeedToggle: (Boolean) -> Unit,
    useMetricTemp: Boolean,
    onUseMetricTempToggle: (Boolean) -> Unit,
    ambientHardwareTemp: Float?,
    batteryTemp: Float?,
    cpuSysfsTemp: Float?,
    canBusTemp: Float?,
    gpsSpeed: Int?,
    networkSpeed: Int?,
    canBusSpeed: Int?,
    isGpsActive: Boolean,
    isCompactScreenMode: Boolean = false,
    scanReport: ComprehensiveSensorScanReport = ComprehensiveSensorScanReport(),
    liveSensorValues: Map<String, List<Float>> = emptyMap(),
    customTempSensorName: String? = null,
    customSysfsTempPath: String? = null,
    customSpeedSensorName: String? = null,
    onSelectCustomTempSensor: (String) -> Unit = {},
    onSelectCustomSysfsThermal: (String) -> Unit = {},
    onSelectCustomSpeedSensor: (String) -> Unit = {},
    onStartLiveAudit: () -> Unit = {},
    onStopLiveAudit: () -> Unit = {},
    onRequestLocationPermission: () -> Unit = {},
    onRescanSensors: () -> Unit = {},
    onAutoDetect: () -> String = { "" },
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val colors = LocalCarColors.current
    var showScannerDialog by remember { mutableStateOf(false) }

    if (showScannerDialog) {
        SensorScannerDialog(
            scanReport = scanReport,
            liveSensorValues = liveSensorValues,
            customTempSensorName = customTempSensorName,
            customSysfsTempPath = customSysfsTempPath,
            customSpeedSensorName = customSpeedSensorName,
            activeTempSource = tempSource,
            activeSpeedSource = speedSource,
            initialCategoryIndex = 0,
            onSelectCustomTempSensor = onSelectCustomTempSensor,
            onSelectCustomSysfsThermal = onSelectCustomSysfsThermal,
            onSelectCustomSpeedSensor = onSelectCustomSpeedSensor,
            onSelectTempSource = onTempSourceSelected,
            onSelectSpeedSource = onSpeedSourceSelected,
            onAutoDetect = onAutoDetect,
            onRequestLocationPermission = onRequestLocationPermission,
            onRescan = {
                onStartLiveAudit()
                onRescanSensors()
            },
            onDismiss = {
                onStopLiveAudit()
                showScannerDialog = false
            }
        )
    }

    Column(
        modifier = modifier
            .background(colors.surface, RoundedCornerShape(16.dp))
            .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp))
            .padding(if (isCompactScreenMode) 10.dp else 14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(if (isCompactScreenMode) 10.dp else 14.dp)
    ) {
        // Section Header
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(
                imageVector = Icons.Filled.Sensors,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = strings.sensorsSectionTitle,
                    color = colors.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = strings.sensorsSectionSubtitle,
                    color = colors.textSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // --- PROMINENT SENSOR SCANNER ACTION CARD ---
        Card(
            onClick = {
                onStartLiveAudit()
                onRescanSensors()
                showScannerDialog = true
            },
            colors = CardDefaults.cardColors(containerColor = colors.accent.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.5.dp, colors.accent)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F172A))
                            .border(1.5.dp, colors.accent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Troubleshoot,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = strings.scanSensorsButton,
                            color = colors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = strings.scanSensorsSubtitle,
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                        if (scanReport.totalFoundCount > 0) {
                            Text(
                                text = String.format(strings.sensorsFoundCountSummary, scanReport.totalFoundCount),
                                color = colors.accent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        HorizontalDivider(color = colors.cardBorder.copy(alpha = 0.5f))

        // 1. LIVE SENSOR DIAGNOSTICS CARD
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surfaceSecondary, RoundedCornerShape(12.dp))
                .border(1.dp, colors.accent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(
                    imageVector = Icons.Filled.Troubleshoot,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = strings.sensorDiagnosticsTitle,
                    color = colors.accent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = strings.sensorDiagnosticsSubtitle,
                color = colors.textSecondary,
                fontSize = 11.sp
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SensorDiagnosticBadge(
                    label = strings.tempSourceAmbientHardware,
                    value = ambientHardwareTemp?.let { "${it.toInt()}°C" } ?: strings.sensorStatusNotFound,
                    isAvailable = ambientHardwareTemp != null,
                    modifier = Modifier.weight(1f)
                )
                SensorDiagnosticBadge(
                    label = strings.tempSourceBattery,
                    value = batteryTemp?.let { "${it.toInt()}°C" } ?: strings.sensorStatusNotFound,
                    isAvailable = batteryTemp != null,
                    modifier = Modifier.weight(1f)
                )
                SensorDiagnosticBadge(
                    label = strings.tempSourceCpuSysfs,
                    value = cpuSysfsTemp?.let { "${it.toInt()}°C" } ?: strings.sensorStatusNotFound,
                    isAvailable = cpuSysfsTemp != null,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SensorDiagnosticBadge(
                    label = strings.speedSourceGpsHighPrecision,
                    value = gpsSpeed?.let { "$it km/h" } ?: if (isGpsActive) strings.sensorStatusActive else strings.sensorStatusNotFound,
                    isAvailable = isGpsActive || gpsSpeed != null,
                    modifier = Modifier.weight(1f)
                )
                SensorDiagnosticBadge(
                    label = strings.tempSourceCanBus,
                    value = canBusTemp?.let { "${it.toInt()}°C" } ?: canBusSpeed?.let { "$it km/h" } ?: strings.sensorStatusNotFound,
                    isAvailable = canBusTemp != null || canBusSpeed != null,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 2. TEMPERATURE SENSOR SOURCE SELECTION
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = strings.tempSensorSourceTitle,
                color = colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = strings.tempSensorSourceSubtitle,
                color = colors.textSecondary,
                fontSize = 11.sp
            )

            val tempSources = mutableListOf(
                TempSource.AUTO to strings.tempSourceAuto,
                TempSource.AMBIENT_HARDWARE to strings.tempSourceAmbientHardware,
                TempSource.BATTERY_SENSOR to strings.tempSourceBattery,
                TempSource.CPU_SYSFS to strings.tempSourceCpuSysfs,
                TempSource.CAN_BUS to strings.tempSourceCanBus,
                TempSource.MANUAL to strings.tempSourceManual
            )
            if (!customTempSensorName.isNullOrEmpty() || tempSource == TempSource.CUSTOM_SENSOR) {
                tempSources.add(TempSource.CUSTOM_SENSOR to "${strings.tempSourceCustomSensor} (${customTempSensorName ?: ""})")
            }
            if (!customSysfsTempPath.isNullOrEmpty() || tempSource == TempSource.CUSTOM_SYSFS) {
                val shortPath = customSysfsTempPath?.substringAfterLast("/sys/class/") ?: ""
                tempSources.add(TempSource.CUSTOM_SYSFS to "${strings.tempSourceCustomSysfs} ($shortPath)")
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                tempSources.chunked(2).forEach { rowSources ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        rowSources.forEach { (src, labelStr) ->
                            val isSelected = tempSource == src
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) colors.accent.copy(alpha = 0.2f) else colors.surfaceSecondary)
                                    .border(
                                        1.dp,
                                        if (isSelected) colors.accent else colors.cardBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onTempSourceSelected(src) }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = labelStr,
                                    color = if (isSelected) colors.accent else colors.textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        if (rowSources.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            if (tempSource == TempSource.MANUAL) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceSecondary, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = strings.manualTempPresetTitle,
                        color = colors.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { onManualTempValueChanged((manualTempValue - 1f).coerceIn(-20f, 60f)) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Filled.Remove, null, tint = colors.accent)
                        }
                        Text(
                            text = "${manualTempValue.toInt()}°C",
                            color = colors.accent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { onManualTempValueChanged((manualTempValue + 1f).coerceIn(-20f, 60f)) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Filled.Add, null, tint = colors.accent)
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = colors.cardBorder.copy(alpha = 0.5f))

        // 3. SPEED SENSOR SOURCE SELECTION
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = strings.speedSensorSourceTitle,
                color = colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = strings.speedSensorSourceSubtitle,
                color = colors.textSecondary,
                fontSize = 11.sp
            )

            val speedSources = mutableListOf(
                SpeedSource.AUTO_GPS to strings.tempSourceAuto,
                SpeedSource.GPS_HIGH_PRECISION to strings.speedSourceGpsHighPrecision,
                SpeedSource.NETWORK_CELL to strings.speedSourceNetworkCell,
                SpeedSource.CAN_BUS to strings.speedSourceCan,
                SpeedSource.SIMULATED to strings.speedSourceSimulated
            )
            if (!customSpeedSensorName.isNullOrEmpty() || speedSource == SpeedSource.CUSTOM_SENSOR) {
                speedSources.add(SpeedSource.CUSTOM_SENSOR to "${strings.speedSourceCustomSensor} (${customSpeedSensorName ?: ""})")
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                speedSources.chunked(2).forEach { rowSources ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        rowSources.forEach { (src, labelStr) ->
                            val isSelected = speedSource == src
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) colors.accent.copy(alpha = 0.2f) else colors.surfaceSecondary)
                                    .border(
                                        1.dp,
                                        if (isSelected) colors.accent else colors.cardBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onSpeedSourceSelected(src) }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = labelStr,
                                    color = if (isSelected) colors.accent else colors.textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        if (rowSources.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            if (speedSource == SpeedSource.SIMULATED) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceSecondary, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = strings.simulatedSpeedPresetTitle,
                        color = colors.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { onSimulatedSpeedValueChanged((simulatedSpeedValue - 5).coerceIn(0, 260)) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Filled.Remove, null, tint = colors.accent)
                        }
                        Text(
                            text = "$simulatedSpeedValue km/h",
                            color = colors.accent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { onSimulatedSpeedValueChanged((simulatedSpeedValue + 5).coerceIn(0, 260)) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Filled.Add, null, tint = colors.accent)
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = colors.cardBorder.copy(alpha = 0.5f))

        // 4. CALIBRATIONS & UNITS
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = strings.speedMultiplierLabel,
                    color = colors.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = String.format("%.2fx", speedMultiplier),
                    color = colors.accent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = speedMultiplier,
                onValueChange = onSpeedMultiplierChanged,
                valueRange = 0.8f..1.2f,
                steps = 8,
                colors = SliderDefaults.colors(
                    thumbColor = colors.accent,
                    activeTrackColor = colors.accent
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = strings.tempCalibrationLabel,
                    color = colors.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${if (tempOffset > 0) "+" else ""}${tempOffset.toInt()}°C",
                    color = colors.accent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = tempOffset,
                onValueChange = onTempOffsetChanged,
                valueRange = -10f..10f,
                steps = 20,
                colors = SliderDefaults.colors(
                    thumbColor = colors.accent,
                    activeTrackColor = colors.accent
                )
            )

            SettingsToggleRow(
                title = strings.showSpeed,
                icon = Icons.Filled.Speed,
                checked = useMetricSpeed,
                onCheckedChange = onUseMetricSpeedToggle
            )
            SettingsToggleRow(
                title = strings.showTemp,
                icon = Icons.Filled.Thermostat,
                checked = useMetricTemp,
                onCheckedChange = onUseMetricTempToggle
            )
        }
    }
}

@Composable
private fun SensorDiagnosticBadge(
    label: String,
    value: String,
    isAvailable: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = LocalCarColors.current
    Column(
        modifier = modifier
            .background(
                if (isAvailable) colors.accent.copy(alpha = 0.15f) else colors.surface.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
            )
            .border(
                1.dp,
                if (isAvailable) colors.accent.copy(alpha = 0.4f) else colors.cardBorder.copy(alpha = 0.4f),
                RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = colors.textSecondary,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            color = if (isAvailable) colors.accent else colors.textPrimary.copy(alpha = 0.5f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SensorScannerDialog(
    scanReport: ComprehensiveSensorScanReport,
    liveSensorValues: Map<String, List<Float>>,
    customTempSensorName: String?,
    customSysfsTempPath: String?,
    customSpeedSensorName: String?,
    activeTempSource: TempSource,
    activeSpeedSource: SpeedSource,
    initialCategoryIndex: Int = 0,
    onSelectCustomTempSensor: (String) -> Unit,
    onSelectCustomSysfsThermal: (String) -> Unit,
    onSelectCustomSpeedSensor: (String) -> Unit,
    onSelectTempSource: (TempSource) -> Unit = {},
    onSelectSpeedSource: (SpeedSource) -> Unit = {},
    onAutoDetect: () -> String = { "" },
    onRequestLocationPermission: () -> Unit,
    onRescan: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val colors = LocalCarColors.current
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isCompactScreenMode = configuration.screenHeightDp < 500 || configuration.screenWidthDp < 700

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryIndex by remember(initialCategoryIndex) { mutableIntStateOf(initialCategoryIndex) } // 0: All, 1: Temp, 2: Speed, 3: Thermal, 4: GPS
    var autoDetectResult by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.95f),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, colors.cardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(if (isCompactScreenMode) 8.dp else 16.dp),
                    verticalArrangement = Arrangement.spacedBy(if (isCompactScreenMode) 8.dp else 12.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(if (isCompactScreenMode) 6.dp else 10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(if (isCompactScreenMode) 32.dp else 40.dp)
                                    .background(colors.accent.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Troubleshoot,
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(if (isCompactScreenMode) 18.dp else 22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = strings.sensorScannerDialogTitle,
                                    color = colors.textPrimary,
                                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = String.format(strings.sensorsFoundCountSummary, scanReport.totalFoundCount),
                                    color = colors.accent,
                                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(if (isCompactScreenMode) 4.dp else 8.dp)
                        ) {
                            Button(
                                onClick = onRescan,
                                colors = ButtonDefaults.buttonColors(containerColor = colors.accent.copy(alpha = 0.2f)),
                                border = BorderStroke(1.dp, colors.accent),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = if (isCompactScreenMode) 8.dp else 12.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(if (isCompactScreenMode) 14.dp else 16.dp)
                                )
                                if (!isCompactScreenMode) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(strings.rescanSensors, color = colors.accent, style = androidx.compose.material3.MaterialTheme.typography.labelMedium)
                                }
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(if (isCompactScreenMode) 32.dp else 36.dp)
                                    .background(colors.surfaceSecondary, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = null,
                                    tint = colors.textPrimary,
                                    modifier = Modifier.size(if (isCompactScreenMode) 18.dp else 20.dp)
                                )
                            }
                        }
                    }

                    // Smart Auto-Detect & Bind Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = colors.accent.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, colors.accent.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AutoFixHigh,
                                        contentDescription = null,
                                        tint = colors.accent,
                                        modifier = Modifier.size(if (isCompactScreenMode) 18.dp else 22.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "كشف وتفعيل ذكي",
                                            color = colors.textPrimary,
                                            style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (!isCompactScreenMode) {
                                            Text(
                                                text = "فحص عتاد السيارة، المعالج، الـ GPS، والبطارية وربط الأفضل",
                                                color = colors.textSecondary,
                                                style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                }
                                Button(
                                    onClick = {
                                        autoDetectResult = onAutoDetect()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.FlashOn,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(if (isCompactScreenMode) 14.dp else 16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("تفعيل الآن", color = Color.Black, style = androidx.compose.material3.MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (!autoDetectResult.isNullOrEmpty()) {
                                Text(
                                    text = autoDetectResult!!,
                                    color = colors.accent,
                                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // GPS & Location Status Banner
                    val showLocationBanner = !scanReport.isLocationPermissionGranted || !scanReport.isGpsEnabled
                    if (showLocationBanner) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF332000)),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFFFB300))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(if (isCompactScreenMode) 6.dp else 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.GpsOff,
                                        contentDescription = null,
                                        tint = Color(0xFFFFB300),
                                        modifier = Modifier.size(if (isCompactScreenMode) 18.dp else 20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = if (!scanReport.isLocationPermissionGranted) strings.gpsPermissionDenied else strings.gpsPermissionRequired,
                                            color = Color.White,
                                            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (!scanReport.isLocationPermissionGranted) {
                                        Button(
                                            onClick = onRequestLocationPermission,
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(strings.grantLocationPermission, color = Color.Black, style = androidx.compose.material3.MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    if (!scanReport.isGpsEnabled) {
                                        Button(
                                            onClick = {
                                                try {
                                                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                    }
                                                    context.startActivity(intent)
                                                } catch (ignored: Exception) {}
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(strings.openLocationSettings, color = Color.Black, style = androidx.compose.material3.MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Search TextField
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(strings.searchSensorsPlaceholder, style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = colors.textSecondary) },
                        leadingIcon = { Icon(Icons.Filled.Search, null, tint = colors.accent, modifier = Modifier.size(if (isCompactScreenMode) 16.dp else 20.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Filled.Clear, null, tint = colors.textSecondary, modifier = Modifier.size(if (isCompactScreenMode) 16.dp else 18.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        textStyle = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.accent,
                            unfocusedBorderColor = colors.cardBorder,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        )
                    )

                    // Category Filter Chips (Scrollable LazyRow)
                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val categoryFilters = listOf(
                            strings.filterAll,
                            strings.filterTemp,
                            strings.filterSpeed,
                            strings.filterThermalSysfs,
                            strings.filterGps
                        )
                        items(categoryFilters.size) { idx ->
                            val label = categoryFilters[idx]
                            val isSelected = selectedCategoryIndex == idx
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) colors.accent else colors.surfaceSecondary)
                                    .clickable { selectedCategoryIndex = idx }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.Black else colors.textPrimary,
                                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = colors.cardBorder.copy(alpha = 0.3f))

                    // Scanned items list
                    val query = searchQuery.trim().lowercase()

                    val filteredSensors = scanReport.hardwareSensors.filter { s ->
                        val matchesSearch = query.isEmpty() ||
                                s.name.lowercase().contains(query) ||
                                s.vendor.lowercase().contains(query) ||
                                s.typeString.lowercase().contains(query)
                        val matchesCategory = when (selectedCategoryIndex) {
                            1 -> s.isTemperatureSensor || s.name.lowercase().contains("temp") || s.name.lowercase().contains("thermal")
                            2 -> s.isSpeedOrMotionSensor || s.name.lowercase().contains("speed") || s.name.lowercase().contains("accel") || s.name.lowercase().contains("gyro")
                            3 -> false
                            4 -> false
                            else -> true
                        }
                        matchesSearch && matchesCategory
                    }

                    val filteredSysfs = scanReport.sysfsThermalZones.filter { z ->
                        val matchesSearch = query.isEmpty() ||
                                z.typeName.lowercase().contains(query) ||
                                z.path.lowercase().contains(query)
                        val matchesCategory = selectedCategoryIndex == 0 || selectedCategoryIndex == 1 || selectedCategoryIndex == 3
                        matchesSearch && matchesCategory
                    }

                    val filteredProviders = scanReport.locationProviders.filter { p ->
                        val matchesSearch = query.isEmpty() || p.name.lowercase().contains(query)
                        val matchesCategory = selectedCategoryIndex == 0 || selectedCategoryIndex == 2 || selectedCategoryIndex == 4
                        matchesSearch && matchesCategory
                    }

                    val showBattery = (selectedCategoryIndex == 0 || selectedCategoryIndex == 1) &&
                            scanReport.batteryTempCelsius != null &&
                            (query.isEmpty() || "battery".contains(query) || "بطارية".contains(query))

                    val totalVisible = filteredSensors.size + filteredSysfs.size + filteredProviders.size + (if (showBattery) 1 else 0)

                    if (totalVisible == 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = strings.noSensorsFoundForFilter,
                                color = colors.textSecondary,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 1. Battery Thermistor Card
                            if (showBattery) {
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = colors.surfaceSecondary),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, if (activeTempSource == TempSource.BATTERY_SENSOR) colors.accent else colors.cardBorder)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(if (isCompactScreenMode) 8.dp else 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.BatteryChargingFull,
                                                    contentDescription = null,
                                                    tint = colors.accent,
                                                    modifier = Modifier.size(if (isCompactScreenMode) 20.dp else 24.dp)
                                                )
                                                Column {
                                                    Text(
                                                        text = strings.tempSourceBattery,
                                                        color = colors.textPrimary,
                                                        style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "Android System Battery",
                                                        color = colors.textSecondary,
                                                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                                                    )
                                                    Text(
                                                        text = "${String.format(Locale.US, "%.1f", scanReport.batteryTempCelsius)}°C",
                                                        color = colors.accent,
                                                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            if (activeTempSource == TempSource.BATTERY_SENSOR) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(colors.accent, RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(strings.activeSensorBadge, color = Color.Black, style = androidx.compose.material3.MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                }
                                            } else {
                                                Button(
                                                    onClick = { onSelectTempSource(TempSource.BATTERY_SENSOR) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent.copy(alpha = 0.2f)),
                                                    border = BorderStroke(1.dp, colors.accent),
                                                    shape = RoundedCornerShape(6.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                                ) {
                                                    Text(strings.setAsTempSensor, color = colors.accent, style = androidx.compose.material3.MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            items(filteredSysfs) { zone ->
                                val isZoneActive = activeTempSource == TempSource.CUSTOM_SYSFS && customSysfsTempPath == zone.path
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = colors.surfaceSecondary),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, if (isZoneActive) colors.accent else colors.cardBorder)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(if (isCompactScreenMode) 8.dp else 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Memory,
                                                contentDescription = null,
                                                tint = colors.accent,
                                                modifier = Modifier.size(if (isCompactScreenMode) 20.dp else 24.dp)
                                            )
                                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Text(
                                                        text = zone.typeName,
                                                        color = colors.textPrimary,
                                                        style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .background(colors.accent.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                                    ) {
                                                        Text("sysfs", color = colors.accent, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
                                                    }
                                                }
                                                Text(
                                                    text = zone.path,
                                                    color = colors.textSecondary,
                                                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = zone.currentTempCelsius?.let { "${String.format(Locale.US, "%.1f", it)}°C" } ?: strings.sensorStatusNotFound,
                                                    color = if (zone.currentTempCelsius != null) colors.accent else colors.textSecondary,
                                                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        if (isZoneActive) {
                                            Box(
                                                modifier = Modifier
                                                    .background(colors.accent, RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(strings.activeSensorBadge, color = Color.Black, style = androidx.compose.material3.MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                            }
                                        } else {
                                            Button(
                                                onClick = { onSelectCustomSysfsThermal(zone.path) },
                                                colors = ButtonDefaults.buttonColors(containerColor = colors.accent.copy(alpha = 0.2f)),
                                                border = BorderStroke(1.dp, colors.accent),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text(strings.setAsTempSensor, color = colors.accent, style = androidx.compose.material3.MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            // 3. Location Providers
                            items(filteredProviders) { prov ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = colors.surfaceSecondary),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, colors.cardBorder)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(if (isCompactScreenMode) 8.dp else 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = if (prov.name.contains("gps", ignoreCase = true)) Icons.Filled.GpsFixed else Icons.Filled.CellTower,
                                                contentDescription = null,
                                                tint = colors.accent,
                                                modifier = Modifier.size(if (isCompactScreenMode) 20.dp else 24.dp)
                                            )
                                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Text(
                                                        text = prov.name.uppercase(),
                                                        color = colors.textPrimary,
                                                        style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .background(if (prov.isEnabled) colors.accent.copy(alpha = 0.2f) else Color.Red.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                                    ) {
                                                        Text(if (prov.isEnabled) "مفعل" else "معطل", color = if (prov.isEnabled) colors.accent else Color.Red, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
                                                    }
                                                }
                                                val speedText = prov.currentSpeedKmh?.let { "$it km/h" } ?: "--"
                                                Text(
                                                    text = "السرعة: $speedText",
                                                    color = colors.accent,
                                                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        val isGpsActive = (activeSpeedSource == SpeedSource.AUTO_GPS || activeSpeedSource == SpeedSource.GPS_HIGH_PRECISION) && prov.isEnabled
                                        if (isGpsActive) {
                                            Box(
                                                modifier = Modifier
                                                    .background(colors.accent, RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(strings.activeSensorBadge, color = Color.Black, style = androidx.compose.material3.MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                            }
                                        } else if (prov.isEnabled) {
                                            Button(
                                                onClick = { onSelectSpeedSource(SpeedSource.AUTO_GPS) },
                                                colors = ButtonDefaults.buttonColors(containerColor = colors.accent.copy(alpha = 0.2f)),
                                                border = BorderStroke(1.dp, colors.accent),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text(strings.setAsSpeedSensor, color = colors.accent, style = androidx.compose.material3.MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            items(filteredSensors) { sensor ->
                                val isTempActive = activeTempSource == TempSource.CUSTOM_SENSOR && customTempSensorName == sensor.name
                                val isSpeedActive = activeSpeedSource == SpeedSource.CUSTOM_SENSOR && customSpeedSensorName == sensor.name
                                val liveVals = liveSensorValues[sensor.name] ?: sensor.currentValues

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = colors.surfaceSecondary),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, if (isTempActive || isSpeedActive) colors.accent else colors.cardBorder)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(if (isCompactScreenMode) 8.dp else 10.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = when {
                                                        sensor.isTemperatureSensor -> Icons.Filled.Thermostat
                                                        sensor.isSpeedOrMotionSensor -> Icons.Filled.Speed
                                                        else -> Icons.Filled.Sensors
                                                    },
                                                    contentDescription = null,
                                                    tint = colors.accent,
                                                    modifier = Modifier.size(if (isCompactScreenMode) 20.dp else 24.dp)
                                                )
                                                Column {
                                                    Text(
                                                        text = sensor.name,
                                                        color = colors.textPrimary,
                                                        style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "${sensor.vendor} | ${sensor.typeString}",
                                                        color = colors.textSecondary,
                                                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                                                    )
                                                }
                                            }

                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                if (sensor.isTemperatureSensor) {
                                                    if (isTempActive) {
                                                        Box(
                                                            modifier = Modifier
                                                                .background(colors.accent, RoundedCornerShape(6.dp))
                                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                        ) {
                                                            Text(strings.activeSensorBadge, color = Color.Black, style = androidx.compose.material3.MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                        }
                                                    } else {
                                                        Button(
                                                            onClick = { onSelectCustomTempSensor(sensor.name) },
                                                            colors = ButtonDefaults.buttonColors(containerColor = colors.accent.copy(alpha = 0.2f)),
                                                            border = BorderStroke(1.dp, colors.accent),
                                                            shape = RoundedCornerShape(6.dp),
                                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                                        ) {
                                                            Text(strings.setAsTempSensor, color = colors.accent, style = androidx.compose.material3.MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                } else if (sensor.isSpeedOrMotionSensor) {
                                                    if (isSpeedActive) {
                                                        Box(
                                                            modifier = Modifier
                                                                .background(colors.accent, RoundedCornerShape(6.dp))
                                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                        ) {
                                                            Text(strings.activeSensorBadge, color = Color.Black, style = androidx.compose.material3.MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                        }
                                                    } else {
                                                        Button(
                                                            onClick = { onSelectCustomSpeedSensor(sensor.name) },
                                                            colors = ButtonDefaults.buttonColors(containerColor = colors.accent.copy(alpha = 0.2f)),
                                                            border = BorderStroke(1.dp, colors.accent),
                                                            shape = RoundedCornerShape(6.dp),
                                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                                        ) {
                                                            Text(strings.setAsSpeedSensor, color = colors.accent, style = androidx.compose.material3.MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Real-time live values bar
                                        if (liveVals.isNotEmpty()) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(colors.surface, RoundedCornerShape(6.dp))
                                                    .padding(6.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                liveVals.forEachIndexed { idx, v ->
                                                    val label = when(idx) { 0 -> "X"; 1 -> "Y"; 2 -> "Z"; else -> "V" }
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                        Text(text = "$label:", color = colors.textSecondary, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
                                                        Text(text = String.format(Locale.US, "%.2f", v), color = colors.accent, style = androidx.compose.material3.MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
