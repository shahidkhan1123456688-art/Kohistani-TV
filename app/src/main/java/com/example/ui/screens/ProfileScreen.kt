package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.UserProfile
import com.example.ui.KohistaniTvViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: KohistaniTvViewModel,
    modifier: Modifier = Modifier
) {
    val loggedInUser by viewModel.userProfile.collectAsStateWithLifecycle()
    val selectedCreatorUsername by viewModel.selectedCreatorUsername.collectAsStateWithLifecycle()
    val selectedCreatorProfile by viewModel.selectedCreatorProfile.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    // Determine which profile is active for view:
    // If exploring a writer/creator on feed, show their profile. Otherwise, show self profile.
    val profile = if (selectedCreatorUsername != null) selectedCreatorProfile else loggedInUser

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Spacer(modifier = Modifier.height(54.dp))

        // Top custom nav bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectedCreatorUsername != null) {
                // Back button to return to self profile
                IconButton(onClick = { viewModel.selectCreator(null) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }

            Text(
                text = if (selectedCreatorUsername != null) "رائٹر پروفائل" else "پروفائل ڈیش بورڈ",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Settings bar: Dark Mode and Log out
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.toggleDarkMode() },
                    modifier = Modifier.testTag("dark_mode_toggle")
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Mode Theme",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                if (selectedCreatorUsername == null && loggedInUser?.isLoggedIn == true) {
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = Color.Red.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (profile == null || !profile.isLoggedIn) {
            // Profile Registration Form (Attach Phone or Google representation)
            ProfileRegistrationCard(onRegister = { id, name, bio, method ->
                viewModel.registerOrUpdateProfile(id, name, bio, method)
            })
        } else {
            // Profile Content display
            ProfileContentMain(
                profile = profile,
                isSelf = (selectedCreatorUsername == null),
                onClearExplore = { viewModel.selectCreator(null) }
            )
        }
    }
}

@Composable
fun ProfileRegistrationCard(
    onRegister: (String, String, String, String) -> Unit
) {
    var loginMethod by remember { mutableStateOf("Google") } // "Google" or "Phone"
    var usernameId by remember { mutableStateOf("") }
    var userDisplayName by remember { mutableStateOf("") }
    var userBio by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "اکاؤنٹ رجسٹریشن",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "کوہستانی ٹی وی نیٹ ورک کا حصہ بننے کے لیے درج ذیل معلومات فراہم کریں۔",
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            // Auth Choice Segment
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(24.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { loginMethod = "Google" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (loginMethod == "Google") MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (loginMethod == "Google") Color.Black else MaterialTheme.colorScheme.onBackground
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(imageVector = Icons.Default.Mail, contentDescription = "GM")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("گوگل لاگ ان", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { loginMethod = "Phone" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (loginMethod == "Phone") MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (loginMethod == "Phone") Color.Black else MaterialTheme.colorScheme.onBackground
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = "PH")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("موبائل نمبر", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Text Inputs
            OutlinedTextField(
                value = usernameId,
                onValueChange = { usernameId = it.trim().lowercase() },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("username_input"),
                label = { Text("صارف آئی ڈی (مثلاً: shahid_khan)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = userDisplayName,
                onValueChange = { userDisplayName = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("displayname_input"),
                label = { Text("آپ کا پورا نام") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            if (loginMethod == "Phone") {
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("phone_input"),
                    label = { Text("موبائل فون نمبر (+92)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            OutlinedTextField(
                value = userBio,
                onValueChange = { userBio = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("تعارف لکھیں (Bio)") },
                minLines = 2,
                maxLines = 3,
                shape = RoundedCornerShape(12.dp)
            )

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = {
                    if (usernameId.isBlank() || userDisplayName.isBlank()) {
                        errorMessage = "براہ کرم تمام لازمی فیلڈز پُر کریں!"
                    } else if (loginMethod == "Phone" && phoneNumber.isBlank()) {
                        errorMessage = "براہ کرم اپنا موبائل نمبر فراہم کریں!"
                    } else {
                        errorMessage = ""
                        onRegister(usernameId, userDisplayName, userBio, loginMethod)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("register_button"),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black)
            ) {
                Text("رجسٹریشن کریں (موبائل ریڈی)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
fun ProfileContentMain(
    profile: UserProfile,
    isSelf: Boolean,
    onClearExplore: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Large profile avatar placeholder
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Avatar",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(54.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Display Name & Login badge
        Text(
            text = profile.displayName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "@${profile.username}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            
            if (profile.loginType.isNotEmpty()) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = profile.loginType,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Profile Bio Text
        Text(
            text = profile.bio.ifEmpty { "کوئی تعارف درج نہیں کیا گیا۔" },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Followers, Following, Likes stats
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(label = "فالوورز", count = formatStatCount(profile.followersCount))
            DividerVertical()
            StatItem(label = "فالوونگ", count = formatStatCount(profile.followingCount))
            DividerVertical()
            StatItem(label = "لائیکس", count = formatStatCount(profile.likesCount))
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (!isSelf) {
            // Un-explore current creator button
            Button(
                onClick = onClearExplore,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Text("اپنی فیڈ پر واپس جائیں", color = Color.Black, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Custom local Video Creations Grid representation
        Text(
            text = "تخلیقات (Videos)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Simulated creation blocks in grid
        val mockGridCreations = listOf(
            Pair("#1 وادی داس", Color(0xFF1C2D37)),
            Pair("#2 رباب سرگرم", Color(0xFF3B1E30)),
            Pair("#3 جھومر ثقافت", Color(0xFF1E3A27)),
            Pair("#4 شو ولاگ", Color(0xFF3F3B1A)),
            Pair("#5 بزرگ نصیحتیں", Color(0xFF2C1A3F))
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(bottom = 90.dp)
        ) {
            items(mockGridCreations) { creation ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.85f),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = creation.second)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "P",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.Center)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.4f))
                                .align(Alignment.BottomStart)
                                .padding(4.dp)
                        ) {
                            Text(
                                text = creation.first,
                                color = Color.White,
                                fontSize = 10.sp,
                                maxLines = 1,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, count: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = count,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun DividerVertical() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(24.dp)
            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f))
    )
}

fun formatStatCount(number: Int): String {
    return if (number >= 1000) {
        "${String.format("%.1f", number / 1000f)}k"
    } else {
        number.toString()
    }
}
