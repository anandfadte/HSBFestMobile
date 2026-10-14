package org.zenith26.fest.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.zenith26.fest.ZenithApplication
import org.zenith26.fest.ui.theme.*

@Composable
fun ServerConfigDialog(
    onDismiss: () -> Unit,
    onUrlUpdated: () -> Unit
) {
    var urlText by remember { mutableStateOf(ZenithApplication.instance.getBaseUrl()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate750)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "🌐", fontSize = 20.sp)
                    Text(
                        text = "Backend Server Configuration",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Configure the API base URL to connect the app to your FastAPI server:",
                    color = Slate300,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                OutlinedTextField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    label = { Text("Base URL", color = Slate400, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Slate800,
                        unfocusedContainerColor = Slate800,
                        focusedBorderColor = Purple500,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    text = "Quick Presets:",
                    color = Slate400,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Button(
                    onClick = { urlText = "https://hsb-fest-mobile.vercel.app/" },
                    modifier = Modifier.fillMaxWidth().height(38.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Purple500),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(text = "▲ Vercel Cloud (hsb-fest-mobile)", color = Purple300, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { urlText = "http://10.0.2.2:8081/" },
                        modifier = Modifier.weight(1f).height(34.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = "Android Studio (10.0.2.2)", color = Cyan300, fontSize = 10.sp)
                    }

                    Button(
                        onClick = { urlText = "http://localhost:8081/" },
                        modifier = Modifier.weight(1f).height(34.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = "Localhost (:8081)", color = Emerald400, fontSize = 10.sp)
                    }
                }

                Text(
                    text = "💡 For Appetize.io or BrowserStack, enter your live Vercel HTTPS URL (e.g. https://your-project.vercel.app/). Localhost and 10.0.2.2 only work on your local machine.",
                    color = Slate400,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = "Cancel", color = Slate400)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            ZenithApplication.instance.setBaseUrl(urlText.trim())
                            onUrlUpdated()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Purple600),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "Save & Reconnect", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
