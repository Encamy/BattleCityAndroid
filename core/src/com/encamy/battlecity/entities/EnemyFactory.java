package com.encamy.battlecity.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.encamy.battlecity.BulletManager;
import com.encamy.battlecity.Settings;
import com.encamy.battlecity.screens.GameScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

public class EnemyFactory implements Settings.EnemyDestroyedCallback
{
    private static Random m_random = new Random();

    // Choose tank level dispersion. Increase difficulty each level.
    private static final int[] m_baseRandomSequence = {0, 0, 0, 1, 1, 2, 3};
    private TextureAtlas m_atlas;
    private MapObjects m_spawnPoints;
    private int m_currentRandomCounter = 0;

    private static final int m_spawnIntervalMs = 1000;
    private static final int m_maxEnemies = 4;
    private int m_elapsedTime = 0;
    private EnemyProperties m_properties;

    private Vector<Enemy> m_enemies = new Vector<Enemy>();

    private World m_world;
    private Box2dSteeringEntity m_playerSteeringEntity;
    private BulletManager m_bulletManager;
    private int m_last_id;

    private Settings.OnEnemySpawnedCallback m_onEnemySpawnedCallback;
    private Settings.OnEnemyUpdateCallback m_onEnemyUpdateCallback;

    public EnemyFactory(MapObjects spawnPoints, TextureAtlas atlas, World world, Box2dSteeringEntity playerSteeringEntity, BulletManager bulletManager)
    {
        Gdx.app.log("Trace", "Creating enemy factory");
        m_spawnPoints = spawnPoints;
        m_atlas = atlas;

        m_properties = new EnemyProperties(m_atlas);

        m_world = world;
        m_playerSteeringEntity = playerSteeringEntity;
        m_bulletManager = bulletManager;
        m_last_id = 0;
    }

    public void update(float deltaTime, boolean freeze)
    {
        if (freeze)
        {
            return;
        }

        m_elapsedTime += deltaTime * 1000;

        if (m_elapsedTime > m_spawnIntervalMs)
        {
            if (m_enemies.size() < m_maxEnemies)
            {
                spawn();
            }
            m_elapsedTime %= m_spawnIntervalMs;
        }
    }

    private Enemy CreateRandomEnemy(Vector2 spawnpoint)
    {
        m_last_id++;
        int level = 0;
        if (m_currentRandomCounter < m_baseRandomSequence.length)
        {
            level = m_baseRandomSequence[m_currentRandomCounter++];
        }
        else
        {
            level = m_random.nextInt(4);
        }

        m_onEnemySpawnedCallback.OnEnemySpawned(m_last_id, spawnpoint.x, spawnpoint.y);
        return new Enemy(spawnpoint, m_properties.Get(level), m_world, m_playerSteeringEntity, m_bulletManager, m_last_id++);
    }

    private void spawn()
    {
        Gdx.app.log("Trace", "Spawning enemy");
        Vector2 spawnpoint = getSpawnPoint();

        if (spawnpoint == null)
        {
            return;
        }

        Enemy enemy = CreateRandomEnemy(spawnpoint);
        enemy.setOnDestroyedCallback(this);
        m_enemies.add(enemy);
    }

    private Vector2 getSpawnPoint()
    {
        List<Vector2> availableSpawns = new ArrayList<Vector2>();

        for (MapObject object : m_spawnPoints)
        {
            if (object instanceof RectangleMapObject)
            {
                Rectangle rectangle = ((RectangleMapObject) object).getRectangle();
                rectangle.width = 64;
                rectangle.height = 64;

                boolean intersect = false;

                for (Enemy enemy : m_enemies)
                {
                    Rectangle enemyRectangle = new Rectangle();

                    enemyRectangle.x = enemy.getX();
                    enemyRectangle.y = enemy.getY();
                    enemyRectangle.width = 64;
                    enemyRectangle.height = 64;

                    if (Intersector.overlaps(rectangle, enemyRectangle))
                    {
                        intersect = true;
                        break;
                    }
                }

                if (!intersect)
                {
                    availableSpawns.add(new Vector2(rectangle.getX(), rectangle.getY()));
                }
            }
        }

        if (availableSpawns.size() == 0)
        {
            Gdx.app.log("Trace", "There is no available spawns");
            return null;
        }

        Vector2 spawnpoint = availableSpawns.get(m_random.nextInt(availableSpawns.size()));
        Gdx.app.log("Trace", "Choosing spawnpoint = " + spawnpoint.x + ":" + spawnpoint.y);

        return spawnpoint;
    }

    public void draw(Batch batch, boolean freeze)
    {
        for (Enemy enemy : m_enemies)
        {
            Vector2 linearVelocity = new Vector2();
            enemy.draw(batch, freeze, linearVelocity);
            m_onEnemyUpdateCallback.OnEnemyUpdate(enemy.getId(), linearVelocity.x, linearVelocity.y);
        }
    }

    public boolean hit(Body body)
    {
        Enemy enemy = getEnemy(body);
        if (enemy == null)
        {
            return false;
        }

        return enemy.hit();
    }

    public Enemy getEnemy(Body body)
    {
        for (Enemy enemy : m_enemies)
        {
            if (enemy.getBody() == body)
            {
                return enemy;
            }
        }

        Gdx.app.log("TRACE", "Enemy was not found");
        return null;
    }


    @Override
    public void OnEnemyDestroyed(Enemy enemy)
    {
        m_enemies.remove(enemy);
    }

    public void setOnSpawnedCallback(Settings.OnEnemySpawnedCallback callback)
    {
        m_onEnemySpawnedCallback = callback;
    }

    public void setOnUpdateCallback(Settings.OnEnemyUpdateCallback callback)
    {
        m_onEnemyUpdateCallback = callback;
    }
}
