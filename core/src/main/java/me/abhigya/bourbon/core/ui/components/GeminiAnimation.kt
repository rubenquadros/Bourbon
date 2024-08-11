package me.abhigya.bourbon.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.abhigya.bourbon.core.utils.animatedGradient

@Composable
fun AnimatedLoadingGradient(
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = MaterialTheme.colorScheme.secondary,
    rows: Int = 3,
    lastRowLength: Float = 0.7f
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        for (i in 0 until rows) {
            val last = i == rows - 1
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .fillMaxWidth(if (last) lastRowLength else 1f)
                    .animatedGradient(
                        primaryColor = containerColor,
                        containerColor = primaryColor
                    )
            )
            if (!last) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}