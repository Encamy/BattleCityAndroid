package com.encamy.battlecity.entities;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.encamy.battlecity.Settings;
import com.encamy.battlecity.utils.Box2dHelpers;

public class Bullet
{
    World m_world;
    Settings.Direction m_direction;
    Body m_body;
    Vector2 m_vector;

    public Bullet(World world, Vector2 coords, Settings.Direction direction)
    {
        m_world = world;
        m_direction = direction;

        // Shoud we set box2d ficture to 'bullet'?
        // Need testing for collision tunneling.
        m_body = Box2dHelpers.createBox(world,
                Box2dHelpers.x2Box2d(coords.x),
                Box2dHelpers.y2Box2d(coords.y),
                2, 2,
                false);

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
    }

    public void update(float deltaTime)
    {
        m_body.applyForceToCenter(m_vector, true);
    }
}
