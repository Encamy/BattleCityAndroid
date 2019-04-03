package com.encamy.battlecity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.encamy.battlecity.entities.Walls.BaseWall;
import com.encamy.battlecity.entities.Walls.BrickWall;
import com.encamy.battlecity.entities.Walls.Flag;
import com.encamy.battlecity.entities.Walls.Grass;
import com.encamy.battlecity.entities.Player;
import com.encamy.battlecity.entities.Walls.StoneWall;
import com.encamy.battlecity.entities.Walls.Water;
import com.encamy.battlecity.network.NetworkManager;
import com.encamy.battlecity.protobuf.NetworkProtocol;
import com.encamy.battlecity.utils.Box2dHelpers;
import com.encamy.battlecity.utils._Placeholder;
import com.encamy.battlecity.utils.utils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;

import static com.encamy.battlecity.Settings.ANIMATION_FRAME_DURATION;
import static com.encamy.battlecity.Settings.SCREEN_HEIGHT;
import static com.encamy.battlecity.Settings.SCREEN_WIDTH;

public class LayerManager implements Settings.WallDestroyedCallback
{
    private TiledMap m_tileMap;
    private TextureAtlas m_atlas;
    private World m_world;
    private Player[] m_players;
    private ArrayList<BaseWall> m_walls;
    private BulletManager m_bulletManager;
    private NetworkManager m_networkManager;
    private Texture m_ui_background;
    private BitmapFont m_bitmapFont;
    private GlyphLayout m_glyphLayout;

    private boolean loaded = false;

    public LayerManager(World world, BulletManager bulletManager, NetworkManager networkManager)
    {
        m_world = world;
        m_walls = new ArrayList<>();
        m_bulletManager = bulletManager;
        m_networkManager = networkManager;
        m_glyphLayout = new GlyphLayout();
        m_bitmapFont = new BitmapFont(Gdx.files.internal("bcFont.fnt"), Gdx.files.internal("bcFont.png"), false);
        m_bitmapFont.getData().setScale(0.75f);
    }

    public void updatePlayers(Batch batch, boolean freeze)
    {
        for (Player player : m_players)
        {
            if (player != null)
            {
                player.draw(batch, freeze);
            }
        }
    }

    public void loadLevel(String levelTitle)
    {
        // Can we rely on GC?
        m_players = new Player[2];
        m_tileMap = new TmxMapLoader().load(levelTitle);

        m_atlas = loadAtlas(m_tileMap);

        MapObjects objects = m_tileMap.getLayers().get("Collisions").getObjects();

        loadCollision(objects);

        m_ui_background = new Texture("UI_background.png");

        loaded = true;
    }

    public void loadPlayer(int index, boolean isRemote)
    {
        if (!loaded)
        {
            Gdx.app.log("Error", "Map is not loaded");
            return;
        }

        if (--index > m_players.length)
        {
            Gdx.app.log("Error", "Invalid player number");
            return;
        }

        Gdx.app.log("Trace", "Loading player " + index + " spawnpoint");

        Vector2 spawnpoint = new Vector2();
        for (MapObject object : m_tileMap.getLayers().get("Objects").getObjects())
        {
            if (object instanceof RectangleMapObject &&
                    object.getProperties().containsKey("type") &&
                    object.getProperties().get("type", String.class).equals("spawn_player" + (index + 1)))
            {
                spawnpoint.x = ((RectangleMapObject)object).getRectangle().getX();
                spawnpoint.y = ((RectangleMapObject)object).getRectangle().getY();
            }
        }

        Body playerBody = Box2dHelpers.createBox(
                m_world,
                spawnpoint.x,
                spawnpoint.y,
                54, 54,
                false,
                EnumSet.of(Settings.ObjectType.PLAYER),
                true);

        Gdx.app.log("Trace", "Loading player " + index + " sprites");

        String tank_name = (index == 0)? "yellow" : "green";

        Animation left = new Animation(ANIMATION_FRAME_DURATION, m_atlas.findRegions(tank_name + "1_left"));
        Animation top = new Animation(ANIMATION_FRAME_DURATION, m_atlas.findRegions(tank_name + "1_top"));
        Animation right = new Animation(ANIMATION_FRAME_DURATION, m_atlas.findRegions(tank_name + "1_right"));
        Animation bottom = new Animation(ANIMATION_FRAME_DURATION, m_atlas.findRegions(tank_name + "1_bottom"));
        left.setPlayMode(Animation.PlayMode.LOOP);
        top.setPlayMode(Animation.PlayMode.LOOP);
        right.setPlayMode(Animation.PlayMode.LOOP);
        bottom.setPlayMode(Animation.PlayMode.LOOP);

        Animation playerSpawnAnimation = new Animation(ANIMATION_FRAME_DURATION * 0.5f,
                m_atlas.findRegion("spawn_animation_1"),
                m_atlas.findRegion("spawn_animation_2"),
                m_atlas.findRegion("spawn_animation_3"),
                m_atlas.findRegion("spawn_animation_4"));

        Animation invulnerabilityAnimation = new Animation(ANIMATION_FRAME_DURATION * 0.5f,
                m_atlas.findRegion("invulnerability_animation_1"),
                m_atlas.findRegion("invulnerability_animation_2"));
        invulnerabilityAnimation.setPlayMode(Animation.PlayMode.LOOP);

        AnimationContainer animationContainer = new AnimationContainer();
        animationContainer.setLeftAnimation(left);
        animationContainer.setTopAnimation(top);
        animationContainer.setRightAnimation(right);
        animationContainer.setBottomAnimation(bottom);
        animationContainer.setInvulnerabilityAnimation(invulnerabilityAnimation);
        animationContainer.setSpawnAnimation(playerSpawnAnimation);

        m_players[index] = new Player(animationContainer, playerBody, index + 1, spawnpoint, m_bulletManager, isRemote, m_networkManager);

        if (m_networkManager == null || isRemote)
        {
            return;
        }

        if (index == 0)
        {
            Gdx.app.log("NETWORK", "announcing about server spawned");
            m_networkManager.notifySpawn(NetworkProtocol.Owner.SERVER_PLAYER, index, 0, 0, 0);
        }
        else
        {
            Gdx.app.log("NETWORK", "announcing about client spawned");
            m_networkManager.notifySpawn(NetworkProtocol.Owner.CLIENT_PLAYER, index, 0, 0, 0);
        }
    }

    public TiledMap getTileMap()
    {
        return m_tileMap;
    }

    public TextureAtlas getAtlas()
    {
        return m_atlas;
    }

    public Player getPlayer(int index)
    {
        return m_players[index-1];
    }

    public void dispose()
    {
        m_tileMap.dispose();
    }

    private TextureAtlas loadAtlas(TiledMap tiledMap)
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

            if (tile.getProperties().containsKey("title"))
            {
                atlas.addRegion("flag", tile.getTextureRegion());
            }

            if (tile.getProperties().containsKey("spawn_animation"))
            {
                String phase = tile.getProperties().get("spawn_animation", String.class);
                atlas.addRegion("spawn_animation_" + phase, tile.getTextureRegion());
            }

            if (tile.getProperties().containsKey("invulnerability_animation"))
            {
                String phase = tile.getProperties().get("invulnerability_animation", String.class);
                atlas.addRegion("invulnerability_animation_" + phase, tile.getTextureRegion());
            }

            if (tile.getProperties().containsKey("hit_animation"))
            {
                String phase = tile.getProperties().get("hit_animation", String.class);
                atlas.addRegion("hit_animation_" + phase, tile.getTextureRegion());
            }
        }

        Iterator<TiledMapTile> solidTiles = tiledMap.getTileSets().getTileSet("solids").iterator();
        while (solidTiles.hasNext())
        {
            TiledMapTile tile = solidTiles.next();
            if (tile.getProperties().containsKey("title"))
            {
                String solid_title = tile.getProperties().get("title", String.class);
                atlas.addRegion(solid_title, tile.getTextureRegion());
            }
        }

        Iterator<TiledMapTile> bulletTiles =  tiledMap.getTileSets().getTileSet("bullets").iterator();
        while (bulletTiles.hasNext())
        {
            TiledMapTile tile = bulletTiles.next();
            if (tile.getProperties().containsKey("direction"))
            {
                String bullet_direction = tile.getProperties().get("direction", String.class);
                atlas.addRegion("bullet_" + bullet_direction, tile.getTextureRegion());
            }
        }

        return atlas;
    }

    private void loadCollision(MapObjects objects)
    {
        Gdx.app.log("Trace", "Loading collisions layer");
        int current_id = 0;
        for (MapObject object : objects)
        {
            if (object instanceof RectangleMapObject)
            {
                if (object.getProperties().containsKey("wall_type"))
                {
                    Rectangle rectangle = ((RectangleMapObject) object).getRectangle();

                    String object_type = object.getProperties().get("wall_type", String.class);
                    switch (object_type)
                    {
                        case "brick":
                            {
                                BaseWall wall = new BrickWall(m_world, rectangle, m_atlas.findRegion("brick"), current_id);
                                wall.setOnDestoryedCallback(this);
                                m_walls.add(wall);
                            }
                            break;
                        case "stone":
                            {
                                BaseWall wall = new StoneWall(m_world, rectangle, m_atlas.findRegion("stone"), current_id);
                                wall.setOnDestoryedCallback(this);
                                m_walls.add(wall);
                            }
                            break;
                        case "grass":
                            {
                                BaseWall wall = new Grass(m_world, rectangle, m_atlas.findRegion("grass"), current_id);
                                wall.setOnDestoryedCallback(this);
                                m_walls.add(wall);
                            }
                                break;
                        case "water":
                            {
                                BaseWall wall = new Water(m_world, rectangle, m_atlas.findRegion("water1"), current_id);
                                wall.setOnDestoryedCallback(this);
                                m_walls.add(wall);
                            }
                            break;
                        case "flag":
                            {
                                Flag flag = new Flag(m_world, rectangle, m_atlas.findRegion("flag"), current_id);
                                flag.setOnDestoryedCallback(this);
                                m_walls.add(flag);
                            }
                            break;
                        default:
                            Gdx.app.log("ERROR", "Invalid type: " + object_type);
                            break;
                    }
                }
            }

            current_id++;
        }

        // Create world boundaries

        // bottom
        Box2dHelpers.createBox(
                m_world,
                0, -15,
                Settings.SCREEN_WIDTH, 10,
                true,
                EnumSet.of(Settings.ObjectType.WALL),
                true);

        // left
        Box2dHelpers.createBox(
                m_world,
                -10,0,
                10, Settings.SCREEN_HEIGHT,
                true,
                EnumSet.of(Settings.ObjectType.WALL),
                true);

        // top
        Box2dHelpers.createBox(
                m_world,
                0, getMapSize().y,
                Settings.SCREEN_WIDTH, 10,
                true,
                EnumSet.of(Settings.ObjectType.WALL),
                true);

        // right
        Box2dHelpers.createBox(
                m_world,
                getMapSize().x, 0,
                10, Settings.SCREEN_HEIGHT,
                true,
                EnumSet.of(Settings.ObjectType.WALL),
                true);
    }

    public boolean hit(Body body, EnumSet<Settings.ObjectType> type, _Placeholder<Integer> ref_id)
    {
        for (BaseWall wall : m_walls)
        {
            if (wall.getBody() == body)
            {
                int power;
                if (type.contains(Settings.ObjectType.PLAYER1_OWNER))
                {
                    power = m_players[0].getLevel();
                }
                else if (type.contains(Settings.ObjectType.PLAYER2_OWNER))
                {
                    power = m_players[1].getLevel();
                }
                else if (type.contains(Settings.ObjectType.ENEMY_OWNER))
                {
                    power = 1;
                }
                else
                {
                    Gdx.app.log("FATAL", "Invalid player id");
                    return false;
                }

                ref_id.variable = wall.getId();
                return wall.hit(power);
            }
        }

        return false;
    }

    public void drawWalls(SpriteBatch spriteBatch)
    {
        for (BaseWall wall : m_walls)
        {
            wall.draw(spriteBatch);
        }
    }

    @Override
    public void OnWallDestroyed(BaseWall wall)
    {
        m_walls.remove(wall);
    }

    public void onNetworkMove(NetworkProtocol.Owner owner, float x, float y, Settings.Direction direction)
    {
        int index;
        if (owner == NetworkProtocol.Owner.CLIENT_PLAYER)
        {
            index = 1;
        }
        else if (owner == NetworkProtocol.Owner.SERVER_PLAYER)
        {
            index = 0;
        }
        else
        {
            Gdx.app.log("FATAL", "Invalid player");
            return;
        }

        m_players[index].setPosition(new Vector2(x, y));
        m_players[index].setDirection(direction);
    }

    public void onNetworkPlayerFire(NetworkProtocol.Fire fire)
    {
        m_players[fire.getId() - 1].fire();
    }

    public void onNetworkWallDestroy(NetworkProtocol.Owner owner, int id)
    {
        Gdx.app.log("NETWORK", "wall was destroyed " + id);
        for(int i = 0; i < m_walls.size(); i++)
        {
            if (m_walls.get(i).getId() == id)
            {
                m_walls.get(i).destroy();
                break;
            }
        }
    }

    public Vector2 getMapSize()
    {
        TiledMapTileLayer layer = (TiledMapTileLayer) m_tileMap.getLayers().get(0);
        Vector2 result = new Vector2();
        result.x = layer.getTileWidth() * layer.getWidth();
        result.y = layer.getTileHeight() * layer.getHeight();
        return result;
    }

    public void drawUI(SpriteBatch batch)
    {
        //batch.draw(m_ui_background, getMapSize().x, Settings.SCREEN_HEIGHT - getMapSize().y);
        //batch.draw(m_ui_background, 0, Settings.SCREEN_HEIGHT - getMapSize().y);
        batch.draw(m_ui_background, 0, 0);

        float XOffset = Settings.SCREEN_WIDTH * 0.95f;
        float YOffset = Settings.SCREEN_HEIGHT - (Settings.SCREEN_HEIGHT * 0.635f);
        m_glyphLayout.setText(m_bitmapFont, Integer.toString(m_players[0].getHealth()));
        m_bitmapFont.draw(batch, m_glyphLayout, XOffset, YOffset);
    }
}
