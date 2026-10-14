package org.zenith26.fest.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import org.zenith26.fest.data.model.EventOut
import org.zenith26.fest.ui.common.CategoryBadge
import org.zenith26.fest.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailDialog(
    event: EventOut,
    isSubmitting: Boolean,
    alertState: Pair<Boolean, String>?,
    onDismiss: () -> Unit,
    onSubmitRegistration: (name: String, roll: String, email: String, phone: String?) -> Unit
) {
    var studentName by remember { mutableStateOf("") }
    var rollNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var errName by remember { mutableStateOf<String?>(null) }
    var errRoll by remember { mutableStateOf<String?>(null) }
    var errEmail by remember { mutableStateOf<String?>(null) }

    val isFull = event.availableSeats <= 0

    // Auto-close on success after 2 seconds matching web app
    LaunchedEffect(alertState) {
        if (alertState != null && alertState.first) {
            delay(2000)
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
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Modal Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate850)
                        .border(androidx.compose.foundation.BorderStroke(1.dp, Slate800))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Purple900.copy(alpha = 0.5f))
                                .border(1.dp, Purple700.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = event.icon ?: "✨", fontSize = 24.sp)
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CategoryBadge(category = event.category)
                                val feeText = if (event.fee > 0) "₹${event.fee}" else "Free"
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Emerald950)
                                        .border(1.dp, Emerald800, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = feeText,
                                        color = Emerald300,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = event.title,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Slate400
                        )
                    }
                }

                // Modal Body (Scrollable)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Event Details Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Slate800.copy(alpha = 0.6f))
                            .border(1.dp, Slate700.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Date", color = Slate400, fontSize = 11.sp)
                            Text(
                                text = event.eventDate,
                                color = Slate200,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Time", color = Slate400, fontSize = 11.sp)
                            Text(
                                text = event.eventTime,
                                color = Slate200,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text(text = "Venue", color = Slate400, fontSize = 11.sp)
                            Text(
                                text = event.venue,
                                color = Slate200,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }

                    // Description Section
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "ABOUT THIS EVENT",
                            color = Slate400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Slate850.copy(alpha = 0.7f))
                                .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = event.description ?: "Join us for this exciting event at Zenith '26!",
                                color = Slate300,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    // Available Seats Banner Callout
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Slate800)
                            .border(1.dp, Slate700, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Available Seats:",
                            color = Slate300,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (isFull) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Rose950)
                                    .border(1.dp, Rose800, CircleShape)
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "0 seats available • FULL",
                                    color = Rose300,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Emerald950)
                                    .border(1.dp, Emerald800, CircleShape)
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "${event.availableSeats} / ${event.capacity} seats remaining",
                                    color = Emerald300,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Divider(color = Slate800)

                    // Registration Form Section
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "📝", fontSize = 16.sp)
                            Text(
                                text = "Student Registration",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Form Alert Box
                        if (alertState != null) {
                            val isSuccess = alertState.first
                            val alertMsg = alertState.second
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSuccess) Emerald950.copy(alpha = 0.9f) else Rose950.copy(alpha = 0.9f))
                                    .border(1.dp, if (isSuccess) Emerald800 else Rose800, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = alertMsg,
                                    color = if (isSuccess) Emerald300 else Rose300,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Field 1: Student Name
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Student Name *",
                                color = Slate300,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            OutlinedTextField(
                                value = studentName,
                                onValueChange = {
                                    studentName = it
                                    errName = null
                                },
                                placeholder = { Text("e.g. Aarav Mehta", color = Slate400, fontSize = 13.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                isError = errName != null,
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
                            if (errName != null) {
                                Text(text = errName!!, color = Rose400, fontSize = 11.sp)
                            }
                        }

                        // Field 2: Roll Number
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Roll Number *",
                                color = Slate300,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            OutlinedTextField(
                                value = rollNumber,
                                onValueChange = {
                                    rollNumber = it
                                    errRoll = null
                                },
                                placeholder = { Text("e.g. CSE2024001", color = Slate400, fontSize = 13.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                isError = errRoll != null,
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
                            if (errRoll != null) {
                                Text(text = errRoll!!, color = Rose400, fontSize = 11.sp)
                            }
                        }

                        // Field 3: Email Address
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Email Address *",
                                color = Slate300,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            OutlinedTextField(
                                value = email,
                                onValueChange = {
                                    email = it
                                    errEmail = null
                                },
                                placeholder = { Text("e.g. student@example.edu", color = Slate400, fontSize = 13.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                isError = errEmail != null,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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
                            if (errEmail != null) {
                                Text(text = errEmail!!, color = Rose400, fontSize = 11.sp)
                            }
                        }

                        // Field 4: Phone (Optional)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Phone Number ",
                                    color = Slate300,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "(Optional)",
                                    color = Slate400,
                                    fontSize = 11.sp
                                )
                            }
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                placeholder = { Text("e.g. 9876543210", color = Slate400, fontSize = 13.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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

                        Spacer(modifier = Modifier.height(4.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                // Client-side validation checks
                                var hasError = false
                                if (studentName.isBlank()) {
                                    errName = "Please enter your full name."
                                    hasError = true
                                }
                                if (rollNumber.isBlank()) {
                                    errRoll = "Please enter your college roll number."
                                    hasError = true
                                }
                                if (email.isBlank()) {
                                    errEmail = "Please enter your email address."
                                    hasError = true
                                } else if (!email.contains("@")) {
                                    errEmail = "Email must contain '@'."
                                    hasError = true
                                }

                                if (!hasError && !isFull && !isSubmitting) {
                                    onSubmitRegistration(studentName, rollNumber, email, phone)
                                }
                            },
                            enabled = !isFull && !isSubmitting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Purple600,
                                disabledContainerColor = Slate750
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Submitting registration...", fontSize = 13.sp, color = Color.White)
                            } else if (isFull) {
                                Text(text = "Registration Closed (Full)", fontSize = 13.sp, color = Slate400)
                            } else {
                                Text(
                                    text = "Register for Event",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
