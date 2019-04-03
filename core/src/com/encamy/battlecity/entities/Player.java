package com.encamy.battlecity.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.encamy.battlecity.AnimationContainer;
import com.encamy.battlecity.BulletManager;
import com.encamy.battlecity.Settings;
import com.encamy.battlecity.network.NetworkManager;
import com.encamy.battlecity.protobuf.NetworkProtocol;
import com.encamy.battlecity.utils.Box2dHelpers;
import com.encamy.battlecity.utils.Dictionary;
import com.encamy.battlecity.utils.utils;

import java.util.ArrayList;
import java.util.EnumSet;


public class Player extends Sprite implements InputProcessor {

    private Vector2 m_velocity = new Vector2();
    private float m_animationTime = 0;
    private Animation m_left, m_top, m_right, m_bottom;
    private Animation m_playerSpawAnimation;
    private Animation m_invulnerabilityAnimation;
    private Body m_body;
    private Box2dSteeringEntity m_steeringEntity;
    private World m_world;
    private Settings.Direction m_direction;
    private int m_health;
    private int m_level;
    private int m_current_player;
    private Vector2 m_spawnPoint;
    private Sprite m_invulnarableSprite;
    private Dictionary<Integer, Integer> m_destroyedEnemies;
    private BulletManager m_bulletManager;
    private NetworkManager m_networkManager;
    private boolean m_isRemote;

    private static final float INVULNERABLE_ANIMATION_TIME = 3000;

    private enum State {
        SPAWNING,
        INVULNERABLE,
        ALIVE
    }

    private State m_state;

    public Player(
            AnimationContainer animation,
            Body body,
            int current_player,
            Vector2 spawnPoint,
            BulletManager bulletManager,
            boolean isRemote,
            NetworkManager networkManager
        )
    {
        super(((TextureAtlas.AtlasRegion) animation.getTopAnimation().getKeyFrame(0)));
        m_left = animation.getLeftAnimation();
        m_top = animation.getTopAnimation();
        m_right = animation.getRightAnimation();
        m_bottom = animation.getBottomAnimation();
        m_playerSpawAnimation = animation.getSpawnAnimation();
        m_invulnerabilityAnimation = animation.getInvulnerabilityAnimation();

        m_body = body;
        m_steeringEntity = new Box2dSteeringEntity(m_body, 10.0f);
        m_world = body.getWorld();
        m_direction = Settings.Direction.TOP;

        m_bulletManager = bulletManager;

        m_health = Settings.PLAYER_HEALTH;
        m_current_player = current_player;
        m_level = 1;
        m_spawnPoint = new Vector2(spawnPoint);

        m_state = State.SPAWNING;

        m_invulnarableSprite = new Sprite((TextureAtlas.AtlasRegion)m_invulnerabilityAnimation.getKeyFrame(0.0f));
        m_invulnarableSprite.setPosition(spawnPoint.x, spawnPoint.y);
        m_isRemote = isRemote;

        if (!isRemote)
        {
            Gdx.input.setInputProcessor(this);
        }
        m_destroyedEnemies = new Dictionary<>();
        m_networkManager = networkManager;
    }

    public void draw(Batch batch, boolean freeze)
    {
        if (!freeze)
        {
            update(Gdx.graphics.getDeltaTime(), batch);
        }
        super.draw(batch);

        if (m_state == State.INVULNERABLE)
        {
            m_invulnarableSprite.draw(batch);
        }
    }

    public void setVelocity(float x, float y)
    {
        m_velocity.set(x, y);
    }

    public float getSpeed()
    {
        return Settings.BASE_MOVEMENT_SPEED;
    }

    public Body getBody()
    {
        return m_body;
    }

    public Box2dSteeringEntity getSteeringEntity()
    {
        return m_steeringEntity;
    }

    public void enemyDestroyed(int level, int score)
    {
        m_destroyedEnemies.put(level, score);
    }

    public Dictionary<Integer, Integer> getDestroyedEnemies()
    {
        return m_destroyedEnemies;
    }

    public void setPosition(Vector2 vector)
    {
        m_body.setTransform(vector, 0);
    }

    public void setDirection(Settings.Direction direction)
    {
        m_direction = direction;
    }

    public int getHealth()
    {
        return m_health;
    }

    private void updateSpawnAnimation(float animationTime)
    {
        super.setRegion((TextureAtlas.AtlasRegion) m_playerSpawAnimation.getKeyFrame(animationTime));
        if (m_playerSpawAnimation.isAnimationFinished(animationTime))
        {
            m_animationTime = 0f;
            m_state = State.INVULNERABLE;
        }
    }

    private void updateIvulnerableAnimation(float animationTime)
    {
        m_invulnarableSprite.setRegion((TextureAtlas.AtlasRegion) m_invulnerabilityAnimation.getKeyFrame(animationTime));
        if (animationTime * 1000.0f > INVULNERABLE_ANIMATION_TIME)
        {
            m_state = State.ALIVE;
        }
    }

    private void updateAliveAnimation(float animationTime, Settings.Direction direction)
    {
        // We do not want to use stored direction here.
        // Animation should be played only while tank is moved
        if (direction != null)
        {
            switch (direction)
            {
                case TOP:
                    super.setRegion(((TextureAtlas.AtlasRegion) m_top.getKeyFrame(m_animationTime)));
                    break;
                case LEFT:
                    super.setRegion(((TextureAtlas.AtlasRegion) m_left.getKeyFrame(m_animationTime)));
                    break;
                case RIGHT:
                    super.setRegion(((TextureAtlas.AtlasRegion) m_right.getKeyFrame(m_animationTime)));
                    break;
                case BOTTOM:
                    super.setRegion(((TextureAtlas.AtlasRegion) m_bottom.getKeyFrame(m_animationTime)));
                    break;
            }
        }
    }

    private void update(float deltaTime, Batch batch)
    {
        if (m_health < 0)
        {
            // Just some placeholder. Implement destory mechanics.
            return;
        }

        // update animation
        m_animationTime += deltaTime;

        switch (m_state)
        {
            case SPAWNING:
                updateSpawnAnimation(m_animationTime);
                break;
            case INVULNERABLE:
                {
                    updateIvulnerableAnimation(m_animationTime);
                }
               // break;
            case ALIVE:
                {
                    Settings.Direction direction = utils.velocity2Direction(m_velocity);

                    if (direction != Settings.Direction.NULL)
                    {
                        m_direction = direction;
                    }

                    updateAliveAnimation(m_animationTime, m_direction);

                    m_body.setLinearVelocity(m_velocity.x, m_velocity.y);
                }
                break;
            default:
                Gdx.app.log("FATAL", "Invalid state");
                break;
        }

        setX(Box2dHelpers.Box2d2x(m_body.getPosition().x, 32));
        setY(Box2dHelpers.Box2d2y(m_body.getPosition().y, 32));

        m_invulnarableSprite.setX(Box2dHelpers.Box2d2x(m_body.getPosition().x, 32));
        m_invulnarableSprite.setY(Box2dHelpers.Box2d2y(m_body.getPosition().y, 32));

        if (m_isRemote || m_networkManager == null)
        {
            return;
        }

        if (isServer())
        {
            m_networkManager.notifyMove(
                    NetworkProtocol.Owner.SERVER_PLAYER,
                    m_current_player,
                    m_body.getPosition().x, m_body.getPosition().y,
                    utils.toNetworkDirection(m_direction)
            );
        }
        else
        {
            m_networkManager.notifyMove(
                    NetworkProtocol.Owner.CLIENT_PLAYER,
                    m_current_player,
                    m_body.getPosition().x, m_body.getPosition().y,
                    utils.toNetworkDirection(m_direction)
            );
        }
    }

    public void fire()
    {
        Vector2 bulletSpawnPos = new Vector2();
        switch (m_direction)
        {
            case TOP:
                bulletSpawnPos.set(Box2dHelpers.Box2d2x(m_body.getPosition().x, 32) + 51, Box2dHelpers.Box2d2y(m_body.getPosition().y, 32) + 90);
                break;
            case LEFT:
                bulletSpawnPos.set(Box2dHelpers.Box2d2x(m_body.getPosition().x, 32) + 20, Box2dHelpers.Box2d2y(m_body.getPosition().y, 32) + 58);
                break;
            case RIGHT:
                bulletSpawnPos.set(Box2dHelpers.Box2d2x(m_body.getPosition().x, 32) + 85, Box2dHelpers.Box2d2y(m_body.getPosition().y, 32) + 58);
                break;
            case BOTTOM:
                bulletSpawnPos.set(Box2dHelpers.Box2d2x(m_body.getPosition().x, 32) + 51, Box2dHelpers.Box2d2y(m_body.getPosition().y, 32) + 20);
                break;
        }

        if (m_current_player == 1)
        {
            m_bulletManager.addBullet(bulletSpawnPos, m_direction, Settings.ObjectType.PLAYER1_OWNER);
        }
        else if (m_current_player == 2)
        {
            m_bulletManager.addBullet(bulletSpawnPos, m_direction, Settings.ObjectType.PLAYER2_OWNER);
        }
        else
        {
            Gdx.app.log("FATAL", "Invalid player id. Should not happen");
        }

        if (m_isRemote || m_networkManager == null)
        {
            return;
        }

        if (isServer())
        {
            m_networkManager.notifyFire(NetworkProtocol.Owner.SERVER_PLAYER, m_current_player, m_direction);
        }
        else
        {
            m_networkManager.notifyFire(NetworkProtocol.Owner.CLIENT_PLAYER, m_current_player, m_direction);
        }
    }

    public int getLevel()
    {
        return m_level;
    }

    public void hit()
    {
        if (m_state == State.INVULNERABLE)
        {
            return;
        }

        m_health--;
        Gdx.app.log("TRACE", "Player was hitted. Current health: " + m_health);

        float x = m_spawnPoint.x / Settings.PPM - Settings.SCREEN_WIDTH / Settings.PPM * 0.5f;
        float y = m_spawnPoint.y / Settings.PPM - Settings.SCREEN_HEIGHT / Settings.PPM * 0.5f;
        m_body.setTransform(new Vector2(x, y), 0);
        m_body.setUserData(EnumSet.of(Settings.ObjectType.PLAYER));

        m_animationTime = 0f;
        m_state = State.SPAWNING;
    }

    @Override
    public boolean keyDown(int keycode)
    {
        switch (keycode)
        {
            case Input.Keys.W:
                setVelocity(0.0f,getSpeed());
                break;
            case Input.Keys.A:
                setVelocity(-1 * getSpeed(), 0.0f);
                break;
            case Input.Keys.D:
                setVelocity(getSpeed(), 0.0f);
                break;
            case Input.Keys.S:
                setVelocity(0.0f, -1 * getSpeed());
                break;
            case Input.Keys.SPACE:
            case Input.Keys.ENTER:
                fire();
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
            setVelocity(0.0f, 0.0f);
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
        if (screenX > Gdx.graphics.getWidth() * 0.5f)
        {
            fire();
            return true;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button)
    {
        setVelocity(0.0f, 0.0f);
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
            setVelocity(0, getSpeed());
            return true;
        }

        if (screenY > Gdx.graphics.getHeight() * 0.6f)
        {
            setVelocity(0, -1 * getSpeed());
            return true;
        }

        if (screenX < Gdx.graphics.getWidth() * 0.15f)
        {
            setVelocity(-1 * getSpeed(), 0.0f);
            return true;
        }

        if (screenX > Gdx.graphics.getWidth() * 0.15f)
        {
           setVelocity(getSpeed(), 0.0f);
            return true;
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

    private boolean isServer()
    {
        return m_current_player == 1;
    }
}
