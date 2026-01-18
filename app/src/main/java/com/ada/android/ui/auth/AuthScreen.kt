package com.ada.android.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.ComponentActivity
import com.ada.android.auth.ADAAuthManager
import com.ada.android.auth.EnrollmentData
import kotlinx.coroutines.launch

/**
 * A.D.A. Authentication Screen
 * Login and enrollment UI for Android
 */
@Composable
fun AuthScreen(
    authManager: ADAAuthManager,
    activity: ComponentActivity
) {
    var showEnrollment by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF000000),
                        Color(0xFF0D0D1A)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            // Logo
            LogoSection()
            
            Spacer(modifier = Modifier.height(48.dp))
            
            if (showEnrollment) {
                EnrollmentForm(
                    authManager = authManager,
                    onBack = { showEnrollment = false }
                )
            } else {
                // Login Form
                LoginForm(
                    authManager = authManager,
                    activity = activity,
                    onCreateAccount = { showEnrollment = true }
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Footer
            Text(
                text = "Secure biometric authentication",
                color = Color.Gray,
                fontSize = 12.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LogoSection() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(160.dp)
    ) {
        // Outer glow
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x4D00BCD4),
                            Color(0x1A9C27B0),
                            Color.Transparent
                        )
                    )
                )
        )
        
        // Inner circle
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color(0x1A00BCD4))
        ) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = "A.D.A.",
                tint = Color(0xFF00BCD4),
                modifier = Modifier.size(60.dp)
            )
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Text(
        text = "A.D.A.",
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
    )
    
    Text(
        text = "Autonomous Digital Assistant",
        fontSize = 14.sp,
        color = Color.Gray
    )
}

@Composable
private fun LoginForm(
    authManager: ADAAuthManager,
    activity: ComponentActivity,
    onCreateAccount: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var passcode by remember { mutableStateOf("") }
    var showPasscode by remember { mutableStateOf(false) }
    
    val isLoading by authManager.isLoading.collectAsState()
    val errorMessage by authManager.errorMessage.collectAsState()
    val scope = rememberCoroutineScope()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sign In",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Username
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00BCD4),
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = Color(0xFF00BCD4),
                    unfocusedLabelColor = Color.Gray
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Passcode
            OutlinedTextField(
                value = passcode,
                onValueChange = { passcode = it },
                label = { Text("Passcode") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = { showPasscode = !showPasscode }) {
                        Icon(
                            if (showPasscode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle visibility"
                        )
                    }
                },
                singleLine = true,
                visualTransformation = if (showPasscode) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00BCD4),
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = Color(0xFF00BCD4),
                    unfocusedLabelColor = Color.Gray
                )
            )
            
            // Error message
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    color = Color(0xFFE57373),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Login button
            Button(
                onClick = {
                    scope.launch {
                        authManager.login(username, passcode)
                    }
                },
                enabled = username.isNotBlank() && passcode.isNotBlank() && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00BCD4)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Sign In", fontWeight = FontWeight.SemiBold)
                }
            }
            
            // Biometric login
            if (authManager.biometricAvailable && authManager.hasExistingAccount) {
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = {
                        authManager.authenticateWithBiometrics(
                            activity = activity,
                            onSuccess = { /* Will be handled by state */ },
                            onError = { /* Show toast */ }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF00BCD4)
                    )
                ) {
                    Icon(
                        Icons.Default.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign in with Biometrics")
                }
            }
        }
    }
    
    Spacer(modifier = Modifier.height(24.dp))
    
    // Create account link
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Don't have an account?",
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onCreateAccount) {
            Text(
                text = "Create Account",
                color = Color(0xFF00BCD4),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun EnrollmentForm(
    authManager: ADAAuthManager,
    onBack: () -> Unit
) {
    var currentStep by remember { mutableStateOf(1) }
    var username by remember { mutableStateOf("") }
    var passcode by remember { mutableStateOf("") }
    var confirmPasscode by remember { mutableStateOf("") }
    var agreedToTerms by remember { mutableStateOf(false) }
    
    val isLoading by authManager.isLoading.collectAsState()
    val scope = rememberCoroutineScope()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back button and title row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Create Account",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(48.dp))
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Step indicator
            StepIndicator(currentStep = currentStep, totalSteps = 4)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            when (currentStep) {
                1 -> CredentialsStep(
                    username = username,
                    onUsernameChange = { username = it },
                    passcode = passcode,
                    onPasscodeChange = { passcode = it },
                    confirmPasscode = confirmPasscode,
                    onConfirmPasscodeChange = { confirmPasscode = it }
                )
                2 -> VoiceEnrollmentStep()
                3 -> FaceEnrollmentStep()
                4 -> ReviewStep(
                    username = username,
                    agreedToTerms = agreedToTerms,
                    onAgreedToTermsChange = { agreedToTerms = it }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentStep > 1) {
                    OutlinedButton(
                        onClick = { currentStep-- },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Back")
                    }
                }
                
                Button(
                    onClick = {
                        if (currentStep < 4) {
                            currentStep++
                        } else {
                            // Submit enrollment
                            scope.launch {
                                authManager.enrollUser(
                                    EnrollmentData(
                                        username = username,
                                        passcode = passcode,
                                        voiceData = null, // TODO: Add voice data
                                        faceData = null   // TODO: Add face data
                                    )
                                )
                            }
                        }
                    },
                    enabled = when (currentStep) {
                        1 -> username.length >= 3 && passcode.length >= 8 && passcode == confirmPasscode
                        4 -> agreedToTerms && !isLoading
                        else -> true
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00BCD4)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading && currentStep == 4) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(if (currentStep == 4) "Complete" else "Continue")
                        if (currentStep < 4) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(currentStep: Int, totalSteps: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(totalSteps) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (index < currentStep) Color(0xFF00BCD4) else Color(0xFF333344)
                    )
            )
        }
    }
}

@Composable
private fun CredentialsStep(
    username: String,
    onUsernameChange: (String) -> Unit,
    passcode: String,
    onPasscodeChange: (String) -> Unit,
    confirmPasscode: String,
    onConfirmPasscodeChange: (String) -> Unit
) {
    var showPasscode by remember { mutableStateOf(false) }
    
    Column {
        Text(
            text = "Step 1: Account Setup",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Text(
            text = "Create your username and secure passcode",
            color = Color.Gray,
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text("Username") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00BCD4),
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            isError = username.isNotEmpty() && username.length < 3,
            supportingText = if (username.isNotEmpty() && username.length < 3) {
                { Text("Username must be at least 3 characters", color = Color(0xFFE57373)) }
            } else null
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = passcode,
            onValueChange = onPasscodeChange,
            label = { Text("Passcode") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { showPasscode = !showPasscode }) {
                    Icon(
                        if (showPasscode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle visibility"
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (showPasscode) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00BCD4),
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            isError = passcode.isNotEmpty() && passcode.length < 8,
            supportingText = if (passcode.isNotEmpty() && passcode.length < 8) {
                { Text("Passcode must be at least 8 characters", color = Color(0xFFE57373)) }
            } else null
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = confirmPasscode,
            onValueChange = onConfirmPasscodeChange,
            label = { Text("Confirm Passcode") },
            leadingIcon = { Icon(Icons.Default.LockOpen, contentDescription = null) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00BCD4),
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            isError = confirmPasscode.isNotEmpty() && passcode != confirmPasscode,
            supportingText = if (confirmPasscode.isNotEmpty() && passcode != confirmPasscode) {
                { Text("Passcodes do not match", color = Color(0xFFE57373)) }
            } else null
        )
    }
}

@Composable
private fun VoiceEnrollmentStep() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Step 2: Voice Profile",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Text(
            text = "Record your voice for authentication",
            color = Color.Gray,
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Voice recording UI
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E3A5F))
        ) {
            Icon(
                Icons.Default.Mic,
                contentDescription = "Record",
                tint = Color(0xFF00BCD4),
                modifier = Modifier.size(48.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Tap to start recording",
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Say: \"Hello A.D.A., this is my voice for authentication.\"",
            color = Color(0xFF00BCD4),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun FaceEnrollmentStep() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Step 3: Face Recognition",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Text(
            text = "Capture your face for visual authentication",
            color = Color.Gray,
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Camera preview placeholder
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E3A5F))
        ) {
            Icon(
                Icons.Default.CameraAlt,
                contentDescription = "Camera",
                tint = Color(0xFF00BCD4),
                modifier = Modifier.size(64.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { /* Capture photo */ },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00BCD4)
            )
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Capture Face")
        }
    }
}

@Composable
private fun ReviewStep(
    username: String,
    agreedToTerms: Boolean,
    onAgreedToTermsChange: (Boolean) -> Unit
) {
    Column {
        Text(
            text = "Step 4: Review & Complete",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Text(
            text = "Review your enrollment and accept terms",
            color = Color.Gray,
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Summary
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF252538))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SummaryRow("Username", username)
                SummaryRow("Voice Profile", "✓ Recorded")
                SummaryRow("Face Recognition", "✓ Captured")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Terms
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = agreedToTerms,
                onCheckedChange = onAgreedToTermsChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF00BCD4)
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "I agree to the terms and conditions",
                color = Color.White,
                fontSize = 14.sp
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Security notice
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A5F))
        ) {
            Row(modifier = Modifier.padding(12.dp)) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    tint = Color(0xFF00BCD4)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Your biometric data is encrypted and stored securely.",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray)
        Text(text = value, color = Color.White)
    }
}
