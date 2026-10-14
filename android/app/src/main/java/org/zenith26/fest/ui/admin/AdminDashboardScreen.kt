package org.zenith26.fest.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.zenith26.fest.data.model.EventOut
import org.zenith26.fest.data.model.RegistrationItemOut
import org.zenith26.fest.ui.common.CategoryBadge
import org.zenith26.fest.ui.common.FestFooter
import org.zenith26.fest.ui.common.LoadingStateView
import org.zenith26.fest.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel
) {
    val state by viewModel.uiState.collectAsState()

    var regToCancel by remember { mutableStateOf<RegistrationItemOut?>(null) }
    var eventFilterExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate900),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Coordinator Header Bar
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate850)
                    .border(androidx.compose.foundation.BorderStroke(1.dp, Slate800))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Coordinator Control Center",
                            color = Slate400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Zenith '26 Admin Suite",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.refreshAllData() },
                            colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(text = "🔄 Refresh", color = Slate300, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.openAddEventDialog() },
                            colors = ButtonDefaults.buttonColors(containerColor = Purple600),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(text = "➕ Add Event", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Stats Cards Section (4 Cards)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val stats = state.stats

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Card 1: Total Events
                    AdminStatCard(
                        modifier = Modifier.weight(1f),
                        icon = "📅",
                        iconBg = Purple950,
                        iconBorder = Purple800,
                        label = "TOTAL EVENTS",
                        value = stats?.totalEvents?.toString() ?: "--",
                        valueColor = Color.White,
                        subtext = "Across 5 categories"
                    )

                    // Card 2: Total Registrations
                    AdminStatCard(
                        modifier = Modifier.weight(1f),
                        icon = "👥",
                        iconBg = Cyan950,
                        iconBorder = Cyan800,
                        label = "TOTAL REGISTRATIONS",
                        value = stats?.totalRegistrations?.toString() ?: "--",
                        valueColor = Cyan300,
                        subtext = "Confirmed student entries"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Card 3: Capacity Utilization
                    AdminStatCard(
                        modifier = Modifier.weight(1f),
                        icon = "📊",
                        iconBg = Emerald950,
                        iconBorder = Emerald800,
                        label = "CAPACITY UTILIZATION",
                        value = if (stats != null) "${stats.overallUtilizationPct}%" else "--%",
                        valueColor = Emerald300,
                        subtext = "Of ${stats?.totalCapacity ?: "--"} total seats"
                    )

                    // Card 4: Upcoming Events
                    AdminStatCard(
                        modifier = Modifier.weight(1f),
                        icon = "⏳",
                        iconBg = Amber950,
                        iconBorder = Amber800,
                        label = "UPCOMING EVENTS",
                        value = stats?.upcomingEventsCount?.toString() ?: "--",
                        valueColor = Amber300,
                        subtext = "Scheduled for fest days"
                    )
                }
            }
        }

        // Category Distribution Breakdown
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Slate800.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🏷️ Events by Category",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = "Live counts", color = Slate400, fontSize = 11.sp)
                    }

                    val catMap = state.stats?.eventsByCategory ?: emptyMap()
                    val total = state.stats?.totalEvents ?: 1

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (catMap.isEmpty()) {
                            Text(text = "Loading category breakdown...", color = Slate400, fontSize = 11.sp)
                        } else {
                            catMap.forEach { (cat, count) ->
                                val pct = if (total > 0) ((count.toDouble() / total) * 100).toInt() else 0
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Slate850)
                                        .border(1.dp, Slate700, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    CategoryBadge(category = cat)
                                    Text(
                                        text = "$count events",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "($pct%)",
                                        color = Slate400,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Tab Navigation: Utilization Table vs Registrations
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val isTab0 = state.selectedTab == 0
                    val isTab1 = state.selectedTab == 1

                    Column(
                        modifier = Modifier
                            .clickable { viewModel.setTab(0) }
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "🏆 Event Utilization",
                            color = if (isTab0) Color.White else Slate400,
                            fontSize = 13.sp,
                            fontWeight = if (isTab0) FontWeight.Bold else FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .height(2.dp)
                                .fillMaxWidth()
                                .background(if (isTab0) Purple500 else Color.Transparent)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .clickable { viewModel.setTab(1) }
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "📋 Student Registrations",
                            color = if (isTab1) Color.White else Slate400,
                            fontSize = 13.sp,
                            fontWeight = if (isTab1) FontWeight.Bold else FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .height(2.dp)
                                .fillMaxWidth()
                                .background(if (isTab1) Purple500 else Color.Transparent)
                        )
                    }
                }
                Divider(color = Slate800)
            }
        }

        // Tab 1: Event Utilization Table Section
        if (state.selectedTab == 0) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Event Capacity & Demand Analysis",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Sorted by utilization descending. Over 90% highlighted in warning.",
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Table Search
                    OutlinedTextField(
                        value = state.tableSearchQuery,
                        onValueChange = { viewModel.setTableSearch(it) },
                        placeholder = { Text("Search table by title, venue, or category...", color = Slate400, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Slate400, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Slate850,
                            unfocusedContainerColor = Slate850,
                            focusedBorderColor = Purple500,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Processed & Sorted Events list
            val processed = state.events.map { event ->
                val registered = event.capacity - event.availableSeats
                val pct = if (event.capacity > 0) ((registered.toDouble() / event.capacity) * 1000).toInt() / 10.0 else 0.0
                Triple(event, registered, pct)
            }.filter { (event, _, _) ->
                if (state.tableSearchQuery.isBlank()) true
                else {
                    val q = state.tableSearchQuery.lowercase()
                    event.title.lowercase().contains(q) ||
                    event.category.lowercase().contains(q) ||
                    event.venue.lowercase().contains(q)
                }
            }.sortedByDescending { it.third } // Default sort by utilization descending!

            if (processed.isEmpty()) {
                item {
                    Text(
                        text = "No events match your table search.",
                        color = Slate400,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(24.dp)
                    )
                }
            } else {
                items(processed) { (event, registered, pct) ->
                    val isWarning = pct >= 90.0 // Highlight in warning color if >= 90%
                    val isFull = event.availableSeats == 0

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isWarning) Rose950.copy(alpha = 0.35f) else Slate800.copy(alpha = 0.8f)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isWarning) Rose600.copy(alpha = 0.7f) else Slate700
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = event.icon ?: "📅", fontSize = 18.sp)
                                    Text(
                                        text = event.title,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                if (isFull) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Rose950)
                                            .border(1.dp, Rose800, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = "FULL", color = Rose300, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                    }
                                } else if (isWarning) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Amber950)
                                            .border(1.dp, Amber800, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = "HIGH DEMAND", color = Amber300, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Emerald950)
                                            .border(1.dp, Emerald800, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = "OPEN", color = Emerald300, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CategoryBadge(category = event.category)
                                Text(
                                    text = "${event.eventDate} • ${event.venue}",
                                    color = Slate400,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Utilization Progress Bar Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "$registered",
                                        color = if (isFull) Rose400 else Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(text = "/ ${event.capacity} registered", color = Slate400, fontSize = 12.sp)
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    LinearProgressIndicator(
                                        progress = (pct / 100.0).toFloat().coerceIn(0f, 1f),
                                        modifier = Modifier.width(60.dp).height(6.dp).clip(RoundedCornerShape(3.dp)),
                                        color = if (isWarning) Rose500 else Emerald400,
                                        trackColor = Slate700
                                    )
                                    Text(
                                        text = "$pct%",
                                        color = if (isWarning) Rose400 else Emerald400,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Tab 2: Student Registrations Management Section
        if (state.selectedTab == 1) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Registered Students List",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Full audit log of student registrations with cancellation option.",
                        color = Slate400,
                        fontSize = 11.sp
                    )

                    // Event Filter Dropdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "Filter by Event:", color = Slate400, fontSize = 12.sp)
                        Box(modifier = Modifier.weight(1f)) {
                            val selectedTitle = state.events.firstOrNull { it.id == state.selectedFilterEventId }?.title ?: "All Events"
                            OutlinedTextField(
                                value = selectedTitle,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth().clickable { eventFilterExpanded = true },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Slate850,
                                    unfocusedContainerColor = Slate850,
                                    focusedBorderColor = Purple500,
                                    unfocusedBorderColor = Slate700,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                            DropdownMenu(
                                expanded = eventFilterExpanded,
                                onDismissRequest = { eventFilterExpanded = false },
                                modifier = Modifier.background(Slate850)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All Events", color = Color.White) },
                                    onClick = {
                                        viewModel.setFilterEventId(null)
                                        eventFilterExpanded = false
                                    }
                                )
                                state.events.forEach { ev ->
                                    DropdownMenuItem(
                                        text = { Text("${ev.title} (${ev.category})", color = Color.White) },
                                        onClick = {
                                            viewModel.setFilterEventId(ev.id)
                                            eventFilterExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (state.registrations.isEmpty()) {
                item {
                    Text(
                        text = "No registrations recorded yet.",
                        color = Slate400,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(32.dp)
                    )
                }
            } else {
                items(state.registrations) { reg ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate800.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "#${reg.id}",
                                        color = Slate400,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = reg.studentName,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Button(
                                    onClick = { regToCancel = reg },
                                    colors = ButtonDefaults.buttonColors(containerColor = Rose950),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Rose800),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(text = "Cancel", color = Rose300, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Roll: ${reg.rollNumber}",
                                    color = Slate300,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = reg.eventTitle,
                                    color = Purple300,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = reg.email,
                                    color = Slate400,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = if (!reg.phone.isNullOrBlank()) "📞 ${reg.phone}" else "",
                                    color = Slate400,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Fest Footer
        item {
            Spacer(modifier = Modifier.height(16.dp))
            FestFooter()
        }
    }

    // Cancel Registration Confirmation Dialog
    if (regToCancel != null) {
        val reg = regToCancel!!
        AlertDialog(
            onDismissRequest = { regToCancel = null },
            title = { Text(text = "Cancel Registration?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Are you sure you want to cancel the registration for \"${reg.studentName}\" in \"${reg.eventTitle}\"?",
                    color = Slate300,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelRegistration(reg.id)
                        regToCancel = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose600)
                ) {
                    Text(text = "Yes, Cancel Registration", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { regToCancel = null }) {
                    Text(text = "Keep Registration", color = Slate400)
                }
            },
            containerColor = Slate900
        )
    }

    // Add Event Dialog (Stretch Goal)
    if (state.isAddEventDialogOpen) {
        AddEventDialog(
            isAdding = state.isAddingEvent,
            alertState = state.addEventAlert,
            onDismiss = { viewModel.closeAddEventDialog() },
            onSubmit = { payload -> viewModel.createEvent(payload) }
        )
    }
}

@Composable
fun AdminStatCard(
    modifier: Modifier = Modifier,
    icon: String,
    iconBg: Color,
    iconBorder: Color,
    label: String,
    value: String,
    valueColor: Color,
    subtext: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Slate800.copy(alpha = 0.8f)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    color = Slate400,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBg)
                        .border(1.dp, iconBorder, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = icon, fontSize = 14.sp)
                }
            }

            Text(
                text = value,
                color = valueColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = subtext,
                color = Slate400,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
