package com.encamy.battlecity.network;

import com.badlogic.gdx.Gdx;
import com.encamy.battlecity.Settings;
import com.encamy.battlecity.protobuf.NetworkProtocol;
import com.encamy.battlecity.utils.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPserver extends Thread
{
    private volatile boolean m_running;
    private ServerSocket m_socket;
    private Settings.OnMessageReceivedCallback m_onMessageReceivedCallback;
    private Settings.OnConnectedCallback m_onConnectedCallback;
    private DataOutputStream m_outputStream;
    private DataInputStream m_inputStream;

    public TCPserver() throws IOException
    {
        m_running = true;

        m_socket = new ServerSocket(Settings.GAME_PORT);
        Gdx.app.log("TCP Server", "Listening...");
    }

    @Override
    public void run()
    {
        try
        {
            Socket client = m_socket.accept();
            Gdx.app.log("TCP Server", "Client has connected");

            m_outputStream = new DataOutputStream(client.getOutputStream());
            m_inputStream = new DataInputStream(client.getInputStream());

            while (m_running)
            {
                NetworkProtocol.PacketWrapper wrapper = utils.parsePacket(m_inputStream);

                if (wrapper == null)
                {
                    continue;
                }

                m_onMessageReceivedCallback.OnMessageReceived(wrapper);

                if (wrapper.hasPing())
                {
                    sendPong();
                    m_onConnectedCallback.OnConnected();
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            Gdx.app.log("FATAL", "Some error happend on socket. Future operations are impossible. Stopping thread");
            e.printStackTrace();
        }
    }

    private void sendPong() throws IOException
    {
        NetworkProtocol.PacketWrapper wrapper =
                NetworkProtocol.PacketWrapper.newBuilder().setPong(NetworkProtocol.Pong.getDefaultInstance()).build();

        wrapper.writeDelimitedTo(m_outputStream);
    }

    public void setOnMessageCallback(Settings.OnMessageReceivedCallback onMessageReceivedCallback)
    {
        m_onMessageReceivedCallback = onMessageReceivedCallback;
    }

    public void setOnConnectedCallback(Settings.OnConnectedCallback m_onConnected)
    {
        m_onConnectedCallback = m_onConnected;
    }

    public void sendSpawnEvent(NetworkProtocol.Owner owner, int id, float x, float y, int level) throws IOException
    {
        NetworkProtocol.PacketWrapper wrapper =
                NetworkProtocol.PacketWrapper.newBuilder().setEvent(
                        NetworkProtocol.Event.newBuilder().setSpawned(
                                NetworkProtocol.Spawned.newBuilder()
                                        .setId(id)
                                        .setX(x)
                                        .setY(y)
                                        .setLevel(level)
                        )).build();

        wrapper.writeDelimitedTo(m_outputStream);
    }

    public void sendUpdateEvent(NetworkProtocol.Owner owner, int id, float x, float y) throws IOException
    {
        NetworkProtocol.PacketWrapper wrapper =
                NetworkProtocol.PacketWrapper.newBuilder().setEvent(
                        NetworkProtocol.Event.newBuilder().setMove(
                                NetworkProtocol.Move.newBuilder()
                                        .setId(id)
                                        .setXVelocity(x)
                                        .setYVelocity(y)
                        )).build();

        wrapper.writeDelimitedTo(m_outputStream);
    }
}
