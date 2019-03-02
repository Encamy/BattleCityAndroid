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
import com.encamy.battlecity.behaviors.RandomBehavior;
import com.encamy.battlecity.utils.Box2dHelpers;
import com.encamy.battlecity.Settings;

import java.util.ArrayList;
import java.util.EnumSet;

import javax.security.auth.callback.Callback;


public class Enemy extends Sprite {
    private float speed;
    private float m_animationTime = 0;
    private float m_health;
    private int score;
    private Animation m_left, m_top, m_right, m_bottom;
    private Animation m_spawning;
    private Body m_body;
    private Box2dSteeringEntity m_steeringEntity;
    private World m_world;
    private Settings.Direction m_direction;
    private ArrayList<Bullet> m_bullets;

    private Settings.EnemyDestroyedCallback m_OnEnemyDestroyed;

    private enum State
    {
        SPAWNING,
        ALIVE
    }

    private State m_state;

    public Enemy(Vector2 spawnpoint, EnemyProperties property, World world, Box2dSteeringEntity playerSteeringEntity)
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

        m_bullets = new ArrayList<Bullet>();

        m_steeringEntity = new Box2dSteeringEntity(m_body, 10.0f);
        m_steeringEntity.setMaxLinearSpeed(speed);
        //Arrive<Vector2> arriveSB = new Arrive<Vector2>(m_steeringEntity, playerSteeringEntity);
        RandomBehavior<Vector2> arriveSB = new RandomBehavior<Vector2>(m_steeringEntity, playerSteeringEntity);
        m_steeringEntity.setBehavior(arriveSB);
        m_state = State.SPAWNING;
    }

    public void setOnDestroyedCallback(Settings.EnemyDestroyedCallback callback)
    {
        m_OnEnemyDestroyed = callback;
    }

    public void draw(Batch batch, boolean freeze)
    {
        if (!freeze)
        {
            update(Gdx.graphics.getDeltaTime());
        }
        super.draw(batch);
    }

    public Body getBody()
    {
        return m_body;
    }

    private void updateAliveAnimation(float animationTime)
    {
        if (m_body.getLinearVelocity().x < 0)
        {
            super.setRegion(((TextureAtlas.AtlasRegion) m_left.getKeyFrame(animationTime)));
        }
        else if (m_body.getLinearVelocity().x > 0)
        {
            super.setRegion(((TextureAtlas.AtlasRegion) m_right.getKeyFrame(animationTime)));
        }
        else if (m_body.getLinearVelocity().y < 0)
        {
            super.setRegion(((TextureAtlas.AtlasRegion) m_bottom.getKeyFrame(animationTime)));
        }
        else if (m_body.getLinearVelocity().y > 0)
        {
            super.setRegion(((TextureAtlas.AtlasRegion) m_top.getKeyFrame(animationTime)));
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


    private void update(float deltaTime)
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
                m_direction = m_steeringEntity.update(deltaTime);
                updateAliveAnimation(m_animationTime);

                if (m_animationTime * 1000 > Settings.FIRE_RATE)
                {
                    Gdx.app.log("TRACE", "FIRE");
                    fire();
                    m_animationTime = 0;
                }
            }
            break;
            default:
                Gdx.app.log("FATAL", "Invalid state");
                break;
        }

        setX(Box2dHelpers.Box2d2x(m_body.getPosition().x));
        setY(Box2dHelpers.Box2d2y(m_body.getPosition().y));

        for (Bullet bullet : m_bullets)
        {
            bullet.update(deltaTime);
        }
    }

    private void fire()
    {
        Vector2 bulletSpawnPos = new Vector2();
        switch (m_direction)
        {
            case TOP:
                bulletSpawnPos.set(Box2dHelpers.Box2d2x(m_body.getPosition().x) + 51, Box2dHelpers.Box2d2y(m_body.getPosition().y) + 90);
                break;
            case LEFT:
                bulletSpawnPos.set(Box2dHelpers.Box2d2x(m_body.getPosition().x) + 20, Box2dHelpers.Box2d2y(m_body.getPosition().y) + 58);
                break;
            case RIGHT:
                bulletSpawnPos.set(Box2dHelpers.Box2d2x(m_body.getPosition().x) + 85, Box2dHelpers.Box2d2y(m_body.getPosition().y) + 58);
                break;
            case BOTTOM:
                bulletSpawnPos.set(Box2dHelpers.Box2d2x(m_body.getPosition().x) + 51, Box2dHelpers.Box2d2y(m_body.getPosition().y) + 20);
                break;
        }

        m_bullets.add(new Bullet(m_world, bulletSpawnPos, m_direction, Settings.ObjectType.ENEMY_OWNER));
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
        score = property.score;
    }

    public void hit()
    {
        m_health--;
        if (m_health < 0)
        {
            destroy();
        }
    }

    private void destroy()
    {
        m_world.destroyBody(m_body);
        for (Bullet bullet : m_bullets)
        {
            bullet.dispose();
        }
        m_bullets.clear();
        m_OnEnemyDestroyed.OnEnemyDestroyed(this);
    }

    public boolean destroyBullet(Body body)
    {
        for (Bullet bullet : m_bullets)
        {
            if (bullet.getBody() == body)
            {
                m_bullets.remove(bullet);
                bullet.dispose();
                return true;
            }
        }

        return false;
    }
}
