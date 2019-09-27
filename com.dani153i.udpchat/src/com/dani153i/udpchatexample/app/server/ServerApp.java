package com.dani153i.udpchatexample.app.server;

import com.dani153i.udpchat.service.server.IChatListener;
import com.dani153i.udpchat.service.server.UdpChatService;
import com.dani153i.udpchat.service.shared.messages.Error;
import com.dani153i.udpchatexample.app.server.logger.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * UDP chat server application.
 * @author daniel blom
 * @version 1.0
 */
public class ServerApp implements IChatListener
{
    Logger logger;
    UdpChatService chatService;

    public static void main(String[] args) {
        UdpChatService chatService = null;
        Logger logger;

        String executionPath = System.getProperty("user.dir").replace("\\", "/");
        String loggerFileName = "UdpChatServer_" +  new SimpleDateFormat("EEE MMM dd, yyyy").format(new Date());

        try {
            logger = new Logger(executionPath, loggerFileName);
            chatService = new UdpChatService(1337, 5);
            ServerApp server = new ServerApp(chatService, logger);
        } catch (SocketException e) {
            System.err.println("Unable to initialize the server.\n" + e.getMessage());
            // log
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * New ServerApp instance.
     * @param chatService for communication
     * @param logger for logging purposes
     */
    public ServerApp(UdpChatService chatService, Logger logger) {
        this.chatService = chatService;
        this.logger = logger;
        this.init();
    }

    /**
     * ServerApp initialization.
     */
    private void init() {
        chatService.addListener(this);
        chatService.listen();
    }

    /**
     * Called when a client has connected.
     * @param username client username
     */
    @Override
    public void onJoined(String username) {
        System.out.println(username + " joined the server.");
    }

    /**
     * Called when server receives a message.
     * @param username message sender
     * @param data message data
     */
    @Override
    public void onMessageReceived(String username, String data) {
        System.out.println(">> " + username + ": " + data);
    }

    /**
     * Called when server broadcasts a message.
     * @param data message data
     * @param clients number of clients broadcast to
     */
    @Override
    public void onBroadcast(String data, int clients) {
        //System.out.println(">> (clients: " + clients + ") " + data);
    }

    /**
     * Called when an error occurs.
     * @param address error instigator address
     * @param port error instigator port
     * @param error the occurred error
     * @param data request that lead to the error
     */
    @Override
    public void onError(InetAddress address, int port, Error error, String data) {
        String message = "(" + error.errorCode + ") " + error.label + " - " + address + ":" + port + " request: " + data;
        System.out.println();

        // log
        try {
            logger.log(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
