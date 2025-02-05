package com.thriic.itchwatch.ui.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchLayout(
    onApplySearch: (String) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    title: String?,
    searchFieldHint: String,
    searchFieldState: TextFieldState = rememberTextFieldState(),
    searchBarOffsetY: () -> Int = { 0 },
    trailingIcon: @Composable () -> Unit = {},
    filter: @Composable (() -> Unit)? = null,
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {

//searchFieldState.setTextAndPlaceCursorAtEnd(keyword)

    fun hideSearchView() {
        onExpandedChange(false)
    }

    fun onApplySearch() {
        // May have invalid whitespaces if pasted from clipboard, replace them with spaces
        val query = searchFieldState.text.trim().replace(WhitespaceRegex, " ")
        onApplySearch(query)
    }


    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                // Placeholder, fill immutable SearchBar padding
                Spacer(modifier = Modifier.statusBarsPadding().height(SearchBarDefaults.InputFieldHeight + 16.dp))
            },
            floatingActionButton = floatingActionButton,
            content = content,
        )
        // https://issuetracker.google.com/337191298
        // Workaround for can't exit SearchBar due to refocus in non-touch mode
        Box(Modifier.size(1.dp).focusable())
        val activeState = rememberCompositionActiveState()
        SearchBar(
            modifier = Modifier.align(Alignment.TopCenter).thenIf(!expanded) { offset { IntOffset(0, searchBarOffsetY()) } }
                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)),
            inputField = {
                SearchBarInputField(
                    state = searchFieldState,
                    onSearch = {
                        hideSearchView()
                        onApplySearch()
                    },
                    expanded = expanded,
                    onExpandedChange = onExpandedChange,
                    modifier = Modifier.widthIn(max = maxWidth - SearchBarHorizontalPadding * 2),
                    placeholder = {
                        val contentActive by activeState.state
                        val text = title.takeUnless { expanded || contentActive } ?: searchFieldHint
                        Text(text, overflow = TextOverflow.Ellipsis, maxLines = 1)
                    },
                    leadingIcon = {
                        if (expanded) {
                            IconButton(onClick = { hideSearchView() }) {
                                Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
                            }
                        } else {
                            IconButton(onClick = { onExpandedChange(true) }) {
                                Icon(Icons.Default.Search, contentDescription = null)
                            }
                        }
                    },
                    trailingIcon = {
                        if (expanded) {
                            AnimatedContent(targetState = searchFieldState.text.isNotEmpty()) { hasText ->
                                if (hasText) {
                                    IconButton(onClick = { searchFieldState.clearText() }) {
                                        Icon(Icons.Default.Close, contentDescription = null)
                                    }
                                }
                            }
                        } else {
                            Row {
                                trailingIcon()
                            }
                        }
                    },
                )
            },
            expanded = expanded,
            onExpandedChange = onExpandedChange,
        ) {
            activeState.Anchor()
            filter?.invoke()
//            LazyColumn(
//                modifier = Modifier.fillMaxSize(),
//                contentPadding = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom).asPaddingValues(),
//            ) {
//                // Workaround for prepending before the first item
//                item {}
//
//            }
        }
    }
}

private val WhitespaceRegex = Regex("\\s+")
private val SearchBarHorizontalPadding = 16.dp

inline fun Modifier.thenIf(condition: Boolean, crossinline block: Modifier.() -> Modifier) =
    if (condition) block() else this

@JvmInline
value class CompositionActiveState(val state: MutableState<Boolean>) {
    @Composable
    fun Anchor() = DisposableEffect(state) {
        state.value = true
        onDispose {
            state.value = false
        }
    }
}

@Composable
fun rememberCompositionActiveState() = remember {
    CompositionActiveState(mutableStateOf(false))
}