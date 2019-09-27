package com.dani153i.udpchat.service.shared.model;

import com.dani153i.udpservice.shared.connection.Connection;

import java.util.Date;

public class ChatClient
{
    String username;
    Connection connection;
    Date hearbeat;

    public ChatClient(String username, Connection connection) {
        this.username = username;
        this.connection = connection;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Date getHearbeat() {
        return hearbeat;
    }

    public void setHearbeat(Date hearbeat) {
        this.hearbeat = hearbeat;
    }
}
