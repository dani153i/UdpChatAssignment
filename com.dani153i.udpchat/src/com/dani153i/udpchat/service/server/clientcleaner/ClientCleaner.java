package com.dani153i.udpchat.service.server.clientcleaner;

import com.dani153i.udpchat.service.shared.model.ChatClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

/**
 * Runnable task that Removes dead clients.
 * @author daniel blom
 * @version 1.0
 */
public class ClientCleaner implements Runnable
{
    private IClientRemover clientRemover;
    private Vector<ChatClient> clients;

    public ClientCleaner(IClientRemover clientRemover, Vector<ChatClient> clients) {
        this.clientRemover = clientRemover;
        this.clients = clients;
    }

    @Override
    public void run() {
        ArrayList<ChatClient> clientsToRemove = new ArrayList<>();
        Date now = new Date();
        Date clientTimeout = null;
        ChatClient client;

        Iterator<ChatClient> it = clients.iterator();
        while (it.hasNext()) {
            client = it.next();
            clientTimeout = Date.from(client.getHearbeat().toInstant().plusMillis(60000));

            if(now.compareTo(clientTimeout) > 0) {
                //System.out.println("Removing client: " + client.getUsername());
                clientsToRemove.add(client);
            }
        }

        clientRemover.removeClients(clientsToRemove);
    }
}
