package com.encamy.battlecity.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.encamy.battlecity.BulletManager;
import com.encamy.battlecity.behaviors.RandomBehavior;
import com.encamy.battlecity.utils.Box2dHelpers;
import com.encamy.battlecity.Settings;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Vector;

import javax.security.auth.callback.Callback;


public class Enemy extends Sprite {
    private float speed;
    private float m_animationTime = 0;
    private float m_health;
    private int m_score;
    private int m_level;
    private Animation m_left, m_top, m_right, m_bottom;
    private Animation m_spawning;
    private Body m_body;
    private Box2dSteeringEntity m_steeringEntity;
    private World m_world;
    private Settings.Direction m_direction;
    private BulletManager m_bulletManager;
    private int m_id;

    private Settings.EnemyDestroyedCallback m_OnEnemyDestroyed;
    private Settings.OnEnemyFiredCallback m_OnEnemyFired;

    private enum State
    {
        SPAWNING,
        ALIVE
    }

    private State m_state;

    public Enemy(Vector2 spawnpoint, EnemyProperties property, World world, Box2dSteeringEntity playerSteeringEntity, BulletManager bulletManager, int id)
    {
        super((TextureAtlas.AtlasRegion)property.animation.getBottomAnimation().getKeyFrame(0));

        SetProperty(property);
        SetPosition(spawnpoint);

        m_body = Box2dHelpers.createBox(
                world,
                spawnpoint.x, spawnpoint.y,
                58, 58,
                false,
                EnumSet.of(Settings.ObjectType.ENEMY),
                true);

        m_world = world;
        m_bulletManager = bulletManager;

        // Means that this enemy is 'zombie' (Server will control it)
        if (playerSteeringEntity != null)
        {
            m_steeringEntity = new Box2dSteeringEntity(m_body, 10.0f);
            m_steeringEntity.setMaxLinearSpeed(speed);
            //Arrive<Vector2> arriveSB = new Arrive<Vector2>(m_steeringEntity, playerSteeringEntity);
            RandomBehavior<Vector2> arriveSB = new RandomBehavior<Vector2>(m_steeringEntity, playerSteeringEntity);
            m_steeringEntity.setBehavior(arriveSB);
        }

        m_state = State.SPAWNING;
        m_id = id;
        m_direction = Settings.Direction.BOTTOM;
    }

    public void setOnDestroyedCallback(Settings.EnemyDestroyedCallback callback)
    {
        m_OnEnemyDestroyed = callback;
    }

    public void setOnFiredCallback(Settings.OnEnemyFiredCallback callback)
    {
        m_OnEnemyFired = callback;
    }

    public Vector2 draw(Batch batch, boolean freeze)
    {
        Vector2 position = null;
        if (!freeze)
        {
            position = update(Gdx.graphics.getDeltaTime());
        }
        super.draw(batch);

        return position;
    }

    public Body getBody()
    {
        return m_body;
    }

    public int getScore()
    {
        return m_score;
    }

    public int getLevel()
    {
        return m_level;
    }

    public int getId()
    {
        return m_id;
    }

    private void updateAliveAnimation(float animationTime)
    {
        switch (m_direction)
        {
            case LEFT:
                super.setRegion(((TextureAtlas.AtlasRegion) m_left.getKeyFrame(animationTime)));
                break;
            case TOP:
                super.setRegion(((TextureAtlas.AtlasRegion) m_top.getKeyFrame(animationTime)));
                break;
            case RIGHT:
                super.setRegion(((TextureAtlas.AtlasRegion) m_right.getKeyFrame(animationTime)));
                break;
            case BOTTOM:
                super.setRegion(((TextureAtlas.AtlasRegion) m_bottom.getKeyFrame(animationTime)));
                break;
        }
    }

    private void updateSpawnAnimation(float animationTime)
    {
        super.setRegion((TextureAtlas.AtlasRegion) m_spawning.getKeyFrame(animationTime));
        if (m_spawning.isAnimationFinished(animationTime))
        {
            m_state = State.ALIVE;
        }
    }

    public void setPosition(Vector2 vector2)
    {
        m_body.setTransform(vector2, 0);
    }

    public void setDirection(Settings.Direction direction)
    {
        m_direction = direction;
    }

    private Vector2 update(float deltaTime)
    {
        m_animationTime += deltaTime;

        switch (m_state)
        {
            case SPAWNING:
                updateSpawnAnimation(m_animationTime);
                break;
            case ALIVE:
            {
                // update behavior
                // Check for master/slave
                if (m_steeringEntity != null)
                {
                    m_direction = m_steeringEntity.update(deltaTime);
                }
                updateAliveAnimation(m_animationTime);

                if (m_animationTime * 1000 > Settings.FIRE_RATE)
                {
                    //Gdx.app.log("TRACE", "FIRE");
                    fire(false);
                    m_animationTime = 0;
                }
            }
            break;
            default:
                Gdx.app.log("FATAL", "Invalid state");
                break;
        }

        setX(Box2dHelpers.Box2d2x(m_body.getPosition().x, 32));
        setY(Box2dHelpers.Box2d2y(m_body.getPosition().y, 32));

        return new Vector2(m_body.getPosition().x, m_body.getPosition().y);
    }

    public void fire(Settings.Direction direction)
    {
        m_direction = direction;
        fire(true);
    }

    public Settings.Direction getDirection()
    {
        return m_direction;
    }

    private void fire(boolean force)
    {
        if (m_OnEnemyFired != null || force)
        {
            Vector2 bulletSpawnPos = new Vector2();
            switch (m_direction)
            {
                case TOP:
                    bulletSpawnPos.set(Box2dHelpers.Box2d2x(m_body.getPosition().x, 32) + 51, Box2dHelpers.Box2d2y(m_body.getPosition().y, 32) + 90);
                    break;
                case LEFT:
                    bulletSpawnPos.set(Box2dHelpers.Box2d2x(m_body.getPosition().x, 32) + 20, Box2dHelpers.Box2d2y(m_body.getPosition().y, 32) + 58);
                    break;
                case RIGHT:
                    bulletSpawnPos.set(Box2dHelpers.Box2d2x(m_body.getPosition().x, 32) + 85, Box2dHelpers.Box2d2y(m_body.getPosition().y, 32) + 58);
                    break;
                case BOTTOM:
                    bulletSpawnPos.set(Box2dHelpers.Box2d2x(m_body.getPosition().x, 32) + 51, Box2dHelpers.Box2d2y(m_body.getPosition().y, 32) + 20);
                    break;
            }

            m_bulletManager.addBullet(bulletSpawnPos, m_direction, Settings.ObjectType.ENEMY_OWNER);
        }

        if (m_OnEnemyFired != null)
        {
            m_OnEnemyFired.OnEnemyFired(m_id, m_direction);
        }
    }

    private void SetPosition(Vector2 spawnpoint)
    {
        setX(spawnpoint.x);
        setY(spawnpoint.y);
    }

    private void SetProperty(EnemyProperties property)
    {
        m_left = property.animation.getLeftAnimation();
        m_top = property.animation.getTopAnimation();
        m_right = property.animation.getRightAnimation();
        m_bottom = property.animation.getBottomAnimation();
        m_spawning = property.animation.getSpawnAnimation();
        speed = property.speed;
        m_health = property.health;
        m_score = property.score;
        m_level = property.level;
    }

    public boolean hit()
    {
        m_health--;
        Gdx.app.log("TRACE", "Current enemy health = " + m_health);
        if (m_health <= 0)
        {
            destroy();
            return true;
        }

        return false;
    }

    public void destroy()
    {
        m_world.destroyBody(m_body);
        m_OnEnemyDestroyed.OnEnemyDestroyed(this);
    }
}
