package bogdandonduk.fileparserstoolboxandroidlib.epub

import android.graphics.Typeface
import bogdandonduk.fileparserstoolboxandroidlib.core.*
import bogdandonduk.fileparserstoolboxandroidlib.core.books.Book
import nl.siegmann.epublib.epub.EpubReader
import org.jsoup.Jsoup
import java.io.File

object EpubUtils {
    fun getValidEpub(path: String) = try {
        EpubReader().readEpub(File(path).inputStream()).apply { coverImage.data }
    } catch(thr: Throwable) {
        null
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
}