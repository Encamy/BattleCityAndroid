package com.encamy.battlecity;

public class Settings {
    public static String APPLICATION_VERSION = "0.1";
    public static int SCREEN_WIDTH = 1280;
    public static int SCREEN_HEIGHT = 720;
    public static final float ANIMATION_FRAME_DURATION = 0.20f;
    private static final float BASE_MOVEMENT_SPEED = 600 * 2;
    public static final float MOVEMENT_SPEED = SCREEN_WIDTH * BASE_MOVEMENT_SPEED;

    public enum Direction {LEFT, TOP, RIGHT, BOTTOM}
}
