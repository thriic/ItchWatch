package com.thriic.itchwatch.ui.detail

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.thriic.core.formatTimeDifference
import com.thriic.core.model.File
import com.thriic.core.model.Platform
import com.thriic.itchwatch.ui.theme.ItchWatchTheme
import com.thriic.core.model.TagType
import com.thriic.core.model.filter
import com.thriic.core.model.getContentLinks
import com.thriic.core.network.model.DevLogItem
import com.thriic.itchwatch.R
import com.thriic.itchwatch.ui.common.PlatformIcon
import com.thriic.itchwatch.ui.nav.library.LibraryIntent
import com.thriic.itchwatch.ui.nav.library.LibraryViewModel
import com.thriic.itchwatch.ui.utils.Href
import com.thriic.itchwatch.ui.utils.getHref
import com.thriic.itchwatch.ui.utils.phraseSocialUrl


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun DetailScreen(
    url: String,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null,
    viewModel: LibraryViewModel
) {
    val uiState by viewModel.detailState.collectAsStateWithLifecycle()
    val (game, localInfo) = uiState
    if(game == null || localInfo == null) throw Exception()
    Surface {
        with(sharedTransitionScope) {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

//                        Row {
//                            IconButton(
//                                onClick = { },
//                                modifier = Modifier,
//                                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.background)
//                            ) {
//                                Icon(Icons.Default.Share, contentDescription = null)
//                            }
//                        }
                item {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(game.image)
                            .crossfade(true)
                            .placeholderMemoryCacheKey("image-${game.url}")
                            .memoryCacheKey("image-${game.url}")
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .statusBarsPadding()
//                                    .sharedElement(
//                                        sharedTransitionScope.rememberSharedContentState(key = "image-$id"),
//                                        animatedVisibilityScope = animatedContentScope
//                                    )
                            .aspectRatio(315f / 250f)
                            .clip(RoundedCornerShape(8.dp))
                            .fillMaxWidth()

                    )
                }

                val cardModifier = Modifier.fillMaxWidth()
                item {
                    val clipboard = LocalClipboardManager.current
                    val uriHandler = LocalUriHandler.current
                    MainCard(
                        title = game.name,
                        author = game.tags.filter(TagType.Author)
                            .joinToString(",") { it.displayName },
                        updatedTime = game.updatedTime?.formatTimeDifference(),
                        publishedTime = game.publishedTime?.formatTimeDifference(),
                        cardModifier = cardModifier,
                        titleModifier = Modifier,
//                                    .sharedElement(
//                                        sharedTransitionScope.rememberSharedContentState(key = "text-$id"),
//                                        animatedVisibilityScope = animatedContentScope
//                                    ),
                        starred = localInfo.starred,
                        onChangeStarred = { starred ->
                            viewModel.send(LibraryIntent.Star(game.url))
                        },
                        onShare = {
                            uriHandler.openUri(game.url)
                            //clipboard.setText(AnnotatedString(game.url))
                            //viewModel.sendMessage("Link copied to clipboard")
                        }
                    )
                }
                if (game.devLogs.isNotEmpty()) {
                    item {
                        DevLogCard(cardModifier = cardModifier, devLogs = game.devLogs)
                    }
                }
                if (game.files.isNotEmpty()) {
                    item {
                        FileCard(cardModifier = cardModifier, files = game.files)
                    }
                }
                val hrefs = (game.tags.getHref() + (game.content?.let { getContentLinks(it) }
                    ?: emptyList())).phraseSocialUrl()
                if (hrefs.isNotEmpty()) {
                    item {
                        LinkCard(cardModifier = cardModifier, links = hrefs)
                    }
                }
                item {
                    Spacer(modifier = Modifier.navigationBarsPadding())
                }

            }


        }
    }
}

@Composable
fun MainCard(
    title: String,
    author: String,
    updatedTime: String? = null,
    publishedTime: String? = null,
    cardModifier: Modifier = Modifier,
    titleModifier: Modifier = Modifier,
    starred: Boolean = false,
    onChangeStarred: (Boolean) -> Unit = {},
    onShare: () -> Unit = {}
) {
    Card(modifier = cardModifier, onClick = {}) {
        ListItem(
            headlineContent = {
                Text(
                    title,
                    modifier = titleModifier,
                    maxLines = 2,
                    style = MaterialTheme.typography.titleMedium,
                    overflow = TextOverflow.Ellipsis
                )

            },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            supportingContent = {
                Text(
                    author,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall,
                    overflow = TextOverflow.Ellipsis
                )
            },
            trailingContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxHeight()
                ) {
                    IconButton(
                        onClick = { onChangeStarred(true) },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.inverseOnSurface)
                    ) {
                        Icon(
                            if (starred) Icons.Default.Star else ImageVector.vectorResource(R.drawable.outline_star),
                            contentDescription = null
                        )
                    }
                    IconButton(
                        onClick = onShare,
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.inverseOnSurface)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                    }
                }
            },
        )
        if (updatedTime != null || publishedTime != null)
            HorizontalDivider()
        if (updatedTime != null) {
            ListItem(
                headlineContent = {
                    Text(
                        "Updated",
                        maxLines = 1,
                        style = MaterialTheme.typography.titleSmall,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                trailingContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Text(
                            updatedTime,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodySmall,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
            )
        }
        if (publishedTime != null) {
            ListItem(
                headlineContent = {
                    Text(
                        "Published",
                        maxLines = 1,
                        style = MaterialTheme.typography.titleSmall,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                trailingContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Text(
                            publishedTime,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodySmall,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
            )
        }


    }
}

@Composable
fun FileCard(
    cardModifier: Modifier = Modifier,
    files: List<File>
) {
    Card(
        modifier = cardModifier,
        onClick = {},
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Text(
            "File",
            modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
            style = MaterialTheme.typography.titleLarge
        )
        files.forEach { file ->
            ListItem(
                headlineContent = {
                    Text(
                        file.name,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelLarge,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                leadingContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        PlatformIcon(file.platform, Modifier.size(16.dp))
                    }
                },
                trailingContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Text(
                            file.size,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodySmall,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
            )
        }

    }
}

@Composable
fun DevLogCard(
    cardModifier: Modifier = Modifier,
    devLogs: List<DevLogItem>
) {
    val uriHandler = LocalUriHandler.current
    Card(
        modifier = cardModifier,
        onClick = {},
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
//        val underlineModifier = Modifier.drawBehind {
//            val strokeWidthPx = 1.dp.toPx()
//            val verticalOffset = size.height - 2.sp.toPx()
//            drawLine(
//                color = Color.Blue,
//                strokeWidth = strokeWidthPx,
//                start = Offset(0f, verticalOffset),
//                end = Offset(size.width, verticalOffset)
//            )
//        }
        Text(
            "Dev Log",
            modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
            style = MaterialTheme.typography.titleLarge
        )
        devLogs.forEach { devLogItem ->
            ListItem(
                headlineContent = {
                    Text(
                        buildAnnotatedString {
                            withLink(
                                LinkAnnotation.Url(
                                    devLogItem.link,
                                    TextLinkStyles(style = SpanStyle(color = Color.Blue))
                                ) {
                                    val url = (it as LinkAnnotation.Url).url
                                    uriHandler.openUri(url)
                                }
                            ) {
                                append(devLogItem.title)
                            }
                        },
                        //modifier = underlineModifier,
                        maxLines = 2,
                        style = MaterialTheme.typography.bodyMedium,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                modifier = Modifier
                    .fillMaxWidth(),
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                trailingContent = {
                    Text(
                        devLogItem.pubDate.formatTimeDifference(),
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall,
                        overflow = TextOverflow.Ellipsis
                    )
                },
            )
        }

    }
}

@Composable
fun LinkCard(
    cardModifier: Modifier = Modifier,
    links: List<Href>
) {
    Card(
        modifier = cardModifier,
        onClick = {},
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        val uriHandler = LocalUriHandler.current
        Text(
            "Links",
            modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            buildAnnotatedString {
                links.forEach { link ->
                    withLink(
                        LinkAnnotation.Url(
                            link.url,
                            TextLinkStyles(style = SpanStyle(color = Color.Blue))
                        ) {
                            val url = (it as LinkAnnotation.Url).url
                            uriHandler.openUri(url)
                        }
                    ) {
                        append(link.display)
                    }
                    append("   ")
                }
            },
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge,
            overflow = TextOverflow.Ellipsis
        )

    }
}

@Preview
@Composable
fun MainCardPreview() {
    ItchWatchTheme {
        Surface() {
            MainCard(
                title = "title",
                author = "author",
                onChangeStarred = {},
                onShare = {}
            )
        }
    }
}

@Preview
@Composable
fun FileCardPreview() {
    val files = listOf(
        File("win0.64.zip", Platform.WINDOWS, "10 MB"),
        File("linux0.64.zip", Platform.LINUX, "11 MB")
    )
    ItchWatchTheme {
        Surface() {
            FileCard(
                cardModifier = Modifier.fillMaxWidth(),
                files = files
            )
        }
    }
}