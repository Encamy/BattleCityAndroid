package com.encamy.battlecity.network;

import com.encamy.battlecity.Settings;

public class NetworkManager
{
    BroadcastAnnouncer m_broadcastAnnouncer;

    public NetworkManager(AndroidInterface androidInterface, Settings.OnDeviceFoundCallback callback)
    {
        m_broadcastAnnouncer = new BroadcastAnnouncer(androidInterface, callback);
        m_broadcastAnnouncer.start();
    }

    public void stopAnnouncement()
    {
        m_broadcastAnnouncer.stopAnnouncement();
    }
}
