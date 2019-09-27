package com.dani153i.udpservice.shared;

import com.dani153i.udpservice.shared.message.Message;

/**
 * Message handler used for surfacing received messages.
 * @author daniel blom
 * @version 1.0
 */
public interface IMessageHandler
{
    /**
     * Contract for handling udp chat messages.
     * @param message message to be handled
     */
    void handleMessage(Message message);
}
