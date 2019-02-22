package com.encamy.battlecity.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.World;

public abstract class BaseWall
{
    protected Texture m_texture;
    protected World m_world;

    public abstract void update();
    public abstract void destroy();
}
