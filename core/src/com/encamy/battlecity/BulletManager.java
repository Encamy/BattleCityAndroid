package com.encamy.battlecity;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.encamy.battlecity.entities.Bullet;

import java.util.ArrayList;

public class BulletManager
{
    private ArrayList<Bullet> m_bullets;
    private World m_world;
    private TextureAtlas m_textureAtlas;

    public BulletManager(World world)
    {
        m_bullets = new ArrayList<>();
        m_world = world;
    }

    public void setAtlas(TextureAtlas atlas)
    {
        m_textureAtlas = atlas;
    }

    public void addBullet(Vector2 spawnPos, Settings.Direction direction, Settings.ObjectType owner)
    {
        m_bullets.add(new Bullet(m_world, spawnPos, direction, owner, m_textureAtlas));
    }

    public void removeBullet(Body body)
    {
        for (Bullet bullet : m_bullets)
        {
            if (bullet.getBody() == body)
            {
                bullet.setState(Bullet.State.DESTROYING);
            }
        }
    }

    public void update(Batch batch)
    {
        for (Bullet bullet : m_bullets)
        {
            if (bullet.update(batch))
            {
                m_bullets.remove(bullet);
                m_world.destroyBody(bullet.getBody());
                break;
            }
        }
    }
}
