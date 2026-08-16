package com.kcmitch.v2p.applets.ads

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kcmitch.v2p.ui.theme.CoolGrey
import com.kcmitch.v2p.ui.theme.TechCyan
import com.kcmitch.v2p.ui.theme.TextLight
import com.kcmitch.v2p.ui.theme.ThemeConfig
import com.kcmitch.v2p.config.AppConfig

@Composable
fun AdFreeUpgradeBanner(
    onUpgradeClick: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (ThemeConfig.isDarkTheme) Color(0xFF1E2836) else Color(0xFFE8F4F8)
        ),
        border = BorderStroke(1.dp, TechCyan.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Ad-Free Pro",
                    tint = TechCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (AppConfig.adFreeUpgradeTestMode) "Upgrade to Ad-Free ($2.99) [TEST]" else "Upgrade to Ad-Free ($2.99)",
                        style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextLight)
                    )
                    Text(
                        text = if (AppConfig.adFreeUpgradeTestMode) "Simulate lifetime Ad-Free upgrade (Test Mode)" else "Remove all banner & video ads permanently",
                        style = TextStyle(fontSize = 10.sp, color = CoolGrey)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onUpgradeClick,
                    colors = ButtonDefaults.buttonColors(containerColor = TechCyan),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
                ) {
                    Text(
                        text = "UPGRADE",
                        style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF12181F))
                    )
                }

                IconButton(
                    onClick = onDismissClick,
                    modifier = Modifier.size(28.dp).padding(start = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Dismiss",
                        tint = CoolGrey,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
