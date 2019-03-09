package com.encamy.battlecity.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.encamy.battlecity.Settings;
import com.encamy.battlecity.network.AndroidInterface;
import com.encamy.battlecity.network.NetworkDevice;
import com.encamy.battlecity.network.NetworkManager;

import java.util.ArrayList;

import static com.encamy.battlecity.Settings.SCREEN_HEIGHT;
import static com.encamy.battlecity.Settings.SCREEN_WIDTH;

public class NetworkScreen implements Screen, InputProcessor, Settings.OnDeviceFoundCallback
{
    private NetworkManager m_networkManager;
    private OrthographicCamera m_camera;
    private Texture background;
    private Game m_game;
    private SpriteBatch m_spriteBatch;
    private volatile ArrayList<NetworkDevice> m_devices;

    public NetworkScreen(Game game, AndroidInterface androidInterface)
    {
        m_game = game;
        m_networkManager = new NetworkManager(androidInterface, this);
        m_devices = new ArrayList<>();
    }

    @Override
    public void show()
    {
        m_camera = new OrthographicCamera();
        m_camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT);
        m_spriteBatch = new SpriteBatch();
        background = new Texture("network_background.jpg");
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
    public void hide()
    {

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

    @Override
    public void OnDeviceFound(NetworkDevice device)
    {
        if (!m_devices.contains(device))
        {
            m_devices.add(device);
            Gdx.app.log("INFO", "Found network device " + device.Host + " " + device.Address + ":" + device.Port + " " + ((device.IsServer)?"Server":"Client"));
        }
    }
}
