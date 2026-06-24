package com.virginactive.android.ui.home.render

import androidx.compose.runtime.Composable
import com.virginactive.shared.domain.home.ManifestBlock

@Composable
fun ManifestBlockView(
    block: ManifestBlock,
    onClassClick: (String) -> Unit,
) {
    when (block) {
        is ManifestBlock.Greeting -> GreetingBlock(block)
        is ManifestBlock.Hero -> HeroBlock(block)
        is ManifestBlock.MyClub -> MyClubBlock(block)
        is ManifestBlock.ClassCarousel -> ClassCarouselBlock(block, onClassClick)
        is ManifestBlock.MyRewards -> MyRewardsBlock(block)
        is ManifestBlock.MyGoals -> MyGoalsBlock(block)
        is ManifestBlock.Promotion -> PromotionBlock(block)
        is ManifestBlock.Unknown -> Unit
    }
}
