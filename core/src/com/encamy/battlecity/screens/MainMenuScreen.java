package com.encamy.battlecity.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.encamy.battlecity.BattleCityGame;
import com.encamy.battlecity.Settings;
import com.encamy.battlecity.network.AndroidInterface;
import com.encamy.battlecity.network.NetworkManager;

import static com.encamy.battlecity.Settings.SCREEN_HEIGHT;
import static com.encamy.battlecity.Settings.SCREEN_WIDTH;

public class MainMenuScreen implements Screen, InputProcessor
{
    private OrthographicCamera m_camera;
    private Texture background;
    private Game m_game;
    private SpriteBatch m_spriteBatch;
    private AndroidInterface m_androidAPI;

    public MainMenuScreen(BattleCityGame game, AndroidInterface androidAPI)
    {
        m_game = game;
        m_androidAPI = androidAPI;
    }

    @Override
    public void show()
    {
        m_camera = new OrthographicCamera();
        m_camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT);
        m_spriteBatch = new SpriteBatch();
        background = new Texture("background.jpg");
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta)
    {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        m_camera.update();

        m_spriteBatch.setProjectionMatrix(m_camera.combined);

        m_spriteBatch.begin();
        m_spriteBatch.draw(background, 0, 0);
        m_spriteBatch.end();
    }

    @Override
    public void resize(int width, int height)
    {

    }

    @Override
    public void pause()
    {

    }

    @Override
    public void resume()
    {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose()
    {
        background.dispose();
    }

    @Override
    public boolean keyDown(int keycode)
    {
        return false;
    }

    @Override
    public boolean keyUp(int keycode)
    {
        return false;
    }

    @Override
    public boolean keyTyped(char character)
    {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button)
    {
        if (screenX > 0.33f * Gdx.graphics.getWidth() && screenX < 0.66f * Gdx.graphics.getWidth())
        {
            if (screenY > 0.63f * Gdx.graphics.getHeight() && screenY < 0.76f * Gdx.graphics.getHeight())
            {
                // 1 Player
                Gdx.app.log("INFO", "1 PLAYER");
                m_game.setScreen(new GameScreen());
            }
            else if (screenY > 0.76f * Gdx.graphics.getHeight() && screenY < 0.86f * Gdx.graphics.getHeight())
            {
                // 2 Player
                NetworkManager networkManager = new NetworkManager(m_androidAPI);
                Gdx.app.log("INFO", "2 PLAYER");
            }
        }
        else
        {
            Gdx.app.log("TRACE", screenX + ":" + screenY);
        }

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button)
    {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer)
    {
        return false;
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
