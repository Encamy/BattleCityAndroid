package com.encamy.battlecity.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.encamy.battlecity.Settings;
import com.encamy.battlecity.utils.Box2dHelpers;

import java.util.EnumSet;

public class Bullet
{
    private World m_world;
    private Settings.Direction m_direction;
    private Body m_body;
    private Vector2 m_vector;
    private boolean active = true;

    public Bullet(World world, Vector2 coords, Settings.Direction direction, Settings.ObjectType owner)
    {
        m_world = world;
        m_direction = direction;

        // Should we set box2d fixture to 'bullet'?
        // Need testing for collision tunneling.
        m_body = Box2dHelpers.createBox(world,
                Box2dHelpers.x2Box2d(coords.x),
                Box2dHelpers.y2Box2d(coords.y),
                2, 2,
                false,
                EnumSet.of(owner, Settings.ObjectType.BULLET));

        m_body.setLinearVelocity(0.0f, 0.0f);

        switch (m_direction)
        {
            case TOP:
                m_vector = new Vector2(0.0f, Settings.BULLET_SPEED);
                break;
            case LEFT:
                m_vector = new Vector2(-Settings.BULLET_SPEED, 0.0f);
                break;
            case RIGHT:
                m_vector = new Vector2(Settings.BULLET_SPEED, 0.0f);
                break;
            case BOTTOM:
                m_vector = new Vector2(0.0f, -Settings.BULLET_SPEED);
                break;
        }

        active = true;
    }

    public void update(float deltaTime)
    {
        if (active)
        {
            Gdx.app.log("TRACE", m_vector.toString());
            m_body.applyForceToCenter(m_vector, true);
        }
        else
        {
            Gdx.app.log("ERROR", "Trying to update dead bullet");
        }
    }

    public Body getBody()
    {
        return m_body;
    }

    public void dispose()
    {
        m_world.destroyBody(m_body);
        active = false;
    }
}
