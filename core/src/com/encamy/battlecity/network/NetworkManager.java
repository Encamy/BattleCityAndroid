package com.encamy.battlecity.network;

import com.encamy.battlecity.Settings;

public class NetworkManager
{
    public NetworkManager(AndroidInterface androidInterface, Settings.OnDeviceFoundCallback callback)
    {
        BroadcastAnnouncer broadcastAnnouncer = new BroadcastAnnouncer(androidInterface, callback);
        broadcastAnnouncer.start();
    }
}
