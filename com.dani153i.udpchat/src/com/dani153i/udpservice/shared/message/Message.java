package com.dani153i.udpservice.shared.message;

import com.dani153i.udpservice.shared.connection.Connection;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

/**
 * A message information container.
 * @author daniel blom
 * @version 1.0
 */
public class Message
{
    private byte[] data;
    private InetAddress address;
    private int port;
    private Connection conn;

    /**
     * Create a new packet
     * @param data The data to send
     * @param senderConnection The packets connection
     */
    public Message(byte[] data, Connection senderConnection) {
        this.data = data;
        this.conn = senderConnection;
        address = null;
    }

    /**
     * Create a new packet with simple information about the client
     * @param data
     * @param address
     * @param port
     */
    public Message(byte[] data, InetAddress address, int port) {
        this.data = data;
        this.address = address;
        this.port = port;

        this.conn = new Connection(null, address, port);
    }

    /**
     * Get the packet data
     * @return the packet data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Get the ip
     * @return the connection address
     * @see InetAddress
     */
    public InetAddress getAddress() {
        if(address == null)
            return conn.getAddress();
        return address;
    }

    /**
     * Get the port
     * @return the connection port
     * @see int
     */
    public int getPort() {
        if(address == null)
            return conn.getPort();
        return port;
    }

    /**
     * Get the connection of this packet
     * @return the connection
     * @see Connection
     */
    public Connection getConnection() {
        return this.conn;
    }

    @Override
    public String toString() {
        return "Data: " + new String(data, StandardCharsets.UTF_8) + "\nAddress: " + getConnection().getAddress() + "\nPort: " + getConnection().getPort();
    }
}
