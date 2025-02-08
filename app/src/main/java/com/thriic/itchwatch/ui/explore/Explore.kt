package com.thriic.itchwatch.ui.explore
//
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.thriic.core.model.Platform
import com.thriic.core.network.model.SearchResult
import com.thriic.itchwatch.ui.common.GameInfoItem

//
//@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3AdaptiveApi::class)
//@Composable
//fun ExploreScreen(layout: WatchLayout, viewModel: ExploreViewModel = viewModel(),
//                  sharedTransitionScope: SharedTransitionScope,
//                  animatedContentScope: AnimatedContentScope
//) {
//    val state by viewModel.state.collectAsStateWithLifecycle()
//
//    var text by rememberSaveable { mutableStateOf("") }
//    var expanded by rememberSaveable { mutableStateOf(false) }
//    Box(
//        Modifier
//            .fillMaxSize()
//            .semantics { isTraversalGroup = true }) {
//        SearchLayout(
//            query = text,
//            onQueryChange = { text = it },
//            onSearch = {
//                expanded = false
//                viewModel.send(ExploreIntent.Search(it))
//            },
//            expanded = expanded,
//            onExpandedChange = { expanded = it },
//            layout = layout
//        ) {
//            Column(Modifier.verticalScroll(rememberScrollState())) {
//                repeat(4) { idx ->
//                    val resultText = "Suggestion $idx"
//                    ListItem(
//                        headlineContent = { Text(resultText) },
//                        supportingContent = { Text("Additional info") },
//                        leadingContent = { Icon(Icons.Filled.Star, contentDescription = null) },
//                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
//                        modifier =
//                        Modifier
//                            .clickable {
//                                expanded = false
//                            }
//                            .fillMaxWidth()
//                            .padding(horizontal = 16.dp, vertical = 4.dp)
//                    )
//                }
//            }
//        }
//
//        Column(
//            modifier = Modifier
//                .statusBarsPadding()
//                .padding(top = 72.dp)
//                .semantics { traversalIndex = 1f }
//        ) {
//            when (val uiState = state) {
//                is ExploreUiState.Ready -> {
//                    if (layout == WatchLayout.Compact) {
//                        LazyColumn(
//                            contentPadding = PaddingValues(
//                                top = 16.dp,
//                                bottom = 16.dp
//                            )
//                        ) {
//                            val itemModifier = Modifier
//                                .fillMaxWidth()
//                            itemsIndexed(uiState.searchApiModel.items, key = { index, item -> index }) { _, item ->
//                                val id = item.gameLink.getId()
//                                with(sharedTransitionScope) {
//                                    SearchItem(
//                                        item,
//                                        itemModifier
//                                            .clickable {
//                                                viewModel.send(ExploreIntent.ClickItem(item.gameLink, id))
//                                            }
//                                            .padding(16.dp)
//                                    )
//                                }
//                            }
//                        }
//                    } else {
//                        LazyVerticalStaggeredGrid(
//                            columns = StaggeredGridCells.Adaptive(250.dp),
//                            horizontalArrangement = Arrangement.spacedBy(16.dp),
//                            verticalItemSpacing = 16.dp,
//                            contentPadding = PaddingValues(end = 16.dp),
//                            content = {
//                                val itemModifier = Modifier
//                                    .fillMaxWidth()
//                                val imageModifier = Modifier
//                                    .aspectRatio(315f / 250f)
//                                    .clip(RoundedCornerShape(8.dp))
//                                items(uiState.searchApiModel.items) {
//                                    SearchItemExpanded(it, itemModifier, imageModifier)
//                                }
//                            },
//                            modifier = Modifier.fillMaxSize()
//                        )
//                    }
//
//                }
//
//                is ExploreUiState.Loading -> {
//                    Column(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(16.dp),
//                        verticalArrangement = Arrangement.Center,
//                        horizontalAlignment = Alignment.CenterHorizontally
//                    ) {
//                        CircularProgressIndicator()
//                    }
//                }
//
//                is ExploreUiState.Error -> {
//                    Column(
//                        modifier = Modifier.fillMaxSize(),
//                        verticalArrangement = Arrangement.Center,
//                        horizontalAlignment = Alignment.CenterHorizontally
//                    ) {
//                        Text("Error: ${uiState.errorMessage}")
//                    }
//                }
//
//                ExploreUiState.Init -> {
//                    Column(
//                        modifier = Modifier.fillMaxSize(),
//                        verticalArrangement = Arrangement.Center,
//                        horizontalAlignment = Alignment.CenterHorizontally
//                    ) {
//                    }
//                }
//            }
//        }
//
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun BoxScope.SearchLayout(
//    query: String,
//    onQueryChange: (String) -> Unit,
//    onSearch: (String) -> Unit,
//    expanded: Boolean,
//    onExpandedChange: (Boolean) -> Unit,
//    layout: WatchLayout,
//    content: @Composable (ColumnScope.() -> Unit)
//) {
//    if (layout == WatchLayout.Compact) {
//        SearchBar(
//            modifier =
//            Modifier
//                .align(Alignment.TopCenter)
//                .semantics {
//                    traversalIndex = 0f
//                },
//            inputField = {
//                SearchBarDefaults.InputField(
//                    query = query,
//                    onSearch = onSearch,
//                    expanded = expanded,
//                    onExpandedChange = onExpandedChange,
//                    placeholder = { Text("Hinted search text") },
//                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
//                    trailingIcon = { Icon(Icons.Default.MoreVert, contentDescription = null) },
//                    onQueryChange = onQueryChange,
//                    modifier = Modifier,
//                    enabled = true,
//                )
//            },
//            expanded = expanded,
//            onExpandedChange = onExpandedChange,
//            content = content
//        )
//    } else {
//        DockedSearchBar(
//            modifier =
//            Modifier
//                .align(Alignment.TopEnd)
//                .padding(end = 16.dp)
//                .statusBarsPadding()
//                .semantics {
//                    traversalIndex = 0f
//                },
//            inputField = {
//                SearchBarDefaults.InputField(
//                    query = query,
//                    onSearch = onSearch,
//                    expanded = expanded,
//                    onExpandedChange = onExpandedChange,
//                    placeholder = { Text("Hinted search text") },
//                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
//                    trailingIcon = { Icon(Icons.Default.MoreVert, contentDescription = null) },
//                    onQueryChange = onQueryChange,
//                    modifier = Modifier,
//                    enabled = true,
//                )
//            },
//            expanded = expanded,
//            onExpandedChange = onExpandedChange,
//            content = content
//        )
//    }
//}
//
//
//TODO click to progress status
@Composable
fun SearchItem(
    searchItem: SearchResult,
    modifier: Modifier = Modifier,
//    imageModifier: Modifier = Modifier,
//    textModifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
    ) {

        if (searchItem.image != null) {
            AsyncImage(
                model = searchItem.image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .aspectRatio(315f / 250f),
            )
        }
        GameInfoItem(
            modifier = if (searchItem.image != null) Modifier.padding(start = 16.dp) else Modifier,
            title = searchItem.name,
            titleModifier = Modifier,
            description = searchItem.description,
            platforms = searchItem.platforms
        )
    }
}

@Composable
fun SearchItemExpanded(
    searchItem: SearchResult,
    modifier: Modifier = Modifier,
    imageModifier: Modifier = Modifier
        .aspectRatio(315f / 250f)
        .clip(RoundedCornerShape(16.dp))
) {
    Card(
        onClick = { /* Do something */ },
        modifier = modifier
    ) {
        Column(modifier = modifier) {
            if (searchItem.image != null)
                AsyncImage(
                    model = searchItem.image,
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = imageModifier
                )
            GameInfoItem(
                modifier = modifier.padding(16.dp),
                title = searchItem.name,
                titleMaxLines = if (searchItem.image != null) 1 else 2,
                description = searchItem.description,
                price = searchItem.price,
                genre = searchItem.genre,
                author = searchItem.author,
                verifiedAuthor = searchItem.verifiedAuthor,
                platforms = searchItem.platforms
            )
        }
    }
}


@Preview
@Composable
fun SearchItemPreview() {
    Surface {
        SearchItem(
            searchItem = SearchResult(
                name = "Hello",
                description = "this is a description",
                image = "https://img.itch.zone/aW1nLzE3NDQ3NDUxLnBuZw==/300x240%23c/rmmJ1M.png",
                url = "",
                author = "author",
                price = "$5.99",
                genre = "Action",
                platforms = listOf(Platform.WINDOWS, Platform.ANDROID),
                verifiedAuthor = true,
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview
@Composable
fun SearchItemExpandedPreview() {
    Surface {
        SearchItemExpanded(
            searchItem = SearchResult(
                name = "Hello",
                description = "this is a description",
                image = "https://img.itch.zone/aW1nLzE3NDQ3NDUxLnBuZw==/300x240%23c/rmmJ1M.png",
                url = "",
                author = "author",
                price = "$5.99",
                genre = "Action",
                platforms = listOf(Platform.WINDOWS, Platform.ANDROID),
                verifiedAuthor = true,
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}