package com.kcmitch.v2p.applets.rating

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.kcmitch.v2p.SettingsPersistence
import com.kcmitch.v2p.ui.theme.*

/**
 * Material 3 Rate & Review App Dialog shown after a successful pipeline run.
 *
 * Features:
 * - Direct App Store redirection button to rate & review the app.
 * - "Maybe Later" button that reactivates the reminder after 24 hours (timestamp cached locally).
 * - "X" button in top corner to permanently ignore/dismiss the review popup.
 */
@Composable
fun RateAndReviewDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedStars by remember { mutableIntStateOf(5) }

    Dialog(
        onDismissRequest = {
            SettingsPersistence.setRatingMaybeLater(context)
            onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (ThemeConfig.isDarkTheme) Color(0xFF1E2836) else Color(0xFFFFFFFF)
            ),
            border = BorderStroke(1.dp, TechCyan.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with title and top-right X (Permanently Ignore)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(TechCyan.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.RateReview,
                                contentDescription = "Review Icon",
                                tint = TechCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Rate & Review V2P",
                            style = TextStyle(
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (ThemeConfig.isDarkTheme) TextLight else Color(0xFF1C1B1F)
                            )
                        )
                    }

                    // Top-right X button: Allows the user to ignore the popup permanently
                    IconButton(
                        onClick = {
                            SettingsPersistence.setRatingPermanentlyIgnored(context, true)
                            Toast.makeText(context, "Review prompt permanently dismissed.", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Permanently Dismiss Review Prompt",
                            tint = CoolGrey,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive 5-star visual rating row
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    for (i in 1..5) {
                        IconButton(
                            onClick = { selectedStars = i },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "$i Stars",
                                tint = if (i <= selectedStars) Color(0xFFFFC107) else CoolGrey.copy(alpha = 0.35f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Enjoying your frame extraction?",
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (ThemeConfig.isDarkTheme) Color.White else Color(0xFF1C1B1F),
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Your rating helps us keep the app fast, free, and constantly updated with new tools. Please leave a quick review on Google Play!",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = CoolGrey,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 1. Primary Action: Rate on App Store / Google Play
                Button(
                    onClick = {
                        SettingsPersistence.setRatingCompleted(context, true)
                        openPlayStore(context)
                        onDismiss()
                    },
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
                        text = "RATE & REVIEW ON GOOGLE PLAY",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF12181F),
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 2. Maybe Later Action: 24-hour reactivation timer stored in User Cache
                OutlinedButton(
                    onClick = {
                        SettingsPersistence.setRatingMaybeLater(context)
                        Toast.makeText(context, "We'll remind you in 24 hours!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CoolGrey),
                    border = BorderStroke(1.dp, CoolGrey.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "MAYBE LATER (24H REMINDER)",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (ThemeConfig.isDarkTheme) CoolGrey else Color(0xFF49454F)
                        )
                    )
                }
            }
        }
    }
}

/**
 * Navigates the user to the Google Play Store to rate and review the application.
 */
fun openPlayStore(context: Context) {
    val packageName = context.packageName
    val playStoreUri = Uri.parse("market://details?id=$packageName")
    val intent = Intent(Intent.ACTION_VIEW, playStoreUri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
    }
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        val webUri = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
    }
}
