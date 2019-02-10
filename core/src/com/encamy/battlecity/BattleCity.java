package com.encamy.battlecity;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.encamy.battlecity.entities.EnemyFactory;
import com.encamy.battlecity.entities.Player;

import java.util.Iterator;

import static com.encamy.battlecity.Settings.ANIMATION_FRAME_DURATION;
import static com.encamy.battlecity.Settings.APPLICATION_VERSION;
import static com.encamy.battlecity.Settings.SCREEN_HEIGHT;
import static com.encamy.battlecity.Settings.SCREEN_WIDTH;

public class BattleCity extends ApplicationAdapter
{
	//SpriteBatch batch;
	private OrthographicCamera m_camera;
	private Viewport m_viewport;
	private TiledMap m_tileMap;
    private OrthogonalTiledMapRenderer m_renderer;

    private Player m_player;
    private EnemyFactory m_enemyFactory;

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

        Gdx.app.log("Trace", "Loading collisions layer");

		MapObjects walls  = m_tileMap.getLayers().get("Collisions").getObjects();
		
		m_player = new Player(left, top, right, bottom, walls);
		m_player.setPosition(spawnpoint.x, spawnpoint.y);

        m_enemyFactory = new EnemyFactory(m_tileMap.getLayers().get("EnemySpawns").getObjects(), atlas);

		Gdx.input.setInputProcessor(m_player);
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
}
