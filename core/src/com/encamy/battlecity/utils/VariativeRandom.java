package com.encamy.battlecity.utils;

import com.badlogic.gdx.Gdx;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class VariativeRandom
{
    private Map<Integer, Double> m_probabilities;
    private double m_sum;

    public VariativeRandom()
    {
        m_probabilities = new HashMap<Integer, Double>();
    }

    public boolean addProbability(Integer value, Double probaility)
    {
        if (m_probabilities.get(value) != null)
        {
            m_sum -= m_probabilities.get(value);
        }

        m_probabilities.put(value, probaility);
        m_sum += probaility;
        return false;
    }

    public int nextValue()
    {
        double rand = Math.random();
        double ratio = 1.0f / m_sum;
        double tempDistribution = 0;

        for (Integer i : m_probabilities.keySet())
        {
            tempDistribution += m_probabilities.get(i);
            if (rand / ratio <= tempDistribution)
            {
                return i;
            }
        }
        return 0;
    }
}
