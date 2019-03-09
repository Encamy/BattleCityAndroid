package com.encamy.battlecity;

import com.encamy.battlecity.entities.BaseWall;
import com.encamy.battlecity.entities.Enemy;
import com.encamy.battlecity.network.NetworkDevice;

import java.util.EnumSet;

public class Settings {
    public static int SCREEN_WIDTH = 1280;
    public static int SCREEN_HEIGHT = 720;
    public static final float ANIMATION_FRAME_DURATION = 0.20f;
    public static final float BASE_MOVEMENT_SPEED = 3.0f;
    public static final float BULLET_SPEED = BASE_MOVEMENT_SPEED * 0.05f;
    public static final float PPM = 64; // Pixels per meter
    public static final int PLAYER_HEALTH = 3;
    public static final int FIRE_RATE = 800;

    public static final int GAME_PORT = 6060;
    public static final int BROADCAST_PORT = 5060;
    public static final int BROADCAST_TIMEOUT = 5000;

    public enum Direction {LEFT, TOP, RIGHT, BOTTOM, NULL}
    public enum ObjectType
    {
        BRICK_WALL,
        STONE_WALL,
        WATER,
        GRASS,
        WALL,
        PLAYER,
        PLAYER1_OWNER,
        PLAYER2_OWNER,
        ENEMY,
        ENEMY_OWNER,
        FLAG,
        BULLET,
        SHOTTED;
    }

    public interface EnemyDestroyedCallback
    {
        void OnEnemyDestroyed(Enemy enemy);
    }

    public interface WallDestroyedCallback
    {
        void OnWallDestroyed(BaseWall wall);
    }

    public interface OnDeviceFoundCallback
    {
        void OnDeviceFound(NetworkDevice device);
    }
}
