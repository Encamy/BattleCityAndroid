package com.encamy.battlecity;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

import java.util.Iterator;

import sun.rmi.runtime.Log;

public class BattleCity extends ApplicationAdapter
{
	//SpriteBatch batch;
	private OrthographicCamera m_camera;
	private TiledMap m_tileMap;
    private OrthogonalTiledMapRenderer m_renderer;

    private Player m_player;

	private static final float FRAME_DURATION = 0.20f;

	@Override
	public void create ()
    {
		//batch = new SpriteBatch();

		m_camera = new OrthographicCamera();
		m_camera.setToOrtho(false, 1067, 540);
		m_camera.viewportWidth = 1067;
        m_camera.viewportHeight = 540;

		m_tileMap = new TmxMapLoader().load("general_map.tmx");
		m_renderer = new OrthogonalTiledMapRenderer(m_tileMap, 0.8f);

		TextureAtlas atlas = new TextureAtlas();

		Iterator<TiledMapTile> tiles = m_tileMap.getTileSets().getTileSet("entities").iterator();
		while (tiles.hasNext())
		{
			TiledMapTile tile = tiles.next();
			if (tile.getProperties().containsKey("tank_name") && tile.getProperties().get("tank_name", String.class).equals("yellow"))
			{
				if (tile.getProperties().containsKey("tank_type") && tile.getProperties().get("tank_type", String.class).equals("1"))
				{
					if (!tile.getProperties().containsKey("direction"))
					{
						Gdx.app.log("ERROR", "Invalid tile format");
					}

					atlas.addRegion(tile.getProperties().get("direction", String.class), tile.getTextureRegion());
				}
			}
		}

		Animation left = new Animation(FRAME_DURATION, atlas.findRegions("left"));
		Animation top = new Animation(FRAME_DURATION, atlas.findRegions("top"));
		Animation right = new Animation(FRAME_DURATION, atlas.findRegions("right"));
		Animation bottom = new Animation(FRAME_DURATION, atlas.findRegions("bottom"));
		left.setPlayMode(Animation.PlayMode.LOOP);
		top.setPlayMode(Animation.PlayMode.LOOP);
		right.setPlayMode(Animation.PlayMode.LOOP);
		bottom.setPlayMode(Animation.PlayMode.LOOP);

		m_player = new Player(left, top, right, bottom);
		m_player.setPosition(0,0);

		Gdx.input.setInputProcessor(m_player);
	}

	@Override
	public void render ()
    {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		m_renderer.setView(m_camera);

		//m_camera.update();
		m_renderer.getBatch().begin();
		m_player.draw(m_renderer.getBatch());
		m_renderer.getBatch().end();

        m_renderer.render();

        //batch.setProjectionMatrix(m_camera.combined);
		//batch.begin();
        // ... some texture render staff
		//batch.end();
	}
	
	@Override
	public void dispose ()
    {
		//batch.dispose();
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
}
