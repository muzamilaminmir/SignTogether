package com.signtogether.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

data class NGOItem(
    val name: String,
    val description: String,
    val address: String,
    val phone: String,
    val website: String,
    val state: String,
    val tags: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NgoLocatorScreen(navController: NavController? = null) {
    var searchQuery by remember { mutableStateOf("") }
    
    val ngos = remember {
        listOf(
            NGOItem("Deaf Enabled Foundation (DEF)", "Empowers the deaf community with vocational training and sign language education.", "Hyderabad, Telangana", "+919000012345", "https://def.org.in", "Telangana", listOf("Education", "Vocational")),
            NGOItem("Noida Deaf Society (NDS)", "Aims to provide quality education and employment opportunities for deaf youth.", "Noida, UP", "+919876543210", "https://noidadeafsociety.org", "Uttar Pradesh", listOf("Employment", "Youth")),
            NGOItem("Assoc. of Sign Language Interpreters", "Professional body for interpreters working to ensure communication access.", "New Delhi", "+911122334455", "https://asli.in", "Delhi", listOf("Interpreters", "Advocacy")),
            NGOItem("Centum Foundation", "Working on skill development for PwD including the deaf and hard of hearing.", "New Delhi", "+919988776655", "https://centum.org", "Delhi", listOf("Skill Dev", "Corporate")),
            NGOItem("Ankur Advocacy Group", "Provides legal and social advocacy for marginalized deaf individuals.", "Mumbai, Maharashtra", "+918877665544", "https://ankur-adv.org", "Maharashtra", listOf("Legal", "Rights")),
            NGOItem("Silence", "Provides training and sustained employment to artists with severe hearing impairment.", "Kolkata, WB", "+917766554433", "https://silence.org", "West Bengal", listOf("Art", "Employment")),
            NGOItem("Sense India", "Supports deafblind individuals but also offers vast resources for the deaf.", "Ahmedabad, Gujarat", "+916655443322", "https://senseintindia.org", "Gujarat", listOf("Deafblind", "Support")),
            NGOItem("Suniye", "An NGO running a school and support group for hearing impaired children.", "New Delhi", "+915544332211", "https://suniye.in", "Delhi", listOf("Children", "School"))
        )
    }

    val filteredNgos = ngos.filter { 
        it.name.contains(searchQuery, ignoreCase = true) || 
        it.state.contains(searchQuery, ignoreCase = true) ||
        it.tags.any { tag -> tag.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "NGO Locator",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by name, state, or service tag...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredNgos) { ngo ->
                NgoCard(ngo)
            }
        }
    }
}

@Composable
fun NgoCard(ngo: NGOItem) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(ngo.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(ngo.state, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(ngo.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ngo.tags.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(tag, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ActionIcon(Icons.Default.Call, "Call") {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${ngo.phone}"))
                            context.startActivity(intent)
                        }
                        ActionIcon(Icons.Default.LocationOn, "Map") {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(ngo.address)}"))
                            context.startActivity(intent)
                        }
                        ActionIcon(Icons.Default.Info, "Website") {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ngo.website))
                            context.startActivity(intent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
    }
}
