package me.anyang.wfodays.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.anyang.wfodays.R
import me.anyang.wfodays.ui.theme.BackgroundWhite
import me.anyang.wfodays.ui.theme.Gray200
import me.anyang.wfodays.ui.theme.Gray300
import me.anyang.wfodays.ui.theme.Gray400
import me.anyang.wfodays.ui.theme.PrimaryBlue
import me.anyang.wfodays.ui.theme.TextPrimary
import me.anyang.wfodays.ui.theme.TextSecondary
import me.anyang.wfodays.ui.theme.SuccessGreen

@Composable
fun PermissionGuideCard(
    title: String,
    description: String,
    isGranted: Boolean,
    onRequest: () -> Unit,
    icon: ImageVector = Icons.Default.LocationOn,
    modifier: Modifier = Modifier
) {
    val iconColor = if (isGranted) SuccessGreen else PrimaryBlue

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { if (!isGranted) onRequest() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.CheckCircle else icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            // Status or Button
            if (isGranted) {
                Text(
                    text = "Allowed",
                    fontSize = 14.sp,
                    color = SuccessGreen
                )
            } else {
                Text(
                    text = "Allow",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryBlue
                )
            }
        }

        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(Gray200)
                .padding(start = 60.dp)
        )
    }
}

@Composable
fun SettingsCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundWhite)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(Gray200)
        )

        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun SettingsGroupTitle(
    title: String,
    icon: ImageVector = Icons.Default.Settings
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Gray400,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun AutoStartGuide(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val manufacturer = android.os.Build.MANUFACTURER

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundWhite)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = stringResource(R.string.auto_start_permission_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.auto_start_permission_desc, manufacturer),
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onOpenSettings,
            modifier = Modifier.align(Alignment.End),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(stringResource(R.string.go_to_settings_button))
        }
    }
}
