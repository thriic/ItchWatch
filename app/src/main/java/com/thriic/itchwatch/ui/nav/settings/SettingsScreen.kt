package com.thriic.itchwatch.ui.nav.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import com.thriic.itchwatch.ui.common.SearchLayout
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(){
    var expanded by remember { mutableStateOf(false) }
    var searchBarOffsetY by remember { mutableIntStateOf(0) }
    SearchLayout(
        onApplySearch = {},
        expanded = expanded,
        onExpandedChange = { expanded = it },
        title = "Title",
        searchFieldHint = "Hint",
        searchFieldState = rememberTextFieldState(),
        searchBarOffsetY = {searchBarOffsetY},
        trailingIcon = { IconButton(onClick = {}){ Icon(Icons.Default.MoreVert,contentDescription = null) } },
        //filter = TODO(),
        //floatingActionButton = TODO()
    ) { contentPadding ->
        val density = LocalDensity.current
        val searchBarConnection = remember {
            val topPaddingPx = with(density) { contentPadding.calculateTopPadding().roundToPx() }
            object : NestedScrollConnection {
                override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                    val dy = -consumed.y
//                    if (dy >= slop) {
//                        fabHidden = true
//                    } else if (dy <= -slop / 2) {
//                        fabHidden = false
//                    }
                    searchBarOffsetY = (searchBarOffsetY - dy).roundToInt().coerceIn(-topPaddingPx, 0)
                    return Offset.Zero // We never consume it
                }
            }
        }
        LazyColumn(contentPadding = contentPadding, modifier = Modifier.nestedScroll(searchBarConnection)) {
            items(100){
                Text("Item $it", modifier = Modifier.fillMaxWidth())
            }
        }
    }
}