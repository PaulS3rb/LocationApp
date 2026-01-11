package com.example.locationapp.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
fun SignupForm(
    name: String,
    onNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    onSignup: () -> Unit,
    onSwitchToLogin: () -> Unit,
    onGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AuthTextField(
            value = name,
            onValueChange = onNameChange,
            label = "Full Name",
            placeholder = "John Doe",
            leadingIcon = Icons.Default.Person
        )

        AuthTextField(
            value = email,
            onValueChange = onEmailChange,
            label = "Email",
            placeholder = "your@email.com",
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email
        )

        AuthTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = "Password",
            placeholder = "••••••••",
            leadingIcon = Icons.Default.Lock,
            isPassword = true
        )

        AuthTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = "Confirm Password",
            placeholder = "••••••••",
            leadingIcon = Icons.Default.Lock,
            isPassword = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onSignup,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryColor,
                contentColor = BackgroundColor
            ),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text("Sign Up", modifier = Modifier.padding(vertical = 4.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Already have an account? ",
                fontSize = 14.sp,
                color = SecondaryColor
            )
            TextButton(
                onClick = onSwitchToLogin,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "Log In",
                    fontSize = 14.sp,
                    color = TextColor
                )
            }
        }

    }
}