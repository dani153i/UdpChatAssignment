package com.dani153i.udpchat.service.shared.messages;

public enum Error
{
    CLIENT_NOT_ACCEPTED("0x01", "client not accepted"),
    USERNAME_TAKEN("0x02", "duplicate username"),
    COMMAND_UNKNOWN("0x03", "unknown command"),
    SYNTAX_UNKNOWN("0x04", "bad command"),
    SERVER_FULL("0x05", "server is full"),
    USERNAME_INVALID("0x06", "username is min 1 and max 12 chars long, only letters, digits, ‘-‘ and ‘_’ allowed");

    public final String label;
    public final String errorCode;
    private Error(final String errorCode, final String label) {
        this.errorCode = errorCode;
        this.label = label;
    }
}
