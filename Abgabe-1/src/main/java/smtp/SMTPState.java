package smtp;

enum SMTPState {
    CONNECTED,
    GREETED,
    MAIL_STARTED,
    RECIPIENTS_ADDED,
    DATA_MODE,
    QUIT
}
