import SwiftUI
import Shared

@ViewBuilder
func ManifestBlockView(_ block: ManifestBlock, onClassClick: @escaping (String) -> Void) -> some View {
    switch onEnum(of: block) {
    case .greeting(let b):
        GreetingBlock(block: b)
    case .hero(let b):
        HeroBlock(block: b)
    case .myClub(let b):
        MyClubBlock(block: b)
    case .classCarousel(let b):
        ClassCarouselBlock(block: b, onClassClick: onClassClick)
    case .myRewards(let b):
        MyRewardsBlock(block: b)
    case .myGoals(let b):
        MyGoalsBlock(block: b)
    case .promotion(let b):
        PromotionBlock(block: b)
    case .unknown:
        EmptyView()
    }
}
