package org.zenith26.fest.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import org.zenith26.fest.data.model.EventCreate
import org.zenith26.fest.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(
    isAdding: Boolean,
    alertState: Pair<Boolean, String>?,
    onDismiss: () -> Unit,
    onSubmit: (EventCreate) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Technical") }
    var icon by remember { mutableStateOf("📅") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("2026-11-15") }
    var time by remember { mutableStateOf("10:00 AM") }
    var venue by remember { mutableStateOf("") }
    var capacityStr by remember { mutableStateOf("50") }
    var feeStr by remember { mutableStateOf("0") }

    var categoryExpanded by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Technical", "Cultural", "Sports", "Gaming", "Workshop")

    LaunchedEffect(alertState) {
        if (alertState != null && alertState.first) {
            delay(1500)
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate750)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate850)
                        .border(androidx.compose.foundation.BorderStroke(1.dp, Slate800))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "➕", fontSize = 18.sp)
                        Text(
                            text = "Add New Event",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Slate400)
                    }
                }

                // Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Alert Message Box
                    if (alertState != null || validationError != null) {
                        val isSuccess = alertState?.first == true
                        val msg = validationError ?: alertState?.second ?: ""
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSuccess) Emerald950.copy(alpha = 0.9f) else Rose950.copy(alpha = 0.9f))
                                .border(1.dp, if (isSuccess) Emerald800 else Rose800, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = msg,
                                color = if (isSuccess) Emerald300 else Rose300,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Title
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Title *", color = Slate300, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it; validationError = null },
                            placeholder = { Text("e.g. Photography Walk", color = Slate400, fontSize = 13.sp) },
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
                    }

                    // Category & Icon Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Category Dropdown
                        Column(modifier = Modifier.weight(2f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Category *", color = Slate300, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Box {
                                OutlinedTextField(
                                    value = category,
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth().clickable { categoryExpanded = true },
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
                                DropdownMenu(
                                    expanded = categoryExpanded,
                                    onDismissRequest = { categoryExpanded = false },
                                    modifier = Modifier.background(Slate850)
                                ) {
                                    categories.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text(cat, color = Color.White) },
                                            onClick = {
                                                category = cat
                                                categoryExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Icon Emoji
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Icon (Emoji)", color = Slate300, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            OutlinedTextField(
                                value = icon,
                                onValueChange = { icon = it },
                                placeholder = { Text("📷", color = Slate400) },
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
                        }
                    }

                    // Description
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Description", color = Slate300, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = { Text("Event details and guidelines...", color = Slate400, fontSize = 13.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
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
                    }

                    // Date & Time Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Event Date *", color = Slate300, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            OutlinedTextField(
                                value = date,
                                onValueChange = { date = it },
                                placeholder = { Text("YYYY-MM-DD", color = Slate400) },
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
                        }

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Event Time *", color = Slate300, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            OutlinedTextField(
                                value = time,
                                onValueChange = { time = it },
                                placeholder = { Text("10:00 AM", color = Slate400) },
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
                        }
                    }

                    // Venue, Capacity, Fee Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1.3f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Venue *", color = Slate300, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            OutlinedTextField(
                                value = venue,
                                onValueChange = { venue = it },
                                placeholder = { Text("Main Hall", color = Slate400) },
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
                        }

                        Column(modifier = Modifier.weight(0.9f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Capacity *", color = Slate300, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            OutlinedTextField(
                                value = capacityStr,
                                onValueChange = { capacityStr = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                        }

                        Column(modifier = Modifier.weight(0.8f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Fee (₹)", color = Slate300, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            OutlinedTextField(
                                value = feeStr,
                                onValueChange = { feeStr = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            val cap = capacityStr.toIntOrNull()
                            val fee = feeStr.toIntOrNull() ?: 0
                            if (title.isBlank() || venue.isBlank() || date.isBlank() || time.isBlank() || cap == null || cap <= 0) {
                                validationError = "Please fill all required fields (*). Capacity must be > 0."
                                return@Button
                            }
                            onSubmit(
                                EventCreate(
                                    title = title.trim(),
                                    category = category,
                                    description = if (description.isNotBlank()) description.trim() else null,
                                    eventDate = date.trim(),
                                    eventTime = time.trim(),
                                    venue = venue.trim(),
                                    capacity = cap,
                                    fee = fee,
                                    icon = if (icon.isNotBlank()) icon.trim() else "📅"
                                )
                            )
                        },
                        enabled = !isAdding,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Purple600),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isAdding) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Creating Event...", fontSize = 13.sp)
                        } else {
                            Text("Create Event", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
