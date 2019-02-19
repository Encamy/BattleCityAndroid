package com.encamy.battlecity.utils;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.encamy.battlecity.Settings;

import java.util.EnumSet;

import static com.encamy.battlecity.Settings.SCREEN_HEIGHT;
import static com.encamy.battlecity.Settings.SCREEN_WIDTH;

public class Box2dHelpers
{
    public static Body createBox(World world, float x, float y, float w, float h, boolean isStatic, EnumSet<Settings.ObjectType> type)
    {
        x /= Settings.PPM;
        y /= Settings.PPM;
        w /= Settings.PPM;
        h /= Settings.PPM;
        BodyDef bodyDef = new BodyDef();
        bodyDef.fixedRotation = true;
        bodyDef.linearDamping = 10f;
        bodyDef.position.set(x + w * 0.5f - Settings.SCREEN_WIDTH / Settings.PPM * 0.5f, y + h * 0.5f - Settings.SCREEN_HEIGHT / Settings.PPM * 0.5f);

        if (isStatic)
        {
            bodyDef.type = BodyDef.BodyType.StaticBody;
        }
        else
        {
            bodyDef.type = BodyDef.BodyType.DynamicBody;
        }

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(w * 0.5f, h * 0.5f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;

        Body body = world.createBody(bodyDef).createFixture(fixtureDef).getBody();
        body.setUserData(type);

        return body;
    }

    public static float x2Box2d(float x)
    {
        return x + Settings.SCREEN_WIDTH / Settings.PPM * 0.5f - 32;
    }

    public static float Box2d2x(float x)
    {
        return x * Settings.PPM + Settings.SCREEN_WIDTH * 0.5f - 32;
    }

    public static float y2Box2d(float y)
    {
        return y + Settings.SCREEN_HEIGHT / Settings.PPM * 0.5f - 32;
    }

    public static float Box2d2y(float y)
    {
        return y * Settings.PPM + Settings.SCREEN_HEIGHT * 0.5f - 32;
    }
}
