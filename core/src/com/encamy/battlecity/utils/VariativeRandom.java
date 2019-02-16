package com.encamy.battlecity.utils;

import com.badlogic.gdx.Gdx;

import java.util.Map;
import java.util.Random;

public class VariativeRandom
{
    Map<Float, Float> m_probabilities;
    Random m_random;

    public VariativeRandom()
    {
        m_random = new Random();
    }

    public boolean addProbability(Float probaility, Float value)
    {
        if (!calculateTotalProbability(value))
        {
            return false;
        }

        m_probabilities.put(probaility, value);
        return false;
    }

    public Float nextValue()
    {
        // TODO:
        return null;
    }

    private boolean calculateTotalProbability(Float type)
    {
        float totalProbability = 0;

        for (Map.Entry<Float, Float> entry : m_probabilities.entrySet())
        {
            totalProbability += entry.getValue();
            if (totalProbability > 1.0f)
            {
                return false;
            }
        }

        return true;
    }
}
