package com.encamy.battlecity.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.encamy.battlecity.Settings;
import com.encamy.battlecity.utils.Box2dHelpers;

import java.util.EnumSet;

public class Bullet extends Sprite
{
    private World m_world;
    private Settings.Direction m_direction;
    private Body m_body;
    private Vector2 m_vector;

    public enum State {
        ALIVE,
        DESTROYING,
        DESTROYED
    }

    private State m_state;

    public Bullet(World world, Vector2 coords, Settings.Direction direction, Settings.ObjectType owner, TextureAtlas atlas)
    {
        super(atlas.findRegion("bullet_top"));

        m_world = world;
        m_direction = direction;

        // Should we set box2d fixture to 'bullet'?
        // Need testing for collision tunneling.
        m_body = Box2dHelpers.createBox(world,
                Box2dHelpers.x2Box2d(coords.x, 32),
                Box2dHelpers.y2Box2d(coords.y, 32),
                2, 2,
                false,
                EnumSet.of(owner, Settings.ObjectType.BULLET),
                true);

        m_body.setLinearVelocity(0.0f, 0.0f);

        switch (m_direction)
        {
            case TOP:
                m_vector = new Vector2(0.0f, Settings.BULLET_SPEED);
                super.setRegion(atlas.findRegion("bullet_top"));
                break;
            case LEFT:
                m_vector = new Vector2(-Settings.BULLET_SPEED, 0.0f);
                super.setRegion(atlas.findRegion("bullet_left"));
                break;
            case RIGHT:
                m_vector = new Vector2(Settings.BULLET_SPEED, 0.0f);
                super.setRegion(atlas.findRegion("bullet_right"));
                break;
            case BOTTOM:
                m_vector = new Vector2(0.0f, -Settings.BULLET_SPEED);
                super.setRegion(atlas.findRegion("bullet_bottom"));
                break;
        }

        m_state = State.ALIVE;
    }

    public boolean update(Batch batch)
    {
        switch (m_state)
        {
            case ALIVE:
                m_body.applyForceToCenter(m_vector, true);
                break;
            case DESTROYING:
                return true;
                //break;
            case DESTROYED:
                return true;
        }

        setX(Box2dHelpers.Box2d2x(m_body.getPosition().x, 16));
        setY(Box2dHelpers.Box2d2y(m_body.getPosition().y, 16));

        super.draw(batch);

        return false;
    }

    public void setState(State state)
    {
        m_state = state;
    }

    public void draw(Batch batch)
    {
        // placeholder
    }

    public Body getBody()
    {
        return m_body;
    }

    public void dispose()
    {
        m_world.destroyBody(m_body);
    }
}
