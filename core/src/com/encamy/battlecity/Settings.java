package com.encamy.battlecity;

import java.util.EnumSet;

public class Settings {
    public static String APPLICATION_VERSION = "0.2";
    public static int SCREEN_WIDTH = 1280;
    public static int SCREEN_HEIGHT = 720;
    public static final float ANIMATION_FRAME_DURATION = 0.20f;
    public static final float BASE_MOVEMENT_SPEED = 3.0f;
    public static final float BULLET_SPEED = BASE_MOVEMENT_SPEED * 0.05f;
    public static final float PPM = 64; // Pixels per meter

    public enum Direction {LEFT, TOP, RIGHT, BOTTOM}
    public enum ObjectType
    {
        WALL,
        PLAYER,
        ENEMY,
        FLAG,
        BULLET,
        SHOTTED;
    }
}
