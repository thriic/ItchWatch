package com.thriic.itchwatch.ui.nav.library

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ContextualFlowRow
import androidx.compose.foundation.layout.ContextualFlowRowOverflow
import androidx.compose.foundation.layout.ContextualFlowRowOverflowScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.thriic.core.formatTimeDifference
import com.thriic.core.model.GameBasic
import com.thriic.core.model.Platform
import com.thriic.core.model.Tag
import com.thriic.core.model.TagType
import com.thriic.core.network.model.DevLogItem
import com.thriic.itchwatch.R
import com.thriic.itchwatch.ui.common.GameInfoItem
import com.thriic.itchwatch.ui.common.PlatformRow
import com.thriic.itchwatch.ui.common.SearchLayout
import com.thriic.itchwatch.ui.utils.WatchLayout
import com.thriic.itchwatch.ui.utils.cleanUrl
import com.thriic.itchwatch.ui.utils.isGamePageUrl
import com.thriic.itchwatch.ui.utils.readTextFile
import java.time.LocalDateTime
import kotlin.math.roundToInt

@Composable
fun Filter(
    allFilters: Set<Tag>,
    selectedTags: Set<Tag>,
    onChangeSelected: (Tag, Boolean) -> Unit
) {
    val allTags by remember { mutableStateOf(allFilters) }

    val modifier = Modifier.padding(top = 8.dp)
    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        ChipRow(
            title = "Platform",
            tags = allTags.filter { it.type == TagType.Platform },
            selectedTags = selectedTags,
            onSelected = onChangeSelected
        )

        ChipRow(
            title = "Language",
            tags = allTags.filter { it.type == TagType.Language },
            selectedTags = selectedTags,
            onSelected = onChangeSelected
        )

        ChipFlowRow(
            title = "Tag",
            tags = allTags.filter { it.type == TagType.NormalTag },
            selectedTags = selectedTags,
            onSelected = onChangeSelected
        )


    }
}


@Composable
fun ChipRow(
    title: String,
    tags: List<Tag>,
    selectedTags: Set<Tag>,
    modifier: Modifier = Modifier,
    onSelected: (Tag, Boolean) -> Unit
) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        modifier = modifier.padding(start = 8.dp)
    )
    LazyRow(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(tags) { tag ->
            val selected = selectedTags.contains(tag)
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChipFlowRow(
    title: String,
    tags: List<Tag>,
    selectedTags: Set<Tag>,
    modifier: Modifier = Modifier,
    onSelected: (Tag, Boolean) -> Unit
) {
    val totalCount = tags.size
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
        val tag = tags[index]
        val selected = selectedTags.contains(tag)
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun LibraryScreen(
    layout: WatchLayout,
    viewModel: LibraryViewModel = viewModel(),
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {
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

    var selectedTags by remember { mutableStateOf(filterState.tags) }
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
                game.filterTags
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
            val topPaddingPx = with(density) { contentPadding.calculateTopPadding().roundToPx() }
            object : NestedScrollConnection {
                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource
                ): Offset {
                    val dy = -consumed.y

                    searchBarOffsetY =
                        (searchBarOffsetY - dy).roundToInt().coerceIn(-topPaddingPx, 0)
                    return Offset.Zero // We never consume it
                }
            }
        }
        LazyColumn(
            contentPadding = contentPadding,
            state = rememberLazyListState(),
            modifier = Modifier.nestedScroll(searchBarConnection),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (state.loading) {
                item {
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

            }
            val itemModifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
            val filterItems = itemState.filter { it.filterTags.containsAll(filterState.tags) && it.name.contains(filterState.keyword,ignoreCase = true) }
            itemsIndexed(filterItems, key = { index, item -> index }) { index, item ->
                with(sharedTransitionScope) {
                    LibraryItem(
                        gameBasic = item,
                        modifier = itemModifier,
                        onClick = { viewModel.send(LibraryIntent.ClickItem(item.url, index)) },
                        onLongClick = { showBottomSheetWithUrl = item.url },
                        imageModifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .sharedElement(
                                sharedTransitionScope.rememberSharedContentState(key = "image-${index}"),
                                animatedVisibilityScope = animatedContentScope
                            ),
                        textModifier = Modifier.sharedElement(
                            sharedTransitionScope.rememberSharedContentState(key = "text-${index}"),
                            animatedVisibilityScope = animatedContentScope,
                        )
                    )
                }
            }

        }
    }




    LibraryBottomSheet(
        showBottomSheetWithUrl,
        onDismiss = { showBottomSheetWithUrl = null },
        onRemove = { viewModel.send(LibraryIntent.Remove(showBottomSheetWithUrl!!)) }
    )

    @Composable
    fun DropMenu(menuExpanded: Boolean, onDismiss: () -> Unit = {}) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.TopEnd)
                .padding(end = 16.dp, top = 16.dp)
        ) {
            DropdownMenu(expanded = menuExpanded, onDismissRequest = onDismiss) {
                val clipboard = LocalClipboardManager.current
                DropdownMenuItem(
                    text = { Text("import from clipboard") },
                    onClick = {
                        if (clipboard.hasText()) {
                            val text = clipboard.getText()!!.text
                            val url = text.cleanUrl()
                            if (url.isGamePageUrl())
                                viewModel.send(LibraryIntent.AddGame(url))
                            else viewModel.sendMessage("Invalid URL")
                        } else {
                            viewModel.sendMessage("Clipboard is empty")
                        }
                    },
                )
                val contentResolver = LocalContext.current.contentResolver

                var pickedImageUri by remember { mutableStateOf<Uri?>(null) }
                val launcher =
                    rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                        Log.i("Library", "selected file URI ${it.data?.data}")
                        pickedImageUri = it.data?.data
                        if (pickedImageUri != null) {
                            val content =
                                contentResolver.openInputStream(pickedImageUri!!)?.readTextFile()
                            if (content == null) {
                                viewModel.sendMessage("find nothing")
                            } else {
                                viewModel.send(LibraryIntent.AddGames(content))
                            }
                        }

                    }
                DropdownMenuItem(
                    text = { Text("import from text file") },
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_OPEN_DOCUMENT,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        )
                            .apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                            }
                        launcher.launch(intent)
                    },
                )
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("Sort") },
                    onClick = {
                        if (state.sortType == SortType.Name)
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
                DropdownMenuItem(
                    text = { Text("Refresh") },
                    onClick = {
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

    DropMenu(menuExpanded, onDismiss = { menuExpanded = false })
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryItem(
    gameBasic: GameBasic,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    imageModifier: Modifier = Modifier
        .size(100.dp)
        .clip(RoundedCornerShape(8.dp))
    //.height(105.dp)
    //.aspectRatio(315f / 250f)
    ,
    textModifier: Modifier = Modifier
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
                GameInfoItem(
                    modifier = Modifier.padding(start = 16.dp),
                    title = gameBasic.name,
                    titleModifier = textModifier,
                    description = gameBasic.versionOrFileName,
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
        filterTags = emptyList()
    )
    Surface {
        Column {
            LibraryItem(
                gameBasic = fakeGameBasic,
                modifier = Modifier.fillMaxWidth()
            )
            LibraryItem(
                gameBasic = fakeGameBasic,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}