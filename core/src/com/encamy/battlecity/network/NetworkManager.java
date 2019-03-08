package com.encamy.battlecity.network;

public class NetworkManager
{
    public NetworkManager(AndroidInterface androidInterface)
    {
        BroadcastAnnouncer broadcastAnnouncer = new BroadcastAnnouncer(androidInterface);
        broadcastAnnouncer.start();
    }
}
