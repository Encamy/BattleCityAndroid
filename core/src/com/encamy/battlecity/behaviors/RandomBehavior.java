package com.encamy.battlecity.behaviors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.steer.Limiter;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import com.encamy.battlecity.Settings;
import com.encamy.battlecity.utils.VariativeRandom;

import java.lang.reflect.Constructor;
import java.util.Random;

public class RandomBehavior<T extends Vector<T>> extends SteeringBehavior<T>
{
    protected Location<T> m_target;
    protected float m_moveTimeLeft;
    protected VariativeRandom m_variativeRandom;
    protected Random m_random;
    protected Settings.Direction m_direction;

    public RandomBehavior(Steerable<T> owner)
    {
        this(owner, null);
    }

    public RandomBehavior (Steerable<T> owner, Location<T> target)
    {
        super(owner);
        m_target = target;
        m_moveTimeLeft = 0;
        m_random = new Random();

        m_variativeRandom = new VariativeRandom();
        m_variativeRandom.addProbability(Settings.Direction.BOTTOM.ordinal(), 0.5);
        m_variativeRandom.addProbability(Settings.Direction.TOP.ordinal(), 0.1);
        m_variativeRandom.addProbability(Settings.Direction.LEFT.ordinal(), 0.2);
        m_variativeRandom.addProbability(Settings.Direction.RIGHT.ordinal(), 0.2);
    }

    @Override
    protected SteeringAcceleration<T> calculateRealSteering(SteeringAcceleration<T> steering)
    {
        return calculateRandomMovement(steering, m_target.getPosition());
    }

    private SteeringAcceleration<T> calculateRandomMovement(SteeringAcceleration<T> steering, T targetPosition)
    {
        Limiter actualLimiter = getActualLimiter();
        float targetSpeed = actualLimiter.getMaxLinearSpeed();

        m_moveTimeLeft -= Gdx.graphics.getDeltaTime();
        if (m_moveTimeLeft <= 0)
        {
            m_direction = Settings.Direction.values()[m_variativeRandom.nextValue()];
            Gdx.app.log("Trace", "Moving to " + m_direction.name());

            m_moveTimeLeft = m_random.nextInt(3);
        }

        Vector2 linear;
        try
        {
            Constructor<?> constructor = targetPosition.getClass().getConstructor(float.class, float.class);
            Object instance = constructor.newInstance(0.0f, 0.0f);
            linear = (Vector2)instance;
        }
        catch (Exception e)
        {
            Gdx.app.error("FATAL", "Can't create class of object " + targetPosition.getClass().toString() + ". " + e.toString());
            steering.setZero();
            return steering;
        }

        switch (m_direction)
        {
            case TOP:
                linear.set(0.0f, targetSpeed);
                break;
            case LEFT:
                linear.set(-targetSpeed, 0.0f);
                break;
            case RIGHT:
                linear.set(targetSpeed, 0.0f);
                break;
            case BOTTOM:
                linear.set(0.0f, -targetSpeed);
                break;
        }

        steering.linear.set((T)linear);
        steering.angular = 0.0f;

        return steering;
    }
}
