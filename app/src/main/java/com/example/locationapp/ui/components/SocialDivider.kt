package com.example.locationapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.locationapp.ui.theme.SecondaryColor

@Composable
fun SocialDivider(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = SecondaryColor.copy(alpha = 0.3f)
        )
        Text(
            text = "Or continue with",
            modifier = Modifier.padding(horizontal = 16.dp),
            fontSize = 14.sp,
            color = SecondaryColor
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = SecondaryColor.copy(alpha = 0.3f)
        )
    }
}