package ch.frequenzdieb.common

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.traversal.DocumentTraversal
import org.w3c.dom.traversal.NodeFilter
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

@Service
class TemplateParser {
    private val logger = LoggerFactory.getLogger(this::class.java.name)

    fun parseHTMLTemplate(
        template: InputStream
    ): Document = DocumentBuilderFactory
        .newInstance()
        .newDocumentBuilder()
        .parse(template)

    fun replaceMarkups(document: Document, markupReplacements: Map<String, Element>): Document =
        document.apply {
            findAllMarkups {
                markupReplacements[textContent]
                    ?.let { replaceMarkup { it } }
            }
            normalize()

            logger.info(ByteArrayOutputStream().let { out ->
                printDocument(out)
                out.toString("UTF-8")
            })
        }

    private fun Document.printDocument(out: OutputStream) =
        TransformerFactory.newInstance().apply {
            newTransformer().apply {
                setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no")
                setOutputProperty(OutputKeys.METHOD, "xml")
                setOutputProperty(OutputKeys.INDENT, "yes")
                setOutputProperty(OutputKeys.ENCODING, "UTF-8")
                setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
                transform(
                    DOMSource(this@printDocument),
                    StreamResult(OutputStreamWriter(out, "UTF-8"))
                )
            }
        }

    private fun Document.findAllMarkups(nodeConsumer: Node.() -> Unit) {
        (this as DocumentTraversal).createNodeIterator(this, NodeFilter.SHOW_COMMENT, null, true).apply {
            do {
                val currentNode = nextNode()
                if (currentNode != null )
                    nodeConsumer(currentNode)
                else break
            } while (true)
        }
    }

    private fun Node.replaceMarkup(nodeConsumer: Node.() -> Node) {
        parentNode.replaceChild(nodeConsumer(),this)
    }
}
