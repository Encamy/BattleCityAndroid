package com.encamy.battlecity.network;

import com.badlogic.gdx.Gdx;
import com.encamy.battlecity.Settings;
import com.encamy.battlecity.protobuf.NetworkProtocol;

import java.io.IOException;

public class NetworkManager implements Settings.OnMessageReceivedCallback
{
    private BroadcastAnnouncer m_broadcastAnnouncer;
    private Settings.OnMessageReceivedCallback m_OnMessageReceivedCallback;

    public NetworkManager(AndroidInterface androidInterface, Settings.OnDeviceFoundCallback callback)
    {
        m_broadcastAnnouncer = new BroadcastAnnouncer(androidInterface, callback);
        m_broadcastAnnouncer.start();
        m_OnMessageReceivedCallback = this;
    }

    public void stopAnnouncement()
    {
        m_broadcastAnnouncer.stopAnnouncement();
    }


    public void connect(NetworkDevice networkDevice) throws IOException
    {
        TCPclient client = new TCPclient(networkDevice);
        client.setOnMessageCallback(this);

        client.start();
    }

    @Override
    public void OnMessageReceived(NetworkProtocol.PacketWrapper packet)
    {
        Gdx.app.log("TCP Socket", "Received packet: " + packet.getWrapperCase());
    }

    public void createServer() throws IOException
    {
        m_broadcastAnnouncer.setServer(true);
        TCPserver server = new TCPserver();
        server.setOnMessageCallback(this);

        server.start();
    }
}
