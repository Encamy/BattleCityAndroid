package com.encamy.battlecity.network;

import com.badlogic.gdx.Gdx;
import com.encamy.battlecity.Settings;

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
                byte[] data = new byte[Settings.PACKET_MAX_LENGTH];
                int length = m_inputStream.read(data);

                if (m_onMessageReceivedCallback != null)
                {
                    m_onMessageReceivedCallback.OnMessageReceived(data);
                }

                if (new String(data, 0, length).equals("Ping"))
                {
                    sendPong();
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void sendPong() throws IOException
    {
        m_outputStream.write("Pong".getBytes());
    }

    public void setOnMessageCallback(Settings.OnMessageReceivedCallback onMessageReceivedCallback)
    {
        m_onMessageReceivedCallback = onMessageReceivedCallback;
    }
}
