package com.encamy.battlecity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
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
import com.encamy.battlecity.entities.Player;
import com.encamy.battlecity.utils.Box2dHelpers;

import java.util.Iterator;

import static com.encamy.battlecity.Settings.ANIMATION_FRAME_DURATION;

public class LayerManager
{
    private TiledMap m_tileMap;
    private TextureAtlas m_atlas;
    private World m_world;
    private MapObjects m_walls;
    private Player[] m_players;

    private boolean loaded = false;

    public LayerManager(World world)
    {
        m_world = world;
    }

    public void loadLevel(String levelTitle)
    {
        // Can we rely on GC?
        m_players = new Player[2];
        m_tileMap = new TmxMapLoader().load(levelTitle);

        m_atlas = load_atlas(m_tileMap);

        m_walls = m_tileMap.getLayers().get("Collisions").getObjects();
        loadCollision(m_walls);

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
                    object.getProperties().get("type", String.class).equals("spawn_player1"))
            {
                spawnpoint.x = ((RectangleMapObject)object).getRectangle().getX();
                spawnpoint.y = ((RectangleMapObject)object).getRectangle().getY();
            }
        }

        Body playerBody = Box2dHelpers.createPlayerBox(
                m_world,
                spawnpoint.x,
                spawnpoint.y,
                54, 54);

        Gdx.app.log("Trace", "Loading player " + index + " sprites");

        Animation left = new Animation(ANIMATION_FRAME_DURATION, m_atlas.findRegions("yellow1_left"));
        Animation top = new Animation(ANIMATION_FRAME_DURATION, m_atlas.findRegions("yellow1_top"));
        Animation right = new Animation(ANIMATION_FRAME_DURATION, m_atlas.findRegions("yellow1_right"));
        Animation bottom = new Animation(ANIMATION_FRAME_DURATION, m_atlas.findRegions("yellow1_bottom"));
        left.setPlayMode(Animation.PlayMode.LOOP);
        top.setPlayMode(Animation.PlayMode.LOOP);
        right.setPlayMode(Animation.PlayMode.LOOP);
        bottom.setPlayMode(Animation.PlayMode.LOOP);

        m_players[index] = new Player(left, top, right, bottom, playerBody);
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

    private TextureAtlas load_atlas(TiledMap tiledMap)
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
        }

        return atlas;
    }

    private void loadCollision(MapObjects walls)
    {
        Gdx.app.log("Trace", "Loading collisions layer");
        for (MapObject object : walls)
        {
            if (object instanceof RectangleMapObject)
            {
                Rectangle rectangle = ((RectangleMapObject) object).getRectangle();

                Box2dHelpers.createBox(
                        m_world,
                        rectangle.x,
                        rectangle.y,
                        rectangle.width,
                        rectangle.height,
                        true);
            }
        }

        // Create world boundaries

        // bottom
        Box2dHelpers.createBox(
                m_world,
                0,-10,
                Settings.SCREEN_WIDTH, 10,
                true);

        // left
        Box2dHelpers.createBox(
                m_world,
                -10,0,
                10, Settings.SCREEN_HEIGHT,
                true);

        // top
        Box2dHelpers.createBox(
                m_world,
                10, Settings.SCREEN_HEIGHT + 1,
                Settings.SCREEN_WIDTH, Settings.SCREEN_HEIGHT + 2,
                true);

        // right
        Box2dHelpers.createBox(
                m_world,
                Settings.SCREEN_WIDTH + 2, 0,
                Settings.SCREEN_WIDTH + 10, Settings.SCREEN_HEIGHT,
                true);
    }
}
