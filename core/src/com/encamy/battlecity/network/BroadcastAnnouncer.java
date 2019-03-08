package com.encamy.battlecity.network;

import com.badlogic.gdx.Gdx;
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

    private void sendBroadcast()
    {
        Gdx.app.log("TRACE", "Sending broadcast");

        String message = "BattleCity server address = " + utils.getIPAddress(true) + "\r\nHost name = " + utils.getHostName(m_androidAPI);

        DatagramSocket socket = null;
        try
        {
            socket = new DatagramSocket();
            socket.setBroadcast(true);
        }
        catch (SocketException e)
        {
            e.printStackTrace();
        }

        byte[] buffer = message.getBytes();

        DatagramPacket packet = null;
        try
        {
            packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("255.255.255.255"), 5050);
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }

        try
        {
            socket.send(packet);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
