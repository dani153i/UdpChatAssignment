package com.dani153i.udpchat.service.client;

import com.dani153i.udpchat.service.shared.messages.Error;

import java.net.InetAddress;

/**
 * An interface for chat event handling / surfacing.
 * @author daniel blom
 * @version 1.0
 */
public interface IChatListener
{
    /**
     * Called when client receives join chat confirmation.
     * @param username client username
     */
    void onJoined(String username);

    /**
     * Called when client receives a message from chat server.
     * @param username message sender
     * @param data message data
     */
    void onMessageReceived(String username, String data);

    /**
     * Called when client receives a message that an error has occurred.
     * @param error occurred error
     */
    void onError(Error error);
}
