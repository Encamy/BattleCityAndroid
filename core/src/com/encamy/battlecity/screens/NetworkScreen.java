package com.encamy.battlecity.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.encamy.battlecity.Settings;
import com.encamy.battlecity.network.AndroidInterface;
import com.encamy.battlecity.network.NetworkDevice;
import com.encamy.battlecity.network.NetworkManager;
import com.encamy.battlecity.utils.utils;

import java.io.IOException;
import java.util.ArrayList;

import static com.encamy.battlecity.Settings.SCREEN_HEIGHT;
import static com.encamy.battlecity.Settings.SCREEN_WIDTH;

public class NetworkScreen implements Screen, InputProcessor, Settings.OnDeviceFoundCallback, Settings.OnConnectedCallback
{
    private NetworkManager m_networkManager;
    private OrthographicCamera m_camera;
    private Texture background;
    private Texture serverIndicator;
    private Game m_game;
    private SpriteBatch m_spriteBatch;
    private volatile ArrayList<NetworkDevice> m_devices;
    private BitmapFont m_font;
    private GlyphLayout m_glyphLayout;
    private float m_maxDeviceNameLength = 0;
    private AndroidInterface m_androidInterface;
    private boolean m_isServer = false;
    private Animation m_waitingAnimation;
    private Sprite m_waiting;
    private float m_animationDuration = 0.0f;

    private enum State {
        SEARCHING,
        CONNECTING,
        CONNECTED
    }

    private volatile State m_state;

    public NetworkScreen(Game game, AndroidInterface androidInterface)
    {
        m_game = game;
        m_networkManager = new NetworkManager(androidInterface);
        m_networkManager.setOnDeviceFoundCallback(this);
        m_networkManager.setOnConnectedCallback(this);
        m_devices = new ArrayList<>();
        m_font = new BitmapFont(Gdx.files.internal("bcFont.fnt"), Gdx.files.internal("bcFont.png"), false);
        m_font.getData().setScale(0.75f);
        m_glyphLayout = new GlyphLayout();
        m_androidInterface = androidInterface;

        m_waitingAnimation = new Animation(Settings.ANIMATION_FRAME_DURATION * 0.5f,
            new TextureAtlas.AtlasRegion(new Texture("network_waiting_0.jpg"), 0, 0, 48, 16),
            new TextureAtlas.AtlasRegion(new Texture("network_waiting_1.jpg"), 0, 0, 48, 16),
            new TextureAtlas.AtlasRegion(new Texture("network_waiting_2.jpg"), 0, 0, 48, 16),
            new TextureAtlas.AtlasRegion(new Texture("network_waiting_3.jpg"), 0, 0, 48, 16),
            new TextureAtlas.AtlasRegion(new Texture("network_waiting_4.jpg"), 0, 0, 48, 16),
            new TextureAtlas.AtlasRegion(new Texture("network_waiting_5.jpg"), 0, 0, 48, 16)
            );
        m_waitingAnimation.setPlayMode(Animation.PlayMode.LOOP);

        m_waiting = new Sprite((TextureAtlas.AtlasRegion)m_waitingAnimation.getKeyFrame(m_animationDuration));
        m_state = State.SEARCHING;
    }

    @Override
    public void show()
    {
        m_camera = new OrthographicCamera();
        m_camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT);
        m_spriteBatch = new SpriteBatch();
        background = new Texture("network_background.jpg");
        serverIndicator = new Texture("server_indicator.png");
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

        if (!m_isServer)
        {
            drawDevices(m_spriteBatch);
        }
        else
        {
            m_animationDuration += delta;
            m_waiting.setRegion((TextureAtlas.AtlasRegion)m_waitingAnimation.getKeyFrame(m_animationDuration));
            m_waiting.draw(m_spriteBatch);
            m_waiting.setX(0.76f * Settings.SCREEN_WIDTH);
            m_waiting.setY(0.48f * Settings.SCREEN_HEIGHT);
        }

        if (m_state == State.CONNECTED)
        {
            Gdx.app.log("LOCK", "CONNECTED");
            m_game.setScreen(new GameScreen(m_networkManager));
        }

        m_spriteBatch.end();
    }

    private void drawDevices(SpriteBatch spriteBatch)
    {
        float YOffset = 0.42f;
        float XOffset = 0.47f - (m_maxDeviceNameLength * 0.5f / SCREEN_WIDTH);

        for (NetworkDevice device : m_devices)
        {
            m_glyphLayout.setText(m_font, utils.formatString(device.Host.toUpperCase()));
            m_font.draw(spriteBatch, m_glyphLayout, SCREEN_WIDTH * XOffset, SCREEN_HEIGHT * YOffset);

            if (device.IsServer)
            {
                spriteBatch.draw(serverIndicator, SCREEN_WIDTH * XOffset + m_glyphLayout.width + 20, SCREEN_HEIGHT * YOffset - 16);
            }

            YOffset -= 0.0772f;
        }
    }

    private void getLongestDeviceName(ArrayList<NetworkDevice> devices)
    {
        for (NetworkDevice device : devices)
        {
            m_glyphLayout.setText(m_font, device.Host.toUpperCase());
            if (m_glyphLayout.width > m_maxDeviceNameLength)
            {
                m_maxDeviceNameLength = m_glyphLayout.width;
            }
        }
    }

    private void becomeServer()
    {
        background = new Texture("network_background_waiting.jpg");
        m_isServer = true;

        try
        {
            m_state = State.CONNECTING;
            m_networkManager.createServer();
        }
        catch (IOException e)
        {
            Gdx.app.log("ERROR", "Failed to create server");
            if (m_androidInterface != null)
            {
                m_androidInterface.showToast("Failed to create server");
            }
        }
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
        int index = 0;
        float height = Gdx.graphics.getHeight();

        if (height - screenY > 0.49f * height)
        {
            return true;
        }

        if (height - screenY > 0.34f * height)
        {
            index = 0;
        }
        else if (height - screenY > 0.27f * height)
        {
            index = 1;
        }
        else if (height - screenY > 0.2f * height)
        {
            index = 2;
        }
        else if (height - screenY > 0.125f * height)
        {
            index = 3;
        }
        else
        {
            Gdx.app.log("TRACE", "Becoming a server");
            becomeServer();
            return true;
        }

        if (m_devices.size() < index + 1)
        {
            return true;
        }

        if (!m_devices.get(index).IsServer)
        {
            Gdx.app.log("INFO", "Device is not server");
            if (m_androidInterface != null)
            {
                m_androidInterface.showToast("Device is not a server");
            }
        }

        try
        {
            m_state = State.CONNECTING;
            m_networkManager.connect(m_devices.get(index));
        }
        catch (IOException e)
        {
            Gdx.app.log("ERROR", "Failed to connect");
            m_androidInterface.showToast("Failed to connect");
        }

        return true;
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
        _Placeholder clear = new _Placeholder(false);
        if (!have(m_devices, device, clear))
        {
            if (clear.value)
            {
                m_devices.clear();
            }

            m_devices.add(device);
            Gdx.app.log("INFO", "Found network device " + device.Host + " " + device.Address + ":" + device.Port + " " + ((device.IsServer)?"Server":"Client"));

            getLongestDeviceName(m_devices);
        }
    }

    private boolean have(ArrayList<NetworkDevice> devices, NetworkDevice device, _Placeholder clear)
    {
        for (NetworkDevice currentDevice : devices)
        {
            if (currentDevice.Address.equals(device.Address) &&
                currentDevice.Port.equals(device.Port))
            {
                clear.value = true;
            }

            if (currentDevice.Address.equals(device.Address) &&
                currentDevice.Port.equals(device.Port) &&
                currentDevice.IsServer == device.IsServer)
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public void OnConnected()
    {
        Gdx.app.log("INFO", "Fully connected");
        m_networkManager.stopAnnouncement();
        m_state = State.CONNECTED;
    }

    private class _Placeholder
    {
        boolean value;
        _Placeholder(boolean value)
        {
            this.value = value;
        }
    }
}
