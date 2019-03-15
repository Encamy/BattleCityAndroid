package com.encamy.battlecity.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.encamy.battlecity.BulletManager;
import com.encamy.battlecity.CollisionListener;
import com.encamy.battlecity.LayerManager;
import com.encamy.battlecity.Settings;
import com.encamy.battlecity.entities.Enemy;
import com.encamy.battlecity.entities.EnemyFactory;
import com.encamy.battlecity.network.NetworkManager;
import com.encamy.battlecity.protobuf.NetworkProtocol;
import com.encamy.battlecity.utils.Dictionary;

import java.util.EnumSet;

import static com.encamy.battlecity.Settings.SCREEN_HEIGHT;
import static com.encamy.battlecity.Settings.SCREEN_WIDTH;

public class GameScreen implements Screen, Settings.OnEnemyUpdateCallback, Settings.OnEnemySpawnedCallback {

	private OrthographicCamera m_camera;
	private OrthographicCamera m_debugCamera;
    private SpriteBatch m_spriteBatch;

    private EnemyFactory m_enemyFactory;

    private Box2DDebugRenderer m_b2drenderer;
    private World m_world;
    private LayerManager m_layerManager;
    private BulletManager m_bulletManager;
    private final int CURRENT_PLAYER = 1;
    private boolean m_freezeWorld = false;

    private NetworkManager m_networkManager;

    public GameScreen(NetworkManager networkManager)
    {
        m_networkManager = networkManager;
    }

    @Override
    public void show()
    {
		Gdx.app.log("Info", this.getClass().getName() + " started");
		Gdx.app.log("Info", "platform = " + Gdx.app.getType().name());

		m_camera = new OrthographicCamera();
        m_camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT);

        m_debugCamera = new OrthographicCamera();
        m_debugCamera.setToOrtho(false, SCREEN_WIDTH / Settings.PPM, SCREEN_HEIGHT / Settings.PPM);

        m_spriteBatch = new SpriteBatch();

        Box2D.init();
        m_b2drenderer = new Box2DDebugRenderer();
        m_b2drenderer.setDrawVelocities(true);
        m_world = new World(new Vector2(0,0), true);
        CollisionListener listener = new CollisionListener(m_world);
        m_world.setContactListener(listener);

        m_bulletManager = new BulletManager(m_world);

        m_layerManager = new LayerManager(m_world, m_bulletManager);

        m_layerManager.loadLevel("general_map.tmx");
        m_layerManager.loadPlayer(CURRENT_PLAYER);

        m_bulletManager.setAtlas(m_layerManager.getAtlas());

        m_enemyFactory = new EnemyFactory(
                m_layerManager.getTileMap().getLayers().get("EnemySpawns").getObjects(),
                m_layerManager.getAtlas(),
                m_world,
                m_layerManager.getPlayer(CURRENT_PLAYER).getSteeringEntity(),
                m_bulletManager,
                m_networkManager.isServer()
        );

        if (m_networkManager != null)
        {
            m_enemyFactory.setOnSpawnedCallback(this);
            m_enemyFactory.setOnUpdateCallback(this);
        }
	}

    @Override
    public void render(float delta)
    {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		m_camera.update();
        m_debugCamera.update();
		m_enemyFactory.update(Gdx.graphics.getDeltaTime(), m_freezeWorld);

        m_spriteBatch.setProjectionMatrix(m_camera.combined);
        m_spriteBatch.begin();
        m_layerManager.getPlayer(CURRENT_PLAYER).draw(m_spriteBatch, m_freezeWorld);
        m_enemyFactory.draw(m_spriteBatch, m_freezeWorld);
        m_layerManager.drawWalls(m_spriteBatch);
        m_bulletManager.update(m_spriteBatch);
        m_spriteBatch.end();
        if (!m_freezeWorld)
        {
            m_world.step(Gdx.graphics.getDeltaTime(), 6, 2);
        }

        m_b2drenderer.render(m_world, m_debugCamera.projection);

        processHitted();
    }

    @Override
    public void resize(int width, int height)
    {

    }

    @Override
    public void pause()
    {
        m_freezeWorld = true;
    }

    @Override
    public void resume()
    {
        m_freezeWorld = false;
    }

    private void processHitted()
    {
        Array<Body> bodies = new Array<Body>();
        m_world.getBodies(bodies);

        for (Body body : bodies)
        {
            if (body.getUserData() == null)
            {
                continue;
            }

            if (!((EnumSet<Settings.ObjectType>)body.getUserData()).contains(Settings.ObjectType.SHOTTED))
            {
                continue;
            }

            EnumSet<Settings.ObjectType> type = (EnumSet<Settings.ObjectType>)body.getUserData();
            Gdx.app.log("TRACE", type.toString());

            if (type.contains(Settings.ObjectType.BULLET ) && !type.contains(Settings.ObjectType.GRASS))
            {
                Gdx.app.log("TRACE", "Handled");
                m_bulletManager.removeBullet(body);
            }
            else if (type.contains(Settings.ObjectType.ENEMY))
            {
                if (!type.contains(Settings.ObjectType.ENEMY_OWNER))
                {
                    Gdx.app.log("TRACE", "Handled");

                    Enemy enemy = m_enemyFactory.getEnemy(body);

                    int score = 0;
                    int level = 0;
                    if (enemy != null)
                    {
                        score = enemy.getScore();
                        level = enemy.getLevel();

                        if (enemy.hit())
                        {
                            if (type.contains(Settings.ObjectType.PLAYER1_OWNER))
                            {
                                m_layerManager.getPlayer(1).enemyDestroyed(level, score);
                            }
                            else if (type.contains(Settings.ObjectType.PLAYER2_OWNER))
                            {
                                m_layerManager.getPlayer(2).enemyDestroyed(level, score);
                            }
                        }
                        else
                        {
                            body.setUserData(EnumSet.of(Settings.ObjectType.ENEMY));
                        }
                    }
                    else
                    {
                        if (type.contains(Settings.ObjectType.PLAYER))
                        {
                            Gdx.app.log("ERROR", "Some strange thing happened. Seems like enemy and player joined?");
                            body.setUserData(EnumSet.of(Settings.ObjectType.PLAYER));
                        }
                        else
                        {
                            body.setUserData(EnumSet.of(Settings.ObjectType.ENEMY));
                            //m_world.destroyBody(body);
                        }
                        break;
                    }
                }
                else
                {
                    Gdx.app.log("TRACE", "Handled");
                    body.setUserData(EnumSet.of(Settings.ObjectType.ENEMY));
                }
            }
            else if (type.contains(Settings.ObjectType.PLAYER))
            {
                if (m_layerManager.getPlayer(1) != null)
                {
                    if (m_layerManager.getPlayer(1).getBody() == body) {
                        Gdx.app.log("TRACE", "Handled");
                        m_layerManager.getPlayer(1).hit();
                        body.setUserData(EnumSet.of(Settings.ObjectType.PLAYER));
                    }
                }
                else if (m_layerManager.getPlayer(2) != null)
                {
                    if (m_layerManager.getPlayer(2).getBody() == body)
                    {
                        Gdx.app.log("TRACE", "Handled");
                        m_layerManager.getPlayer(2).hit();
                        body.setUserData(EnumSet.of(Settings.ObjectType.PLAYER));
                    }
                }
                else
                {
                    Gdx.app.log("FATAL", "Invalid player was hitted");
                }
            }
            else if (type.contains(Settings.ObjectType.BRICK_WALL))
            {
                Gdx.app.log("TRACE", "handled");
                boolean destroyed = m_layerManager.hit(body, type);
                if (!destroyed)
                {
                    body.setUserData(EnumSet.of(Settings.ObjectType.BRICK_WALL));
                }
            }
            else if (type.contains(Settings.ObjectType.STONE_WALL))
            {
                Gdx.app.log("TRACE", "handled");
                boolean destroyed = m_layerManager.hit(body, type);
                if (!destroyed)
                {
                    body.setUserData(EnumSet.of(Settings.ObjectType.STONE_WALL));
                }
            }
            else if (type.contains(Settings.ObjectType.GRASS))
            {
                boolean destroyed = m_layerManager.hit(body, type);
                if (!destroyed)
                {
                    Gdx.app.log("TRACE", "handled");
                }

                if (!type.contains(Settings.ObjectType.BULLET))
                {
                    body.setUserData(EnumSet.of(Settings.ObjectType.GRASS));
                }
                else
                {
                    if (type.contains(Settings.ObjectType.PLAYER1_OWNER))
                    {
                        body.setUserData(EnumSet.of(Settings.ObjectType.BULLET, Settings.ObjectType.PLAYER1_OWNER));
                    }
                    else if (type.contains(Settings.ObjectType.PLAYER2_OWNER))
                    {
                        body.setUserData(EnumSet.of(Settings.ObjectType.BULLET, Settings.ObjectType.PLAYER2_OWNER));
                    }
                    else if (type.contains(Settings.ObjectType.ENEMY_OWNER))
                    {
                        body.setUserData(EnumSet.of(Settings.ObjectType.BULLET, Settings.ObjectType.ENEMY_OWNER));
                    }

                }
            }
            else if (type.contains(Settings.ObjectType.WATER))
            {
                Gdx.app.log("TRACE", "handled");
                boolean destroyed = m_layerManager.hit(body, type);
                if (!destroyed)
                {
                    body.setUserData(EnumSet.of(Settings.ObjectType.WATER));
                }
            }
            else if (type.contains(Settings.ObjectType.WALL))
            {
                Gdx.app.log("TRACE", "Handled");
                body.setUserData(EnumSet.of(Settings.ObjectType.WALL));
            }
            else if (type.contains(Settings.ObjectType.FLAG ))
            {
                if (m_layerManager.hit(body, type))
                {
                    Gdx.app.log("INFO", "GAME OVER");
                    m_freezeWorld = true;

                    Dictionary<Integer, Integer> dictionary = m_layerManager.getPlayer(1).getDestroyedEnemies();
                    for(int i = 0; i < dictionary.size(); i++)
                    {
                        Dictionary.Entry entry = dictionary.getAt(i);
                        Gdx.app.log("TRACE","level = " + entry.getKey() + " score = " + entry.getValue());
                    }
                }
                Gdx.app.log("TRACE", "FLAG");
                break;
            }
        }
    }

    @Override
	public void dispose ()
    {
        m_layerManager.dispose();
	}

    @Override
    public void hide() {

    }

    @Override
    public void OnEnemySpawned(int id, float x, float y, int level)
    {
        Gdx.app.log("NETWORK", "Enemy (" + id + ") has spawned at " + x + ":" + y);
        m_networkManager.notifySpawn(NetworkProtocol.Owner.ENEMY, id, x, y, level);
    }

    @Override
    public void OnEnemyUpdate(int id, float x, float y)
    {
        Gdx.app.log("NETWORK", "Enemy (" + id + ") moved with velocity " + x + ":" + y);
        m_networkManager.notifyUpdate(NetworkProtocol.Owner.ENEMY, id, x, y);
    }
}
