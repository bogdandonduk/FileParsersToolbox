package bogdandonduk.fileparserstoolboxandroidlib.fb2

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Base64
import bogdandonduk.fileparserstoolboxandroidlib.core.ContentItem
import bogdandonduk.fileparserstoolboxandroidlib.core.ImageContentItem
import bogdandonduk.fileparserstoolboxandroidlib.core.TextContentItem
import bogdandonduk.fileparserstoolboxandroidlib.core.TitleTextContentItem
import bogdandonduk.fileparserstoolboxandroidlib.core.books.Book
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

object Fb2Utils {
    fun getTitle(path: String) = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(File(path)).getElementsByTagName("book-title").item(0).textContent

    fun getCoverImageAsByteArray(path: String) : ByteArray? {
        var imageData: ByteArray? = null

        DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(File(path)).run {
            getElementsByTagName("coverpage").item(0).childNodes.item(0).attributes.item(0).textContent.let { coverImageName ->
                getElementsByTagName("binary").let {
                    for(i in 0 until it.length) {
                        it.item(i).attributes.let { attributes ->
                            for(j in 0 until attributes.length) {
                                attributes.item(j).textContent.let { attributeText ->
                                    if(attributeText.contains(coverImageName) || coverImageName.contains(attributeText))
                                        imageData = Base64.decode(it.item(i).textContent, 0)
                                }
                            }
                        }
                    }
                }
            }
        }

        return imageData
    }

    fun getTableOfContentsTitles(path: String) : MutableList<String>? {
        val tocTitles = mutableListOf<String>().apply {
            DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(File(path)).getElementsByTagName("section").run {
                for(i in 0 until length) {
                    item(i).childNodes.let {
                        for(j in 0 until it.length) {
                            it.item(j).nodeName.let { nodeName ->
                                if(nodeName.equals("title", true))
                                    add(nodeName)
                            }
                        }
                    }
                }
            }
        }

        return if(tocTitles.isNotEmpty()) tocTitles else null
    }

    fun getValidMergedFb2(path: String, titleInlinedIntoContents: Boolean = false, coverImageInlinedIntoContents: Boolean = false) = try {
        val title: String?

        var coverImage: ByteArray? = null

        var tocTitles: MutableList<String>? = null

        val contents = mutableListOf<ContentItem>().apply {
            DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(File(path)).run {
                title = getElementsByTagName("book-title").item(0).textContent

                getElementsByTagName("coverpage").item(0).childNodes.item(0).attributes.item(0).textContent.let { coverImageName ->
                    getElementsByTagName("binary").let {
                        for(i in 0 until it.length) {
                            it.item(i).attributes.let { attributes ->
                                for(j in 0 until attributes.length) {
                                    attributes.item(j).textContent.let { attributeText ->
                                        if(attributeText.contains(coverImageName) || coverImageName.contains(attributeText))
                                            coverImage = Base64.decode(it.item(i).textContent, 0)
                                    }
                                }
                            }
                        }
                    }
                }

                mutableListOf<String>().let { tocTitlesList ->
                    getElementsByTagName("section").run {
                        for(i in 0 until length) {
                            item(i).childNodes.let {
                                for(j in 0 until it.length) {
                                    it.item(j).let { node ->
                                        if(node.nodeName.equals("title", true))
                                            tocTitlesList.add(node.textContent)
                                    }
                                }
                            }
                        }
                    }

                    if(tocTitlesList.isNotEmpty())
                        tocTitles = tocTitlesList
                }

                if(coverImageInlinedIntoContents && coverImage != null)
                    add((ImageContentItem(coverImage!!, true)))

                if(titleInlinedIntoContents && title != null)
                    add(TitleTextContentItem(title, isBookTitle = true))

                val lineSeparator = System.getProperty("line.separator") ?: "\r\n"

                getElementsByTagName("section").let {
                    for(i in 0 until it.length) {
                        it.item(i).childNodes.let { sectionContents ->
                            for(j in 0 until sectionContents.length) {
                                val node = sectionContents.item(j)

                                when(node.nodeName.lowercase()) {
                                    "p" ->
                                        try {
                                            last().let { lastItem ->
                                                if(lastItem is TextContentItem)
                                                    if(node.textContent.isNotEmpty() && node.textContent != lastItem.text.substring(lastItem.text.length - node.textContent.length, lastItem.text.length))
                                                        lastItem.text = SpannableStringBuilder(lastItem.text).append("$lineSeparator$lineSeparator${node.textContent}")
                                                else
                                                    add(TextContentItem(node.textContent))
                                            }
                                        } catch(thr: Throwable) {
                                            add(TextContentItem(node.textContent))
                                        }

                                    "title" ->
                                        try {
                                            last().let { lastItem ->
                                                if(lastItem is TextContentItem)
                                                    if(node.textContent.isNotEmpty() && node.textContent != lastItem.text.substring(lastItem.text.length - node.textContent.length, lastItem.text.length))
                                                        lastItem.text = SpannableStringBuilder(lastItem.text).append("$lineSeparator$lineSeparator${node.textContent}").apply {
                                                            setSpan(StyleSpan(Typeface.BOLD), length - node.textContent.length, length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                                                        }
                                                else
                                                    add(TitleTextContentItem(node.textContent, isChapterTitle = true))
                                            }
                                        } catch(thr: Throwable) {
                                            add(TitleTextContentItem(node.textContent, isChapterTitle = true))
                                        }

                                    "epigraph", "cite" ->
                                        try {
                                            last().let { lastItem ->
                                                if(lastItem is TextContentItem)
                                                    if(node.textContent.isNotEmpty() && node.textContent != lastItem.text.substring(lastItem.text.length - node.textContent.length, lastItem.text.length))
                                                        lastItem.text = SpannableStringBuilder(lastItem.text).append("$lineSeparator$lineSeparator${node.textContent}").apply {
                                                            setSpan(StyleSpan(Typeface.ITALIC), length - node.textContent.length, length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                                                        }
                                                else
                                                    add(TextContentItem(node.textContent, style = Typeface.ITALIC))
                                            }
                                        } catch(thr: Throwable) {
                                            add(TextContentItem(node.textContent, style = Typeface.ITALIC))
                                        }
                                }
                            }
                        }
                    }
                }
            }
        }

        if(title != null && tocTitles != null && contents.isNotEmpty())
            Book(
                TitleTextContentItem(title, isBookTitle = true),
                ImageContentItem(coverImage!!, true),
                tocTitles,
                contents
            )
        else null
    } catch(thr: Throwable) {
        null
    }

    fun getValidFb2(path: String, titleInlinedIntoContents: Boolean = false, coverImageInlinedIntoContents: Boolean = false) = try {
        val title: String?

        var coverImage: ByteArray? = null

        var tocTitles: MutableList<String>? = null

        val contents = mutableListOf<ContentItem>().apply {
            DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(File(path)).run {
                title = getElementsByTagName("book-title").item(0).textContent

                getElementsByTagName("coverpage").item(0).childNodes.item(0).attributes.item(0).textContent.let { coverImageName ->
                    getElementsByTagName("binary").let {
                        for(i in 0 until it.length) {
                            it.item(i).attributes.let { attributes ->
                                for(j in 0 until attributes.length) {
                                    attributes.item(j).textContent.let { attributeText ->
                                        if(attributeText.contains(coverImageName) || coverImageName.contains(attributeText))
                                            coverImage = Base64.decode(it.item(i).textContent, 0)
                                    }
                                }
                            }
                        }
                    }
                }

                mutableListOf<String>().let { tocTitlesList ->
                    getElementsByTagName("section").run {
                        for(i in 0 until length) {
                            item(i).childNodes.let {
                                for(j in 0 until it.length) {
                                    it.item(j).let { node ->
                                        if(node.nodeName.equals("title", true))
                                            tocTitlesList.add(node.textContent)
                                    }
                                }
                            }
                        }
                    }

                    if(tocTitlesList.isNotEmpty())
                        tocTitles = tocTitlesList
                }

                if(coverImageInlinedIntoContents && coverImage != null)
                    add((ImageContentItem(coverImage!!, true)))

                if(titleInlinedIntoContents && title != null)
                    add(TitleTextContentItem(title, isBookTitle = true))

                getElementsByTagName("section").let {
                    for(i in 0 until it.length) {
                        it.item(i).childNodes.let { sectionContents ->
                            for(j in 0 until sectionContents.length) {
                                val node = sectionContents.item(j)

                                when(node.nodeName.lowercase()) {
                                    "p" ->
                                        if(node.textContent.isNotEmpty() && (last() !is TextContentItem || node.textContent != (last() as TextContentItem).text))
                                            add(TextContentItem(node.textContent))
                                    "title" ->
                                        if(node.textContent.isNotEmpty() && (last() !is TextContentItem || node.textContent != (last() as TextContentItem).text))
                                            add(TitleTextContentItem(node.textContent, isChapterTitle = true))
                                    "epigraph", "cite" ->
                                        if(node.textContent.isNotEmpty() && (last() !is TextContentItem || node.textContent != (last() as TextContentItem).text))
                                            add(TextContentItem(node.textContent, style = Typeface.ITALIC))
                                }
                            }
                        }
                    }
                }
            }
        }

        if(title != null && tocTitles != null && contents.isNotEmpty())
            Book(
                TitleTextContentItem(title, isBookTitle = true),
                ImageContentItem(coverImage!!, true),
                tocTitles,
                contents
            )
        else null
    } catch(thr: Throwable) {
        null
    }
}