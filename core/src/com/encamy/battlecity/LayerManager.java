package com.encamy.battlecity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.compression.lzma.Base;
import com.encamy.battlecity.entities.BaseWall;
import com.encamy.battlecity.entities.BrickWall;
import com.encamy.battlecity.entities.Grass;
import com.encamy.battlecity.entities.Player;
import com.encamy.battlecity.entities.StoneWall;
import com.encamy.battlecity.entities.Water;
import com.encamy.battlecity.utils.Box2dHelpers;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;

import static com.encamy.battlecity.Settings.ANIMATION_FRAME_DURATION;

public class LayerManager implements Settings.WallDestroyedCallback
{
    private TiledMap m_tileMap;
    private TextureAtlas m_atlas;
    private World m_world;
    private Player[] m_players;
    private ArrayList<BaseWall> m_walls;

    private boolean loaded = false;

    public LayerManager(World world)
    {
        m_world = world;
        m_walls = new ArrayList<BaseWall>();
    }

    public void loadLevel(String levelTitle)
    {
        // Can we rely on GC?
        m_players = new Player[2];
        m_tileMap = new TmxMapLoader().load(levelTitle);

        m_atlas = loadAtlas(m_tileMap);

        MapObjects objects = m_tileMap.getLayers().get("Collisions").getObjects();

        loadCollision(objects);

        loaded = true;
    }

    public void loadPlayer(int index)
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

        m_players[index] = new Player(left, top, right, bottom, playerBody, m_world, index + 1);
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

        return atlas;
    }

    private void loadCollision(MapObjects objects)
    {
        Gdx.app.log("Trace", "Loading collisions layer");
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
                                BaseWall wall = new BrickWall(m_world, rectangle, m_atlas.findRegion("brick"));
                                wall.setOnDestoryedCallback(this);
                                m_walls.add(wall);
                            }
                            break;
                        case "stone":
                            {
                                BaseWall wall = new StoneWall(m_world, rectangle, m_atlas.findRegion("stone"));
                                wall.setOnDestoryedCallback(this);
                                m_walls.add(wall);
                            }
                            break;
                        case "grass":
                            {
                                BaseWall wall = new Grass(m_world, rectangle, m_atlas.findRegion("grass"));
                                wall.setOnDestoryedCallback(this);
                                m_walls.add(wall);
                            }
                                break;
                        case "water":
                            {
                                BaseWall wall = new Water(m_world, rectangle, m_atlas.findRegion("water1"));
                                wall.setOnDestoryedCallback(this);
                                m_walls.add(wall);
                            }
                            break;
                        case "flag":
                            {
                                Flag flag = new Flag(m_world, rectangle, m_atlas.findRegion("flag"));
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
        }

        // Create world boundaries

        // bottom
        Box2dHelpers.createBox(
                m_world,
                0,-10,
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
                10, Settings.SCREEN_HEIGHT + 1,
                Settings.SCREEN_WIDTH, Settings.SCREEN_HEIGHT + 2,
                true,
                EnumSet.of(Settings.ObjectType.WALL),
                true);

        // right
        Box2dHelpers.createBox(
                m_world,
                Settings.SCREEN_WIDTH + 2, 0,
                Settings.SCREEN_WIDTH + 10, Settings.SCREEN_HEIGHT,
                true,
                EnumSet.of(Settings.ObjectType.WALL),
                true);
    }

    public boolean hit(Body body, EnumSet<Settings.ObjectType> type)
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
                else
                {
                    Gdx.app.log("FATAL", "Invalid player id");
                    return false;
                }

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
}
