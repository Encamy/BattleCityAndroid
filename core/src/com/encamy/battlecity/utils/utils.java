package com.encamy.battlecity.utils;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.encamy.battlecity.Settings;

public class utils
{
    public static Settings.Direction velocity2Direction(Vector2 velocity)
    {
        if (velocity.x < 0)
        {
            return Settings.Direction.LEFT;
        }
        else if (velocity.x > 0)
        {
            return Settings.Direction.RIGHT;
        }
        else if (velocity.y < 0)
        {
            return Settings.Direction.BOTTOM;
        }
        else if (velocity.y > 0)
        {
            return Settings.Direction.TOP;
        }

        return Settings.Direction.NULL;
    }
}
