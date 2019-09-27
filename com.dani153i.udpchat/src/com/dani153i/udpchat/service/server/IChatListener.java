package com.dani153i.udpchat.service.server;

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
     * On client joined chat.
     * @param username client username
     */
    void onJoined(String username);

    /**
     * On message received.
     * @param username message sender
     * @param data message data
     */
    void onMessageReceived(String username, String data);

    /**
     * On server broadcast.
     * @param data
     * @param clients
     */
    void onBroadcast(String data, int clients);

    /**
     * On client request error.
     * @param address client address
     * @param port client port
     * @param error occurred error
     * @param data request which lead to the error
     */
    void onError(InetAddress address, int port, Error error, String data);
}
