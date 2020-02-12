package ch.frequenzdieb.api.services.ticketing.payment.datatrans

import ch.frequenzdieb.api.services.ticketing.payment.datatrans.UppTransactionService.Body.Transaction
import ch.frequenzdieb.api.services.ticketing.payment.datatrans.UppTransactionService.Body.Transaction.UserParameters
import javax.xml.bind.annotation.XmlRegistry

/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the generated.datatrans package.
 *
 * An ObjectFactory allows you to programatically
 * construct new instances of the Java representation
 * for XML content. The Java representation of XML
 * content can consist of schema derived interfaces
 * and classes representing the binding of schema
 * type definitions, element declarations and model
 * groups.  Factory methods for each of these are
 * provided in this class.
 *
 */
@XmlRegistry
class ObjectFactory
/**
 * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: generated.datatrans
 *
 */
{
    /**
     * Create an instance of [UppTransactionService]
     *
     */
    fun createUppTransactionService(): UppTransactionService {
        return UppTransactionService()
    }

    /**
     * Create an instance of [UppTransactionService.Body]
     *
     */
    fun createUppTransactionServiceBody(): UppTransactionService.Body {
        return UppTransactionService.Body()
    }

    /**
     * Create an instance of [UppTransactionService.Body.Transaction]
     *
     */
    fun createUppTransactionServiceBodyTransaction(): Transaction {
        return Transaction()
    }

    /**
     * Create an instance of [UppTransactionService.Body.Transaction.UserParameters]
     *
     */
    fun createUppTransactionServiceBodyTransactionUserParameters(): UserParameters {
        return UserParameters()
    }

    /**
     * Create an instance of [generated.datatrans.Error]
     *
     */
    fun createError(): Error {
        return Error()
    }

    /**
     * Create an instance of [UppTransactionService.Body.Transaction.Success]
     *
     */
    fun createUppTransactionServiceBodyTransactionSuccess(): UppTransactionService.Body.Transaction.Success {
        return Transaction.Success()
    }

    /**
     * Create an instance of [UppTransactionService.Body.Transaction.Error]
     *
     */
    fun createUppTransactionServiceBodyTransactionError(): UppTransactionService.Body.Transaction.Error {
        return Transaction.Error()
    }

    /**
     * Create an instance of [UppTransactionService.Body.Transaction.UserParameters.Parameter]
     *
     */
    fun createUppTransactionServiceBodyTransactionUserParametersParameter(): UppTransactionService.Body.Transaction.UserParameters.Parameter {
        return UserParameters.Parameter()
    }
}
