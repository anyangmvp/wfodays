package me.anyang.wfodays.ui.screens

import android.content.Context
import android.Manifest
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import me.anyang.wfodays.R
import me.anyang.wfodays.data.local.PreferencesManager
import me.anyang.wfodays.location.NativeLocationManager
import me.anyang.wfodays.notification.NotificationHelper
import me.anyang.wfodays.ui.theme.BackgroundLight
import me.anyang.wfodays.ui.theme.BackgroundWhite
import me.anyang.wfodays.ui.theme.Gray100
import me.anyang.wfodays.ui.theme.Gray200
import me.anyang.wfodays.ui.theme.Gray400
import me.anyang.wfodays.ui.theme.Gray500
import me.anyang.wfodays.ui.theme.PrimaryBlue
import me.anyang.wfodays.ui.theme.PrimaryBlueDark
import me.anyang.wfodays.ui.theme.SuccessGreen
import me.anyang.wfodays.ui.theme.TextPrimary
import me.anyang.wfodays.ui.theme.TextSecondary
import me.anyang.wfodays.ui.theme.WarningOrange

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun LocationScreen(
    locationManager: NativeLocationManager
) {
    val context = LocalContext.current
    val locationState by locationManager.locationState.collectAsState()

    var currentLat by remember { mutableStateOf(0.0) }
    var currentLon by remember { mutableStateOf(0.0) }
    var currentAccuracy by remember { mutableStateOf(0f) }
    var distanceToOffice by remember { mutableStateOf(0f) }
    var isVisible by remember { mutableStateOf(false) }

    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    LaunchedEffect(locationState) {
        if (locationState is NativeLocationManager.LocationState.Success) {
            val state = locationState as NativeLocationManager.LocationState.Success
            currentLat = state.latitude
            currentLon = state.longitude
            currentAccuracy = state.accuracy
            distanceToOffice = locationManager.calculateDistanceToOffice(currentLat, currentLon)
        }
    }

    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted) {
            locationManager.startLocationUpdates()
        }
    }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        isVisible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Location",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundWhite
                )
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Current Location Card with Map
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(400)) + slideInVertically(
                    initialOffsetY = { -30 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                CurrentLocationCard(
                    locationState = locationState,
                    currentLat = currentLat,
                    currentLon = currentLon,
                    distanceToOffice = distanceToOffice,
                    isInRange = distanceToOffice <= NativeLocationManager.OFFICE_RADIUS_METERS,
                    onRefresh = { locationManager.getLastKnownLocation() },
                    onRequestPermission = { locationPermissionState.launchPermissionRequest() },
                    hasPermission = locationPermissionState.status.isGranted
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Office Center Card
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500)) + slideInVertically(
                    initialOffsetY = { 30 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                OfficeCenterCard()
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Office Radius Card
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(600)) + slideInVertically(
                    initialOffsetY = { 30 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                OfficeRadiusCard()
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Test Check-in Card
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(700)) + slideInVertically(
                    initialOffsetY = { 30 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            ) {
                TestCheckinCard(
                    isInRange = distanceToOffice <= NativeLocationManager.OFFICE_RADIUS_METERS,
                    distance = distanceToOffice,
                    context = context,
                    locationManager = locationManager
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CurrentLocationCard(
    locationState: NativeLocationManager.LocationState,
    currentLat: Double,
    currentLon: Double,
    distanceToOffice: Float,
    isInRange: Boolean,
    onRefresh: () -> Unit,
    onRequestPermission: () -> Unit,
    hasPermission: Boolean
) {
    val isLoading = locationState is NativeLocationManager.LocationState.Loading

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(BackgroundWhite)
            .padding(16.dp)
    ) {
        // Location Info
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Current Location",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    if (currentLat != 0.0 && currentLon != 0.0) {
                        Text(
                            text = "Updated now",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray500
                        )
                    }
                }
                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = if (isLoading) Gray400 else PrimaryBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = PrimaryBlue,
                        strokeWidth = 3.dp
                    )
                }
            } else if (currentLat != 0.0 && currentLon != 0.0) {
                // Distance Display
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "${distanceToOffice.toInt()}",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = " m",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isInRange) SuccessGreen.copy(alpha = 0.1f)
                            else WarningOrange.copy(alpha = 0.1f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isInRange) "Inside Office Area" else "Outside Office Area",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = if (isInRange) SuccessGreen else WarningOrange
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Coordinates
                Text(
                    text = "${String.format("%.4f", currentLat)}° N, ${String.format("%.4f", currentLon)}° E",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
            } else {
                // No location state
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Gray400,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (hasPermission) "Tap refresh to get location" else "Location permission required",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray500
                    )
                    if (!hasPermission) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onRequestPermission,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Grant Permission")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OfficeCenterCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(BackgroundWhite)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PrimaryBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Office Center",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = NativeLocationManager.OFFICE_NAME,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "${String.format("%.4f", NativeLocationManager.OFFICE_LATITUDE)}° N, ${String.format("%.4f", NativeLocationManager.OFFICE_LONGITUDE)}° E",
            style = MaterialTheme.typography.bodySmall,
            color = Gray500
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OfficeRadiusCard() {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val scope = rememberCoroutineScope()
    var selectedRadius by remember { mutableFloatStateOf(500f) }
    val radiusOptions = listOf(300f, 500f, 800f, 1000f)

    // Load saved radius
    LaunchedEffect(Unit) {
        preferencesManager.officeRadius.collect { radius ->
            selectedRadius = radius
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = Color.Black.copy(alpha = 0.05f)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundWhite)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Office Radius",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = "Detection range for auto check-in",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Text(
                text = "${selectedRadius.toInt()} m",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryBlue
            )
        }

        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(Gray200)
        )

        // Radius Options
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                radiusOptions.forEach { radius ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (selectedRadius == radius) PrimaryBlue
                                else Gray100
                            )
                            .clickable {
                                selectedRadius = radius
                                scope.launch {
                                    preferencesManager.setOfficeRadius(radius)
                                }
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${radius.toInt()} m",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (selectedRadius == radius) Color.White else TextSecondary
                        )
                    }
                }
            }
        }

        // Slider
        Slider(
            value = selectedRadius,
            onValueChange = { selectedRadius = it },
            onValueChangeFinished = {
                scope.launch {
                    preferencesManager.setOfficeRadius(selectedRadius)
                }
            },
            valueRange = 100f..2000f,
            steps = 18,
            modifier = Modifier.padding(horizontal = 16.dp),
            colors = SliderDefaults.colors(
                thumbColor = PrimaryBlue,
                activeTrackColor = PrimaryBlue,
                inactiveTrackColor = Gray200
            )
        )

        // Range labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("100 m", style = MaterialTheme.typography.labelSmall, color = Gray400)
            Text("2000 m", style = MaterialTheme.typography.labelSmall, color = Gray400)
        }
    }
}

@Composable
private fun TestCheckinCard(
    isInRange: Boolean,
    distance: Float,
    context: Context,
    locationManager: NativeLocationManager
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = Color.Black.copy(alpha = 0.05f)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundWhite)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PrimaryBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Test Check-in",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "Simulate a check-in to verify your current location.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = {
                val distanceDisplay = if (distance <= NativeLocationManager.OFFICE_RADIUS_METERS) {
                    "${distance.toInt()} m"
                } else {
                    String.format("%.1f km", distance / 1000)
                }

                if (isInRange) {
                    NotificationHelper.showAttendanceNotification(
                        context,
                        java.time.LocalDate.now(),
                        "Office Check-in Successful",
                        "Distance to office: $distanceDisplay - WFO"
                    )
                } else {
                    NotificationHelper.showAttendanceNotification(
                        context,
                        java.time.LocalDate.now(),
                        "Remote Work Recorded",
                        "Distance to office: $distanceDisplay - WFH"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = PrimaryBlue
            )
        ) {
            Text("Test Now")
        }
    }
}
