package com.thriic.itchwatch.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

@Composable
fun RightModalDrawer(
    modifier: Modifier = Modifier,
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    drawerShape: Shape = DrawerDefaults.shape,
    drawerElevation: Dp = DrawerDefaults.ModalDrawerElevation,
    drawerContainerColor: Color = DrawerDefaults.modalContainerColor,
    drawerContentColor: Color = contentColorFor(drawerContainerColor),
    content: @Composable ColumnScope.() -> Unit,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalDrawerSheet(
            modifier = modifier,
            drawerState = drawerState,
            drawerShape = drawerShape,
        drawerContainerColor = DrawerDefaults.modalContainerColor,
        drawerContentColor = drawerContentColor,
        drawerTonalElevation = drawerElevation,
        windowInsets = DrawerDefaults.windowInsets,
            content = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    // under the hood, drawerContent is wrapped in a Column, but it would be under the Rtl layout
                    // so we create new column filling max width under the Ltr layout
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        content = content
                    )
                }
            },
        )

    }
}