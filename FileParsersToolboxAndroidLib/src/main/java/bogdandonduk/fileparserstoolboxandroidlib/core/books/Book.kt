package bogdandonduk.fileparserstoolboxandroidlib.core.books

import bogdandonduk.fileparserstoolboxandroidlib.core.ContentItem
import bogdandonduk.fileparserstoolboxandroidlib.core.ImageContentItem
import bogdandonduk.fileparserstoolboxandroidlib.core.TextContentItem
import bogdandonduk.fileparserstoolboxandroidlib.core.TitleTextContentItem

class Book(var title: TitleTextContentItem?, var coverImage: ImageContentItem?, var tableOfContentsTitles: MutableList<String>?, var contents: MutableList<ContentItem>?) {
    var titleInlined = false
    var coverImageInlined = false

    var indexedTableOfContentsTitles = mutableMapOf<String, Int>().apply {
        contents?.forEachIndexed { i: Int, contentItem: ContentItem ->
            (contentItem as? TextContentItem)?.text?.toString()?.let { text ->
                if(!containsKey(text) && contentItem.isChapterTitle)
                    this[text] = i
            }
        }
    }

    var checked = false
}