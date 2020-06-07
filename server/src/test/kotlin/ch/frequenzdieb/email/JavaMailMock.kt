package ch.frequenzdieb.email

import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessagePreparator
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.io.InputStream
import javax.mail.internet.MimeMessage

@Component
class JavaMailMock : JavaMailSender {
    override fun createMimeMessage(): MimeMessage {
        TODO("Not yet implemented")
    }

    override fun createMimeMessage(contentStream: InputStream): MimeMessage {
        TODO("Not yet implemented")
    }

    override fun send(mimeMessage: MimeMessage) {
        TODO("Not yet implemented")
    }

    override fun send(vararg mimeMessages: MimeMessage?) {
        TODO("Not yet implemented")
    }

    override fun send(mimeMessagePreparator: MimeMessagePreparator) {
        TODO("Not yet implemented")
    }

    override fun send(vararg mimeMessagePreparators: MimeMessagePreparator?) {
        TODO("Not yet implemented")
    }

    override fun send(simpleMessage: SimpleMailMessage) {
        TODO("Not yet implemented")
    }

    override fun send(vararg simpleMessages: SimpleMailMessage?) {
        TODO("Not yet implemented")
    }
}
