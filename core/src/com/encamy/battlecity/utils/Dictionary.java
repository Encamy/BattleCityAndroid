package com.encamy.battlecity.utils;

import java.util.ArrayList;

public class Dictionary<K, V>
{
    private ArrayList<Entry<K,V>> m_array;

    public Dictionary()
    {
        m_array = new ArrayList<>();
    }

    public void put(K key, V value)
    {
        m_array.add(new Entry(key, value));
    }

    public ArrayList<Entry<K,V>> getArray()
    {
        return m_array;
    }

    public int size()
    {
        return m_array.size();
    }

    public Entry<K, V> getAt(Integer index)
    {
        return m_array.get(index);
    }

    public class Entry <K, V>
    {
        private K key;
        private V value;

        Entry (K key, V value)
        {
            this.key = key;
            this.value = value;
        }

        public K getKey()
        {
            return key;
        }

        public V getValue()
        {
            return value;
        }
    }
}
