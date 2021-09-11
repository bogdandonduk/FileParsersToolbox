package bogdandonduk.fileparserstoolboxandroidlib.core

import android.graphics.Typeface

open class TextContentItem(
    var text: CharSequence,
    var textSize: Int = 18,
    var style: Int = Typeface.NORMAL,
    var isBookTitle: Boolean = false,
    var isChapterTitle: Boolean = false
) : ContentItem() {
    override fun toString() = text.toString()
}