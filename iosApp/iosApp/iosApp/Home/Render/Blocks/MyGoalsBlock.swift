import SwiftUI
import Shared

struct MyGoalsBlock: View {
    let block: ManifestBlockMyGoals

    var body: some View {
        SectionWithImageRow(
            title: block.title,
            items: block.items.map { item in
                ImageRowEntry(
                    id: item.id,
                    assetName: item.image.assetName,
                    title: item.title,
                    subtitle: item.subtitle
                )
            }
        )
    }
}
