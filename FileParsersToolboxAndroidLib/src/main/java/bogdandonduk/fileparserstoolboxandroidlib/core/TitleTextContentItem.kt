package bogdandonduk.fileparserstoolboxandroidlib.core

import android.graphics.Typeface

class TitleTextContentItem(text: CharSequence, level: TitleTextLevel = TitleTextLevel.H1, isBookTitle: Boolean = false, isChapterTitle: Boolean = false)
    : TextContentItem(text, when(level) {
        TitleTextLevel.H1 -> 24
        TitleTextLevel.H2 -> 22
        TitleTextLevel.H3 -> 20
        TitleTextLevel.H4 -> 18
        TitleTextLevel.H5 -> 16
        TitleTextLevel.H6 -> 14
    }, Typeface.BOLD, isBookTitle, isChapterTitle)