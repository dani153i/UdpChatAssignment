package com.dani153i.udpchat.service.server;

import com.dani153i.udpchat.service.server.clientcleaner.ClientCleaner;
import com.dani153i.udpchat.service.server.clientcleaner.IClientRemover;
import com.dani153i.udpchat.service.shared.messages.Command;
import com.dani153i.udpchat.service.shared.messages.Error;
import com.dani153i.udpservice.server.UdpServer;
import com.dani153i.udpchat.service.shared.model.ChatClient;
import com.dani153i.udpservice.shared.IMessageHandler;
import com.dani153i.udpservice.shared.connection.Connection;
import com.dani153i.udpservice.shared.message.Message;

import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A UDP chat server service.
 * @author daniel blom
 * @version 1.0
 */
public class UdpChatService implements IClientRemover
{
    private final int port;
    private final int clientMax;
    private UdpServer udpServer;
    private final Vector<ChatClient> clients;
    private ArrayList<IChatListener> listeners;
    private ScheduledExecutorService scheduledExecutorService;
    private Future futureClientCleaner;

    /**
     * Add listener for callbacks.
     * @param listener
     */
    public void addListener(IChatListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove listener.
     * @param listener
     */
    public void removeListener(IChatListener listener) {
        listeners.remove(listener);
    }

    /**
     * New instance of UdpChatService
     * @param port port the UdpServer will listen on
     * @param clientMax max amount of clients
     * @throws SocketException
     */
    public UdpChatService(int port, int clientMax) throws SocketException {
        this.port = port;
        this.clientMax = clientMax;
        this.clients = new Vector<>();
        this.listeners = new ArrayList<>();
        this.scheduledExecutorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        this.futureClientCleaner = null;
        this.init();
    }

    /**
     * UdpChatService initialization.
     * @throws SocketException
     */
    private void init() throws SocketException {
        this.udpServer = new UdpServer(this.port);
        this.futureClientCleaner = scheduledExecutorService.scheduleWithFixedDelay(new ClientCleaner(this, clients), 90000, 90000, TimeUnit.MILLISECONDS);
    }

    /**
     * Listen for inbound messages.
     */
    public void listen() {
        udpServer.listen(new IMessageHandler() {
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

                if(command.indexOf(Command.CONNECT.label) == 0) {
                    onJoin(message, content);
                } else if(command.indexOf(Command.MESSAGE.label) == 0) {
                    onData(message, content);
                } else if(command.indexOf(Command.HEARTBEAT.label) == 0) {
                    // if sender is not localhost
                    if(!(message.getAddress().getHostName().equals("127.0.0.1") || message.getAddress().getHostName().equals("localhost")))
                        onHeartbeat(message);
                    else
                        onLocalhostHeatbeat(message, content);
                } else if(command.indexOf(Command.DISCONNECT.label) == 0) {
                    // if sender is not localhost
                    if(!(message.getAddress().getHostName().equals("127.0.0.1") || message.getAddress().getHostName().equals("localhost")))
                        onQuit(message);
                    else
                        onLocalQuit(message, content);
                } else {
                    onError(Error.COMMAND_UNKNOWN, message);
                }

            }
        });
    }

    /**
     * Find connected client by connection.
     * @param connection client connection
     * @return chat client if connected
     * @see ChatClient
     */
    private synchronized ChatClient findChatClient(Connection connection) {
        ChatClient result = null;

        for (ChatClient client : clients) {
            if(client.getConnection().equals(connection))
            {
                result = client;
                break;
            }
        }
        return result;
    }

    /**
     * Find connected client by connection and username.
     * @param connection client connection
     * @param username client username
     * @return chat client if connected
     * @see ChatClient
     */
    private synchronized ChatClient findChatClient(Connection connection, String username) {
        ChatClient result = null;

        for (ChatClient client : clients) {
            if(client.getConnection().equals(connection) && client.getUsername().equals(username))
            {
                result = client;
                break;
            }
        }
        return result;
    }

    /**
     * Get current connections.
     * @return a list of connections of all currently online clients.
     */
    private ArrayList<Connection> getConnections() {
        ArrayList<Connection> connections = new ArrayList<>();
        ChatClient client = null;
        synchronized (clients) {
            Iterator<ChatClient> it = clients.iterator();
            while (it.hasNext()) {
                client = it.next();
                connections.add(client.getConnection());
            }
        }

        return connections;
    }

    /**
     * Send data through a connection.
     * @param connection
     * @param data
     */
    private void sendData(Connection connection, String data) {
        connection.send(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Broadcast a message to all currently online clients.
     * @param message message to be broadcast
     */
    private void broadcast(String message) {
        udpServer.broadcast(getConnections(), message.getBytes(StandardCharsets.UTF_8));

        for (IChatListener listener: listeners) {
            listener.onBroadcast(message, clients.size());
        }
    }

    /**
     * Broadcast a list of all currently online users.
     */
    private void broadcastUsersOnline() {
        StringBuilder usersOnline = new StringBuilder();

        synchronized (this) {
            clients.forEach(client -> {
                usersOnline.append(" ").append(client.getUsername());
            });
        }

        broadcast("LIST" + usersOnline.toString());
    }

    /**
     * Called on chat join request.
     * @param message client request
     * @param username client username
     */
    private void onJoin(Message message, String username) {
        ChatClient existingClient = null;
        Pattern usernamePattern;
        Matcher patternMatcher;
        boolean nameTaken = false;
        StringBuilder usersOnline = null;

        if(username.isEmpty())
        {
            onError(Error.SYNTAX_UNKNOWN, message);
            return;
        }

        // check for username validity
        usernamePattern = Pattern.compile("^[[a-z][A-Z][0-9]_-]{3,15}$");
        patternMatcher = usernamePattern.matcher(username);
        if(!patternMatcher.matches()) {
            onError(Error.USERNAME_INVALID, message);
            return;
        }

        // check if username is taken
        synchronized (clients) {
            for (ChatClient chatClient : clients) {
                if (username.equals(chatClient.getUsername())) {
                    nameTaken = true;
                    break;
                }
            }
        }

        // if username is taken
        if(nameTaken) {
            onError(Error.USERNAME_TAKEN, message);
            return;
        }

        // if connection is not from localhost
        if(!(message.getAddress().getHostName().equals("127.0.0.1") || message.getAddress().getHostName().equals("localhost"))) {
            // find potential existing connected client
            existingClient = findChatClient(message.getConnection());
        }

        // if client is already connected
        if(existingClient != null) {
            existingClient.setUsername(username);
        } else {
            if(clients.size() >= clientMax)
            {
                onError(Error.SERVER_FULL, message);
                return;
            }
            clients.add(new ChatClient(username, message.getConnection()));
        }

        sendData(message.getConnection(), Command.CONNECTED.label);
        broadcastUsersOnline();

        for (IChatListener listener: listeners) {
            listener.onJoined(username);
        }
    }

    /**
     * Called when server receives a quit message from a client.
     * @param message client request
     */
    private void onQuit(Message message) {
        ArrayList<ChatClient> clientsToRemove = new ArrayList<>();
        clientsToRemove.add(findChatClient(message.getConnection()));

        removeClients(clientsToRemove);
        broadcastUsersOnline();
    }

    /**
     * Called when server receives a quit message from a client on localhost.
     * @param message client request
     * @param username message sender
     */
    private void onLocalQuit(Message message, String username) {
        ArrayList<ChatClient> clientsToRemove = new ArrayList<>();
        clientsToRemove.add(findChatClient(message.getConnection(), username));

        removeClients(clientsToRemove);
        broadcastUsersOnline();
    }

    /**
     * Called when server receives a message from client.
     * @param message client request
     * @param content message data
     */
    private void onData(Message message, String content) {
        String username = null;
        String data = null;
        ChatClient client =  null;

        client = findChatClient(message.getConnection());
        // client not joined
        if(client == null) {
            onError(Error.CLIENT_NOT_ACCEPTED, message);
            return;
        }

        /* IF NOT
         * 1) correct syntax
         * 2) has data
         * 3) data is found
         * 4) username is found and equals client username
         */
        if(
                ! (content.contains(": ")) ||
                        (content.length() <= content.indexOf(": ") + 1) ||
                        (data = content.substring(content.indexOf(": ") + 1).trim()).isEmpty()  ||
                        ! (username = content.substring(0, content.indexOf(": ")).trim()).equals(client.getUsername()))
        {
            onError(Error.SYNTAX_UNKNOWN, message);
            return;
        }

        for (IChatListener listener: listeners) {
            listener.onMessageReceived(username, data);
        }

        broadcast(new String("DATA " + username + ": " + data));
    }

    /**
     * Called when a client request leads to an error.
     * @param error occurred error
     * @param message client request
     */
    private void onError(Error error, Message message) {
        sendData(message.getConnection(), "J_ERR " + error.errorCode + ": " + error.label);

        for (IChatListener listener: listeners) {
            listener.onError(message.getAddress(), message.getPort(), error, new String(message.getData()));
        }
    }

    /**
     * Called when server receives a heartbeat.
     * @param message client request
     */
    private void onHeartbeat(Message message) {
        ChatClient existingClient = null;


        // find potential existing connected client
        existingClient = findChatClient(message.getConnection());

        // if client is not connected
        if(existingClient == null) {
            onError(Error.CLIENT_NOT_ACCEPTED, message);
            return;
        }

        // setHeartbeat
        synchronized (this) {
            existingClient.setHearbeat(new Date());
        }
    }

    /**
     * Called when server receives a heartbeat from a client on localhost.
     * @param message client request
     */
    private void onLocalhostHeatbeat(Message message, String content) {
        ChatClient existingClient = null;

        // find potential existing connected client
        existingClient = findChatClient(message.getConnection(), content);

        // if client is not connected
        if(existingClient == null) {
            onError(Error.CLIENT_NOT_ACCEPTED, message);
            return;
        }

        // setHeartbeat
        synchronized (this) {
            existingClient.setHearbeat(new Date());
        }
    }

    /**
     * Called when ClientCleaner finds dead clients.
     * @param clientsToRemove
     */
    @Override
    public void removeClients(ArrayList<ChatClient> clientsToRemove) {
        synchronized (this) {
            for (ChatClient client : clientsToRemove) {
                clients.remove(client);
            }
        }
        broadcastUsersOnline();
    }
}
