package com.encamy.battlecity.network;

import com.badlogic.gdx.Gdx;
import com.encamy.battlecity.Settings;
import com.encamy.battlecity.protobuf.NetworkProtocol;
import com.encamy.battlecity.utils.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class TCPclient extends Thread
{
    private volatile boolean m_running;
    private Socket m_socket;
    private Settings.OnMessageReceivedCallback m_onMessageReceivedCallback;
    private Settings.OnConnectedCallback m_onConnectedCallback;
    private DataOutputStream m_outputStream;
    private DataInputStream m_inputStream;


    public TCPclient(NetworkDevice networkDevice) throws IOException {
        m_running = true;

        InetAddress serverAddress = InetAddress.getByName(networkDevice.Address);
        Gdx.app.log("TCP Client", "Connecting...");

        m_socket = new Socket(serverAddress, networkDevice.Port);
        m_socket.setTcpNoDelay(true);

        m_outputStream = new DataOutputStream(m_socket.getOutputStream());
        m_inputStream = new DataInputStream(m_socket.getInputStream());
    }

    @Override
    public void run()
    {
        try
        {
            sendPing();

            while (m_running)
            {
                NetworkProtocol.PacketWrapper wrapper = utils.parsePacket(m_inputStream);

                if (wrapper == null)
                {
                    continue;
                }

                m_onMessageReceivedCallback.OnMessageReceived(wrapper);

                if (wrapper.hasPong())
                {
                    m_onConnectedCallback.OnConnected();
                }
            }
        }
        catch (Exception e)
        {
            Gdx.app.log("TCP Client", "Error: " + e);
            Gdx.app.log("FATAL", "Some error happend on socket. Future operations are impossible. Stopping thread");
        }
        finally
        {
            try
            {
                m_socket.close();
            }
            catch (IOException e)
            {
                Gdx.app.log("TCP Client", e.toString());
            }
        }
    }

    public void setOnMessageCallback(Settings.OnMessageReceivedCallback onMessageReceivedCallback)
    {
        m_onMessageReceivedCallback = onMessageReceivedCallback;
    }

    private void sendPing() throws IOException
    {
        NetworkProtocol.PacketWrapper wrapper =
                NetworkProtocol.PacketWrapper.newBuilder().setPing(NetworkProtocol.Ping.getDefaultInstance()).build();

        wrapper.writeDelimitedTo(m_outputStream);
    }

    public void setOnConnectedCallback(Settings.OnConnectedCallback m_onConnected)
    {
        m_onConnectedCallback = m_onConnected;
    }
}
