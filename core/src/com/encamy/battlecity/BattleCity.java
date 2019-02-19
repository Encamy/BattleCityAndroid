package com.encamy.battlecity;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.ContactFilter;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.encamy.battlecity.entities.EnemyFactory;

import static com.encamy.battlecity.Settings.APPLICATION_VERSION;
import static com.encamy.battlecity.Settings.SCREEN_HEIGHT;
import static com.encamy.battlecity.Settings.SCREEN_WIDTH;

public class BattleCity extends ApplicationAdapter implements InputProcessor {
	//SpriteBatch batch;
	private OrthographicCamera m_camera;
    private OrthogonalTiledMapRenderer m_renderer;

    private EnemyFactory m_enemyFactory;

    private Box2DDebugRenderer m_b2drenderer;
    private World m_world;
    private LayerManager m_layerManager;
    private static final int CURRENT_PLAYER = 1;

	@Override
	public void create ()
    {
		Gdx.app.log("Info", this.getClass().getName() + " started. v" + APPLICATION_VERSION);
		Gdx.app.log("Info", "platform = " + Gdx.app.getType().name());

		m_camera = new OrthographicCamera();
		m_camera.setToOrtho(false, SCREEN_WIDTH / Settings.PPM, SCREEN_HEIGHT / Settings.PPM);

        Box2D.init();
        m_b2drenderer = new Box2DDebugRenderer();
        m_b2drenderer.setDrawVelocities(true);
        m_world = new World(new Vector2(0,0), true);
        CollisionListener listener = new CollisionListener(m_world);
        m_world.setContactListener(listener);

        m_layerManager = new LayerManager(m_world);
        m_layerManager.loadLevel("general_map.tmx");

        m_renderer = new OrthogonalTiledMapRenderer(m_layerManager.getTileMap(), 1 / Settings.PPM);
        m_renderer.setView(m_camera);

        m_layerManager.loadPlayer(CURRENT_PLAYER);

        m_enemyFactory = new EnemyFactory(
                m_layerManager.getTileMap().getLayers().get("EnemySpawns").getObjects(),
                m_layerManager.getAtlas(),
                m_world,
                m_layerManager.getPlayer(CURRENT_PLAYER).getSteeringEntity()
        );

		Gdx.input.setInputProcessor(this);
	}

    @Override
	public void render ()
    {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		m_camera.update();
		m_enemyFactory.update(Gdx.graphics.getDeltaTime());

        SpriteBatch spriteBatch = new SpriteBatch();
        spriteBatch.begin();
        m_layerManager.getPlayer(CURRENT_PLAYER).draw(spriteBatch);
		m_enemyFactory.draw(spriteBatch);
        spriteBatch.end();

        m_renderer.render();

        m_world.step(Gdx.graphics.getDeltaTime(), 6, 2);
        m_b2drenderer.render(m_world, m_camera.projection);

        processHitted();
    }

    private void processHitted()
    {
        Array<Body> bodies = new Array<Body>();
        m_world.getBodies(bodies);

        for (Body body : bodies)
        {
            if (body.getUserData() != null)
            {
                if ((body.getUserData()).equals(Settings.ObjectType.SHOTTED))
                {
                    Gdx.app.log("Trace", "Body was hitted by bullet. Destroying");
                    m_world.destroyBody(body);
                }
            }
        }
    }

    @Override
	public void dispose ()
    {
        m_layerManager.dispose();
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

    @Override
    public boolean keyDown(int keycode)
    {
        switch (keycode)
        {
            case Input.Keys.W:
                m_layerManager.getPlayer(CURRENT_PLAYER).setVelocity(0.0f, m_layerManager.getPlayer(CURRENT_PLAYER).getSpeed());
                break;
            case Input.Keys.A:
                m_layerManager.getPlayer(CURRENT_PLAYER).setVelocity(-1 * m_layerManager.getPlayer(CURRENT_PLAYER).getSpeed(), 0.0f);
                break;
            case Input.Keys.D:
                m_layerManager.getPlayer(CURRENT_PLAYER).setVelocity(m_layerManager.getPlayer(CURRENT_PLAYER).getSpeed(), 0.0f);
                break;
            case Input.Keys.S:
                m_layerManager.getPlayer(CURRENT_PLAYER).setVelocity(0.0f, -1 * m_layerManager.getPlayer(CURRENT_PLAYER).getSpeed());
                break;
            case Input.Keys.SPACE:
            case Input.Keys.ENTER:
            case Input.Buttons.LEFT:
                m_layerManager.getPlayer(CURRENT_PLAYER).fire();
                break;
        }

        return true;
    }

    @Override
    public boolean keyUp(int keycode)
    {
        if (keycode == Input.Keys.W ||
            keycode == Input.Keys.S ||
            keycode == Input.Keys.A ||
            keycode == Input.Keys.D)
        {
            m_layerManager.getPlayer(CURRENT_PLAYER).setVelocity(0.0f, 0.0f);
        }

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
        m_layerManager.getPlayer(CURRENT_PLAYER).setVelocity(0.0f, 0.0f);
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
            m_layerManager.getPlayer(CURRENT_PLAYER).setVelocity(0, m_layerManager.getPlayer(CURRENT_PLAYER).getSpeed());
            return  true;
        }

        if (screenY > Gdx.graphics.getHeight() * 0.6f)
        {
            m_layerManager.getPlayer(CURRENT_PLAYER).setVelocity(0, -1 * m_layerManager.getPlayer(CURRENT_PLAYER).getSpeed());
            return  true;
        }

        if (screenX < Gdx.graphics.getWidth() * 0.15f)
        {
            m_layerManager.getPlayer(CURRENT_PLAYER).setVelocity(-1 * m_layerManager.getPlayer(CURRENT_PLAYER).getSpeed(), 0.0f);
            return  true;
        }

        if (screenX > Gdx.graphics.getWidth() * 0.15f)
        {
            m_layerManager.getPlayer(CURRENT_PLAYER).setVelocity(m_layerManager.getPlayer(CURRENT_PLAYER).getSpeed(), 0.0f);
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
