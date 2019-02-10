package com.encamy.battlecity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import static com.encamy.battlecity.Settings.SCREEN_HEIGHT;
import static com.encamy.battlecity.Settings.SCREEN_WIDTH;

public class Box2dHelpers
{
    public static Body createBox(World world, float x, float y, float w, float h, boolean isStatic)
    {
        BodyDef bodyDef = new BodyDef();
        bodyDef.fixedRotation = true;
        bodyDef.linearDamping = 10f;

        if (isStatic)
        {
            bodyDef.type = BodyDef.BodyType.StaticBody;
        }
        else
        {
            bodyDef.type = BodyDef.BodyType.DynamicBody;
        }

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(w * 0.5f, h * 0.5f, new Vector2(x + w * 0.5f - Settings.SCREEN_WIDTH * 0.5f, y + h * 0.5f - Settings.SCREEN_HEIGHT * 0.5f), 0.0f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;

        return world.createBody(bodyDef).createFixture(fixtureDef).getBody();
    }

    public static Body createPlayerBox(World world, float x, float y, float w, float h)
    {
        BodyDef bodyDef = new BodyDef();
        bodyDef.fixedRotation = true;
        bodyDef.linearDamping = 10f;
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x - SCREEN_WIDTH * 0.5f + 32, y - SCREEN_HEIGHT * 0.5f + 32);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(w * 0.5f, h * 0.5f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;

        return world.createBody(bodyDef).createFixture(fixtureDef).getBody();
    }
}
