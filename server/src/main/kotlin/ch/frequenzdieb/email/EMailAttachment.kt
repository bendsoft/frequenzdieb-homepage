package ch.frequenzdieb.email

import org.springframework.core.io.ByteArrayResource

data class EMailAttachment (
    val attachmentFilename: String,
    val file: ByteArrayResource
)
