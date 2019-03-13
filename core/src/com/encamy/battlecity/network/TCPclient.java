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

               /* byte[] data = new byte[Settings.PACKET_MAX_LENGTH];
                int length = m_inputStream.read(data);

                if (m_onMessageReceivedCallback != null)
                {
                    m_onMessageReceivedCallback.OnMessageReceived(data);
                }

                if (new String(data, 0, length).equals("Pong"))
                {
                    Gdx.app.log("INFO", "Fully connected");
                }*/

                if (wrapper == null)
                {
                    continue;
                }

               if (wrapper.hasPong())
               {
                   Gdx.app.log("INFO", "Fully connected");
                   m_onMessageReceivedCallback.OnMessageReceived("Fully connected".getBytes());
               }
            }
        }
        catch (Exception e)
        {
            Gdx.app.log("TCP Client", "Error: " + e);
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
}
