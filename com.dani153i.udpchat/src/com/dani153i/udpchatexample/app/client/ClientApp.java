package com.dani153i.udpchatexample.app.client;

import com.dani153i.udpchat.service.client.IChatListener;
import com.dani153i.udpchat.service.client.UdpChatService;
import com.dani153i.udpchat.service.shared.messages.Command;
import com.dani153i.udpchat.service.shared.messages.Error;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * UDP chat client application.
 * @author daniel blom
 * @version 1.0
 */
public class ClientApp implements IChatListener
{
    private UdpChatService chatService;
    private BufferedReader inputReader;
    private boolean connected;
    private boolean connecting;
    private boolean run;

    public synchronized boolean isConnected() {
        return connected;
    }
    private synchronized void setConnected(boolean connected) {
        this.connected = connected;
    }

    public synchronized boolean isConnecting() {
        return connecting;
    }
    private synchronized void setConnecting(boolean connecting) {
        this.connecting = connecting;
    }

    public static void main(String[] args) {
        UdpChatService chatService = null;
        try {
            chatService = new UdpChatService("127.0.0.1", 1337);
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
            ClientApp client = new ClientApp(chatService, inputReader);
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
            // log
        }
    }

    /**
     * New ClientApp instance.
     * @param chatService for communication
     * @param inputReader for reading user console input
     */
    public ClientApp(UdpChatService chatService, BufferedReader inputReader) {
        this.chatService = chatService;
        this.inputReader = inputReader;
        this.init();
    }

    /**
     * ClientApp initialization.
     */
    private void init() {
        chatService.addListener(this);
        chatService.listen();
        connected = false;
        connecting = false;

        mainLoop();
    }

    /**
     * This is the applications main loop.
     * It should be called in class initializer.
     */
    private void mainLoop() {
        run = true;
        try {
            while (run) {
                if(!isConnected() && !isConnecting()) {
                    displayJoin();
                } else if(isConnected() && !isConnecting()) {
                    processInput(getInput());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        chatService.disconnect();
    }

    /**
     * Ask for username and initiates a join chat request
     * @throws IOException on error reading user console input
     */
    private void displayJoin() throws IOException {
        System.out.print("Username >> ");
        String username = inputReader.readLine();
        connecting = true;
        chatService.join(username);
    }

    /**
     * Wait for user console input
     * @return user console input
     * @throws IOException
     * @see String
     */
    private String getInput() throws IOException {
        String input = inputReader.readLine();

        return input;
    }

    /**
     * Process user console input
     * @param input user console input
     */
    private void processInput(String input) {
        switch (input.toLowerCase()) {
            case "help":
                displayHelp();
                break;
            case "quit":
                run = false;
                break;
            case "users":
                displayUsersOnline();
                break;
            default:
                chatService.sendMessage(input);
                break;
        }

        try {
            TimeUnit.MILLISECONDS.sleep(64);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Display a help menu
     */
    private void displayHelp() {
        StringBuilder menu = new StringBuilder();

        menu.append("~~~~~~ COMMANDS ~~~~~~\n");
        menu.append("<<message>>\n");
        menu.append("USERS\n");
        menu.append("QUIT\n");
        menu.append("~~~~~~~~~~~~~~~~~~~~~~\n");

        System.out.println(menu);
    }

    /**
     * Display list of online users
     */
    private void displayUsersOnline() {
        StringBuilder usersOnline = new StringBuilder();
        chatService.getUsersOnline().forEach(username -> usersOnline.append(username).append(", "));
        if(usersOnline.length() > 2 && usersOnline.charAt(usersOnline.length()-1) == ' ')
            usersOnline.delete(usersOnline.length()-2, usersOnline.length());
        System.out.println(usersOnline);
    }

    /**
     * Called when client receives join confirmation
     * @param username client username
     */
    @Override
    public void onJoined(String username) {
        System.out.println("You joined the chat as " + username + ".");
        System.out.println("Type 'help' to display commands.");
        setConnected(true);
        setConnecting(false);
    }

    /**
     * Called when client receives a message
     * @param username message sender
     * @param data message data
     */
    @Override
    public void onMessageReceived(String username, String data) {
        System.out.println(">> " + username + ": " + data);
    }

    /**
     * Called when client receives receives an error
     * @param error client error
     */
    @Override
    public void onError(Error error) {
        System.out.println(Command.ERROR.label + ": (" + error.errorCode + ") " + error.label + "." );

        if(
                error.errorCode.equals(Error.USERNAME_TAKEN.errorCode) ||
                error.errorCode.equals(Error.CLIENT_NOT_ACCEPTED.errorCode) ||
                error.errorCode.equals(Error.SERVER_FULL.errorCode) ||
                error.errorCode.equals(Error.USERNAME_INVALID.errorCode))
        {
            setConnected(false);
            setConnecting(false);
        }
    }
}
