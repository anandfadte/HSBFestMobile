package org.zenith26.fest.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import org.zenith26.fest.data.model.EventOut
import org.zenith26.fest.ui.common.*
import org.zenith26.fest.ui.theme.*

@Composable
fun StudentPortalScreen(
    viewModel: StudentViewModel
) {
    val state by viewModel.uiState.collectAsState()

    val categories = listOf("All", "Technical", "Cultural", "Sports", "Gaming", "Workshop")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Hero Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Purple950.copy(alpha = 0.5f),
                                Slate900,
                                Slate900
                            )
                        )
                    )
                    .border(
                        androidx.compose.foundation.BorderStroke(1.dp, Slate800.copy(alpha = 0.8f))
                    )
                    .padding(horizontal = 20.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dates pill
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Purple500.copy(alpha = 0.12f))
                        .border(1.dp, Purple500.copy(alpha = 0.35f), CircleShape)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "📍 Nov 14 – 15, 2026 • Campus Grounds",
                        color = Purple300,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                val gradient = Brush.linearGradient(
                    colors = listOf(Purple400, Pink400, Amber300)
                )
                Text(
                    text = "Ignite Your Spark at",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Zenith '26",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    style = androidx.compose.ui.text.TextStyle(brush = gradient),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "The ultimate college arena of technology, culture, design, gaming, and sports. Explore 14 premier events and lock in your registrations early!",
                    color = Slate300,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.fillMaxWidth(0.95f)
                )
            }
        }

        // Controls Bar: Search & Result Counter
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Search Input Field
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = {
                        Text(
                            text = "Search events by title...",
                            color = Slate400,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Slate400,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.clearSearch() }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = Slate400,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Slate800.copy(alpha = 0.9f),
                        unfocusedContainerColor = Slate800.copy(alpha = 0.9f),
                        focusedBorderColor = Purple500,
                        unfocusedBorderColor = Slate700,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                // Showing count row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "Showing:", color = Slate400, fontSize = 12.sp)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Purple950.copy(alpha = 0.8f))
                                .border(1.dp, Purple800.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "${state.events.size}",
                                color = Purple300,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(text = "events", color = Slate400, fontSize = 12.sp)
                    }
                }

                // Category Filter Pills (Horizontal Scroll)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = state.selectedCategory == category
                        val categoryIcon = when (category) {
                            "Technical" -> "💻 "
                            "Cultural" -> "🎭 "
                            "Sports" -> "⚽ "
                            "Gaming" -> "🎮 "
                            "Workshop" -> "⚡ "
                            else -> ""
                        }

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) Purple600 else Slate800)
                                .border(
                                    1.dp,
                                    if (isSelected) Purple500 else Slate700,
                                    CircleShape
                                )
                                .clickable { viewModel.setCategory(category) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "$categoryIcon$category",
                                color = if (isSelected) Color.White else Slate300,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Main Content Area: Loading, Error, Empty, or Cards
        when {
            state.isLoading -> {
                item {
                    LoadingStateView(message = "Loading events...")
                }
            }

            state.errorMessage != null -> {
                item {
                    ErrorStateView(
                        message = state.errorMessage!!,
                        onRetry = { viewModel.fetchEvents() }
                    )
                }
            }

            state.events.isEmpty() -> {
                item {
                    EmptyStateView(
                        title = "No events match your search",
                        subtitle = "Try adjusting your category filter or search keywords.",
                        onReset = { viewModel.resetFilters() }
                    )
                }
            }

            else -> {
                itemsIndexed(state.events) { index, event ->
                    EventCardItem(
                        event = event,
                        onViewDetails = { viewModel.openEventDetail(event.id) }
                    )
                }
            }
        }

        // Fest Footer
        item {
            Spacer(modifier = Modifier.height(16.dp))
            FestFooter()
        }
    }

    // Modal Dialog
    if (state.selectedEventForDetail != null) {
        EventDetailDialog(
            event = state.selectedEventForDetail!!,
            isSubmitting = state.isSubmittingRegistration,
            alertState = state.registrationAlert,
            onDismiss = { viewModel.closeEventDetail() },
            onSubmitRegistration = { name, roll, email, phone ->
                viewModel.submitRegistration(
                    eventId = state.selectedEventForDetail!!.id,
                    studentName = name,
                    rollNumber = roll,
                    email = email,
                    phone = phone,
                    onSuccess = { /* Dialog auto-closes on success */ }
                )
            }
        )
    }
}

@Composable
fun EventCardItem(
    event: EventOut,
    onViewDetails: () -> Unit
) {
    val isFull = event.availableSeats <= 0
    val feeText = if (event.fee > 0) "₹${event.fee}" else "Free"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Slate800.copy(alpha = 0.75f)),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card Top Row: Icon, Category badge, Title, Fee/FULL badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Slate750),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = event.icon ?: "✨", fontSize = 22.sp)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        CategoryBadge(category = event.category)
                        Text(
                            text = event.title,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Fee or FULL badge
                if (isFull) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Rose950)
                            .border(1.dp, Rose800, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "FULL",
                            color = Rose300,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Slate900)
                            .border(1.dp, Slate700, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = feeText,
                            color = Slate300,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Description (2-line clamp)
            Text(
                text = event.description ?: "No description provided.",
                color = Slate300,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Info rows: Date & Time, Venue, Seats
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp)
                    .border(
                        androidx.compose.foundation.BorderStroke(1.dp, Slate700.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "📅", fontSize = 12.sp)
                    Text(
                        text = "${event.eventDate} • ${event.eventTime}",
                        color = Slate300,
                        fontSize = 12.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "📍", fontSize = 12.sp)
                    Text(
                        text = event.venue,
                        color = Slate300,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Seats:", color = Slate300, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text(
                        text = "${event.availableSeats} / ${event.capacity} left",
                        color = if (isFull) Rose400 else Emerald400,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // View Details Button
            Button(
                onClick = onViewDetails,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Slate750,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "View Details & Register",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(text = "→", fontSize = 13.sp)
                }
            }
        }
    }
}
