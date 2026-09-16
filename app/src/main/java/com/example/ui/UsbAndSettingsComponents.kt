package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VolumeEntity
import com.example.localization.AppLanguage
import com.example.localization.LocalAppStrings
import com.example.theme.LocalCarColors
import com.example.theme.ThemeMode

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
    onAutoLaunchOnUsbToggle: (Boolean) -> Unit = {}
) {
    val strings = LocalAppStrings.current
    val colors = LocalCarColors.current

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isNarrow = isCompactScreenMode || maxWidth < 880.dp
        var selectedSubTab by remember { mutableIntStateOf(0) } // 0: USB Storage, 1: Settings

        if (isNarrow) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Top Segmented Switcher for Small / Narrow Screens
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surface, RoundedCornerShape(12.dp))
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val mountedCount = volumesList.count { it.isMounted }

                    // Tab 0: USB Storage
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedSubTab == 0) colors.surfaceSecondary else Color.Transparent)
                            .border(
                                width = if (selectedSubTab == 0) 1.5.dp else 0.dp,
                                color = if (selectedSubTab == 0) colors.accent else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedSubTab = 0 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Usb,
                                contentDescription = null,
                                tint = if (selectedSubTab == 0) colors.accent else colors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "${strings.storageTitle} ($mountedCount)",
                                color = if (selectedSubTab == 0) colors.textPrimary else colors.textSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (selectedSubTab == 0) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Tab 1: Settings & Display
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedSubTab == 1) colors.surfaceSecondary else Color.Transparent)
                            .border(
                                width = if (selectedSubTab == 1) 1.5.dp else 0.dp,
                                color = if (selectedSubTab == 1) colors.accent else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedSubTab = 1 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = null,
                                tint = if (selectedSubTab == 1) colors.accent else colors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = strings.tabSettings,
                                color = if (selectedSubTab == 1) colors.textPrimary else colors.textSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (selectedSubTab == 1) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Active SubTab Content taking full width
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (selectedSubTab == 0) {
                        UsbStoragePane(
                            volumesList = volumesList,
                            isScanning = isScanning,
                            scanProgress = scanProgress,
                            onTriggerScan = onTriggerScan,
                            isCompactScreenMode = true,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
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
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        } else {
            // Wide Screen: Dual Pane side-by-side
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
                        .weight(1.05f)
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
                    modifier = Modifier
                        .weight(1.15f)
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
