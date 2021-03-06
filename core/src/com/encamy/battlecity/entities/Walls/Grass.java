package com.encamy.battlecity.entities.Walls;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.encamy.battlecity.Settings;
import com.encamy.battlecity.utils.Box2dHelpers;

import java.util.EnumSet;

public class Grass extends Sprite implements BaseWall
{
    private World m_world;
    private Body m_body;
    private Settings.WallDestroyedCallback m_OnWallDestroyed;
    private int m_id;

    public Grass(World world, Rectangle rectangle, TextureAtlas.AtlasRegion region, int id)
    {
        super(region);

        m_body = Box2dHelpers.createBox(
                world,
                rectangle.x,
                rectangle.y,
                rectangle.width,
                rectangle.height,
                true,
                EnumSet.of(Settings.ObjectType.GRASS),
                false);

        m_world = world;
        m_id = id;
    }

    @Override
    public void update()
    {
        setX(Box2dHelpers.Box2d2x(m_body.getPosition().x, 16));
        setY(Box2dHelpers.Box2d2y(m_body.getPosition().y, 16));
    }

    @Override
    public void draw(Batch batch)
    {
        update();
        super.draw(batch);
    }

    @Override
    public void setOnDestoryedCallback(Settings.WallDestroyedCallback callback)
    {
        m_OnWallDestroyed = callback;
    }

    @Override
    public int getId()
    {
        return m_id;
    }

    @Override
    public void destroy()
    {
        m_world.destroyBody(m_body);
        m_OnWallDestroyed.OnWallDestroyed(this);
    }

    @Override
    public Body getBody()
    {
        return m_body;
    }

    @Override
    public boolean hit(int power)
    {
        if (power >= 4)
        {
            destroy();
            return true;
        }
        return false;
    }
}
