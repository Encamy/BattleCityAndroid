package com.encamy.battlecity.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.encamy.battlecity.Settings;
import com.encamy.battlecity.utils.Box2dHelpers;

import java.util.EnumSet;

public class BrickWall implements BaseWall
{
    private Texture m_texture;
    private World m_world;
    private Body m_body;

    public BrickWall(World world, Rectangle rectangle)
    {
        m_body = Box2dHelpers.createBox(
                world,
                rectangle.x,
                rectangle.y,
                rectangle.width,
                rectangle.height,
                true,
                EnumSet.of(Settings.ObjectType.BRICK_WALL),
                true);

        m_world = world;
    }

    @Override
    public void update()
    {

    }

    @Override
    public void destroy()
    {
        m_world.destroyBody(m_body);
    }

    @Override
    public Body getBody()
    {
        return m_body;
    }

    @Override
    public void hit(int power)
    {
        if (power >= 1)
        {
            destroy();
        }
    }
}
