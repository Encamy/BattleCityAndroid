package com.encamy.battlecity.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.encamy.battlecity.BulletManager;
import com.encamy.battlecity.CollisionListener;
import com.encamy.battlecity.EventQueue;
import com.encamy.battlecity.LayerManager;
import com.encamy.battlecity.Settings;
import com.encamy.battlecity.entities.Enemy;
import com.encamy.battlecity.entities.EnemyFactory;
import com.encamy.battlecity.network.NetworkManager;
import com.encamy.battlecity.protobuf.NetworkProtocol;
import com.encamy.battlecity.utils.Dictionary;
import com.encamy.battlecity.utils._Placeholder;
import com.encamy.battlecity.utils.utils;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import static com.encamy.battlecity.Settings.SCREEN_HEIGHT;
import static com.encamy.battlecity.Settings.SCREEN_WIDTH;

public class GameScreen implements
        Screen,
        Settings.OnEnemyMovedCallback,
        Settings.OnEnemySpawnedCallback,
        Settings.OnMessageReceivedCallback,
        Settings.OnEnemyFiredCallback,
        EventQueue.EventQueueCallback
{

	private OrthographicCamera m_camera;
	private OrthographicCamera m_debugCamera;
    private SpriteBatch m_spriteBatch;

    private EnemyFactory m_enemyFactory;

    private Box2DDebugRenderer m_b2drenderer;
    private World m_world;
    private LayerManager m_layerManager;
    private BulletManager m_bulletManager;
    private int m_currentPlayer = 1;
    private boolean m_freezeWorld = false;

    private NetworkManager m_networkManager;
    private EventQueue m_eventQueue;
    private Settings.GameState m_state;

    private Texture m_scoreBackground;

    private BitmapFont m_bitmapFont;
    private GlyphLayout m_glyphLayout;

    private Vector<Integer> m_destroyedFirstPlayer;
    private Vector<Integer> m_destroyedSecondPlayer;
    private Vector<Integer> m_enemyCost;

    public GameScreen(NetworkManager networkManager)
    {
        m_networkManager = networkManager;
    }

    @Override
    public void show()
    {
		Gdx.app.log("Info", this.getClass().getName() + " started");
		Gdx.app.log("Info", "platform = " + Gdx.app.getType().name());

        m_eventQueue = new EventQueue();

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

        boolean isMaster = true;

        if (m_networkManager != null)
        {
            isMaster  = m_networkManager.isServer();
            m_networkManager.setOnMessageReceivedCallback(this);
        }

        m_layerManager = new LayerManager(m_world, m_bulletManager, m_networkManager);

        if (!isMaster)
        {
            m_currentPlayer = 2;
        }

        m_state = Settings.GameState.LOAD_STATE;
        m_layerManager.loadLevel("general_map.tmx");
        m_layerManager.loadPlayer(m_currentPlayer, false);

        m_bulletManager.setAtlas(m_layerManager.getAtlas());

        m_enemyFactory = new EnemyFactory(
                m_layerManager.getTileMap().getLayers().get("EnemySpawns").getObjects(),
                m_layerManager.getAtlas(),
                m_world,
                m_layerManager.getPlayer(m_currentPlayer).getSteeringEntity(),
                m_bulletManager,
                isMaster
        );

        if (isMaster  && m_networkManager != null)
        {
            m_enemyFactory.setOnSpawnedCallback(this);
            m_enemyFactory.setOnUpdateCallback(this);
            m_enemyFactory.setOnEnemyFiredCallback(this);
        }

        m_scoreBackground = new Texture("score_background.png");
        m_glyphLayout = new GlyphLayout();
        m_bitmapFont = new BitmapFont(Gdx.files.internal("bcFont.fnt"), Gdx.files.internal("bcFont.png"), false);
        m_bitmapFont.getData().setScale(0.75f);

        m_state = Settings.GameState.PLAY_STATE;

        m_destroyedFirstPlayer = new Vector<>();
        m_destroyedSecondPlayer = new Vector<>();
        m_enemyCost = new Vector<>();
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
        //m_layerManager.getPlayer(m_currentPlayer).draw(m_spriteBatch, m_freezeWorld);
        m_layerManager.updatePlayers(m_spriteBatch, m_freezeWorld);
        m_enemyFactory.draw(m_spriteBatch, m_freezeWorld);
        m_layerManager.drawWalls(m_spriteBatch);
        m_layerManager.drawUI(m_spriteBatch, m_enemyFactory.getEnemiesLeft());
        //Gdx.app.log("TRACE", "Map width = " + m_layerManager.getMapSize().x + " height = " + m_layerManager.getMapSize().y);
        m_bulletManager.update(m_spriteBatch);

        if (m_state == Settings.GameState.SCORE_STATE || m_state == Settings.GameState.GAME_OVER_STATE)
        {
            m_spriteBatch.draw(m_scoreBackground, 0, 0);

            if (m_layerManager.getPlayer(1) != null)
            {
                Vector<Integer> countFirstPlayer = new Vector<>();
                int scoreFirstPlayer = 0;

                if (m_networkManager != null && !m_networkManager.isServer())
                {
                    countFirstPlayer = m_destroyedFirstPlayer;
                    scoreFirstPlayer =
                            m_destroyedFirstPlayer.get(0) * m_enemyCost.get(0) +
                            m_destroyedFirstPlayer.get(1) * m_enemyCost.get(1) +
                            m_destroyedFirstPlayer.get(2) * m_enemyCost.get(2) +
                            m_destroyedFirstPlayer.get(3) * m_enemyCost.get(3);
                }
                else
                {
                    Dictionary<Integer, Integer> dictionary1P = m_layerManager.getPlayer(1).getDestroyedEnemies();

                    countFirstPlayer.add(0);
                    countFirstPlayer.add(0);
                    countFirstPlayer.add(0);
                    countFirstPlayer.add(0);

                    for(int i = 0; i < dictionary1P.size(); i++)
                    {
                        Dictionary.Entry entry = dictionary1P.getAt(i);
                        int id = (int)entry.getKey();
                        countFirstPlayer.set(id, countFirstPlayer.get(id) + 1);
                        scoreFirstPlayer += (int)entry.getValue();
                    }
                }

                m_glyphLayout.setText(m_bitmapFont, Integer.toString(countFirstPlayer.get(0)));
                m_bitmapFont.draw(m_spriteBatch, m_glyphLayout, 0.3f * Settings.SCREEN_WIDTH, Settings.SCREEN_HEIGHT - Settings.SCREEN_HEIGHT * 0.42f);

                m_glyphLayout.setText(m_bitmapFont, Integer.toString(countFirstPlayer.get(1)));
                m_bitmapFont.draw(m_spriteBatch, m_glyphLayout, 0.3f * Settings.SCREEN_WIDTH, Settings.SCREEN_HEIGHT - Settings.SCREEN_HEIGHT * 0.52f);

                m_glyphLayout.setText(m_bitmapFont, Integer.toString(countFirstPlayer.get(2)));
                m_bitmapFont.draw(m_spriteBatch, m_glyphLayout, 0.3f * Settings.SCREEN_WIDTH, Settings.SCREEN_HEIGHT - Settings.SCREEN_HEIGHT * 0.62f);

                m_glyphLayout.setText(m_bitmapFont, Integer.toString(countFirstPlayer.get(3)));
                m_bitmapFont.draw(m_spriteBatch, m_glyphLayout, 0.3f * Settings.SCREEN_WIDTH, Settings.SCREEN_HEIGHT - Settings.SCREEN_HEIGHT * 0.72f);

                m_glyphLayout.setText(m_bitmapFont, Integer.toString(scoreFirstPlayer));
                m_bitmapFont.draw(m_spriteBatch, m_glyphLayout, 0.45f * Settings.SCREEN_WIDTH, Settings.SCREEN_HEIGHT - Settings.SCREEN_HEIGHT * 0.90f);
            }

            if (m_layerManager.getPlayer(2) != null)
            {
                Vector<Integer> countSecondPlayer = new Vector<>();
                int scoreSecondPlayer = 0;

                if (m_networkManager != null && !m_networkManager.isServer())
                {
                    countSecondPlayer = m_destroyedSecondPlayer;
                    scoreSecondPlayer =
                            m_destroyedSecondPlayer.get(0) * m_enemyCost.get(0) +
                            m_destroyedSecondPlayer.get(1) * m_enemyCost.get(1) +
                            m_destroyedSecondPlayer.get(2) * m_enemyCost.get(2) +
                            m_destroyedSecondPlayer.get(3) * m_enemyCost.get(3);
                }
                else
                {
                    Dictionary<Integer, Integer> dictionary2P = m_layerManager.getPlayer(2).getDestroyedEnemies();

                    countSecondPlayer.add(0);
                    countSecondPlayer.add(0);
                    countSecondPlayer.add(0);
                    countSecondPlayer.add(0);
                    for (int i = 0; i < dictionary2P.size(); i++)
                    {
                        Dictionary.Entry entry = dictionary2P.getAt(i);
                        int id = (int)entry.getKey();
                        countSecondPlayer.set(id, countSecondPlayer.get(id) + 1);
                        scoreSecondPlayer += (int)entry.getValue();
                    }
                }

                m_glyphLayout.setText(m_bitmapFont, Integer.toString(countSecondPlayer.get(0)));
                m_bitmapFont.draw(m_spriteBatch, m_glyphLayout, 0.70f * Settings.SCREEN_WIDTH, Settings.SCREEN_HEIGHT - Settings.SCREEN_HEIGHT * 0.42f);

                m_glyphLayout.setText(m_bitmapFont, Integer.toString(countSecondPlayer.get(1)));
                m_bitmapFont.draw(m_spriteBatch, m_glyphLayout, 0.70f * Settings.SCREEN_WIDTH, Settings.SCREEN_HEIGHT - Settings.SCREEN_HEIGHT * 0.52f);

                m_glyphLayout.setText(m_bitmapFont, Integer.toString(countSecondPlayer.get(2)));
                m_bitmapFont.draw(m_spriteBatch, m_glyphLayout, 0.70f * Settings.SCREEN_WIDTH, Settings.SCREEN_HEIGHT - Settings.SCREEN_HEIGHT * 0.62f);

                m_glyphLayout.setText(m_bitmapFont, Integer.toString(countSecondPlayer.get(3)));
                m_bitmapFont.draw(m_spriteBatch, m_glyphLayout, 0.70f * Settings.SCREEN_WIDTH, Settings.SCREEN_HEIGHT - Settings.SCREEN_HEIGHT * 0.72f);

                m_glyphLayout.setText(m_bitmapFont, Integer.toString(scoreSecondPlayer));
                m_bitmapFont.draw(m_spriteBatch, m_glyphLayout, 0.80f * Settings.SCREEN_WIDTH, Settings.SCREEN_HEIGHT - Settings.SCREEN_HEIGHT * 0.90f);
            }
        }

        m_spriteBatch.end();
        if (!m_freezeWorld)
        {
            m_world.step(Gdx.graphics.getDeltaTime(), 6, 2);
        }

        //m_b2drenderer.render(m_world, m_debugCamera.projection);

        processHitted();

        m_eventQueue.poll(this);
    }

    @Override
    public void resize(int width, int height)
    {

    }

    @Override
    public void pause()
    {
        //m_freezeWorld = true;
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
            //Gdx.app.log("TRACE", type.toString());

            if (type.contains(Settings.ObjectType.BULLET ) && !type.contains(Settings.ObjectType.GRASS))
            {
                //Gdx.app.log("TRACE", "Handled");
                m_bulletManager.removeBullet(body);
            }
            else if (type.contains(Settings.ObjectType.ENEMY))
            {
                if (!type.contains(Settings.ObjectType.ENEMY_OWNER))
                {
                    if (m_networkManager != null && !m_networkManager.isServer())
                    {
                        return;
                    }
                    //Gdx.app.log("TRACE", "Handled");

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

                            if (m_networkManager != null)
                            {
                                m_networkManager.notifyDestroyed(NetworkProtocol.Owner.ENEMY, enemy.getId());
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
                    //Gdx.app.log("TRACE", "Handled");
                    body.setUserData(EnumSet.of(Settings.ObjectType.ENEMY));
                }
            }
            else if (type.contains(Settings.ObjectType.PLAYER))
            {
                if (m_layerManager.getPlayer(1) != null)
                {
                    if (m_layerManager.getPlayer(1).getBody() == body) {
                       // Gdx.app.log("TRACE", "Handled");
                        m_layerManager.getPlayer(1).hit();
                        body.setUserData(EnumSet.of(Settings.ObjectType.PLAYER));
                    }
                }
                else if (m_layerManager.getPlayer(2) != null)
                {
                    if (m_layerManager.getPlayer(2).getBody() == body)
                    {
                        //Gdx.app.log("TRACE", "Handled");
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
                if (m_networkManager != null && !m_networkManager.isServer())
                {
                    return;
                }
                //Gdx.app.log("TRACE", "handled");
                _Placeholder<Integer> ref_id = new _Placeholder();
                boolean destroyed = m_layerManager.hit(body, type, ref_id);
                if (!destroyed)
                {
                    body.setUserData(EnumSet.of(Settings.ObjectType.BRICK_WALL));
                }
                else if (m_networkManager != null && m_networkManager.isServer())
                {
                    m_networkManager.notifyDestroyed(NetworkProtocol.Owner.WALL, ref_id.variable);
                }
            }
            else if (type.contains(Settings.ObjectType.STONE_WALL))
            {
                if (m_networkManager != null && !m_networkManager.isServer())
                {
                    return;
                }
                //Gdx.app.log("TRACE", "handled");
                _Placeholder<Integer> ref_id = new _Placeholder();
                boolean destroyed = m_layerManager.hit(body, type, ref_id);
                if (!destroyed)
                {
                    body.setUserData(EnumSet.of(Settings.ObjectType.STONE_WALL));
                }
                else if (m_networkManager != null && m_networkManager.isServer())
                {
                    m_networkManager.notifyDestroyed(NetworkProtocol.Owner.WALL, ref_id.variable);
                }
            }
            else if (type.contains(Settings.ObjectType.GRASS))
            {
                _Placeholder<Integer> ref_id = new _Placeholder();
                boolean destroyed = m_layerManager.hit(body, type, ref_id);
                if (!destroyed)
                {
                    //Gdx.app.log("TRACE", "handled");
                }
                else if (m_networkManager != null && m_networkManager.isServer())
                {
                    m_networkManager.notifyDestroyed(NetworkProtocol.Owner.WALL, ref_id.variable);
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
                //Gdx.app.log("TRACE", "handled");
                _Placeholder<Integer> ref_id = new _Placeholder();
                boolean destroyed = m_layerManager.hit(body, type, ref_id);
                if (!destroyed)
                {
                    body.setUserData(EnumSet.of(Settings.ObjectType.WATER));
                }
                else if (m_networkManager != null && m_networkManager.isServer())
                {
                    m_networkManager.notifyDestroyed(NetworkProtocol.Owner.WALL, ref_id.variable);
                }
            }
            else if (type.contains(Settings.ObjectType.WALL))
            {
                //Gdx.app.log("TRACE", "Handled");
                body.setUserData(EnumSet.of(Settings.ObjectType.WALL));
            }
            else if (type.contains(Settings.ObjectType.FLAG ))
            {
                _Placeholder<Integer> ref_id = new _Placeholder();
                if (m_layerManager.hit(body, type, ref_id))
                {
                    Gdx.app.log("INFO", "GAME OVER");
                    m_state = Settings.GameState.GAME_OVER_STATE;
                    m_freezeWorld = true;

                    Dictionary<Integer, Integer> dictionary = m_layerManager.getPlayer(1).getDestroyedEnemies();
                    for(int i = 0; i < dictionary.size(); i++)
                    {
                        Dictionary.Entry entry = dictionary.getAt(i);
                        Gdx.app.log("TRACE","level = " + entry.getKey() + " score = " + entry.getValue());
                    }

                    if (m_networkManager != null && m_networkManager.isServer())
                    {
                        m_networkManager.notifyDestroyed(NetworkProtocol.Owner.WALL, ref_id.variable);

                        Vector<Integer> countFirstPlayer = new Vector<>();
                        Vector<Integer> countSecondPlayer = new Vector<>();
                        Vector<Integer> enemyCost = new Vector<>();
                        enemyCost.add(0);
                        enemyCost.add(0);
                        enemyCost.add(0);
                        enemyCost.add(0);

                        if (m_layerManager.getPlayer(1) != null)
                        {
                            Dictionary<Integer, Integer> dictionaryDestroyed = m_layerManager.getPlayer(1).getDestroyedEnemies();
                            countFirstPlayer.add(0);
                            countFirstPlayer.add(0);
                            countFirstPlayer.add(0);
                            countFirstPlayer.add(0);

                            for(int i = 0; i < dictionaryDestroyed.size(); i++)
                            {
                                Dictionary.Entry entry = dictionaryDestroyed.getAt(i);
                                int id = (int)entry.getKey();
                                countFirstPlayer.set(id, countFirstPlayer.get(id) + 1);
                                enemyCost.set(id, (int)entry.getValue());
                            }
                        }

                        if (m_layerManager.getPlayer(2) != null)
                        {
                            Dictionary<Integer, Integer> dictionaryDestroyed = m_layerManager.getPlayer(2).getDestroyedEnemies();

                            countSecondPlayer.add(0);
                            countSecondPlayer.add(0);
                            countSecondPlayer.add(0);
                            countSecondPlayer.add(0);

                            for(int i = 0; i < dictionaryDestroyed.size(); i++)
                            {
                                Dictionary.Entry entry = dictionaryDestroyed.getAt(i);
                                int id = (int)entry.getKey();
                                countSecondPlayer.set(id, countSecondPlayer.get(id) + 1);
                                enemyCost.set(id, (int)entry.getValue());
                            }
                        }

                        m_networkManager.notifyGameOver(countFirstPlayer, countSecondPlayer, enemyCost);
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
        //Gdx.app.log("NETWORK", "Enemy (" + id + ") has spawned at " + x + ":" + y);
        m_networkManager.notifySpawn(NetworkProtocol.Owner.ENEMY, id, x, y, level);
    }

    @Override
    public void OnEnemyMoved(int id, float x, float y, Settings.Direction direction)
    {
        //Gdx.app.log("NETWORK", "Enemy (" + id + ") moved with position " + x + ":" + y);
        m_networkManager.notifyMove(NetworkProtocol.Owner.ENEMY, id, x, y, utils.toNetworkDirection(direction));
    }

    @Override
    public void OnEnemyFired(int id, Settings.Direction direction)
    {
        //Gdx.app.log("NETWORK", "Enemy (" + id + ") fired at direction " + direction.toString());
        m_networkManager.notifyFire(NetworkProtocol.Owner.ENEMY, id, direction);
    }

    @Override
    public void OnMessageReceived(NetworkProtocol.PacketWrapper packet)
    {
        switch (packet.getWrapperCase())
        {
            case PING:
            case PONG:
                // do nothing
                break;
            case EVENT:
                m_eventQueue.addEvent(packet.getEvent());
                break;
            case WRAPPER_NOT_SET:
                Gdx.app.log("NETWORK ERROR", "Wrapper type is not set. Probably wrong packet structure");
                break;
        }
    }

    private void eventDispatcher(NetworkProtocol.Event event)
    {
        switch (event.getEventTypeCase())
        {
            case FIRE:
                {
                    NetworkProtocol.Fire fire = event.getFire();

                    if (fire.getOwner() == NetworkProtocol.Owner.ENEMY)
                    {
                        m_enemyFactory.onNetworkFire(fire.getId(), fire.getDirection());
                    }
                    else if (fire.getOwner() == NetworkProtocol.Owner.CLIENT_PLAYER ||
                             fire.getOwner() == NetworkProtocol.Owner.SERVER_PLAYER)
                    {
                        m_layerManager.onNetworkPlayerFire(fire);
                    }
                }
                break;
            case MOVE:
                {
                    NetworkProtocol.Move move = event.getMove();

                    if (move.getOwner() == NetworkProtocol.Owner.ENEMY)
                    {
                        m_enemyFactory.onNetworkMove(move.getId(), move.getX(), move.getY(), move.getDirection());
                    }

                    if (move.getOwner() == NetworkProtocol.Owner.SERVER_PLAYER && m_networkManager.isServer())
                    {
                        Gdx.app.log("NETWORK", "Got information about server player on server side. Should not happen");
                        break;
                    }

                    if (move.getOwner() == NetworkProtocol.Owner.CLIENT_PLAYER && !m_networkManager.isServer())
                    {
                        Gdx.app.log("NETWORK", "Got information about client player on client side. Should not happen");
                        break;
                    }

                    if (move.getOwner() == NetworkProtocol.Owner.SERVER_PLAYER ||
                        move.getOwner() == NetworkProtocol.Owner.CLIENT_PLAYER)
                    {
                        m_layerManager.onNetworkMove(
                                move.getOwner(),
                                move.getX(), move.getY(),
                                utils.fromNetworkDirection(move.getDirection()));
                    }
                }
                break;
            case SPAWNED:
                {
                    NetworkProtocol.Spawned spawned = event.getSpawned();

                    if (spawned.getOwner() == NetworkProtocol.Owner.ENEMY)
                    {
                        m_enemyFactory.onNetworkSpawn(spawned.getId(), spawned.getX(), spawned.getY(), spawned.getLevel());
                    }

                    if (spawned.getOwner() == NetworkProtocol.Owner.CLIENT_PLAYER ||
                        spawned.getOwner() == NetworkProtocol.Owner.SERVER_PLAYER)
                    {
                        m_layerManager.loadPlayer(spawned.getId() + 1, true);
                    }
                }
                break;
            case DESTROYED:
                {
                    NetworkProtocol.Destroyed destroyed = event.getDestroyed();

                    if (destroyed.getItem() == NetworkProtocol.Owner.WALL && !m_networkManager.isServer())
                    {
                        m_layerManager.onNetworkWallDestroy(destroyed.getItem(), destroyed.getId());
                    }
                    else if (destroyed.getItem() == NetworkProtocol.Owner.ENEMY)
                    {
                        m_enemyFactory.OnNetworkDestroyed(destroyed.getId());
                    }
                }
                break;
            case GAMEOVER:
                {
                    m_destroyedFirstPlayer.add(event.getGameover().getCountPlayer1Type0());
                    m_destroyedFirstPlayer.add(event.getGameover().getCountPlayer1Type1());
                    m_destroyedFirstPlayer.add(event.getGameover().getCountPlayer1Type2());
                    m_destroyedFirstPlayer.add(event.getGameover().getCountPlayer1Type3());

                    m_destroyedSecondPlayer.add(event.getGameover().getCountPlayer2Type0());
                    m_destroyedSecondPlayer.add(event.getGameover().getCountPlayer2Type1());
                    m_destroyedSecondPlayer.add(event.getGameover().getCountPlayer2Type2());
                    m_destroyedSecondPlayer.add(event.getGameover().getCountPlayer2Type3());

                    m_enemyCost.add(event.getGameover().getCostType0());
                    m_enemyCost.add(event.getGameover().getCostType1());
                    m_enemyCost.add(event.getGameover().getCostType2());
                    m_enemyCost.add(event.getGameover().getCostType3());

                    m_state = Settings.GameState.GAME_OVER_STATE;
                }
                break;
            case EVENTTYPE_NOT_SET:
                Gdx.app.log("NETWORK ERROR", "Wrapper type is not set. Probably wrong packet structure");
                break;
        }
    }

    @Override
    public void OnEventPoll(NetworkProtocol.Event event)
    {
        //Gdx.app.log("EVENT_QUEUE", "Polled " + event.toString());
        eventDispatcher(event);
    }
}
