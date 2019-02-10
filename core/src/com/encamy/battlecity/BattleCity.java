package com.encamy.battlecity;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.encamy.battlecity.entities.EnemyFactory;
import com.encamy.battlecity.entities.Player;

import java.util.Iterator;

import static com.encamy.battlecity.Settings.ANIMATION_FRAME_DURATION;
import static com.encamy.battlecity.Settings.APPLICATION_VERSION;
import static com.encamy.battlecity.Settings.SCREEN_HEIGHT;
import static com.encamy.battlecity.Settings.SCREEN_WIDTH;

public class BattleCity extends ApplicationAdapter implements InputProcessor {
	//SpriteBatch batch;
	private OrthographicCamera m_camera;
	private TiledMap m_tileMap;
    private OrthogonalTiledMapRenderer m_renderer;

    private Player m_player;
    private EnemyFactory m_enemyFactory;

    private Box2DDebugRenderer m_b2drenderer;
    private World m_world;

	@Override
	public void create ()
    {
		Gdx.app.log("Info", this.getClass().getName() + " started. v" + APPLICATION_VERSION);
		Gdx.app.log("Info", "platform = " + Gdx.app.getType().name());

		m_camera = new OrthographicCamera();
		m_camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT);

		m_tileMap = new TmxMapLoader().load("general_map.tmx");
		m_renderer = new OrthogonalTiledMapRenderer(m_tileMap);
        m_renderer.setView(m_camera);

        Box2D.init();
        m_b2drenderer = new Box2DDebugRenderer();
        m_world = new World(new Vector2(0,0), true);

        Gdx.app.log("Trace", "Loading animations");

		TextureAtlas atlas = load_atlas(m_tileMap);

		Animation left = new Animation(ANIMATION_FRAME_DURATION, atlas.findRegions("yellow1_left"));
		Animation top = new Animation(ANIMATION_FRAME_DURATION, atlas.findRegions("yellow1_top"));
		Animation right = new Animation(ANIMATION_FRAME_DURATION, atlas.findRegions("yellow1_right"));
		Animation bottom = new Animation(ANIMATION_FRAME_DURATION, atlas.findRegions("yellow1_bottom"));
		left.setPlayMode(Animation.PlayMode.LOOP);
		top.setPlayMode(Animation.PlayMode.LOOP);
		right.setPlayMode(Animation.PlayMode.LOOP);
		bottom.setPlayMode(Animation.PlayMode.LOOP);

        Gdx.app.log("Trace", "Loading player sprites");

		Vector2 spawnpoint = new Vector2();
		for (MapObject object : m_tileMap.getLayers().get("Objects").getObjects())
		{
			if (object instanceof RectangleMapObject &&
				object.getProperties().containsKey("type") &&
				object.getProperties().get("type", String.class).equals("spawn_player1"))
			{
				spawnpoint.x = ((RectangleMapObject)object).getRectangle().getX();
				spawnpoint.y = ((RectangleMapObject)object).getRectangle().getY();
			}
		}

		MapObjects walls  = m_tileMap.getLayers().get("Collisions").getObjects();
        loadCollission(walls);

        Body playerBody = Box2dHelpers.createPlayerBox(
                m_world,
                spawnpoint.x,
                spawnpoint.y,
                54, 54);

        m_player = new Player(left, top, right, bottom, playerBody);
        m_enemyFactory = new EnemyFactory(m_tileMap.getLayers().get("EnemySpawns").getObjects(), atlas);

		Gdx.input.setInputProcessor(this);
	}

    @Override
	public void render ()
    {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		m_camera.update();
		m_enemyFactory.update(Gdx.graphics.getDeltaTime());

		m_renderer.getBatch().begin();
		m_player.draw(m_renderer.getBatch());
		m_enemyFactory.draw(m_renderer.getBatch());
		m_renderer.getBatch().end();

        m_renderer.render();

        m_world.step(1/60f, 6, 2);
        m_b2drenderer.render(m_world, m_camera.projection);
    }
	
	@Override
	public void dispose ()
    {
		m_tileMap.dispose();
		m_renderer.dispose();
	}

	@Override
	public void pause()
    {
		super.pause();
	}

	@Override
	public void resume()
    {
		super.resume();
	}

	private void loadCollission(MapObjects walls)
    {
        Gdx.app.log("Trace", "Loading collisions layer");
        for (MapObject object : walls)
        {
            if (object instanceof RectangleMapObject)
            {
                Rectangle rectangle = ((RectangleMapObject) object).getRectangle();

                Box2dHelpers.createBox(
                        m_world,
                        rectangle.x,
                        rectangle.y,
                        rectangle.width,
                        rectangle.height,
                        true);
            }
        }
    }

	private TextureAtlas load_atlas(TiledMap tiledMap)
	{
		TextureAtlas atlas = new TextureAtlas();

		Iterator<TiledMapTile> tiles = tiledMap.getTileSets().getTileSet("entities").iterator();
		while (tiles.hasNext())
		{
			TiledMapTile tile = tiles.next();
			if (tile.getProperties().containsKey("tank_name") && tile.getProperties().containsKey("tank_type") && tile.getProperties().containsKey("direction"))
			{
					String tank_name = tile.getProperties().get("tank_name", String.class);
					String tank_type = tile.getProperties().get("tank_type", String.class);
					String direction = tile.getProperties().get("direction", String.class);

					atlas.addRegion(tank_name + tank_type + "_" + direction, tile.getTextureRegion());
			}
		}

		return atlas;
	}

    @Override
    public boolean keyDown(int keycode)
    {
        switch (keycode)
        {
            case Input.Keys.W:
                m_player.setVelocity(0.0f, m_player.getSpeed());
                break;
            case Input.Keys.A:
                m_player.setVelocity(-1 * m_player.getSpeed(), 0.0f);
                break;
            case Input.Keys.D:
                m_player.setVelocity(m_player.getSpeed(), 0.0f);
                break;
            case Input.Keys.S:
                m_player.setVelocity(0.0f, -1 * m_player.getSpeed());
                break;
        }

        return true;
    }

    @Override
    public boolean keyUp(int keycode)
    {
        m_player.setVelocity(0.0f, 0.0f);

        return true;
    }

    @Override
    public boolean keyTyped(char character)
    {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button)
    {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button)
    {
        m_player.setVelocity(0.0f, 0.0f);
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer)
    {
        if (screenX > Gdx.graphics.getWidth() * 0.3f)
        {
            return false;
        }

        if (screenY < Gdx.graphics.getHeight() * 0.3f)
        {
            m_player.setVelocity(0, m_player.getSpeed());
            return  true;
        }

        if (screenY > Gdx.graphics.getHeight() * 0.6f)
        {
            m_player.setVelocity(0, -1 * m_player.getSpeed());
            return  true;
        }

        if (screenX < Gdx.graphics.getWidth() * 0.15f)
        {
            m_player.setVelocity(-1 * m_player.getSpeed(), 0.0f);
            return  true;
        }

        if (screenX > Gdx.graphics.getWidth() * 0.15f)
        {
            m_player.setVelocity(m_player.getSpeed(), 0.0f);
            return  true;
        }

        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY)
    {
        return false;
    }

    @Override
    public boolean scrolled(int amount)
    {
        return false;
    }
}
