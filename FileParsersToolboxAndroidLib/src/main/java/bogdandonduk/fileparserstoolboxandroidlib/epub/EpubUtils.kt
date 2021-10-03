package bogdandonduk.fileparserstoolboxandroidlib.epub

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import bogdandonduk.fileparserstoolboxandroidlib.core.*
import bogdandonduk.fileparserstoolboxandroidlib.core.books.Book
import nl.siegmann.epublib.domain.Book as EpubLibBook
import nl.siegmann.epublib.epub.EpubReader
import org.jsoup.Jsoup
import java.io.File



object EpubUtils {
    fun getValidEpub(path: String) : EpubLibBook? {
        EpubReader().readEpub(File(path).inputStream())?.run {
            if(title != null)
                return this
        }

        return null
    }

    fun parse(path: String, titleInlinedIntoContents: Boolean = false, coverImageInlinedIntoContents: Boolean = false) : Book? {
        getValidEpub(path)?.run {
            val book = EpubReader().readEpub(File(path).inputStream())

            val tocTitles = mutableListOf<String>().apply {
                book.tableOfContents.tocReferences.forEach {
                    add(it.title)
                }
            }

            val title = if(book.title != null)
                TitleTextContentItem(book.title, TitleTextLevel.H1, true, tocTitles.contains(book.title))
            else null

            val coverImage = if(book.coverImage != null && book.coverImage!!.data != null)
                ImageContentItem(book.coverImage.data, true)
            else null

            val toc = book.tableOfContents.tocReferences

            return Book(
                title,
                coverImage,
                if(toc.isNotEmpty())
                    mutableListOf<String>().apply {
                        toc.forEach {
                            it.title?.let { title ->
                                add(title)
                            }
                        }
                    }
                else
                    null,
                if(book.contents.isNotEmpty())
                    mutableListOf<ContentItem>().apply {
                        if(coverImageInlinedIntoContents && coverImage != null)
                            add(coverImage as ContentItem)

                        if(titleInlinedIntoContents && title != null)
                            add(title as ContentItem)

                        book.contents.forEach {
                            Jsoup.parse(it.reader.readText()).body().allElements.forEach { element ->
                                when(element.tagName().lowercase()) {
                                    "h1" -> if(element.text().isNotEmpty() && (last() !is TextContentItem || element.text() != (last() as TextContentItem).text))
                                        add(TitleTextContentItem(element.text(), TitleTextLevel.H1, isChapterTitle = tocTitles.contains(element.text())))
                                    "h2" -> if(element.text().isNotEmpty() && (last() !is TextContentItem || element.text() != (last() as TextContentItem).text))
                                        add(TitleTextContentItem(element.text(), TitleTextLevel.H2, isChapterTitle = tocTitles.contains(element.text())))
                                    "h3" -> if(element.text().isNotEmpty() && (last() !is TextContentItem || element.text() != (last() as TextContentItem).text))
                                        add(TitleTextContentItem(element.text(), TitleTextLevel.H3, isChapterTitle = tocTitles.contains(element.text())))
                                    "h4" -> if(element.text().isNotEmpty() && (last() !is TextContentItem || element.text() != (last() as TextContentItem).text))
                                        add(TitleTextContentItem(element.text(), TitleTextLevel.H4, isChapterTitle = tocTitles.contains(element.text())))
                                    "h5" -> if(element.text().isNotEmpty() && (last() !is TextContentItem || element.text() != (last() as TextContentItem).text))
                                        add(TitleTextContentItem(element.text(), TitleTextLevel.H5, isChapterTitle = tocTitles.contains(element.text())))
                                    "h6" -> if(element.text().isNotEmpty() && (last() !is TextContentItem || element.text() != (last() as TextContentItem).text))
                                        add(TitleTextContentItem(element.text(), TitleTextLevel.H6, isChapterTitle = tocTitles.contains(element.text())))

                                    "em" -> if(element.text().isNotEmpty() && (last() !is TextContentItem || element.text() != (last() as TextContentItem).text))
                                        add(TextContentItem(element.text(), 18, Typeface.ITALIC))

                                    "p" -> if(element.text().isNotEmpty() && (last() !is TextContentItem || element.text() != (last() as TextContentItem).text))
                                        add(TextContentItem(element.text(), isChapterTitle = tocTitles.contains(element.text())))
                                }
                            }
                        }
                    }
                else null
            ).apply {
                titleInlined = titleInlinedIntoContents
                coverImageInlined = coverImageInlinedIntoContents
            }
        }

        return null
    }

    fun parseAndMerge(path: String, titleInlinedIntoContents: Boolean = false, coverImageInlinedIntoContents: Boolean = false) : Book? {
        getValidEpub(path)?.run {
            val book = EpubReader().readEpub(File(path).inputStream())

            val tocTitles = mutableListOf<String>().apply {
                book.tableOfContents.tocReferences.forEach {
                    add(it.title)
                }
            }

            val title = if(book.title != null)
                TitleTextContentItem(book.title, TitleTextLevel.H1, true, tocTitles.contains(book.title))
            else null

            val coverImage = if(book.coverImage != null && book.coverImage!!.data != null)
                ImageContentItem(book.coverImage.data, true)
            else null

            val toc = book.tableOfContents.tocReferences

            return Book(
                title,
                coverImage,
                if(toc.isNotEmpty())
                    mutableListOf<String>().apply {
                        toc.forEach {
                            it.title?.let { title ->
                                add(title)
                            }
                        }
                    }
                else
                    null,
                if(book.contents.isNotEmpty())
                    mutableListOf<ContentItem>().apply {
                        if(coverImageInlinedIntoContents && coverImage != null)
                            add(coverImage as ContentItem)

                        if(titleInlinedIntoContents && title != null)
                            add(title as ContentItem)

                        val lineSeparator = System.getProperty("line.separator") ?: "\r\n"

                        book.contents.forEach {
                            Jsoup.parse(it.reader.readText()).body().allElements.forEach { element ->
                                when(element.tagName().lowercase()) {
                                    "p" ->
                                        try {
                                            val lastItem = last()

                                            if(lastItem is TextContentItem) {
                                                if(element.text().isNotEmpty()
                                                    && (lastItem.text.length < element.text().length || element.text() != lastItem.text.substring(lastItem.text.length - element.text().length, lastItem.text.lastIndex))
                                                )
                                                    lastItem.text = SpannableStringBuilder(lastItem.text).append("${element.text()}$lineSeparator").apply {
                                                        setSpan(StyleSpan(Typeface.NORMAL), lastIndexOf(element.text()), length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                                                    }
                                            } else
                                                add(TextContentItem(element.text(), isChapterTitle = tocTitles.contains(element.text())))
                                        } catch(thr: Throwable) {
                                            add(TextContentItem(element.text(), isChapterTitle = tocTitles.contains(element.text())))
                                        }
                                    "h1", "h2", "h3", "h4", "h5", "h6" ->
                                        try {
                                            val lastItem = last()

                                            if(lastItem is TextContentItem) {
                                                if(element.text().isNotEmpty()
                                                    && (lastItem.text.length < element.text().length || element.text() != lastItem.text.substring(lastItem.text.length - element.text().length, lastItem.text.lastIndex))
                                                )
                                                    lastItem.text = SpannableStringBuilder(lastItem.text).append("$lineSeparator$lineSeparator${element.text()}$lineSeparator$lineSeparator").apply {
                                                        setSpan(StyleSpan(Typeface.BOLD), lastIndexOf(element.text()), length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                                                    }
                                            } else
                                                add(TextContentItem(element.text(), isChapterTitle = tocTitles.contains(element.text()), isBookTitle = element.text() == title?.text))
                                        } catch(thr: Throwable) {
                                            add(TextContentItem(element.text(), isChapterTitle = tocTitles.contains(element.text()), isBookTitle = element.text() == title?.text))
                                        }
//                                    "h2" ->
//                                        try {
//                                            val lastItem = last()
//
//                                            if(lastItem is TextContentItem) {
//                                                if(element.text().isNotEmpty())
//                                                    lastItem.text = SpannableStringBuilder(lastItem.text).append("$lineSeparator${element.text()}$lineSeparator").apply {
//                                                        setSpan(StyleSpan(Typeface.BOLD), lastIndexOf(element.text()), length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
//                                                    }
//                                            } else
//                                                add(TextContentItem(element.text(), style = Typeface.BOLD, isChapterTitle = tocTitles.contains(element.text()), isBookTitle = element.text() == title?.text))
//                                        } catch(thr: Throwable) {
//                                            add(TextContentItem(element.text(), style = Typeface.BOLD, isChapterTitle = tocTitles.contains(element.text()), isBookTitle = element.text() == title?.text))
//                                        }
//                                    "h3" ->
//                                        try {
//                                            val lastItem = last()
//
//                                            if(lastItem is TextContentItem) {
//                                                if(element.text().isNotEmpty())
//                                                    lastItem.text = SpannableStringBuilder(lastItem.text).append("$lineSeparator${element.text()}$lineSeparator").apply {
//                                                        setSpan(StyleSpan(Typeface.BOLD), lastIndexOf(element.text()), length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
//                                                    }
//                                            } else
//                                                add(TextContentItem(element.text(), style = Typeface.BOLD, isChapterTitle = tocTitles.contains(element.text()), isBookTitle = element.text() == title?.text))
//                                        } catch(thr: Throwable) {
//                                            add(TextContentItem(element.text(), style = Typeface.BOLD, isChapterTitle = tocTitles.contains(element.text()), isBookTitle = element.text() == title?.text))
//                                        }
//                                    "h4" ->
//                                        try {
//                                            val lastItem = last()
//
//                                            if(lastItem is TextContentItem) {
//                                                if(element.text().isNotEmpty())
//                                                    lastItem.text = SpannableStringBuilder(lastItem.text).append("$lineSeparator${element.text()}$lineSeparator").apply {
//                                                        setSpan(StyleSpan(Typeface.BOLD), lastIndexOf(element.text()), length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
//                                                    }
//                                            } else
//                                                add(TextContentItem(element.text(), style = Typeface.BOLD, isChapterTitle = tocTitles.contains(element.text()), isBookTitle = element.text() == title?.text))
//                                        } catch(thr: Throwable) {
//                                            add(TextContentItem(element.text(), style = Typeface.BOLD, isChapterTitle = tocTitles.contains(element.text()), isBookTitle = element.text() == title?.text))
//                                        }
//                                    "h5" ->
//                                        try {
//                                            val lastItem = last()
//
//                                            if(lastItem is TextContentItem) {
//                                                if(element.text().isNotEmpty())
//                                                    lastItem.text = SpannableStringBuilder(lastItem.text).append("$lineSeparator${element.text()}$lineSeparator").apply {
//                                                        setSpan(StyleSpan(Typeface.BOLD), lastIndexOf(element.text()), length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
//                                                    }
//                                            } else
//                                                add(TextContentItem(element.text(), style = Typeface.BOLD, isChapterTitle = tocTitles.contains(element.text()), isBookTitle = element.text() == title?.text))
//                                        } catch(thr: Throwable) {
//                                            add(TextContentItem(element.text(), style = Typeface.BOLD, isChapterTitle = tocTitles.contains(element.text()), isBookTitle = element.text() == title?.text))
//                                        }
//                                    "h6" ->
//                                        try {
//                                            val lastItem = last()
//
//                                            if(lastItem is TextContentItem) {
//                                                if(element.text().isNotEmpty())
//                                                    lastItem.text = SpannableStringBuilder(lastItem.text).append("$lineSeparator${element.text()}$lineSeparator").apply {
//                                                        setSpan(StyleSpan(Typeface.BOLD), lastIndexOf(element.text()), length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
//                                                    }
//                                            } else
//                                                add(TextContentItem(element.text(), style = Typeface.BOLD, isChapterTitle = tocTitles.contains(element.text()), isBookTitle = element.text() == title?.text))
//                                        } catch(thr: Throwable) {
//                                            add(TextContentItem(element.text(), style = Typeface.BOLD, isChapterTitle = tocTitles.contains(element.text()), isBookTitle = element.text() == title?.text))
//                                        }
                                    "em" ->
                                        try {
                                            val lastItem = last()

                                            if(lastItem is TextContentItem) {
                                                if(element.text().isNotEmpty())
                                                    lastItem.text = SpannableStringBuilder(lastItem.text).append("${element.text()}$lineSeparator").apply {
                                                        setSpan(StyleSpan(Typeface.ITALIC), lastIndexOf(element.text()), length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                                                    }
                                            } else
                                                add(TextContentItem(element.text(), style = Typeface.ITALIC, isChapterTitle = tocTitles.contains(element.text()), isBookTitle = element.text() == title?.text))
                                        } catch(thr: Throwable) {
                                            add(TextContentItem(element.text(), style = Typeface.ITALIC, isChapterTitle = tocTitles.contains(element.text()), isBookTitle = element.text() == title?.text))
                                        }
                                }
                            }
                        }
                    }
                else null
            ).apply {
                titleInlined = titleInlinedIntoContents
                coverImageInlined = coverImageInlinedIntoContents
            }
        }

        return null
    }
}