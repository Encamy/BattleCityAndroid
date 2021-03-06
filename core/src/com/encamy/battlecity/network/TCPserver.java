package com.encamy.battlecity.network;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.encamy.battlecity.Settings;
import com.encamy.battlecity.protobuf.NetworkProtocol;
import com.encamy.battlecity.utils.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

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
                                        .setOwner(owner)
                                        .setId(id)
                                        .setX(x)
                                        .setY(y)
                                        .setLevel(level)
                        )).build();

        wrapper.writeDelimitedTo(m_outputStream);
    }

    public void sendMoveEvent(NetworkProtocol.Owner owner, int id, float x, float y, NetworkProtocol.Direction direction) throws IOException
    {
        NetworkProtocol.PacketWrapper wrapper =
                NetworkProtocol.PacketWrapper.newBuilder().setEvent(
                        NetworkProtocol.Event.newBuilder().setMove(
                                NetworkProtocol.Move.newBuilder()
                                        .setOwner(owner)
                                        .setId(id)
                                        .setX(x)
                                        .setY(y)
                                        .setDirection(direction)
                        )
                ).build();

        wrapper.writeDelimitedTo(m_outputStream);
    }

    public void sendFireEvent(NetworkProtocol.Owner owner, int id, NetworkProtocol.Direction direction) throws IOException
    {
        NetworkProtocol.PacketWrapper wrapper =
                NetworkProtocol.PacketWrapper.newBuilder().setEvent(
                        NetworkProtocol.Event.newBuilder().setFire(
                                NetworkProtocol.Fire.newBuilder()
                                    .setId(id)
                                    .setOwner(owner)
                                    .setDirection(direction)
                        )
                ).build();

        wrapper.writeDelimitedTo(m_outputStream);
    }

    public void sendDestroyEvent(NetworkProtocol.Owner wall, Integer id) throws IOException
    {
        NetworkProtocol.PacketWrapper wrapper =
                NetworkProtocol.PacketWrapper.newBuilder().setEvent(
                        NetworkProtocol.Event.newBuilder().setDestroyed(
                                NetworkProtocol.Destroyed.newBuilder()
                                .setId(id)
                                .setItem(wall)
                        )
                ).build();

        wrapper.writeDelimitedTo(m_outputStream);
    }

    public void sendGameOverEvent(Vector<Integer> firstP, Vector<Integer> secondP, Vector<Integer> enemyCost) throws IOException
    {
        NetworkProtocol.PacketWrapper wrapper =
                NetworkProtocol.PacketWrapper.newBuilder().setEvent(
                        NetworkProtocol.Event.newBuilder().setGameover(
                                NetworkProtocol.GameOver.newBuilder()
                                .setCountPlayer1Type0(firstP.get(0))
                                .setCountPlayer1Type1(firstP.get(1))
                                .setCountPlayer1Type2(firstP.get(2))
                                .setCountPlayer1Type3(firstP.get(3))

                                .setCountPlayer2Type0(secondP.get(0))
                                .setCountPlayer2Type1(secondP.get(1))
                                .setCountPlayer2Type2(secondP.get(2))
                                .setCountPlayer2Type3(secondP.get(3))

                                .setCostType0(enemyCost.get(0))
                                .setCostType1(enemyCost.get(1))
                                .setCostType2(enemyCost.get(2))
                                .setCostType3(enemyCost.get(3))
                        )
                ).build();

        wrapper.writeDelimitedTo(m_outputStream);
    }
}
