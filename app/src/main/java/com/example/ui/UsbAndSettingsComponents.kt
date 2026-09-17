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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.view.KeyEvent
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
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
    carSpeed: Int = 0
) {
    val strings = LocalAppStrings.current
    val colors = LocalCarColors.current

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isNarrow = isCompactScreenMode || maxWidth < 1200.dp
        var selectedSubTab by remember { mutableIntStateOf(0) } // 0: USB, 1: SWC, 2: CAN-Bus, 3: App Settings

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

                        if (isNext && selectedSubTab < 3) {
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
                // Top Segmented Switcher for Small / Narrow Screens (4 Tabs)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surface, RoundedCornerShape(12.dp))
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val mountedCount = volumesList.count { it.isMounted }

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
                        label = "CAN-Bus",
                        isSelected = selectedSubTab == 2,
                        onClick = { selectedSubTab = 2 }
                    )

                    // Tab 3: App Settings
                    SubTabButton(
                        icon = Icons.Filled.Settings,
                        label = strings.tabSettings,
                        isSelected = selectedSubTab == 3,
                        onClick = { selectedSubTab = 3 }
                    )
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
                            isCompactScreenMode = true,
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
                    isCompactScreenMode = isCompactScreenMode,
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
                        text = "تتبع لغة السيارة الافتراضية / Reset to System Language",
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

        // 1. Live Key Monitor (لوحة الفحص المباشر لأزرار المقود)
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

        // 2. Vehicle Preset Profiles
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(strings.swcPresetTitle, color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PresetButton(
                    title = strings.swcPresetSmartAuto,
                    isSelected = swcConfig.presetProfile == SwcPresetProfile.SMART_AUTO,
                    modifier = Modifier.weight(1f),
                    onClick = { onPresetSelected(SwcPresetProfile.SMART_AUTO) }
                )
                PresetButton(
                    title = strings.swcPresetDedicatedPause,
                    isSelected = swcConfig.presetProfile == SwcPresetProfile.DEDICATED_PAUSE_BTN,
                    modifier = Modifier.weight(1f),
                    onClick = { onPresetSelected(SwcPresetProfile.DEDICATED_PAUSE_BTN) }
                )
                PresetButton(
                    title = strings.swcPresetPeugeot,
                    isSelected = swcConfig.presetProfile == SwcPresetProfile.PEUGEOT_PSA,
                    modifier = Modifier.weight(1.2f),
                    onClick = { onPresetSelected(SwcPresetProfile.PEUGEOT_PSA) }
                )
            }
            if (swcConfig.presetProfile == SwcPresetProfile.PEUGEOT_PSA) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceSecondary),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, colors.accent)
                ) {
                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.DirectionsCar, null, tint = colors.accent, modifier = Modifier.size(16.dp))
                            Text(strings.peugeotModeBadge, color = colors.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(strings.peugeotModeHelp, color = colors.textSecondary, fontSize = 10.sp)
                    }
                }
            }
        }

        HorizontalDivider(color = colors.cardBorder)

        // 6. Extended Keys & Debounce
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(strings.swcExtendedKeysTitle, color = colors.textPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(strings.swcExtendedKeysSubtitle, color = colors.textSecondary, fontSize = 10.sp)
            }
            Switch(
                checked = swcConfig.acceptExtendedKeys,
                onCheckedChange = { onAcceptExtendedKeysToggled(it) }
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(strings.swcDebounceTitle, color = colors.textPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("${swcConfig.debounceMs} ms", color = colors.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Text(strings.swcDebounceSubtitle, color = colors.textSecondary, fontSize = 10.sp)
            Slider(
                value = swcConfig.debounceMs.toFloat(),
                onValueChange = { onDebounceChanged(it.toLong()) },
                valueRange = 100f..500f,
                steps = 8
            )
        }

        HorizontalDivider(color = colors.cardBorder)

        // Custom SWC Configuration Section (Custom Profile & Dual Button Pause / Acceleration)
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
                Icon(Icons.Filled.Tune, null, tint = colors.accent, modifier = Modifier.size(18.dp))
                Text(strings.swcCustomSectionTitle, color = colors.accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Text(strings.swcCustomSectionSubtitle, color = colors.textSecondary, fontSize = 10.sp)

            // Button 1 Code
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(strings.swcCustomBtn1Label, color = colors.textPrimary, fontSize = 11.sp)
                Text("${swcConfig.customButton1KeyCode}", color = colors.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(
                    onClick = { onCustomButton1Changed(android.view.KeyEvent.KEYCODE_MEDIA_NEXT) },
                    colors = ButtonDefaults.buttonColors(containerColor = if (swcConfig.customButton1KeyCode == android.view.KeyEvent.KEYCODE_MEDIA_NEXT) colors.accent else colors.surface),
                    modifier = Modifier.weight(1f).height(32.dp),
                    contentPadding = PaddingValues(2.dp)
                ) { Text("NEXT", fontSize = 10.sp, color = colors.textPrimary) }
                Button(
                    onClick = { onCustomButton1Changed(android.view.KeyEvent.KEYCODE_CHANNEL_UP) },
                    colors = ButtonDefaults.buttonColors(containerColor = if (swcConfig.customButton1KeyCode == android.view.KeyEvent.KEYCODE_CHANNEL_UP) colors.accent else colors.surface),
                    modifier = Modifier.weight(1f).height(32.dp),
                    contentPadding = PaddingValues(2.dp)
                ) { Text("CH_UP", fontSize = 10.sp, color = colors.textPrimary) }
                Button(
                    onClick = { onCustomButton1Changed(android.view.KeyEvent.KEYCODE_DPAD_DOWN) },
                    colors = ButtonDefaults.buttonColors(containerColor = if (swcConfig.customButton1KeyCode == android.view.KeyEvent.KEYCODE_DPAD_DOWN) colors.accent else colors.surface),
                    modifier = Modifier.weight(1f).height(32.dp),
                    contentPadding = PaddingValues(2.dp)
                ) { Text("DPAD_DN", fontSize = 9.sp, color = colors.textPrimary) }
                Button(
                    onClick = { onCustomButton1Changed(android.view.KeyEvent.KEYCODE_DPAD_RIGHT) },
                    colors = ButtonDefaults.buttonColors(containerColor = if (swcConfig.customButton1KeyCode == android.view.KeyEvent.KEYCODE_DPAD_RIGHT) colors.accent else colors.surface),
                    modifier = Modifier.weight(1f).height(32.dp),
                    contentPadding = PaddingValues(2.dp)
                ) { Text("RIGHT", fontSize = 9.sp, color = colors.textPrimary) }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Button 2 Code
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(strings.swcCustomBtn2Label, color = colors.textPrimary, fontSize = 11.sp)
                Text("${swcConfig.customButton2KeyCode}", color = colors.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(
                    onClick = { onCustomButton2Changed(android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS) },
                    colors = ButtonDefaults.buttonColors(containerColor = if (swcConfig.customButton2KeyCode == android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS) colors.accent else colors.surface),
                    modifier = Modifier.weight(1f).height(32.dp),
                    contentPadding = PaddingValues(2.dp)
                ) { Text("PREV", fontSize = 10.sp, color = colors.textPrimary) }
                Button(
                    onClick = { onCustomButton2Changed(android.view.KeyEvent.KEYCODE_CHANNEL_DOWN) },
                    colors = ButtonDefaults.buttonColors(containerColor = if (swcConfig.customButton2KeyCode == android.view.KeyEvent.KEYCODE_CHANNEL_DOWN) colors.accent else colors.surface),
                    modifier = Modifier.weight(1f).height(32.dp),
                    contentPadding = PaddingValues(2.dp)
                ) { Text("CH_DN", fontSize = 10.sp, color = colors.textPrimary) }
                Button(
                    onClick = { onCustomButton2Changed(android.view.KeyEvent.KEYCODE_DPAD_UP) },
                    colors = ButtonDefaults.buttonColors(containerColor = if (swcConfig.customButton2KeyCode == android.view.KeyEvent.KEYCODE_DPAD_UP) colors.accent else colors.surface),
                    modifier = Modifier.weight(1f).height(32.dp),
                    contentPadding = PaddingValues(2.dp)
                ) { Text("DPAD_UP", fontSize = 9.sp, color = colors.textPrimary) }
                Button(
                    onClick = { onCustomButton2Changed(android.view.KeyEvent.KEYCODE_DPAD_LEFT) },
                    colors = ButtonDefaults.buttonColors(containerColor = if (swcConfig.customButton2KeyCode == android.view.KeyEvent.KEYCODE_DPAD_LEFT) colors.accent else colors.surface),
                    modifier = Modifier.weight(1f).height(32.dp),
                    contentPadding = PaddingValues(2.dp)
                ) { Text("LEFT", fontSize = 9.sp, color = colors.textPrimary) }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Acceleration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(strings.swcCustomAccelLabel, color = colors.textPrimary, fontSize = 11.sp)
                Text("${swcConfig.customLongPressAcceleration}x", color = colors.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = swcConfig.customLongPressAcceleration.toFloat(),
                onValueChange = { onCustomAccelerationChanged(it.toInt()) },
                valueRange = 1f..4f,
                steps = 3
            )

            HorizontalDivider(color = colors.cardBorder, modifier = Modifier.padding(vertical = 4.dp))

            // Advanced Customization / Mapping (كوستوميشن)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Filled.SettingsInputComponent, null, tint = colors.accent, modifier = Modifier.size(18.dp))
                Text(strings.swcCustomizationTabTitle, color = colors.accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Text(strings.swcMapFunctionsTitle, color = colors.textPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(strings.swcMapSelectHint, color = colors.textSecondary, fontSize = 10.sp)

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Map Next
                MappingRow(
                    label = strings.swcMapNextLabel,
                    currentCode = swcConfig.customButton1KeyCode,
                    isMapping = mappingTarget == 0,
                    onMapClick = { mappingTarget = 0 },
                    strings = strings,
                    colors = colors
                )
                // Map Prev
                MappingRow(
                    label = strings.swcMapPrevLabel,
                    currentCode = swcConfig.customButton2KeyCode,
                    isMapping = mappingTarget == 1,
                    onMapClick = { mappingTarget = 1 },
                    strings = strings,
                    colors = colors
                )
                // Map Play/Pause
                MappingRow(
                    label = strings.swcMapPlayPauseLabel,
                    currentCode = swcConfig.customPlayPauseKeyCode,
                    isMapping = mappingTarget == 2,
                    onMapClick = { mappingTarget = 2 },
                    strings = strings,
                    colors = colors
                )
            }

            Button(
                onClick = { onPresetSelected(SwcPresetProfile.CUSTOM) },
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                modifier = Modifier.fillMaxWidth().height(36.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(strings.saveCustomProfileBtn, color = colors.onAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                            Text("$carSpeed km/h", color = colors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    // Temp Card
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
                            Text("$ambientTemp°C", color = colors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 2. WHEEL MODE SELECTION
            item {
                Text(strings.canBusWheelModeLabel, color = colors.textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CanBusWheelMode.values().forEach { mode ->
                        PresetButton(
                            title = when(mode) {
                                CanBusWheelMode.NORMAL -> strings.canBusWheelModeNormal
                                CanBusWheelMode.REVERSED -> strings.canBusWheelModeReversed
                                CanBusWheelMode.SMART_ROTARY -> strings.canBusWheelModeSmart
                            },
                            isSelected = swcConfig.canBusWheelMode == mode,
                            modifier = Modifier.weight(1f),
                            onClick = { onWheelModeChanged(mode) }
                        )
                    }
                }
            }

            // 3. CAN-BUS PROTOCOL
            item {
                Text(strings.canBusProtocolLabel, color = colors.textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    CanBusProtocol.values().forEach { protocol ->
                        DualModeOption(
                            title = when(protocol) {
                                CanBusProtocol.GENERIC_KEYBOARD -> strings.canBusProtocolGeneric
                                CanBusProtocol.HIWORLD -> "HiWorld / SimpleSoft"
                                CanBusProtocol.RAISE -> "Raise / SimpleSoft"
                                CanBusProtocol.SIMPLE_SOFT -> "SimpleSoft (XP)"
                                CanBusProtocol.XP_XINPU -> "XinPu (XP)"
                            },
                            isSelected = swcConfig.canBusProtocol == protocol,
                            onClick = { onProtocolChanged(protocol) }
                        )
                    }
                }
            }
        }
    }
}
