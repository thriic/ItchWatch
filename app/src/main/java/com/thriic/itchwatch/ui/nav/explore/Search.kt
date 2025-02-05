package com.thriic.itchwatch.ui.nav.explore

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.thriic.core.formatTimeDifference
import com.thriic.core.model.SearchSortType
import com.thriic.core.model.SearchTag
import com.thriic.core.model.containsTag
import com.thriic.core.network.model.SearchResult
import com.thriic.itchwatch.R
import com.thriic.itchwatch.ui.common.GameInfoItem
import com.thriic.itchwatch.ui.common.PlatformRow
import com.thriic.itchwatch.ui.common.SearchLayout
import com.thriic.itchwatch.ui.detail.DetailScreen
import kotlin.math.roundToInt

@OptIn(
    ExperimentalMaterial3AdaptiveApi::class, ExperimentalSharedTransitionApi::class,
    ExperimentalLayoutApi::class
)
@Composable
fun SearchScreen(viewModel: ExploreViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val toastMsg by viewModel.toastMessage.collectAsState()
    LaunchedEffect(toastMsg) {
        if (toastMsg != null) {
            Toast.makeText(
                context,
                toastMsg,
                Toast.LENGTH_SHORT,
            ).show()
            viewModel.sendMessage(null)
        }
    }

    var searchFieldState = rememberTextFieldState()
    var expanded by rememberSaveable { mutableStateOf(false) }
    var selectedTags by rememberSaveable { mutableStateOf(listOf<SearchTag>()) }
    val navigator = rememberListDetailPaneScaffoldNavigator<String>()
    BackHandler(navigator.currentDestination != null && navigator.currentDestination?.pane == ListDetailPaneScaffoldRole.Detail) {
        navigator.navigateBack()
    }

    var menuExpanded by remember { mutableStateOf(false) }
    var searchBarOffsetY by remember { mutableIntStateOf(0) }

    @Composable
    fun DropMenu(menuExpanded: Boolean, onDismiss: () -> Unit = {}) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.TopEnd)
                .padding(end = 16.dp, top = 16.dp)
        ) {
            DropdownMenu(expanded = menuExpanded, onDismissRequest = onDismiss) {
                DropdownMenuItem(
                    text = { Text("Popular") },
                    onClick = {
                        viewModel.send(ExploreIntent.Sort(SearchSortType.Popular))
                    },
                    trailingIcon = {
                        if (state.sortType == SearchSortType.Popular) Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = null
                        )
                    }
                )

                DropdownMenuItem(
                    text = { Text("Top Rated") },
                    onClick = {
                        viewModel.send(ExploreIntent.Sort(SearchSortType.TopRated))
                    },
                    trailingIcon = {
                        if (state.sortType == SearchSortType.TopRated) Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = null
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text("Most Recent") },
                    onClick = {
                        viewModel.send(ExploreIntent.Sort(SearchSortType.MostRecent))
                    },
                    trailingIcon = {
                        if (state.sortType == SearchSortType.MostRecent) Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = null
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text("Top Sellers") },
                    onClick = {
                        viewModel.send(ExploreIntent.Sort(SearchSortType.TopSellers))
                    },
                    trailingIcon = {
                        if (state.sortType == SearchSortType.TopSellers) Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = null
                        )
                    }
                )
//                HorizontalDivider()
//                DropdownMenuItem(
//                    text = { Text("Sort") },
//                    onClick = {
//
//                    },
//                    trailingIcon = {
//                        Icon(
//                            imageVector = ImageVector.vectorResource(R.drawable.sort),
//                            contentDescription = null
//                        )
//                    }
//                )
//                DropdownMenuItem(
//                    text = { Text("Refresh") },
//                    onClick = {
//
//                    },
//                    trailingIcon = {
//                        Icon(
//                            imageVector = Icons.Filled.Refresh,
//                            contentDescription = null
//                        )
//                    }
//                )
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        ListDetailPaneScaffold(
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            listPane = {
                AnimatedPane {
                    DropMenu(menuExpanded, onDismiss = { menuExpanded = false })
                    SearchLayout(
                        onApplySearch = { query ->
                            viewModel.send(
                                ExploreIntent.SearchByTag(selectedTags)
                            )
                        },
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        title = "search by tag",
                        searchFieldHint = "select tag name",
                        searchFieldState = searchFieldState,
                        searchBarOffsetY = { searchBarOffsetY },
                        trailingIcon = {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = null
                                )
                            }
                        },
                        filter = {
                            //fetch tags
                            if (state.allTags.isEmpty()) viewModel.send(ExploreIntent.AllTags)
                            Column {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    selectedTags.forEach { tag ->
                                        AssistChip(
                                            onClick = { selectedTags = selectedTags - tag },
                                            label = { Text(tag.displayName) },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Remove Tag"
                                                )
                                            }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = WindowInsets.safeDrawing.only(
                                        WindowInsetsSides.Bottom
                                    ).asPaddingValues(),
                                ) {
                                    // Workaround for prepending before the first item
                                    item {}
                                    val suggestions =
                                        if (searchFieldState.text.isNotBlank()) state.allTags.filter { tag ->
                                            tag.containsTag(searchFieldState.text)
                                        } else
                                            state.allTags
                                    val itemModifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp)
                                    item {
                                        ListItem(
                                            headlineContent = { Text("History") },
                                            //supportingContent = { Text("Additional info") },
                                            leadingContent = {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null
                                                )
                                            },
                                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                            modifier = itemModifier.clickable {
                                                expanded = false
                                            }

                                        )
                                    }

                                    //filter selected tags for avoiding repeat selection
                                    items(items = suggestions.filter { !selectedTags.contains(it) }) { tag ->
                                        ListItem(
                                            headlineContent = { Text(tag.displayName) },
                                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                            modifier = itemModifier.clickable {
                                                selectedTags = selectedTags + tag
                                                searchFieldState.clearText()
                                            }
                                        )
                                    }
                                }
                            }
                        },
                        //floatingActionButton = TODO()
                    ) { contentPadding ->
                        val density = LocalDensity.current
                        val searchBarConnection = remember {
                            val topPaddingPx =
                                with(density) {
                                    contentPadding.calculateTopPadding().roundToPx()
                                }
                            object : NestedScrollConnection {
                                override fun onPostScroll(
                                    consumed: Offset,
                                    available: Offset,
                                    source: NestedScrollSource
                                ): Offset {
                                    val dy = -consumed.y

                                    searchBarOffsetY =
                                        (searchBarOffsetY - dy).roundToInt()
                                            .coerceIn(-topPaddingPx, 0)
                                    return Offset.Zero // We never consume it
                                }
                            }
                        }

                        if (state.searchLoading) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                            }
                        } else {
                            val listState = rememberLazyListState()
                            LazyColumn(
                                contentPadding = contentPadding,
                                state = listState,
                                modifier = Modifier.nestedScroll(searchBarConnection),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {

                                val itemModifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)

                                val imageModifier = Modifier
                                    .sizeIn(maxHeight = 80.dp)
                                    .aspectRatio(315f / 250f)
                                    .clip(RoundedCornerShape(8.dp))

                                itemsIndexed(
                                    state.searchApiModel?.items ?: listOf(),
                                    key = { index, _ -> index }) { _, item ->
                                    SearchItem(
                                        searchItem = item,
                                        modifier = itemModifier
                                            .clickable {
                                                viewModel.send(
                                                    ExploreIntent.ClickItem(item.url) {
                                                        navigator.navigateTo(
                                                            ListDetailPaneScaffoldRole.Detail,
                                                            item.url
                                                        )
                                                    }
                                                )
                                            },
                                        imageModifier = imageModifier,
                                        sortType = state.sortType
                                    )

                                }
                            }

                        }
                    }
                }
            },
            detailPane = {
                AnimatedPane {
                    // Show the detail pane content if selected item is available
                    navigator.currentDestination?.content?.let {
                        Log.i("Search", it)
                        val detailState by viewModel.detailState.collectAsStateWithLifecycle()
                        val (game, localInfo) = detailState
                        if (game == null) throw Exception()
                        DetailScreen(
                            url = it,
                            game = game,
                            localInfo = localInfo,
                            onChangeStarred = { url ->
                                viewModel.send(ExploreIntent.AddLocal(url))
                            }
                        )
                    }
                }
            },
        )
    }


}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchItem(
    searchItem: SearchResult,
    modifier: Modifier = Modifier,
    imageModifier: Modifier = Modifier,
    sortType: SearchSortType
) {
    Card(
        modifier = modifier
            .heightIn(min = 120.dp),
        //onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Start,
            ) {
                if (searchItem.image != null) {
                    AsyncImage(
                        model = searchItem.image,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = imageModifier,
                    )
                }


                GameInfoItem(
                    modifier = Modifier.padding(start = 16.dp),
                    title = searchItem.name,
                    description = searchItem.description,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                PlatformRow(searchItem.platforms)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val content =
                        if (sortType == SearchSortType.TopRated)
                            "%.2f".format(searchItem.rating!!.ratingPercent!! * 5 / 100) + "(${searchItem.rating!!.ratingCount})"
                        else if (sortType == SearchSortType.TopSellers) {
                            searchItem.price
                        } else {
                            searchItem.genre
                        }
                    if (content != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(90.dp))
                                .background(MaterialTheme.colorScheme.inverseOnSurface)
                                .padding(8.dp)
                        ) {
                            Text(content, style = MaterialTheme.typography.bodySmall)
                        }
                    }
//                    if (gameBasic.localInfo.starred && showStar) {
//                        Box(
//                            modifier = Modifier
//                                .clip(RoundedCornerShape(90.dp))
//                                .background(MaterialTheme.colorScheme.inverseOnSurface)
//                                .padding(8.dp)
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.Star,
//                                contentDescription = null,
//                                modifier = Modifier.size(16.dp)
//                            )
//                        }
//                    }
                }
            }
        }

    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
@Preview
fun TagSearchScreen() {
    Surface {
        var searchText by remember { mutableStateOf("") }
        var selectedTags by remember { mutableStateOf(listOf<String>("Kotlin", "Compose")) }
        val allTags = listOf("Kotlin", "Compose", "Jetpack", "Material", "Android") // 这里替换成实际的数据源
        val filteredTags =
            allTags.filter { it.contains(searchText, ignoreCase = true) && it !in selectedTags }

        Column(modifier = Modifier.padding(16.dp)) {
            // 显示已选标签
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedTags.forEach { tag ->
                    AssistChip(
                        onClick = { selectedTags = selectedTags - tag },
                        label = { Text(tag) },
                        leadingIcon = {
                            Icon(Icons.Default.Close, contentDescription = "Remove Tag")
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 搜索输入框
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Search Tag") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(onClick = { searchText = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                        }
                    }
                }
            )

            // 提示列表
            if (filteredTags.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .border(1.dp, Color.Gray)
                ) {
                    items(filteredTags) { tag ->
                        Text(
                            text = tag,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedTags = selectedTags + tag
                                    searchText = "" // 选中后清空输入框
                                }
                                .padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}
