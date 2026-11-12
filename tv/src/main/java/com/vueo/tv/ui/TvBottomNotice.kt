package com.vueo.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun BoxScope.TvBottomNotice(message: String) {
    Text(
        text = message,
        color = TvDesign.White,
        fontSize = 16.sp,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(horizontal = 24.dp)
            .padding(bottom = 34.dp)
            .background(TvDesign.SurfaceRaised.copy(alpha = .96f), RoundedCornerShape(percent = 50))
            .padding(horizontal = 24.dp, vertical = 9.dp),
    )
}
