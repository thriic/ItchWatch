package com.thriic.itchwatch.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thriic.core.model.Platform
import com.thriic.itchwatch.R

@Composable
fun PlatformRow(platforms: List<Platform>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val iconModifier = Modifier.size(16.dp)
        platforms.forEach {
            PlatformIcon(it, iconModifier)
        }
    }
}

@Composable
fun PlatformIcon(platform: Platform, iconModifier: Modifier = Modifier.size(16.dp)) {
    when (platform) {
        Platform.WINDOWS -> Icon(
            painterResource(R.drawable.windows),
            contentDescription = "windows",
            modifier = iconModifier
        )

        Platform.LINUX -> Icon(
            painterResource(R.drawable.linux),
            contentDescription = "linux",
            modifier = iconModifier
        )

        Platform.MACOS -> Icon(
            painterResource(R.drawable.mac),
            contentDescription = "macos",
            modifier = iconModifier.offset(y = (-1).dp)
        )



        Platform.ANDROID -> Icon(
            painterResource(R.drawable.android),
            contentDescription = "android",
            modifier = iconModifier
        )

        Platform.WEB -> Icon(
            painterResource(R.drawable.web),
            contentDescription = "web",
            modifier = iconModifier
        )

        Platform.IOS -> { Box(modifier = iconModifier) }
        Platform.UNKNOWN -> { Box(modifier = iconModifier) }
    }
}

@Preview
@Composable
fun PlatformPreview() {
    val platforms =
        listOf(Platform.WEB, Platform.ANDROID, Platform.WINDOWS, Platform.MACOS, Platform.LINUX)
    Surface() {
        PlatformRow(platforms)
    }
}