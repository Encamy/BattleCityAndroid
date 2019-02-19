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
import java.util.EnumSet;

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
            EnumSet<Settings.ObjectType> objectA = (EnumSet<Settings.ObjectType>)userDataA;
            EnumSet<Settings.ObjectType> objectB = (EnumSet<Settings.ObjectType>)userDataB;
            Gdx.app.log("Trace", objectA + "  " + objectB);

            if (objectA.contains(Settings.ObjectType.BULLET) || objectB.contains(Settings.ObjectType.BULLET))
            {
                if (!objectA.equals(Settings.ObjectType.PLAYER))
                {
                    contact.getFixtureA().getBody().setUserData(Settings.ObjectType.SHOTTED);
                }

                if (!objectB.equals(Settings.ObjectType.PLAYER))
                {
                    contact.getFixtureB().getBody().setUserData(Settings.ObjectType.SHOTTED);
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
