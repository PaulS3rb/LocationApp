package com.example.locationapp.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.locationapp.ui.components.AuthHeader
import com.example.locationapp.ui.theme.*

@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit = {},
    onSignupSuccess: () -> Unit = {}
) {
    var isSignup by remember { mutableStateOf(true) }

    // Login state
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }

    // Signup state
    var signupName by remember { mutableStateOf("") }
    var signupEmail by remember { mutableStateOf("") }
    var signupPassword by remember { mutableStateOf("") }
    var signupConfirmPassword by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        // Background Image
        AsyncImage(
            model = "https://images.unsplash.com/photo-1554878516-1691fd114521?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=1080",
            contentDescription = "City skyline",
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.2f),
            contentScale = ContentScale.Crop
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AuthHeader()

            Spacer(modifier = Modifier.height(32.dp))

            // Auth Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 450.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = if (isSignup) "Create Account" else "Welcome Back",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isSignup)
                            "Sign up to start tracking your travels"
                        else
                            "Sign in to continue your journey",
                        fontSize = 14.sp,
                        color = SecondaryColor
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (isSignup) {
                        SignupForm(
                            name = signupName,
                            onNameChange = { signupName = it },
                            email = signupEmail,
                            onEmailChange = { signupEmail = it },
                            password = signupPassword,
                            onPasswordChange = { signupPassword = it },
                            confirmPassword = signupConfirmPassword,
                            onConfirmPasswordChange = { signupConfirmPassword = it },
                            onSignup = {
                                // TODO: Add validation and API call
                                println("Signup: $signupName, $signupEmail")
                                onSignupSuccess()
                            },
                            onSwitchToLogin = { isSignup = false },
                            onGoogleSignIn = {
                                // TODO: Implement Google Sign In
                                println("Google Sign In clicked")
                            }
                        )
                    } else {
                        LoginForm(
                            email = loginEmail,
                            onEmailChange = { loginEmail = it },
                            password = loginPassword,
                            onPasswordChange = { loginPassword = it },
                            onLogin = {
                                // TODO: Add validation and API call
                                println("Login: $loginEmail")
                                onLoginSuccess()
                            },
                            onSwitchToSignup = { isSignup = true },
                            onForgotPassword = {
                                // TODO: Implement forgot password
                                println("Forgot password clicked")
                            },
                            onGoogleSignIn = {
                                // TODO: Implement Google Sign In
                                println("Google Sign In clicked")
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer
            Text(
                text = "By continuing, you agree to our Terms of Service and Privacy Policy",
                fontSize = 12.sp,
                color = SecondaryColor,
                textAlign = TextAlign.Center
            )
        }
    }
}