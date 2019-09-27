package com.dani153i.udpchat.service.shared.messages;

public enum Command
{
    CONNECT("JOIN"),
    CONNECTED("J_OK"),
    MESSAGE("DATA"),
    ERROR("J_ERR"),
    HEARTBEAT("IMAV"),
    DISCONNECT("QUIT"),
    USERLIST("LIST");

    public final String label;
    private Command(String label) {
        this.label = label;
    }
}
