package com.encamy.battlecity.network;

import com.badlogic.gdx.Gdx;
import com.encamy.battlecity.Settings;
import com.encamy.battlecity.protobuf.NetworkProtocol;

import java.io.IOException;

public class NetworkManager implements Settings.OnMessageReceivedCallback
{
    private BroadcastAnnouncer m_broadcastAnnouncer;
    private Settings.OnMessageReceivedCallback m_OnMessageReceivedCallback;
    private Settings.OnConnectedCallback m_OnConnected;
    private boolean m_isServer;
    private TCPclient m_client;
    private TCPserver m_server;

    public NetworkManager(AndroidInterface androidInterface)
    {
        m_broadcastAnnouncer = new BroadcastAnnouncer(androidInterface);
        m_broadcastAnnouncer.start();
        m_OnMessageReceivedCallback = this;
    }

    public void setOnDeviceFoundCallback(Settings.OnDeviceFoundCallback callback)
    {
        m_broadcastAnnouncer.setOnDeviceFoundCallback(callback);
    }

    public void setOnConnectedCallback(Settings.OnConnectedCallback callback)
    {
        m_OnConnected = callback;
    }

    public void stopAnnouncement()
    {
        m_broadcastAnnouncer.stopAnnouncement();
    }


    public void connect(NetworkDevice networkDevice) throws IOException
    {
        m_isServer = false;

        m_client = new TCPclient(networkDevice);
        m_client.setOnMessageCallback(this);
        m_client.setOnConnectedCallback(m_OnConnected);

        m_client.start();
    }

    @Override
    public void OnMessageReceived(NetworkProtocol.PacketWrapper packet)
    {
        Gdx.app.log("TCP Socket", "Received packet: " + packet.getWrapperCase());
    }

    public void createServer() throws IOException
    {
        m_isServer = true;

        m_broadcastAnnouncer.setServer(true);

        m_server = new TCPserver();
        m_server.setOnMessageCallback(this);
        m_server.setOnConnectedCallback(m_OnConnected);

        m_server.start();
    }

    public void notifySpawn(NetworkProtocol.Owner owner, int id, float x, float y)
    {
        if (owner == NetworkProtocol.Owner.ENEMY && !m_isServer)
        {
            Gdx.app.log("FATAL", "Trying to notify about enemy spawn from client side");
            return;
        }

        try
        {
            m_server.sendSpawnEvent(owner, id, x, y);
        }
        catch (IOException e)
        {
            //
        }
    }

    public void notifyUpdate(NetworkProtocol.Owner owner, int id, float x, float y)
    {
        if (owner == NetworkProtocol.Owner.ENEMY && !m_isServer)
        {
            Gdx.app.log("FATAL", "Trying to update enemy on client side. It is prohibited");
            return;
        }

        try
        {
            m_server.sendUpdateEvent(owner, id, x, y);
        }
        catch (IOException e)
        {
            //
        }
    }
}
