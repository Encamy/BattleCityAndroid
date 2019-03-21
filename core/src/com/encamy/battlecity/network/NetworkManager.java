package com.encamy.battlecity.network;

import com.badlogic.gdx.Gdx;
import com.encamy.battlecity.Settings;
import com.encamy.battlecity.protobuf.NetworkProtocol;
import com.encamy.battlecity.utils.utils;

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
    }

    public void setOnMessageReceivedCallback(Settings.OnMessageReceivedCallback callback)
    {
        m_OnMessageReceivedCallback = callback;
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
        if (m_OnMessageReceivedCallback == null)
        {
            Gdx.app.log("TCP Socket", "OnMessageReceived callback is not set. Just outputting events to log.\n" +
                    "Received packet: " + packet.toString());
        }
        else
        {
            //Gdx.app.log("NETWORK DEBUG", packet.toString());
            m_OnMessageReceivedCallback.OnMessageReceived(packet);
        }
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

    public void notifySpawn(NetworkProtocol.Owner owner, int id, float x, float y, int level)
    {
        if (owner == NetworkProtocol.Owner.ENEMY && !m_isServer)
        {
            Gdx.app.log("FATAL", "Trying to notify about enemy spawn from client side");
            return;
        }

        // Enemy handling
        if (owner == NetworkProtocol.Owner.ENEMY)
        {
            try
            {
                m_server.sendSpawnEvent(owner, id, x, y, level);
            }
            catch (IOException e)
            {
                // Do nothing
            }
        }

        // Player handling
        if (owner == NetworkProtocol.Owner.CLIENT_PLAYER ||
            owner == NetworkProtocol.Owner.SERVER_PLAYER)
        {
            try
            {
                if (m_isServer)
                {
                    m_server.sendSpawnEvent(owner, id, x, y, level);
                }
                else
                {
                    m_client.sendSpawnEvent(owner, id, x, y, level);
                }
            }
            catch (Exception e)
            {
                // Do nothing
            }
        }
    }

    public void notifyMove(NetworkProtocol.Owner owner, int id, float x, float y, NetworkProtocol.Direction direction)
    {
        if (owner == NetworkProtocol.Owner.ENEMY && !m_isServer)
        {
            Gdx.app.log("FATAL", "Trying to update enemy on client side. It is prohibited");
            return;
        }

        // Handling enemy
        if (owner == NetworkProtocol.Owner.ENEMY)
        {
            try
            {
                m_server.sendMoveEvent(owner, id, x, y, direction);
            }
            catch (IOException e)
            {
                //
            }
        }

        // Handling player
        if (owner == NetworkProtocol.Owner.CLIENT_PLAYER ||
            owner == NetworkProtocol.Owner.SERVER_PLAYER)
        {
            try
            {
                if (m_isServer)
                {
                    m_server.sendMoveEvent(owner, id, x, y, direction);
                }
                else
                {
                    m_client.sendMoveEvent(owner, id, x, y, direction);
                }
            }
            catch (Exception e)
            {
                // Do nothing
            }
        }
    }

    public void notifyFire(NetworkProtocol.Owner owner, int id, Settings.Direction direction)
    {
        if (owner == NetworkProtocol.Owner.ENEMY && !m_isServer)
        {
            Gdx.app.log("FATAL", "Trying to update enemy FIRED on client side. It is prohibited");
            return;
        }

        NetworkProtocol.Direction networkDirection = utils.toNetworkDirection(direction);
        try
        {
            if (owner == NetworkProtocol.Owner.ENEMY || owner == NetworkProtocol.Owner.SERVER_PLAYER)
            {
                m_server.sendFireEvent(owner, id, networkDirection);
            }
            else if (owner == NetworkProtocol.Owner.CLIENT_PLAYER)
            {
                m_client.sendFireEvent(owner, id, networkDirection);
            }
        }
        catch (IOException e)
        {
            //
        }
    }

    public boolean isServer()
    {
        return m_isServer;
    }

    public void notifyDestroyed(NetworkProtocol.Owner wall, Integer variable)
    {
        Gdx.app.log("FATAL", "Not implemented");
    }
}
