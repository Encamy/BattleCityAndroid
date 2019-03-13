package com.encamy.battlecity.utils;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.encamy.battlecity.Settings;
import com.encamy.battlecity.network.AndroidInterface;
import com.encamy.battlecity.protobuf.NetworkProtocol;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

public class utils
{
    public static Settings.Direction velocity2Direction(Vector2 velocity)
    {
        if (velocity.x < 0)
        {
            return Settings.Direction.LEFT;
        }
        else if (velocity.x > 0)
        {
            return Settings.Direction.RIGHT;
        }
        else if (velocity.y < 0)
        {
            return Settings.Direction.BOTTOM;
        }
        else if (velocity.y > 0)
        {
            return Settings.Direction.TOP;
        }

        return Settings.Direction.NULL;
    }

    public static String getIPAddress(boolean useIPv4)
    {
        try
        {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces)
            {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs)
                {
                    if (!addr.isLoopbackAddress())
                    {
                        String sAddr = addr.getHostAddress();
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4)
                        {
                            if (isIPv4)
                                return sAddr;
                        }
                        else
                        {
                            if (!isIPv4)
                            {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        }
        catch (Exception ignored)
        {

        } // for now eat exceptions

        return "";
    }

    public static String getHostName(AndroidInterface m_androidAPI)
    {
        if (Gdx.app.getType() == Application.ApplicationType.Android)
        {
            return m_androidAPI.getDeviceName();
        }
        else
        {
            try
            {
                return InetAddress.getLocalHost().getHostName();
            }
            catch (UnknownHostException e)
            {
                e.printStackTrace();
                return "UNKNOWN";
            }
        }
    }

    public static String formatString(String string)
    {
        return string.replaceAll("[^\\d^\\w.]", " ");
    }

    public static NetworkProtocol.PacketWrapper parsePacket(DataInputStream stream)
    {
        try
        {
            return NetworkProtocol.PacketWrapper.parseDelimitedFrom(stream);
        }
        catch (IOException e)
        {
            Gdx.app.log("PROTOBUF", "Failed to parse packet");
        }

        return null;
    }
}
