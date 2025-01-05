package com.thriic.itchwatch.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.thriic.core.model.Platform
import com.thriic.itchwatch.R

@Composable
fun GameInfoItem(
    modifier: Modifier = Modifier,
    title: String,
    titleModifier: Modifier = Modifier,
    titleMaxLines: Int = 1,
    description: String? = null,
    price: String? = null,
    author: String? = null,
    verifiedAuthor: Boolean? = null,
    genre: String? = null,
    platforms: List<Platform>? = null
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.Top) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = titleMaxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = titleModifier
        )
        price?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        description?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.paddingFromBaseline(top = 8.dp)
            )
        }
        author?.let {
            if (verifiedAuthor != null && verifiedAuthor)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp),
                ) {
                    Text(
                        text = "by $it",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Image(painter = painterResource(R.drawable.verified), contentDescription = null)
                }
            else
                Text(
                    text = "by $it",
                    modifier = Modifier.padding(vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
        }
        genre?.let {
            Text(
                text = it,
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (!platforms.isNullOrEmpty()) PlatformRow(
            platforms,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}