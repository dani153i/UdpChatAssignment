package com.dani153i.udpchatexample.app.client;

import com.dani153i.udpchat.service.client.UdpChatService;

import java.io.BufferedReader;

/**
 * UDP chat server application.
 * This extension is solely for IDE debugging.
 * @author daniel blom
 * @version 1.0
 */
public class ClientApp2 extends ClientApp {
    public ClientApp2(UdpChatService chatService, BufferedReader inputReader) {
        super(chatService, inputReader);
    }
}
