package com.encamy.battlecity.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.encamy.battlecity.AnimationContainer;
import com.encamy.battlecity.Settings;
import com.encamy.battlecity.utils.Box2dHelpers;
import com.encamy.battlecity.utils.utils;

import java.util.ArrayList;
import java.util.EnumSet;


public class Player extends Sprite implements InputProcessor {

    private Vector2 m_velocity = new Vector2();
    private float m_speed = Settings.BASE_MOVEMENT_SPEED;
    private float m_animationTime = 0;
    private Animation m_left, m_top, m_right, m_bottom;
    private Animation m_playerSpawAnimation;
    private Animation m_invulnerabilityAnimation;
    private Body m_body;
    private Box2dSteeringEntity m_steeringEntity;
    private World m_world;
    private Settings.Direction m_direction;
    private ArrayList<Bullet> m_bullets;
    private int m_health;
    private int m_level;
    private int m_current_player;
    private Vector2 m_spawnPoint;
    private Sprite m_invulnarableSprite;

    private static final float INVULNERABLE_ANIMATION_TIME = 3000;

    private enum State {
        SPAWNING,
        INVULNERABLE,
        ALIVE
    }

    private State m_state;

    public Player(AnimationContainer animation, Body body, int current_player, Vector2 spawnPoint)
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

        m_bullets = new ArrayList<Bullet>();

        m_health = Settings.PLAYER_HEALTH;
        m_current_player = current_player;
        m_level = 1;
        m_spawnPoint = new Vector2(spawnPoint);

        m_state = State.SPAWNING;

        m_invulnarableSprite = new Sprite((TextureAtlas.AtlasRegion)m_invulnerabilityAnimation.getKeyFrame(0.0f));
        m_invulnarableSprite.setPosition(spawnPoint.x, spawnPoint.y);

        Gdx.input.setInputProcessor(this);
        //m_invulnarableSprite.setAlpha(0.5f);
    }

    public void draw(Batch batch, boolean freeze)
    {
        if (!freeze)
        {
            update(Gdx.graphics.getDeltaTime());
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
        return m_speed;
    }

    public Body getBody()
    {
        return m_body;
    }

    public Box2dSteeringEntity getSteeringEntity()
    {
        return m_steeringEntity;
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

    private void update(float deltaTime)
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

        setX(Box2dHelpers.Box2d2x(m_body.getPosition().x));
        setY(Box2dHelpers.Box2d2y(m_body.getPosition().y));

        m_invulnarableSprite.setX(Box2dHelpers.Box2d2x(m_body.getPosition().x));
        m_invulnarableSprite.setY(Box2dHelpers.Box2d2y(m_body.getPosition().y));

        for (Bullet bullet : m_bullets)
        {
            bullet.update(deltaTime);
        }
    }

    public void fire()
    {
        Vector2 bulletSpawnPos = new Vector2();
        switch (m_direction)
        {
            case TOP:
                bulletSpawnPos.set(Box2dHelpers.Box2d2x(m_body.getPosition().x) + 51, Box2dHelpers.Box2d2y(m_body.getPosition().y) + 90);
                break;
            case LEFT:
                bulletSpawnPos.set(Box2dHelpers.Box2d2x(m_body.getPosition().x) + 20, Box2dHelpers.Box2d2y(m_body.getPosition().y) + 58);
                break;
            case RIGHT:
                bulletSpawnPos.set(Box2dHelpers.Box2d2x(m_body.getPosition().x) + 85, Box2dHelpers.Box2d2y(m_body.getPosition().y) + 58);
                break;
            case BOTTOM:
                bulletSpawnPos.set(Box2dHelpers.Box2d2x(m_body.getPosition().x) + 51, Box2dHelpers.Box2d2y(m_body.getPosition().y) + 20);
                break;
        }

        if (m_current_player == 1)
        {
            m_bullets.add(new Bullet(m_world, bulletSpawnPos, m_direction, Settings.ObjectType.PLAYER1_OWNER));
        }
        else if (m_current_player == 2)
        {
            m_bullets.add(new Bullet(m_world, bulletSpawnPos, m_direction, Settings.ObjectType.PLAYER2_OWNER));
        }
        else
        {
            Gdx.app.log("FATAL", "Invalid player id. Should not happen");
        }
    }

    public void destroyBullet(Body body)
    {
        for (Bullet bullet : m_bullets)
        {
            if (bullet.getBody() == body)
            {
                m_bullets.remove(bullet);
                bullet.dispose();
                break;
            }
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

        // Is there a better way to change x and y?ss
        m_world.destroyBody(m_body);
        m_body = Box2dHelpers.createBox(
                m_world,
                m_spawnPoint.x,
                m_spawnPoint.y,
                54, 54,
                false,
                EnumSet.of(Settings.ObjectType.PLAYER),
                true);

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
}
