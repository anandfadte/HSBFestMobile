package org.zenith26.fest.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.zenith26.fest.ui.theme.*

@Composable
fun FestTopAppBar(
    currentScreen: String,
    onNavigate: (String) -> Unit,
    onOpenServerConfig: () -> Unit,
    onRefresh: (() -> Unit)? = null
) {
    Surface(
        color = Slate900.copy(alpha = 0.95f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate800),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "✨", fontSize = 22.sp)
                    val gradient = Brush.linearGradient(
                        colors = listOf(Purple400, Pink400, Amber300)
                    )
                    Text(
                        text = "ZENITH '26",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        style = androidx.compose.ui.text.TextStyle(brush = gradient),
                        letterSpacing = (-0.5).sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Purple900.copy(alpha = 0.6f))
                            .border(1.dp, Purple700.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Annual Fest",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Purple300
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (onRefresh != null) {
                        IconButton(
                            onClick = onRefresh,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = Slate300,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = onOpenServerConfig,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Server Settings",
                            tint = Purple300,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Navigation Pill Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isStudent = currentScreen == "student"
                val isAdmin = currentScreen == "admin"

                // Student Portal Tab Button
                Button(
                    onClick = { onNavigate("student") },
                    modifier = Modifier.weight(1f).height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isStudent) Slate800 else Slate850,
                        contentColor = if (isStudent) Color.White else Slate400
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isStudent) Slate700 else Slate800
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Events Portal",
                        fontSize = 12.sp,
                        fontWeight = if (isStudent) FontWeight.Bold else FontWeight.Medium
                    )
                }

                // Admin Dashboard Tab Button
                Button(
                    onClick = { onNavigate("admin") },
                    modifier = Modifier.weight(1f).height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAdmin) Purple950 else Slate850,
                        contentColor = if (isAdmin) Purple300 else Slate400
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isAdmin) Purple800 else Slate800
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "⚙️", fontSize = 12.sp)
                        Text(
                            text = "Admin Dashboard",
                            fontSize = 12.sp,
                            fontWeight = if (isAdmin) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryBadge(
    category: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, borderColor) = when (category.lowercase()) {
        "technical" -> Triple(Cyan950, Cyan300, Cyan800.copy(alpha = 0.6f))
        "cultural" -> Triple(Purple950, Purple300, Purple800.copy(alpha = 0.6f))
        "sports" -> Triple(Emerald950, Emerald300, Emerald800.copy(alpha = 0.6f))
        "gaming" -> Triple(Rose950, Rose300, Rose800.copy(alpha = 0.6f))
        "workshop" -> Triple(Amber950, Amber300, Amber800.copy(alpha = 0.6f))
        else -> Triple(Slate800, Slate300, Slate700)
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(bgColor)
            .border(1.dp, borderColor, CircleShape)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = category,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun LoadingStateView(
    message: String = "Loading events..."
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = Purple500,
                strokeWidth = 3.dp,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = message,
                color = Slate300,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ErrorStateView(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Rose950.copy(alpha = 0.35f))
            .border(1.dp, Rose800.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = "⚠️", fontSize = 36.sp)
            Text(
                text = "Unable to load events",
                color = Rose300,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = message,
                color = Slate300,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Rose600),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Retry Loading",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun EmptyStateView(
    title: String = "No events match your search",
    subtitle: String = "Try adjusting your category filter or search keywords.",
    buttonText: String = "Reset Filters",
    onReset: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Slate800.copy(alpha = 0.45f))
            .border(1.dp, Slate750, RoundedCornerShape(16.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "🔍", fontSize = 36.sp)
            Text(
                text = title,
                color = Slate200,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = Slate400,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onReset,
                colors = ButtonDefaults.buttonColors(containerColor = Purple600),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = buttonText,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun FestFooter() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Slate950)
            .border(androidx.compose.foundation.BorderStroke(1.dp, Slate800))
            .padding(vertical = 16.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Zenith '26 College Fest Management System",
            color = Slate400,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Native Android Kotlin Frontend • FastAPI Backend & SQLite",
            color = Slate400.copy(alpha = 0.7f),
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
