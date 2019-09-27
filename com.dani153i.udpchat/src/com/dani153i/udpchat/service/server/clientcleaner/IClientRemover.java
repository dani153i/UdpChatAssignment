package com.dani153i.udpchat.service.server.clientcleaner;

import com.dani153i.udpchat.service.shared.model.ChatClient;

import java.util.ArrayList;
import java.util.Vector;

public interface IClientRemover
{
    /**
     * Called when ClientCleaner finds dead clients.
     * @param clientsToRemove
     */
    void removeClients(ArrayList<ChatClient> clientsToRemove);
}
