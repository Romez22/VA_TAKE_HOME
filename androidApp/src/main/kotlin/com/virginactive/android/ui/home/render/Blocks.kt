package com.virginactive.android.ui.home.render

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import com.virginactive.android.image.toDrawableRes
import com.virginactive.android.theme.Spacing
import com.virginactive.shared.domain.home.DateTimeDisplay
import com.virginactive.shared.domain.home.ManifestBlock



@Composable
fun GreetingBlock(block: ManifestBlock.Greeting) {
    Text(
        text = block.title,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md),
    )
}

@Composable
fun HeroBlock(block: ManifestBlock.Hero) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Text(
                text = block.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            block.subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(top = Spacing.xs),
                )
            }
        }
    }
}

@Composable
fun MyClubBlock(block: ManifestBlock.MyClub) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Text(
                text = block.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            block.addressLine?.let { MutedLine(it) }
            block.openingHoursToday?.let { MutedLine(it) }
            block.phoneNumber?.let { MutedLine(it) }
        }
    }
}

@Composable
fun MyRewardsBlock(block: ManifestBlock.MyRewards) {
    SectionWithImageRow(
        title = block.title,
        items = block.items.map { ImageRowItem(it.title, it.subtitle ?: it.badge, it.image.toDrawableRes()) },
    )
}

@Composable
fun MyGoalsBlock(block: ManifestBlock.MyGoals) {
    SectionWithImageRow(
        title = block.title,
        items = block.items.map { ImageRowItem(it.title, it.subtitle, it.image.toDrawableRes()) },
    )
}

@Composable
fun PromotionBlock(block: ManifestBlock.Promotion) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(block.image.toDrawableRes()),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
            )
            Spacer(Modifier.width(Spacing.md))
            Column {
                Text(
                    text = block.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                block.subtitle?.let { MutedLine(it) }
            }
        }
    }
}


@Composable
fun ClassCarouselBlock(
    block: ManifestBlock.ClassCarousel,
    onClassClick: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = block.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = Spacing.md),
        )
        Spacer(Modifier.height(Spacing.sm))
        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            items(block.items, key = { it.id }) { item ->
                ClassCard(item = item, onClick = { onClassClick(item.id) })
            }
        }
    }
}

@Composable
private fun ClassCard(
    item: ManifestBlock.ClassCarousel.Item,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column {
            Image(
                painter = painterResource(item.image.toDrawableRes()),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp),
            )
            Column(modifier = Modifier.padding(Spacing.md)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                item.subtitle?.let { MutedLine(it) }
                Text(
                    text = "${DateTimeDisplay.timeLabel(item.startsAt)} · ${DateTimeDisplay.dateLabel(item.startsAt)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.xs),
                )
            }
        }
    }
}


private data class ImageRowItem(val title: String, val subtitle: String?, val drawableRes: Int)

@Composable
private fun SectionWithImageRow(title: String, items: List<ImageRowItem>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = Spacing.md),
        )
        Spacer(Modifier.height(Spacing.sm))
        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            items(items) { item ->
                Card(
                    modifier = Modifier.width(160.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(modifier = Modifier.padding(Spacing.md)) {
                        Image(
                            painter = painterResource(item.drawableRes),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp)),
                        )
                        Spacer(Modifier.height(Spacing.sm))
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                        )
                        item.subtitle?.let { MutedLine(it) }
                    }
                }
            }
        }
    }
}

@Composable
private fun MutedLine(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = Spacing.xs),
    )
}
