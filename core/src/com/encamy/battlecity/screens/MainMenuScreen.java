package com.encamy.battlecity.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.encamy.battlecity.BattleCityGame;
import com.encamy.battlecity.Settings;

import static com.encamy.battlecity.Settings.SCREEN_HEIGHT;
import static com.encamy.battlecity.Settings.SCREEN_WIDTH;

public class MainMenuScreen implements Screen, InputProcessor
{
    private OrthographicCamera m_camera;
    private Texture background;
    private Game m_game;

    public MainMenuScreen(BattleCityGame game)
    {
        m_game = game;
    }

    @Override
    public void show()
    {
        m_camera = new OrthographicCamera();
        m_camera.setToOrtho(false, SCREEN_WIDTH / Settings.PPM, SCREEN_HEIGHT / Settings.PPM);
        background = new Texture("background.jpg");
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta)
    {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        m_camera.update();
        SpriteBatch spriteBatch = new SpriteBatch();
        spriteBatch.begin();
        spriteBatch.draw(background, 0, 0);
        spriteBatch.end();
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
        if (screenX > 430 && screenX < 850)
        {
            if (screenY > 460 && screenY < 550)
            {
                // 1 Player
                Gdx.app.log("INFO", "1 PLAYER");
                m_game.setScreen(new GameScreen());
            }
            else if (screenY > 550 && screenY < 620)
            {
                // 2 Player
                Gdx.app.log("INFO", "2 PLAYER");
            }
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
