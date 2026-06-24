import SwiftUI
import Shared

struct MyRewardsBlock: View {
    let block: ManifestBlockMyRewards

    var body: some View {
        SectionWithImageRow(
            title: block.title,
            items: block.items.map { item in
                ImageRowEntry(
                    id: item.id,
                    assetName: item.image.assetName,
                    title: item.title,
                    subtitle: item.subtitle ?? item.badge
                )
            }
        )
    }
}
