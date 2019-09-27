package com.dani153i.udpservice.server;

import com.dani153i.udpservice.shared.IMessageHandler;
import com.dani153i.udpservice.shared.connection.Connection;
import com.dani153i.udpservice.shared.message.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Collection;

/**
 * A UDP server service for handling connectivity and packet transfer.
 * @author daniel blom
 * @version 1.0
 */
public class UdpServer implements Runnable
{
    private int port;
    private DatagramSocket socket;
    private boolean running;

    private Thread serverThread;
    private Thread listenerThread;
    private Thread senderThread;

    // could add listener to expose exceptions to server thread
    //private IUdpServerListener listener

    /**
     * New instance of UDPServer listening on a specified port.
     * @param port listening port
     * @throws SocketException when port is already in use
     */
    public UdpServer(int port) throws SocketException {
        this.port = port;

        this.init();

    }

    /**
     * Initialize the server.
     * @throws SocketException when port is already in use
     */
    public void init() throws SocketException {
        this.socket = new DatagramSocket(this.port);
        serverThread = new Thread(this, "server_thread");
        serverThread.start();
    }

    /**
     * Method will be called on thread start.
     */
    @Override
    public void run() {
        running = true;
        System.out.println("Server started on port " + port);
    }

    /**
     * Get the server bound port.
     * @return port
     * @see int
     */
    public int getPort() {
        return port;
    }


    /**
     * Send a packet to a client.
     * @param message message to be sent
     */
    public void send(final Message message) {
        senderThread = new Thread("sender_thread") {
            public void run() {
                DatagramPacket packet = new DatagramPacket(message.getData(),message.getData().length, message.getAddress(), message.getPort());

                try {
                    socket.send(packet);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        };

        senderThread.start();
    }

    /**
     * Broadcast a packet to a collection of clients.
     * @param connections Collection of Connection
     * @param data  data to be broadcast
     */
    public void broadcast(final Collection<Connection> connections, byte[] data) {
        for(Connection connection : connections) {
            send(new Message(data, connection.getAddress(), connection.getPort()));
        }
    }

    /**
     * Wait for inbound messages on server DatagramSocket.
     * @param messageHandler MessageHandler
     */
    public void listen(final IMessageHandler messageHandler) {
        listenerThread = new Thread("receiver_thread") {
            public void run() {
                while(running) {
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                    try {
                        socket.receive(packet);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    messageHandler.handleMessage(new Message(packet.getData(), new Connection(socket, packet.getAddress(), packet.getPort())));
                }
            }
        };

        listenerThread.start();
    }
}
