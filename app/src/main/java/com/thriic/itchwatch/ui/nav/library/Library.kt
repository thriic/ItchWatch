package com.thriic.itchwatch.ui.nav.library

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ContextualFlowRow
import androidx.compose.foundation.layout.ContextualFlowRowOverflow
import androidx.compose.foundation.layout.ContextualFlowRowOverflowScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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
import com.thriic.core.model.GameBasic
import com.thriic.core.model.LocalInfo
import com.thriic.core.model.Platform
import com.thriic.core.model.FilterTag
import com.thriic.core.model.SortType
import com.thriic.core.model.TagType
import com.thriic.core.network.model.DevLogItem
import com.thriic.itchwatch.R
import com.thriic.itchwatch.ui.common.GameInfoItem
import com.thriic.itchwatch.ui.common.PlatformRow
import com.thriic.itchwatch.ui.common.SearchLayout
import com.thriic.itchwatch.ui.detail.DetailScreen
import com.thriic.itchwatch.utils.getId
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import kotlin.math.roundToInt

@Composable
fun Filter(
    allFilters: Set<FilterTag>,
    selectedFilterTags: Set<FilterTag>,
    onChangeSelected: (FilterTag, Boolean) -> Unit
) {
    val allTags by remember { mutableStateOf(allFilters) }

    val modifier = Modifier.padding(horizontal = 8.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        val platformTags = allTags.filter { it.type == TagType.Platform }
        if(platformTags.isNotEmpty()) {
            ChipRow(
                title = "Platform",
                filterTags = platformTags,
                selectedFilterTags = selectedFilterTags,
                onSelected = onChangeSelected,
                modifier = modifier.padding(top = 8.dp)
            )
        }

        val languageTags = allTags.filter { it.type == TagType.Language }
        if(languageTags.isNotEmpty()) {
            ChipRow(
                title = "Language",
                filterTags = languageTags,
                selectedFilterTags = selectedFilterTags,
                onSelected = onChangeSelected,
                modifier = modifier
            )
        }

//        ChipFlowRow(
//            title = "Tag",
//            tags = allTags.filter { it.type == TagType.NormalTag },
//            selectedTags = selectedTags,
//            onSelected = onChangeSelected
//        )


    }
}


@Composable
fun ChipRow(
    title: String,
    filterTags: List<FilterTag>,
    selectedFilterTags: Set<FilterTag>,
    modifier: Modifier = Modifier,
    onSelected: (FilterTag, Boolean) -> Unit
) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        modifier = modifier
    )
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Spacer(modifier = Modifier.width(8.dp)) }
        items(filterTags) { tag ->
            val selected = selectedFilterTags.contains(tag)
            FilterChip(
                selected = selected,
                onClick = { onSelected(tag, !selected) },
                label = { Text(tag.displayName) },
                leadingIcon =
                if (selected) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else {
                    null
                }
            )
        }
        item { Spacer(modifier = Modifier.width(8.dp)) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChipFlowRow(
    title: String,
    filterTags: List<FilterTag>,
    selectedFilterTags: Set<FilterTag>,
    modifier: Modifier = Modifier,
    onSelected: (FilterTag, Boolean) -> Unit
) {
    val totalCount = filterTags.size
    var maxLines by remember {
        mutableStateOf(2)
    }

    val moreOrCollapseIndicator = @Composable { scope: ContextualFlowRowOverflowScope ->
        val remainingItems = totalCount - scope.shownItemCount
        AssistChip(
            label = { Text(if (remainingItems == 0) "Less" else "+$remainingItems") },
            onClick = {
                if (remainingItems == 0) {
                    maxLines = 2
                } else {
                    maxLines += 5
                }
            })
    }
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        modifier = modifier.padding(start = 8.dp)
    )
    ContextualFlowRow(
        modifier = Modifier
            .safeDrawingPadding()
            .fillMaxWidth(1f)
            .padding(8.dp)
            .wrapContentHeight(align = Alignment.Top),
        //.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        maxLines = maxLines,
        overflow = ContextualFlowRowOverflow.expandOrCollapseIndicator(
            minRowsToShowCollapse = 4,
            expandIndicator = moreOrCollapseIndicator,
            collapseIndicator = moreOrCollapseIndicator
        ),
        itemCount = totalCount
    ) { index ->
        val tag = filterTags[index]
        val selected = selectedFilterTags.contains(tag)
        FilterChip(
            selected = selected,
            onClick = { onSelected(tag, !selected) },
            label = { Text(tag.displayName) },
            leadingIcon =
            if (selected) {
                {
                    Icon(
                        imageVector = Icons.Filled.Done,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            } else {
                null
            }
        )
    }

}

@Composable
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun ListDetailPane() {

}


@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = viewModel(),
    listState: LazyListState = rememberLazyListState(),
) {
    viewModel.send(LibraryIntent.SyncRepository)
    val state by viewModel.state.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()

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
    val itemState by viewModel.items.collectAsStateWithLifecycle()


    var showBottomSheetWithUrl: String? by remember { mutableStateOf(null) }

    var menuExpanded by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var searchBarOffsetY by remember { mutableIntStateOf(0) }


    var selectedTags by remember { mutableStateOf(filterState.filterTags) }



    val navigator = rememberListDetailPaneScaffoldNavigator<String>()
//    Log.i("Lib", "Navigator${navigator.currentDestination}")
    BackHandler(navigator.currentDestination != null && navigator.currentDestination?.pane == ListDetailPaneScaffoldRole.Detail) {
        Log.i("Lib","Back")
        navigator.navigateBack()
    }

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
                    text = { Text("Top starred") },
                    onClick = {
                        viewModel.send(LibraryIntent.Sort(SortType.Starred))
                    },
                    trailingIcon = {
                        if (SortType.Starred in state.sortTypes) Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = null
                        )
                    }
                )

                DropdownMenuItem(
                    text = { Text("Top updated") },
                    onClick = {
                        viewModel.send(LibraryIntent.Sort(SortType.Updated))
                    },
                    trailingIcon = {
                        if (SortType.Updated in state.sortTypes) Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = null
                        )
                    }
                )
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("Sort") },
                    onClick = {
                        if (SortType.Name in state.sortTypes)
                            viewModel.send(LibraryIntent.Sort(SortType.TimeReverse))
                        else
                            viewModel.send(LibraryIntent.Sort(SortType.Name))

                    },
                    trailingIcon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.sort),
                            contentDescription = null
                        )
                    }
                )
                val coroutineScope = rememberCoroutineScope()
                DropdownMenuItem(
                    text = { Text("Refresh") },
                    onClick = {
                        coroutineScope.launch {
                            listState.scrollToItem(0)
                        }
                        viewModel.send(LibraryIntent.Refresh)
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null
                        )
                    }
                )
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (state.loading) {
            if (state.progress != null) {
                val animatedProgress by
                animateFloatAsState(
                    targetValue = state.progress!!,
                    animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
                )
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    progress = { animatedProgress },
                )
            } else {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

        }
        ListDetailPaneScaffold(
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            listPane = {
                AnimatedPane {
                    DropMenu(menuExpanded, onDismiss = { menuExpanded = false })
                    SearchLayout(
                        onApplySearch = { query ->
                            viewModel.send(
                                LibraryIntent.UpdateFilter(
                                    query,
                                    selectedTags
                                )
                            )
                        },
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        title = "search something...",
                        searchFieldHint = "input keyword",
                        searchFieldState = rememberTextFieldState(),
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
                            val tagSet = itemState.flatMap { game ->
                                game.filterFilterTags
                            }.toSet()
                            Filter(tagSet, selectedTags) { tag, selected ->
                                selectedTags = if (selected) {
                                    selectedTags.plus(tag)
                                } else {
                                    selectedTags.minus(tag)
                                }
                            }
                        },
                        //floatingActionButton = TODO()
                    ) { contentPadding ->
                        val density = LocalDensity.current
                        val searchBarConnection = remember {
                            val topPaddingPx =
                                with(density) { contentPadding.calculateTopPadding().roundToPx() }
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



                        LazyColumn(
                            contentPadding = contentPadding,
                            state = listState,
                            modifier = Modifier.nestedScroll(searchBarConnection),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {


                            val itemModifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                            val filterItems = itemState.filter {
                                it.filterFilterTags.containsAll(filterState.filterTags) && it.name.contains(
                                    filterState.keyword,
                                    ignoreCase = true
                                )
                            }

                            itemsIndexed(
                                filterItems,
                                key = { index, item -> index }) { index, item ->
                                    LibraryItem(
                                        gameBasic = item,
                                        modifier = itemModifier,
                                        onClick = {
                                            viewModel.send(LibraryIntent.ClickItem(item.url) {
                                                navigator.navigateTo(
                                                    ListDetailPaneScaffoldRole.Detail,
                                                    item.url
                                                )
                                            })

                                        },
                                        onLongClick = { showBottomSheetWithUrl = item.url },
                                        imageModifier = Modifier
//                                        .sharedElement(
//                                            sharedTransitionScope.rememberSharedContentState(key = "image-$id"),
//                                            animatedVisibilityScope = animatedContentScope
//                                        )
                                            //.size(100.dp)
                                            .sizeIn(maxHeight = 80.dp)
                                            .aspectRatio(315f / 250f)
                                            .clip(RoundedCornerShape(8.dp)),
//                                    textModifier = Modifier.sharedElement(
//                                        sharedTransitionScope.rememberSharedContentState(key = "text-$id"),
//                                        animatedVisibilityScope = animatedContentScope,
//                                    ),
                                        showStar = SortType.Starred in state.sortTypes
//                        image = ImageRequest.Builder(LocalContext.current)
//                            .data(item.image)
//                            .crossfade(true)
//                            .placeholderMemoryCacheKey("image-$id")
//                            .memoryCacheKey("image-$id")
//                            .build()
                                    )
                                }


                        }
                    }
                }
            },
            detailPane = {
                AnimatedPane {
                    // Show the detail pane content if selected item is available
                    navigator.currentDestination?.content?.let {
                        Log.i("Lib", it)
                        val uiState by viewModel.detailState.collectAsStateWithLifecycle()
                        val (game, localInfo) = uiState
                        if(game == null || localInfo == null) throw Exception()
                        DetailScreen(
                            url = it,
                            game = game,
                            localInfo = localInfo,
                            onChangeStarred = { url ->
                                viewModel.send(LibraryIntent.Star(url))
                            }
                        )
                    }
                }
            },
        )
    }






    LibraryBottomSheet(
        showBottomSheetWithUrl,
        onDismiss = { showBottomSheetWithUrl = null },
        onRemove = { viewModel.send(LibraryIntent.Remove(showBottomSheetWithUrl!!){ if(navigator.currentDestination != null && navigator.currentDestination?.pane == ListDetailPaneScaffoldRole.Detail) navigator.navigateBack() }) },
        onStar = { viewModel.send(LibraryIntent.Star(showBottomSheetWithUrl!!)) },
        onMark = { viewModel.send(LibraryIntent.Mark(showBottomSheetWithUrl!!)) }
    )




}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryItem(
    gameBasic: GameBasic,
    modifier: Modifier = Modifier,
    //image: ImageRequest,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    imageModifier: Modifier = Modifier
        .size(100.dp)
        .clip(RoundedCornerShape(8.dp)),
    textModifier: Modifier = Modifier,
    showStar: Boolean = false
) {
    Card(
        modifier = modifier
            .heightIn(min = 120.dp),
        //onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .combinedClickable(onLongClick = onLongClick, onClick = onClick)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Start,
            ) {
                if (gameBasic.image != null) {
                    AsyncImage(
                        model = gameBasic.image,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = imageModifier,
                    )
                }
                val versionDisplay = when {
                    gameBasic.updated -> "[update]" + gameBasic.localInfo.lastPlayedVersion + "\n=>" + gameBasic.versionOrFileName
                    gameBasic.localInfo.lastPlayedVersion!=null -> gameBasic.versionOrFileName+"✓"
                    else -> gameBasic.versionOrFileName
                }
                GameInfoItem(
                    modifier = Modifier.padding(start = 16.dp),
                    title = gameBasic.name,
                    titleModifier = textModifier,
                    description = versionDisplay,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                PlatformRow(gameBasic.platforms.toList())
                val textBox = if (gameBasic.updatedTime != null)
                    "Last Updated·${gameBasic.updatedTime!!.formatTimeDifference()}"
                else if (!gameBasic.devLogs.isNullOrEmpty())
                    "Last DevLog·${gameBasic.devLogs!![0].pubDate.formatTimeDifference()}"
                else if (gameBasic.publishedTime != null)
                    "Published·${gameBasic.publishedTime!!.formatTimeDifference()}"
                else
                    ""
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (textBox.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(90.dp))
                                .background(MaterialTheme.colorScheme.inverseOnSurface)
                                .padding(8.dp)
                        ) {
                            Text(textBox, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    if (gameBasic.localInfo.starred && showStar) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(90.dp))
                                .background(MaterialTheme.colorScheme.inverseOnSurface)
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

    }
}

@Preview
@Composable
fun SearchItemPreview() {
    val fakeGameBasic = GameBasic(
        name = "Hello",
        url = "https://anuke.itch.io/mindustry",
        image = "https://img.itch.zone/aW1nLzE2ODU1NzU3LnBuZw==/300x240%23c/VS4P%2Bb.png",
        platforms = setOf(
            Platform.WINDOWS,
            Platform.ANDROID,
            Platform.LINUX,
            Platform.WEB,
            Platform.MACOS
        ),
        updatedTime = LocalDateTime.now(),
        publishedTime = LocalDateTime.now(),
        versionOrFileName = "v1.5",
        devLogs = listOf(
            DevLogItem(
                title = "Notice: Changelog Moved To GitHub",
                link = "https://anuke.itch.io/mindustry/devlog/68611/notice-changelog-moved-to-github",
                pubDate = LocalDateTime.now(),
            ),
            DevLogItem(
                title = "Notice: Changelog Moved To GitHub",
                link = "https://anuke.itch.io/mindustry/devlog/68611/notice-changelog-moved-to-github",
                pubDate = LocalDateTime.now(),
            )
        ),
        filterFilterTags = emptyList(),
        localInfo = LocalInfo(
            url = "https://anuke.itch.io/mindustry",
            blurb = null,
            lastPlayedVersion = null,
            lastPlayedTime = null,
            starred = false
        )
    )
//    Surface {
//        Column {
//            LibraryItem(
//                gameBasic = fakeGameBasic,
//                modifier = Modifier.fillMaxWidth()
//            )
//            LibraryItem(
//                gameBasic = fakeGameBasic,
//                modifier = Modifier.fillMaxWidth(),
//            )
//        }
//    }
}