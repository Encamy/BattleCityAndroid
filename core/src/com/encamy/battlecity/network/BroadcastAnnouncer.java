package com.encamy.battlecity.network;

import com.badlogic.gdx.Gdx;
import com.encamy.battlecity.Settings;
import com.encamy.battlecity.utils.utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class BroadcastAnnouncer extends Thread
{
    volatile int attempts = 0;
    private AndroidInterface m_androidAPI;
    private DatagramSocket m_socket;

    public BroadcastAnnouncer(AndroidInterface androidInterface)
    {
        m_androidAPI = androidInterface;
    }

    @Override
    public void run()
    {
        while (true)
        {
            if (++attempts > 5)
            {
                break;
            }

            sendBroadcast();
            receiveBroadcast();

            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void receiveBroadcast()
    {
        try
        {
            byte[] receiveData = new byte[256];

            if (m_socket.isClosed())
            {
                m_socket = new DatagramSocket(Settings.BROADCAST_PORT);
                m_socket.setSoTimeout(Settings.BROADCAST_TIMEOUT);
            }

            DatagramPacket packet = new DatagramPacket(receiveData, receiveData.length);
            m_socket.receive(packet);
            m_socket.close();

            String sentence = new String(packet.getData(), 0, packet.getLength());
            Gdx.app.log("TRACE", "GOT MESSAGE: \r\n" + sentence);
        }
        catch (SocketException e)
        {
            e.printStackTrace();
            m_socket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void sendBroadcast()
    {
        Gdx.app.log("TRACE", "Sending broadcast");

        try
        {
            if (m_socket == null || m_socket.isClosed())
            {
                m_socket = new DatagramSocket();
                m_socket.setBroadcast(true);
            }
        }
        catch (SocketException e)
        {
            e.printStackTrace();
        }

        String message = "BattleCity server address = " + utils.getIPAddress(true) + "\r\nHost name = " + utils.getHostName(m_androidAPI);

        byte[] buffer = message.getBytes();

        DatagramPacket packet = null;
        try
        {
            packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("255.255.255.255"), Settings.BROADCAST_PORT);
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }

        try
        {
            m_socket.send(packet);
            m_socket.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
