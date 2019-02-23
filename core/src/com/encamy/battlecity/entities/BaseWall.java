package com.encamy.battlecity.entities;

import com.badlogic.gdx.physics.box2d.Body;

public interface BaseWall
{
    void update();
    void destroy();
    Body getBody();
    void hit(int power);
}
