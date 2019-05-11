package com.encamy.battlecity.entities.Walls;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.physics.box2d.Body;
import com.encamy.battlecity.Settings;

public interface BaseWall
{
    void update();
    void destroy();
    Body getBody();
    boolean hit(int power);
    void draw(Batch batch);
    void setOnDestoryedCallback(Settings.WallDestroyedCallback callback);
    int getId();
}
