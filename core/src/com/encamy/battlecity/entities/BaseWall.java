package com.encamy.battlecity.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

public abstract class BaseWall
{
    protected Texture m_texture;
    protected World m_world;
    protected Body m_body;

    public abstract void update();
    public abstract void destroy();

    public abstract Body getBody();

    public abstract void hit(int power);
}
