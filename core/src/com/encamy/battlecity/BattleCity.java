package com.encamy.battlecity;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

public class BattleCity extends ApplicationAdapter
{
	//SpriteBatch batch;
    OrthographicCamera m_camera;
    TiledMap m_tileMap;
    OrthogonalTiledMapRenderer m_renderer;

	@Override
	public void create ()
    {
		//batch = new SpriteBatch();

		m_camera = new OrthographicCamera();
		m_camera.setToOrtho(false, 1067, 540);
		m_camera.viewportWidth = 1067;
        m_camera.viewportHeight = 540;

		m_tileMap = new TmxMapLoader().load("general_map.tmx");
		m_renderer = new OrthogonalTiledMapRenderer(m_tileMap, 0.5f);
	}

	@Override
	public void render ()
    {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //m_camera.update();
        m_renderer.setView(m_camera);
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
