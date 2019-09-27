package com.dani153i.udpchat.service.client;

import com.dani153i.udpchat.service.shared.messages.Command;
import com.dani153i.udpchat.service.shared.messages.Error;
import com.dani153i.udpservice.client.UdpClient;
import com.dani153i.udpservice.shared.IMessageHandler;
import com.dani153i.udpservice.shared.message.Message;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A UDP chat client service.
 * @author daniel blom
 * @version 1.0
 */
public class UdpChatService
{
    private String address;
    private int port;
    private UdpClient udpClient;
    private String username;
    private Vector<String> usersOnline;
    private ArrayList<IChatListener> listeners;
    private ScheduledExecutorService scheduledExecutorService;
    private Future futureHeartbeat;

    /**
     * Get all online users.
     * @return A Vector of all online users
     * @see Vector
     */
    public Vector<String> getUsersOnline() {
        return usersOnline;
    }

    /**
     * Add IChatListener for callbacks.
     * @param listener
     */
    public void addListener(IChatListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove IChatListener.
     * @param listener
     */
    public void removeListener(IChatListener listener) {
        listeners.remove(listener);
    }


    /**
     * New UdpChatService instance.
     * @param address host address
     * @param port host port
     * @throws SocketException
     * @throws UnknownHostException
     */
    public UdpChatService(String address, int port) throws SocketException, UnknownHostException {
        this.address = address;
        this.port = port;
        this.usersOnline = new Vector<>();
        this.scheduledExecutorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        this.futureHeartbeat = null;
        this.listeners = new ArrayList<>();
        this.init();
    }

    /**
     * UdpChatService initialization.
     * @throws SocketException
     * @throws UnknownHostException
     */
    private void init() throws SocketException, UnknownHostException {
        udpClient = new UdpClient(address, port);
        this.username = null;
    }

    /**
     * Listen for inbound message
     */
    public void listen() {
        udpClient.listen(new IMessageHandler() {
            @Override
            public void handleMessage(Message message) {
                String dataString = null;
                String command = null;
                String content = null;

                dataString = new String(message.getData(), StandardCharsets.UTF_8).trim();
                command = (dataString.contains(" ")) ? dataString.substring(0, dataString.indexOf(" ")) : dataString;
                command = command.trim();
                if (dataString.contains(" ") && dataString.length() > dataString.indexOf(" ") + 1)
                    content = dataString.substring(dataString.indexOf(" ") + 1);
                else
                    content = "";
                content = content.trim();

                //System.out.println(dataString);

                if(command.equals(Command.CONNECTED.label)) {
                    onJoined();
                } else if(command.equals(Command.MESSAGE.label)) {
                    onData(content);
                } else if(command.equals(Command.USERLIST.label)) {
                    onList(content);
                } else if(command.equals(Command.ERROR.label)) {
                    onError(content);
                }
            }
        });
    }

    /**
     * Send join chat request.
     * @param username client username
     */
    public void join(String username) {

        this.username = username;
        sendData(Command.CONNECT.label + " " + username);
    }

    /**
     * Send disconnect message.
     */
    public void disconnect() {
        futureHeartbeat.cancel(true);

        // if connected to localhost
        if(address.equals("127.0.0.1") ||address.equals("localhost"))
            sendData("QUIT " + username);
        else
            sendData("QUIT");

        udpClient.close();
    }

    /**
     * Send message.
     * @param data
     */
    public void sendMessage(String data) {
        udpClient.send(("DATA " + username + ": " + data).getBytes());
    }

    /**
     * Send data as a byte array.
     * @param data
     */
    private void sendData(String data) {
        udpClient.send(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Called when client has received chat join confirmation.
     */
    private void onJoined() {
        futureHeartbeat = scheduledExecutorService.scheduleAtFixedRate(() ->
        {
            if(address.equals("127.0.0.1") || address.equals("localhost"))
                sendData("IMAV " + username);
            else
                sendData("IMAV");


        }, 5000, 60000, TimeUnit.MILLISECONDS);

        for(IChatListener listener : listeners) {
            listener.onJoined(username);
        }
    }

    /**
     * Called when client receives a message that an error has occurred.
     * @param content occurred error
     */
    private void onError(String content) {
        Error error = null;

        for (Error err : Error.values()) {
            if(content.contains(err.errorCode))
                error = err;
        }

        for (IChatListener listener : listeners) {
            listener.onError(error);
        }
    }

    /**
     * Called when client receives a message.
     * @param content message data
     */
    private void onData(String content) {
        String username = null;
        String data = null;

        username = content.substring(0, content.indexOf(": "));
        data = content.substring(content.indexOf(": ") + 1);

        for (IChatListener listener : listeners) {
            listener.onMessageReceived(username, data);
        }
    }

    /**
     * Called when server pushes a message containing all currently online users.
     * @param content message data
     */
    private void onList(String content) {
            usersOnline.removeAllElements();
            String[] usernameArray = content.split(" ");
            usersOnline.addAll(Arrays.asList(usernameArray));
    }
}
