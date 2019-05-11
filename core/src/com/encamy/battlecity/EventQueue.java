package com.encamy.battlecity;

import com.badlogic.gdx.Gdx;
import com.encamy.battlecity.protobuf.NetworkProtocol;

import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class EventQueue
{
    // Java is odd.
    // There is no class that implements raw Queue interface
    // PriorityQueue need comparator. Unfortunately we can't add custom comparator for protobuf generated class.
    // But why the hell LinkedList implemets Queue?!
    private LinkedList<NetworkProtocol.Event> m_queue;

    private int m_addThreadId;
    private int m_pollThreadId;

    private Lock m_lock;

    public EventQueue()
    {
        m_queue = new LinkedList<>();
        m_lock = new ReentrantLock();
    }

    public void addEvent(NetworkProtocol.Event event)
    {
        m_addThreadId = (int)Thread.currentThread().getId();

        m_lock.lock();
        m_queue.add(event);
        m_lock.unlock();
    }

    public void poll(EventQueueCallback eventQueueCallback)
    {
        m_pollThreadId = (int)Thread.currentThread().getId();
        checkSameThread();

        m_lock.lock();
        for (int i = 0; i < m_queue.size(); i++)
        {
            NetworkProtocol.Event event = m_queue.poll();
            eventQueueCallback.OnEventPoll(event);
        }
        m_lock.unlock();
    }

    private void checkSameThread()
    {
        if (m_addThreadId == m_pollThreadId)
        {
            Gdx.app.log("NOTICE", "Using same thread for adding to queue and executing from it. There is no need in Event Queue");
        }
    }

    public interface EventQueueCallback
    {
        void OnEventPoll(NetworkProtocol.Event event);
    }
}
