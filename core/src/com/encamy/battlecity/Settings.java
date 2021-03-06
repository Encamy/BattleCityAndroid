package com.encamy.battlecity;

import com.encamy.battlecity.entities.Walls.BaseWall;
import com.encamy.battlecity.entities.Enemy;
import com.encamy.battlecity.network.NetworkDevice;
import com.encamy.battlecity.protobuf.NetworkProtocol;

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
    public static final int PACKET_MAX_LENGTH = 4096;

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

    public enum GameState
    {
        LOAD_STATE,
        PLAY_STATE,
        SCORE_STATE,
        GAME_OVER_STATE
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

    public interface OnMessageReceivedCallback
    {
        void OnMessageReceived(NetworkProtocol.PacketWrapper packet);
    }

    public interface OnConnectedCallback
    {
        void OnConnected();
    }

    public interface OnEnemySpawnedCallback
    {
        void OnEnemySpawned(int id, float x, float y, int level);
    }

    public interface OnEnemyMovedCallback
    {
        void OnEnemyMoved(int id, float x, float y, Settings.Direction direction);
    }

    public interface OnEnemyFiredCallback
    {
        void OnEnemyFired(int id, Settings.Direction direction);
    }

    public interface OnAllEnemiesDestroyedCallback
    {
        void OnAllEnemiesDestroyed();
    }
}
