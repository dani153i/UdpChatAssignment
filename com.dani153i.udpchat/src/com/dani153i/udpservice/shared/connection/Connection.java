package com.dani153i.udpservice.shared.connection;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * A connection information container used for connectivity and data transfer.
 * @author daniel blom
 * @version 1.0
 */
public class Connection
{
    private final DatagramSocket socket;
    private InetAddress address;
    private int port;

    /**
     * New Connection instance
     * @param socket
     * @param addr
     * @param port
     */
    public Connection(DatagramSocket socket, InetAddress addr, int port) {
        this.address = addr;
        this.port = port;
        this.socket = socket;
    }

    /**
     * Send data through connection
     * @param data
     */
    public void send(byte[] data) {
        DatagramPacket packet = new DatagramPacket(data, data.length, address, port);

        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return port number
     * @see int
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Get address of connection
     * @return address of connection
     * @see InetAddress
     */
    public InetAddress getAddress() {
        return this.address;
    }

    /**
     * Close the connection to the server
     */
    public void close() {
        new Thread() {
            public void run() {
                synchronized(socket) {
                    socket.close();
                }
            }
        }.start();
    }

    public boolean equals(Connection connection) {
        return address.equals(connection.getAddress()) && port == connection.getPort();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Connection) {
            Connection con = (Connection) obj;
            return equals(con);
        }
        return super.equals(obj);
    }
}
