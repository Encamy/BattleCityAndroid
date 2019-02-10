package com.encamy.battlecity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Vector2;

import java.util.Random;
import java.util.Vector;

public class EnemyFactory
{
    private static Random m_random = new Random();

    private static final int[] m_baseRandomSequence = {0, 0, 0, 1, 1, 2, 3};
    private TextureAtlas m_atlas;
    private MapObjects m_spawnPoints;
    private int m_currentRandomCounter = 0;

    private static final int m_spawnIntervalMs = 1000;
    private static final int m_maxEnemies = 4;
    private int m_elapsedTime = 0;
    private EnemyProperties m_properties;

    private Vector<Enemy> m_enemies = new Vector<Enemy>();

    public EnemyFactory(MapObjects spawnPoints, TextureAtlas atlas)
    {
        Gdx.app.log("Trace", "Creating enemy factory");
        m_spawnPoints = spawnPoints;
        m_atlas = atlas;

        m_properties = new EnemyProperties(m_atlas);
    }

    public void update(float deltaTime)
    {
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
        int level = 0;
        if (m_currentRandomCounter < m_baseRandomSequence.length)
        {
            level = m_baseRandomSequence[m_currentRandomCounter++];
        }
        else
        {
            level = m_random.nextInt(4);
        }

        return new Enemy(spawnpoint, m_properties.Get(level));
    }

    private void spawn()
    {
        Gdx.app.log("Trace", "Spawning enemy");
        Vector2 spawnpoint = getSpawnPoint();
        PlaySpawnAnimation(spawnpoint);

        m_enemies.add(CreateRandomEnemy(spawnpoint));
    }

    private void PlaySpawnAnimation(Vector2 spawnpoint)
    {
        // just a placeholder for now
    }

    private Vector2 getSpawnPoint()
    {
        MapObject spawnObject = m_spawnPoints.get(m_random.nextInt(m_spawnPoints.getCount()));
        Vector2 spawnpoint = new Vector2(
                ((RectangleMapObject)spawnObject).getRectangle().getX(),
                ((RectangleMapObject)spawnObject).getRectangle().getY()
        );

        return spawnpoint;
    }

    public void draw(Batch batch)
    {
        for (Enemy enemy : m_enemies)
        {
            enemy.draw(batch);
        }
    }
}
