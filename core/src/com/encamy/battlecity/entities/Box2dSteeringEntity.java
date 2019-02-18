package com.encamy.battlecity.entities;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.encamy.battlecity.Settings;
import com.encamy.battlecity.utils.SteeringUtils;


public class Box2dSteeringEntity implements Steerable<Vector2>
{
    private final Body m_body;
    private boolean m_tagged = false;
    private final float m_boundingRadius;
    private float m_maxLinearSpeed = Settings.BASE_MOVEMENT_SPEED;
    private float m_maxLinearAcceleration = Settings.BASE_MOVEMENT_SPEED * 100;
    private float m_maxAngularSpeed = 30;
    private float m_maxAngularAcceleration = 5;

    private SteeringBehavior<Vector2> m_behavior;
    private final SteeringAcceleration<Vector2> m_steeringOutput;

    public Box2dSteeringEntity(Body body, float boundingRadius)
    {
        m_body = body;
        m_boundingRadius = boundingRadius;

        m_steeringOutput = new SteeringAcceleration<Vector2>(new Vector2());
        m_body.setUserData(this);
    }

    public void update(float deltaTime)
    {
        if (m_behavior == null)
        {
            return;
        }

        m_behavior.calculateSteering(m_steeringOutput);
        applySteering(m_steeringOutput, deltaTime);
    }

    private void applySteering(SteeringAcceleration<Vector2> steering, float deltaTime)
    {
        if (steering.linear.isZero())
        {
            return;
        }

        Vector2 force = steering.linear;
        m_body.setLinearVelocity(force);
    }

    @Override
    public Vector2 getLinearVelocity()
    {
        return m_body.getLinearVelocity();
    }

    @Override
    public float getAngularVelocity()
    {
        return m_body.getAngularVelocity();
    }

    @Override
    public float getBoundingRadius()
    {
        return m_boundingRadius;
    }

    @Override
    public boolean isTagged()
    {
        return m_tagged;
    }

    @Override
    public void setTagged(boolean tagged)
    {
        m_tagged = tagged;
    }

    @Override
    public float getZeroLinearSpeedThreshold()
    {
        return 0.001f;
    }

    @Override
    public void setZeroLinearSpeedThreshold(float value)
    {

    }

    @Override
    public float getMaxLinearSpeed()
    {
        return m_maxLinearSpeed;
    }

    @Override
    public void setMaxLinearSpeed(float maxLinearSpeed)
    {
        m_maxLinearSpeed = maxLinearSpeed;
    }

    @Override
    public float getMaxLinearAcceleration()
    {
        return m_maxLinearAcceleration;
    }

    @Override
    public void setMaxLinearAcceleration(float maxLinearAcceleration)
    {
        m_maxLinearAcceleration = maxLinearAcceleration;
    }

    @Override
    public float getMaxAngularSpeed()
    {
        return m_maxAngularSpeed;
    }

    @Override
    public void setMaxAngularSpeed(float maxAngularSpeed)
    {
        m_maxAngularSpeed = maxAngularSpeed;
    }

    @Override
    public float getMaxAngularAcceleration()
    {
        return m_maxAngularAcceleration;
    }

    @Override
    public void setMaxAngularAcceleration(float maxAngularAcceleration)
    {
        m_maxAngularAcceleration = maxAngularAcceleration;
    }

    @Override
    public Vector2 getPosition()
    {
        return m_body.getPosition();
    }

    @Override
    public float getOrientation()
    {
        return m_body.getAngle();
    }

    @Override
    public void setOrientation(float orientation)
    {
        m_body.setTransform(getPosition(), orientation);
    }

    @Override
    public float vectorToAngle(Vector2 vector)
    {
        return SteeringUtils.vectorToAngle(vector);
    }

    @Override
    public Vector2 angleToVector(Vector2 outVector, float angle)
    {
        return SteeringUtils.angleToVector(outVector, angle);
    }

    @Override
    public Location<Vector2> newLocation()
    {
        return new Box2dLocation();
    }

    public void setBehavior(SteeringBehavior<Vector2> behavior)
    {
        m_behavior = behavior;
    }
}
