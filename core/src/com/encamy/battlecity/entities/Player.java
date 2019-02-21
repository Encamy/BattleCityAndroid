package com.encamy.battlecity.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.encamy.battlecity.Settings;
import com.encamy.battlecity.utils.Box2dHelpers;
import com.encamy.battlecity.utils.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class Player extends Sprite {

    private Vector2 m_velocity = new Vector2();
    private float m_speed = Settings.BASE_MOVEMENT_SPEED;
    private float m_animationTime = 0;
    private Animation m_left, m_top, m_right, m_bottom;
    private Body m_body;
    private Box2dSteeringEntity m_steeringEntity;
    private World m_world;
    private Settings.Direction m_direction;
    private ArrayList<Bullet> m_bullets;
    private int m_health;

    public Player(Animation left, Animation top, Animation right, Animation bottom, Body body, World world)
    {
        super(((TextureAtlas.AtlasRegion) top.getKeyFrame(0)));
        m_left = left;
        m_top = top;
        m_right = right;
        m_bottom = bottom;

        m_body = body;
        m_steeringEntity = new Box2dSteeringEntity(m_body, 10.0f);
        m_world = world;
        m_direction = Settings.Direction.TOP;

        m_bullets = new ArrayList<Bullet>();

        m_health = Settings.PLAYER_HEALTH;
    }

    @Override
    public void draw(Batch batch)
    {
        update(Gdx.graphics.getDeltaTime());
        super.draw(batch);
    }

    public void setVelocity(float x, float y)
    {
        m_velocity.set(x, y);
    }

    public float getSpeed()
    {
        return m_speed;
    }

    public Body getBody()
    {
        return m_body;
    }

    public Box2dSteeringEntity getSteeringEntity()
    {
        return m_steeringEntity;
    }

    private void update(float deltaTime)
    {
        // update animation
        m_animationTime += deltaTime;

        Settings.Direction direction = utils.velocity2Direction(m_velocity);

        if (direction != null)
        {
            m_direction = direction;
        }

        // We do not want to use stored direction here.
        // Animation should be played only while tank is moved
        if (direction != null)
        {
            switch (direction)
            {
                case TOP:
                    super.setRegion(((TextureAtlas.AtlasRegion) m_top.getKeyFrame(m_animationTime)));
                    break;
                case LEFT:
                    super.setRegion(((TextureAtlas.AtlasRegion) m_left.getKeyFrame(m_animationTime)));
                    break;
                case RIGHT:
                    super.setRegion(((TextureAtlas.AtlasRegion) m_right.getKeyFrame(m_animationTime)));
                    break;
                case BOTTOM:
                    super.setRegion(((TextureAtlas.AtlasRegion) m_bottom.getKeyFrame(m_animationTime)));
                    break;
            }
        }

        m_body.setLinearVelocity(m_velocity.x, m_velocity.y);

        //Gdx.app.log("Trace", "Current player position: " + getX() + ":" + getY());

        setX(Box2dHelpers.Box2d2x(m_body.getPosition().x));
        setY(Box2dHelpers.Box2d2y(m_body.getPosition().y));

        for (Bullet bullet : m_bullets)
        {
            bullet.update(deltaTime);
        }
    }

    public void fire()
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

        m_bullets.add(new Bullet(m_world, bulletSpawnPos, m_direction, Settings.ObjectType.PLAYER_OWNER));
    }

    public void destroyBullet(Body body)
    {
        for (Bullet bullet : m_bullets)
        {
            if (bullet.getBody() == body)
            {
                m_bullets.remove(bullet);
                bullet.dispose();
                break;
            }
        }
    }
}
