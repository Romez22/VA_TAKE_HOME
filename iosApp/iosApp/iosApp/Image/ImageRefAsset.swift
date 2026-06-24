import Shared

extension ImageRef {
    var assetName: String {
        switch self {
        case .classSpin: return "class_generic"
        case .classYoga: return "class_generic"
        case .classHiit: return "class_generic"
        case .classGroupWorkout: return "class_generic"
        case .classPilates: return "class_generic"

        case .rewardSmoothie: return "reward_tile"
        case .rewardRetail: return "reward_tile"
        case .rewardGuest: return "reward_tile"

        case .goalClasses: return "goal_tile"
        case .goalSpin: return "goal_tile"
        case .goalExplore: return "goal_tile"

        case .promoSummerChallenge: return "promo_tile"
        case .promoWelcomeBonus: return "promo_tile"

        case .fallback: return "ic_fallback_tile"

        @unknown default: return "ic_fallback_tile"
        }
    }
}
