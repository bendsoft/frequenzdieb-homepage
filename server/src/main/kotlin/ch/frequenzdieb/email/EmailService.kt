package ch.frequenzdieb.email

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component

@Component
class EmailService {
    @Autowired
    lateinit var javaMailSender: JavaMailSender

    @Value("\${mail.sender}")
    lateinit var senderEMailAddress: String

    fun sendEmail(
        emailAddress: String,
        subject: String,
        message: String,
        attachment: EMailAttachment? = null
    ) = GlobalScope.launch {
        val mailMessage = javaMailSender.createMimeMessage()
        MimeMessageHelper(mailMessage, true).apply {
            setFrom(senderEMailAddress)
            setTo(emailAddress)
            setSubject(subject)
            setText(message, true)
            if (attachment != null) {
                addAttachment(
                    attachment.attachmentFilename,
                    attachment.file
                )
            }
        }

        javaMailSender.send(mailMessage)
    }
}
