package ch.frequenzdieb.api.services.common

import org.springframework.core.io.ByteArrayResource

data class EMailAttachment (
    val attachmentFilename: String,
    val file: ByteArrayResource
)
