package com.kcmitch.v2p.applets.ads

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kcmitch.v2p.config.AppConfig
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kcmitch.v2p.ui.theme.CoolGrey
import com.kcmitch.v2p.ui.theme.TechCyan
import com.kcmitch.v2p.ui.theme.TextLight
import com.kcmitch.v2p.ui.theme.ThemeConfig
import com.kcmitch.v2p.ui.theme.WarningAmber

@Composable
fun FixedAdFreeUpgradeCard(
    onUpgradeClick: () -> Unit,
    onLinkAccountClick: () -> Unit,
    isAdFree: Boolean,
    isLinked: Boolean,
    linkedEmail: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (ThemeConfig.isDarkTheme) Color(0xFF1E2836) else Color(0xFFE8F4F8)
        ),
        border = BorderStroke(1.dp, TechCyan.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(TechCyan.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isAdFree) Icons.Filled.CheckCircle else Icons.Filled.Star,
                        contentDescription = "Ad-Free Status",
                        tint = TechCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isAdFree) "✨ Pro-Tier Active" else "🚀 Upgrade to Ad-Free",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextLight
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isAdFree) TechCyan.copy(alpha = 0.2f) else WarningAmber.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, if (isAdFree) TechCyan.copy(alpha = 0.5f) else WarningAmber.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = if (isAdFree) "PRO ACTIVE" else "LIFETIME",
                                style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isAdFree) TechCyan else WarningAmber),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = if (isAdFree) {
                            if (isLinked) "Linked to Google Account ($linkedEmail). All ads permanently removed."
                            else "All ads permanently removed on this device. Link your Google Account to protect your purchase."
                        } else {
                            "Permanently remove all banner and video ads across the app. Enjoy maximum performance without distractions."
                        },
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = CoolGrey,
                            lineHeight = 15.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (!isAdFree) {
                Button(
                    onClick = onUpgradeClick,
                    colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color(0xFF12181F),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (AppConfig.adFreeUpgradeTestMode) "UPGRADE TO AD-FREE — $2.99 ONE-TIME (TEST MODE)" else "UPGRADE TO AD-FREE — $2.99 ONE-TIME",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF12181F),
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onLinkAccountClick,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TechCyan),
                    border = BorderStroke(1.dp, TechCyan.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isLinked) "LINKED: $linkedEmail" else "🔗 LINK ACTIVE GOOGLE ACCOUNT",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }
        }
    }
}
