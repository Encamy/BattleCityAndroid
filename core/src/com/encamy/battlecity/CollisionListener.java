package com.encamy.battlecity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.JointEdge;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;

public class CollisionListener implements ContactListener
{
    World m_world;

    public CollisionListener(World world)
    {
        m_world = world;
    }

    @Override
    public void beginContact(Contact contact)
    {
        Object userDataA = contact.getFixtureA().getBody().getUserData();
        Object userDataB = contact.getFixtureB().getBody().getUserData();
        if (userDataA != null && userDataB != null)
        {
            String objectA = (String)userDataA;
            String objectB = (String)userDataB;
            Gdx.app.log("Trace", objectA + "  " + objectB);

            if (objectA.contains("BULLET") || objectB.contains("BULLET"))
            {
                if (!objectA.equals("PLAYER"))
                {
                    contact.getFixtureA().getBody().setUserData("SHOT");
                }

                if (!objectB.equals("PLAYER"))
                {
                    contact.getFixtureB().getBody().setUserData("SHOT");
                }
            }
        }
    }

    @Override
    public void endContact(Contact contact)
    {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold)
    {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse)
    {

    }
}
