package ch.frequenzdieb.common

enum class ErrorCode {
    EMAIL_INVALID,
    VALIDATION_ERROR,
    SIGNATURE_INVALID,
    ENTITY_INVALID,
    NOT_AUTHORIZED,
    SUBSCRIPTION_ALREADY_CONFIRMED,
    SUBSCRIPTION_INVALID_ID,
    SUBSCRIPTION_NOT_EXISTS,
    TICKET_MISSING_SUBSCRIPTION,
    TICKET_ID_INVALID,
    TICKET_ALREADY_USED,
    TICKET_FOR_ANOTHER_EVENT,
    TICKET_NOT_PAID,
    TICKET_TYPE_DUPLICATE_TEMPLATE_TAG,
    TICKET_TYPE_DUPLICATE_ATTRIBUTE_IDS,
    TICKET_TYPE_MISSING_ATTRIBUTES,
    TICKET_TYPE_INVALID_ATTRIBUTES,
    TICKET_ALREADY_ARCHIVED
}
