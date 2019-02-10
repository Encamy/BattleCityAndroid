package com.encamy.battlecity.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Shape;
import com.encamy.battlecity.Settings;


public class Player extends Sprite {

    private Vector2 velocity = new Vector2();
    private float m_speed = Settings.BASE_MOVEMENT_SPEED;
    private float m_animationTime = 0;
    private Animation m_left, m_top, m_right, m_bottom;
    private Body m_body;

    public Player(Animation left, Animation top, Animation right, Animation bottom, Body body)
    {
        super(((TextureAtlas.AtlasRegion) top.getKeyFrame(0)));
        m_left = left;
        m_top = top;
        m_right = right;
        m_bottom = bottom;

        m_body = body;
       // setX(body.getPosition().x);
      //  setY(body.getPosition().y);
    }

    @Override
    public void draw(Batch batch)
    {
        update(Gdx.graphics.getDeltaTime());
        super.draw(batch);
    }

    public void setVelocity(float x, float y)
    {
        velocity.set(x, y);
    }

    public float getSpeed()
    {
        return m_speed;
    }

    private void update(float deltaTime)
    {
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

        m_body.setLinearVelocity(velocity.x, velocity.y);


        Gdx.app.log("Trace", "Current player position: " + getX() + ":" + getY());
        setX(m_body.getPosition().x + Settings.SCREEN_WIDTH * 0.5f - 32);
        setY(m_body.getPosition().y + Settings.SCREEN_HEIGHT * 0.5f - 32);
    }
}
