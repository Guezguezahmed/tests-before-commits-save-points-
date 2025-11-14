package tn.esprit.dam.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import tn.esprit.dam.components.AnimatedCard
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController // Import NavHostController
import androidx.navigation.compose.rememberNavController
import tn.esprit.dam.ui.theme.DAMTheme
import tn.esprit.dam.ui.theme.WinrateProgress
import java.util.Locale
import tn.esprit.dam.components.HomeBottomNavigationBar // <-- New Import
import tn.esprit.dam.components.Screen

// --- 1. Data Structures (omitted for brevity, assume content remains the same) ---
data class StatItem(
    val value: String,
    val label: String,
    val icon: ImageVector,
    val iconColor: Color,
    val bgColor: Color
)

data class TabItem(
    val label: String,
    val count: Int,
    val isSelected: Boolean
)

data class RegisteredTeam(
    val rank: Int,
    val name: String,
    val rating: Float,
    val wins: Int
)

data class Event(
    val title: String,
    val host: String,
    val type: String,
    val typeIcon: ImageVector,
    val headerColor: Color,
    val location: String,
    val date: String,
    val time: String,
    val playersJoined: Int,
    val playersMax: Int,
    val entryFee: Int,
    val prizePool: Int,
    val status: String,
    val registeredTeams: List<RegisteredTeam>,
    val rules: List<String>
)

// --- 2. Static Data (omitted for brevity, assume content remains the same) ---

val CardBlue = Color(0xFF64B5F6)
val CardOrange = Color(0xFFFFB74D)
val CardPurple = Color(0xFF9575CD)
val PriceGreen = Color(0xFF85E4A0)

private val statData = listOf(
    StatItem("12", "Wins", Icons.Rounded.EmojiEvents, Color(0xFF60B17A), Color(0xFFE8F5E9)),
    StatItem("8", "Active", Icons.Filled.Place, Color(0xFF03A9F4), Color(0xFFE1F5FE)),
    StatItem("75%", "Win Rate", Icons.Rounded.FlashOn, Color(0xFFFF9800), Color(0xFFFFF8E1))
)

private val sampleTeams = listOf(
    RegisteredTeam(1, "Thunder FC", 4.8f, 24),
    RegisteredTeam(2, "Lightning Squad", 4.6f, 19),
    RegisteredTeam(3, "Storm United", 4.5f, 18),
    RegisteredTeam(4, "Blaze FC", 4.3f, 15),
    RegisteredTeam(5, "Phoenix Rising", 4.2f, 14),
    RegisteredTeam(6, "Eagles United", 4.0f, 12)
)

private val sampleRules = listOf(
    "Teams must arrive 15 minutes before match time",
    "Minimum 5 players per team required",
    "Match duration: 2 x 20 minutes halves",
    "Fair play and sportsmanship expected"
)

private val eventData = listOf(
    Event(
        title = "Champions Cup 2024",
        host = "Hosted by PlayPeak Official",
        type = "Knockout",
        typeIcon = Icons.Filled.EmojiEvents,
        headerColor = CardPurple,
        location = "Arena Sports Complex",
        date = "Nov 15",
        time = "18:00",
        playersJoined = 14,
        playersMax = 16,
        entryFee = 25,
        prizePool = 500,
        status = "Open",
        registeredTeams = sampleTeams.take(14),
        rules = sampleRules
    ),
    Event(
        title = "Sunday League",
        host = "Hosted by Local League Org",
        type = "League",
        typeIcon = Icons.Filled.Adjust,
        headerColor = CardBlue,
        location = "Green Field Stadium",
        date = "Nov 10",
        time = "14:00",
        playersJoined = 8,
        playersMax = 10,
        entryFee = 10,
        prizePool = 300,
        status = "Open",
        registeredTeams = sampleTeams.take(8),
        rules = sampleRules
    ),
    Event(
        title = "Quick Match",
        host = "Hosted by Riverside",
        type = "Quick Match",
        typeIcon = Icons.Filled.FlashOn,
        headerColor = CardOrange,
        location = "Riverside Arena",
        date = "Nov 8",
        time = "20:00",
        playersJoined = 10,
        playersMax = 10,
        entryFee = 5,
        prizePool = 50,
        status = "Full",
        registeredTeams = sampleTeams.take(10),
        rules = sampleRules.take(2).toList()
    ),
    Event(
        title = "Elite Knockout",
        host = "Hosted by Victory Arena",
        type = "Knockout",
        typeIcon = Icons.Filled.EmojiEvents,
        headerColor = CardPurple,
        location = "Victory Stadium",
        date = "Nov 12",
        time = "16:00",
        playersJoined = 6,
        playersMax = 8,
        entryFee = 30,
        prizePool = 800,
        status = "Open",
        registeredTeams = sampleTeams.take(6),
        rules = sampleRules
    ),
    Event(
        title = "Weekend Warriors",
        host = "Hosted by City Park Rec",
        type = "League",
        typeIcon = Icons.Filled.Adjust,
        headerColor = CardBlue,
        location = "City Park",
        date = "Nov 19",
        time = "10:00",
        playersJoined = 1,
        playersMax = 10,
        entryFee = 15,
        prizePool = 200,
        status = "Open",
        registeredTeams = sampleTeams.take(1),
        rules = sampleRules
    )
)

// --- 3. Main Screen Composables (omitted for brevity, assume content remains the same) ---
@Composable
fun StatCard(item: StatItem) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val isDarkTheme = isSystemInDarkTheme()

    val cardBg = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else surfaceColor
    val iconBg = if (isDarkTheme) item.iconColor.copy(alpha = 0.2f) else item.bgColor

    Card(
        modifier = Modifier
            .width(110.dp)
            .height(110.dp)
            .padding(4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = item.iconColor,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Column {
                Text(
                    text = item.value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = onSurfaceColor
                )
                Text(
                    text = item.label,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        placeholder = { Text("Search tournaments or stadiums...", fontSize = 14.sp) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
        trailingIcon = {
            IconButton(onClick = { /* Handle filter click */ }) {
                Icon(Icons.Filled.FilterList, contentDescription = "Filter")
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabChip(item: TabItem, onSelected: (String) -> Unit) {
    val isSelected = item.isSelected
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val outlineColor = MaterialTheme.colorScheme.outline

    FilterChip(
        selected = isSelected,
        onClick = { onSelected(item.label) },
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.label, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
                Spacer(Modifier.width(4.dp))
                Text(
                    "(${item.count})",
                    fontWeight = FontWeight.Light,
                    color = if (isSelected) primaryColor else outlineColor
                )
            }
        },
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, if (isSelected) primaryColor else outlineColor.copy(alpha = 0.5f)),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = primaryColor.copy(alpha = 0.15f),
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = onSurfaceColor,
            selectedLabelColor = primaryColor
        ),
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
fun EventCard(event: Event, onClick: () -> Unit) {
    val outlineColor = MaterialTheme.colorScheme.outline
    val textColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurface else Color.Black
    val cardElevation = if (isSystemInDarkTheme()) 1.dp else 4.dp

    val playerRatio = event.playersJoined.toFloat() / event.playersMax.toFloat()

    AnimatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        defaultElevation = cardElevation,
        pressedElevation = if (isSystemInDarkTheme()) 4.dp else 8.dp
    ) {
        Column {
            // --- Card Header (Colored Box) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(event.headerColor)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Title and Type
                    Column {
                        Text(
                            text = event.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = event.typeIcon,
                                contentDescription = event.type,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = event.type,
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                    // Status Button
                    if (event.status == "Open") {
                        Button(
                            onClick = { /* Join action */ },
                            modifier = Modifier.height(30.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) { Text("Open", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    } else if (event.status == "Full") {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) { Text("Full", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    }
                }
            }

            // --- Card Body (Details) ---
            Column(modifier = Modifier.padding(16.dp)) {
                DetailRow(
                    icon = Icons.Filled.LocationOn,
                    text = event.location,
                    textColor = textColor
                )
                Spacer(Modifier.height(8.dp))
                DetailRow(
                    icon = Icons.Filled.CalendarToday,
                    text = "${event.date} • ${event.time}",
                    textColor = textColor
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DetailRow(
                        icon = Icons.Filled.People,
                        text = "${event.playersJoined}/${event.playersMax} Players",
                        textColor = textColor,
                        iconTint = outlineColor
                    )
                    // Use prizePool for the price tag
                    if (event.prizePool > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(PriceGreen)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$${event.prizePool}",
                                color = Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Spacer(Modifier.width(1.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = playerRatio,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = WinrateProgress,
                    trackColor = outlineColor.copy(alpha = 0.2f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "${(playerRatio * 100).toInt()}% Full",
                        fontSize = 10.sp,
                        color = outlineColor.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DetailRow(icon: ImageVector, text: String, textColor: Color, iconTint: Color = MaterialTheme.colorScheme.primary) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = textColor
        )
    }
}

// --- 4. Main Screen Container (Updated for Navigation Bar) ---

@Composable
fun EventsScreen(navController: NavHostController) { // <-- Changed type to NavHostController
    var selectedEvent by remember { mutableStateOf<Event?>(null) }

    Crossfade(targetState = selectedEvent, label = "ScreenCrossfade") { event ->
        if (event == null) {
            // Event List View (Show Navigation Bar)
            EventsScreenContent(
                navController = navController,
                onEventClick = { clickedEvent ->
                    selectedEvent = clickedEvent
                }
            )
        } else {
            // Event Detail View (Hide Navigation Bar)
            EventDetailScreen(
                event = event,
                onBackClick = {
                    selectedEvent = null
                }
            )
        }
    }
}

// --- 5. Events List Content (Updated for Search and Bottom Nav) ---

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventsScreenContent(navController: NavHostController, onEventClick: (Event) -> Unit) { // <-- Changed type to NavHostController
    val scrollState = rememberScrollState()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    var selectedType by remember { mutableStateOf("All Events") }
    var searchQuery by remember { mutableStateOf("") }

    val uniqueEventTypes = listOf("All Events") + eventData.map { it.type }.distinct()

    // Step 1: Filter by Tab
    val tabFilteredEvents = if (selectedType == "All Events") {
        eventData
    } else {
        eventData.filter { it.type == selectedType }
    }

    // Step 2: Filter by Search Query
    val filteredEvents = if (searchQuery.isBlank()) {
        tabFilteredEvents
    } else {
        val lowerCaseQuery = searchQuery.lowercase(Locale.getDefault())
        tabFilteredEvents.filter { event ->
            event.title.lowercase(Locale.getDefault()).contains(lowerCaseQuery) ||
                    event.location.lowercase(Locale.getDefault()).contains(lowerCaseQuery) ||
                    event.host.lowercase(Locale.getDefault()).contains(lowerCaseQuery)
        }
    }

    // Recalculate tab counts based on ALL events (not filtered events)
    val calculatedTabData = uniqueEventTypes.map { type ->
        val count = if (type == "All Events") eventData.size else eventData.count { it.type == type }
        TabItem(label = type, count = count, isSelected = type == selectedType)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        // <-- Integrate the Bottom Navigation Bar here -->
        // This composable is only called when selectedEvent is null (i.e., we are on the list screen).
        bottomBar = {
            HomeBottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Apply content padding provided by the Scaffold, accounting for the bottom bar
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Welcome Back! \u26BD",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = onSurfaceColor
            )
            Text(
                text = "Find your next match",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                statData.forEach { StatCard(it) }
            }
            Spacer(Modifier.height(24.dp))
            SearchBar(
                query = searchQuery,
                onQueryChange = {
                    searchQuery = it
                    selectedType = "All Events"
                }
            )
            Spacer(Modifier.height(16.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth().height(40.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(calculatedTabData) { item ->
                    TabChip(item) { type ->
                        selectedType = type
                        searchQuery = ""
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Available Tournaments (${filteredEvents.size})",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = onSurfaceColor
            )
            Spacer(Modifier.height(16.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                if (filteredEvents.isEmpty()) {
                    Text(
                        text = "No events found.",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(20.dp)
                    )
                } else {
                    filteredEvents.forEachIndexed { index, event ->
                        EventCard(event, onClick = { onEventClick(event) })
                        if (index < filteredEvents.lastIndex) {
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}

// --- 6. Event Detail Screen (Navigation Bar is omitted here, as requested) ---

@Composable
fun EventDetailScreen(event: Event, onBackClick: () -> Unit) {
    val scrollState = rememberScrollState()
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    // NOTE: Scaffold here does NOT include a bottomBar
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // --- 1. Detail Header ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(event.headerColor)
                    .padding(16.dp),
                contentAlignment = Alignment.TopStart
            ) {
                // Top Bar with Back and Share
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    IconButton(
                        onClick = { /* Handle Share */ },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Share", tint = Color.White)
                    }
                }

                // Header Content (at bottom)
                Column(modifier = Modifier.align(Alignment.BottomStart)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = event.type,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = event.title,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = event.host,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }

            // --- 2. Detail Body ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-30).dp)
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                    .background(surfaceColor)
                    .padding(20.dp)
            ) {
                // Event Info
                Text(
                    text = event.location,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = onSurfaceColor
                )
                Spacer(Modifier.height(24.dp))

                // Detail Info Rows
                DetailInfoRow(icon = Icons.Filled.CalendarToday, title = "Date", value = "Friday, November 15, 2024")
                DetailInfoRow(icon = Icons.Filled.Schedule, title = "Time", value = event.time)
                DetailInfoRow(icon = Icons.Filled.People, title = "Participants", value = "${event.playersJoined}/${event.playersMax} Teams")
                DetailInfoRow(icon = Icons.Filled.AttachMoney, title = "Entry Fee", value = "$${event.entryFee}")
                DetailInfoRow(icon = Icons.Filled.EmojiEvents, title = "Prize Pool", value = "$${event.prizePool}", isLast = true)

                Spacer(Modifier.height(32.dp))

                // Registered Teams
                Text(
                    text = "Registered Teams (${event.registeredTeams.size})",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = onSurfaceColor
                )
                Spacer(Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    event.registeredTeams.forEach { team ->
                        TeamRow(team = team)
                    }
                }

                Spacer(Modifier.height(32.dp))

                // Tournament Rules
                RulesCard(rules = event.rules)

                Spacer(Modifier.height(30.dp))
            }
        }
    }
}

// --- 7. Detail Screen Helpers (omitted for brevity, assume content remains the same) ---

@Composable
fun DetailInfoRow(icon: ImageVector, title: String, value: String, isLast: Boolean = false) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Column
            Column(
                modifier = Modifier.width(48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            // Text Column (takes remaining space)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        // Divider
        if (!isLast) {
            Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)
        }
    }
}


@Composable
fun TeamRow(team: RegisteredTeam) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${team.rank}",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = team.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${team.rating} • ${team.wins} wins",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
fun RulesCard(rules: List<String>) {
    val cardBg = if (isSystemInDarkTheme()) CardBlue.copy(alpha = 0.1f) else Color(0xFFE3F2FD)
    val iconBg = if (isSystemInDarkTheme()) CardBlue.copy(alpha = 0.3f) else Color.White
    val iconTint = Color(0xFF1E88E5)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, CardBlue.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = "Rules",
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Tournament Rules",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                rules.forEach { rule ->
                    Row {
                        Text("• ", color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            text = rule,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}


// --- 8. Previews ---

@Preview(showBackground = true, name = "Events List (Light)")
@Composable
fun EventsScreenPreviewLight() {
    DAMTheme(darkTheme = false) {
        // Use rememberNavController() for previews
        EventsScreen(rememberNavController())
    }
}

@Preview(showBackground = true, name = "Events List (Dark)")
@Composable
fun EventsScreenPreviewDark() {
    DAMTheme(darkTheme = true) {
        EventsScreen(rememberNavController())
    }
}

@Preview(showBackground = true, name = "Event Detail (Light)")
@Composable
fun EventDetailPreviewLight() {
    DAMTheme(darkTheme = false) {
        EventDetailScreen(event = eventData[0], onBackClick = {})
    }
}

@Preview(showBackground = true, name = "Event Detail (Dark)")
@Composable
fun EventDetailPreviewDark() {
    DAMTheme(darkTheme = true) {
        EventDetailScreen(event = eventData[0], onBackClick = {})
    }
}