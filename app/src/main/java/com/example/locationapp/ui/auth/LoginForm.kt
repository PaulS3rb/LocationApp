package com.example.locationapp.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.locationapp.ui.components.AuthTextField
import com.example.locationapp.ui.components.SocialDivider
import com.example.locationapp.ui.theme.*

@Composable
fun LoginForm(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onSwitchToSignup: () -> Unit,
    onForgotPassword: () -> Unit,
    onGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Email Field
        AuthTextField(
            value = email,
            onValueChange = onEmailChange,
            label = "Email",
            placeholder = "your@email.com",
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email
        )

        // Password Field
        AuthTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = "Password",
            placeholder = "••••••••",
            leadingIcon = Icons.Default.Lock,
            isPassword = true
        )

        // Forgot Password
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onForgotPassword,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "Forgot password?",
                    fontSize = 14.sp,
                    color = SecondaryColor
                )
            }
        }

        // Login Button
        Button(
            onClick = onLogin,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryColor,
                contentColor = BackgroundColor
            ),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text("Log In", modifier = Modifier.padding(vertical = 4.dp))
        }

        // Switch to Signup
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Don't have an account? ",
                fontSize = 14.sp,
                color = SecondaryColor
            )
            TextButton(
                onClick = onSwitchToSignup,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "Sign Up",
                    fontSize = 14.sp,
                    color = TextColor
                )
            }
        }

        SocialDivider()

        // Google Button
        OutlinedButton(
            onClick = onGoogleSignIn,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = TextColor
            ),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text("Google", modifier = Modifier.padding(vertical = 4.dp))
        }
    }
}