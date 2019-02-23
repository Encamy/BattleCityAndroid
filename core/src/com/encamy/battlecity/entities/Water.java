package com.encamy.battlecity.entities;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.encamy.battlecity.Settings;
import com.encamy.battlecity.utils.Box2dHelpers;

import java.util.EnumSet;

public class Water extends BaseWall
{
    public Water(World world, Rectangle rectangle)
    {
        super.m_body = Box2dHelpers.createBox(
                world,
                rectangle.x,
                rectangle.y,
                rectangle.width,
                rectangle.height,
                true,
                EnumSet.of(Settings.ObjectType.WATER),
                true);

        super.m_world = world;
    }

    @Override
    public void update()
    {

    }

    @Override
    public void destroy()
    {

    }

    @Override
    public Body getBody()
    {
        return super.m_body;
    }

    @Override
    public void hit(int power)
    {
        // do nothing
    }
}
