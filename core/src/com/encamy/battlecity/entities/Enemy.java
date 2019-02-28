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

import java.util.EnumSet;

import javax.security.auth.callback.Callback;


public class Enemy extends Sprite {
    private Vector2 velocity = new Vector2();
    private float speed;
    private float m_animationTime = 0;
    private float m_health;
    private int score;
    private Animation m_left, m_top, m_right, m_bottom;
    private Body m_body;
    private Box2dSteeringEntity m_steeringEntity;
    private World m_world;
    private Settings.Direction m_direction;

    private Settings.EnemyDestroyedCallback m_OnEnemyDestroyed;

    public Enemy(Vector2 spawnpoint, EnemyProperties property, World world, Box2dSteeringEntity playerSteeringEntity)
    {
        super((TextureAtlas.AtlasRegion)property.bottomAnimation.getKeyFrame(0));

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

        m_steeringEntity = new Box2dSteeringEntity(m_body, 10.0f);
        m_steeringEntity.setMaxLinearSpeed(speed);
        //Arrive<Vector2> arriveSB = new Arrive<Vector2>(m_steeringEntity, playerSteeringEntity);
        RandomBehavior<Vector2> arriveSB = new RandomBehavior<Vector2>(m_steeringEntity, playerSteeringEntity);
        m_steeringEntity.setBehavior(arriveSB);
    }

    public void setOnDestroyedCallback(Settings.EnemyDestroyedCallback callback)
    {
        m_OnEnemyDestroyed = callback;
    }

    @Override
    public void draw(Batch batch) {
        update(Gdx.graphics.getDeltaTime());
        super.draw(batch);
    }

    public Body getBody()
    {
        return m_body;
    }

    private void update(float deltaTime)
    {
        // update behavior
        m_direction = m_steeringEntity.update(deltaTime);

        // update animation
        m_animationTime += deltaTime;
        if (velocity.x < 0)
        {
            super.setRegion(((TextureAtlas.AtlasRegion) m_left.getKeyFrame(m_animationTime)));
        }
        else if (velocity.x > 0)
        {
            super.setRegion(((TextureAtlas.AtlasRegion) m_right.getKeyFrame(m_animationTime)));
        }
        else if (velocity.y < 0)
        {
            super.setRegion(((TextureAtlas.AtlasRegion) m_bottom.getKeyFrame(m_animationTime)));
        }
        else if (velocity.y > 0)
        {
            super.setRegion(((TextureAtlas.AtlasRegion) m_top.getKeyFrame(m_animationTime)));
        }

        setX(Box2dHelpers.Box2d2x(m_body.getPosition().x));
        setY(Box2dHelpers.Box2d2y(m_body.getPosition().y));
    }

    private void SetPosition(Vector2 spawnpoint)
    {
        setX(spawnpoint.x);
        setY(spawnpoint.y);
    }

    private void SetProperty(EnemyProperties property)
    {
        m_left = property.leftAnimation;
        m_top = property.topAnimation;
        m_right = property.rightAnimation;
        m_bottom = property.bottomAnimation;
        speed = property.speed;
        m_health = property.health;
        score = property.score;
    }

    public float getHealth()
    {
        return m_health;
    }

    public void setHealth(float health)
    {
        m_health = health;
        if (m_health < 0)
        {
            destroy();
        }
    }

    private void destroy()
    {
        m_world.destroyBody(m_body);
        m_OnEnemyDestroyed.OnEnemyDestroyed(this);
    }
}
