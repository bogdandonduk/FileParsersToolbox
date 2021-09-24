package bogdandonduk.fileparserstoolboxandroidlib.core.books

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import bogdandonduk.fileparserstoolboxandroidlib.core.ContentItem
import bogdandonduk.fileparserstoolboxandroidlib.core.ImageContentItem
import bogdandonduk.fileparserstoolboxandroidlib.core.TextContentItem
import bogdandonduk.fileparserstoolboxandroidlib.core.TitleTextContentItem

object BookUtils {
    fun validate(book: Book) = !(book.title == null && book.contents == null)

    @Deprecated("Using native EpubUtils#parseAndMerge and Fb2Utils#getValidMerged methods is more efficient than using this one")
    fun mergeContents(book: Book?, inlineCoverImage: Boolean = true, inlineTitle: Boolean = true) = book?.apply {
        book.contents = mutableListOf<ContentItem>().apply {
            if(inlineCoverImage && !book.coverImageInlined && book.coverImage != null)
                add(book.coverImage!!)

            if(inlineTitle && !book.titleInlined && book.title != null)
                add(book.title!!)

            val lineSeparator = System.getProperty("line.separator") ?: "\r\n"

            book.contents?.forEach {
                when(it) {
                    is TitleTextContentItem ->
                        try {
                            last().let { lastItem ->
                                if(lastItem is TextContentItem)
                                    lastItem.text = SpannableStringBuilder(lastItem.text).append("$lineSeparator$lineSeparator${it.text}").apply {
                                        setSpan(StyleSpan(Typeface.BOLD), length - it.text.length, length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                                    }
                                else
                                    add(it)
                            }
                        } catch(thr: Throwable) {
                            add(it)
                        }

                    is TextContentItem -> {
                        try {
                            last().let { lastItem ->
                                if(lastItem is TextContentItem)
                                    lastItem.text = SpannableStringBuilder(lastItem.text).append("$lineSeparator${it.text}")
                                else
                                    add(it)
                            }
                        } catch(thr: Throwable) {
                            add(it)
                        }
                    }

                    is ImageContentItem -> {
                        add(it)
                    }
                }
            }
        }
    }
}