package com.dani153i.udpservice.client;

import com.dani153i.udpservice.shared.IMessageHandler;
import com.dani153i.udpservice.shared.connection.Connection;
import com.dani153i.udpservice.shared.message.Message;

import java.io.IOException;
import java.net.*;

/**
 * A UDP client service for handling connectivity and packet transfer.
 * @author daniel blom
 * @version 1.0
 */
public class UdpClient implements Runnable {

    private Connection connection;
    private boolean running;

    private DatagramSocket socket;
    private Thread serviceThread;
    private Thread listenerThread;
    private Thread senderThread;

    // threads could be future or managed by one or more thread pools (ExecutorService, etc..)

    // could add listener to capture exceptions for logging purposes
    //private IUdpClientListener listener

    /**
     * New instance of UdpClient connecting to specified host.
     * @param address host address
     * @param port host port
     * @throws SocketException
     * @throws UnknownHostException
     */
    public UdpClient(String address, int port) throws SocketException, UnknownHostException {

            socket = new DatagramSocket();
            connection = new Connection(socket, InetAddress.getByName(address), port);
            this.init();
    }

    /**
     * Initialize the UdpClient
     */
    private void init() {
        serviceThread = new Thread(this, "service_thread");
        serviceThread.start();
    }

    /**
     * Method will be called on thread start.
     */
    @Override
    public void run() {
        running = true;
    }

    /**
     * Send data
     * @param data data to be sent
     */
    public void send(final byte[] data) {
        senderThread = new Thread("sender_thread") {
            public void run() {
                connection.send(data);
            }
        };

        senderThread.start();
    }

    /**
     * Listen for inbound messages on the DatagramSocket
     */
    public void listen(final IMessageHandler handler) {
        listenerThread = new Thread("listener_thread") {
            public void run() {
                while(running) {
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                    try {
                        socket.receive(packet);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    handler.handleMessage(new Message(packet.getData(), new Connection(socket, packet.getAddress(), packet.getPort())));
                }
            }
        };

        listenerThread.start();
    }

    /**
     * Close connection and stop listening
     */
    public void close() {
        connection.close();
        running = false;
    }
}