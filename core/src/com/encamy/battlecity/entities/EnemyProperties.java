package com.encamy.battlecity.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.encamy.battlecity.Settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.encamy.battlecity.Settings.ANIMATION_FRAME_DURATION;

/*
    Enemy levels:
    >Level 0: Basically identical to your first tank, slow and basic.
    >Level 1: Like yours, the first upgrade for the enemy tanks entails a speed
        upgrade. The level 1 tank is much faster than yours, but fires at the same
        speed.
    >Level 2: The level 2 upgrade isn't as fast as the level one upgrade, but is
        still faster than the level 1. Like your level 2, the enemy's level 2 allows
        him to shoot twice.
    >Level 3: The level 3 upgrade differs the most of any from your own. The level
        3 is bigger, slower and can only fire once at a time, but it takes 4 hits to
        destroy it. Not the hardest thing to take down.
*/

public class EnemyProperties
{
    public int level;
    public float speed;
    public Animation leftAnimation;
    public Animation rightAnimation;
    public Animation topAnimation;
    public Animation bottomAnimation;
    public Animation spawningAnimation;
    public float health;
    public int score;

    private EnemyProperties[] m_enemyProperties = new EnemyProperties[4];

    public EnemyProperties(TextureAtlas atlas)
    {
        Gdx.app.log("Trace", "Creating enemy properties");
        for (int i = 0; i < 4; i++)
        {
            m_enemyProperties[i] = CreateProperty(i, atlas);
        }
        Gdx.app.log("Trace", "Eenemy properties created");
    }

    public EnemyProperties Get(int level)
    {
        Gdx.app.log("Trace", "Creating enemy with level " + level);
        if (level >= m_enemyProperties.length)
        {
            return null;
        }

        return m_enemyProperties[level];
    }

    private EnemyProperties()
    {

    }

    private EnemyProperties CreateProperty(int level, TextureAtlas atlas)
    {
        EnemyProperties property = new EnemyProperties();
        property.level = level;

        int textureIndex;
        List<TextureAtlas.AtlasRegion> left_animationTextures = new ArrayList<TextureAtlas.AtlasRegion>();
        List<TextureAtlas.AtlasRegion> top_animationTextures = new ArrayList<TextureAtlas.AtlasRegion>();
        List<TextureAtlas.AtlasRegion> right_animationTextures = new ArrayList<TextureAtlas.AtlasRegion>();
        List<TextureAtlas.AtlasRegion> bottom_animationTextures = new ArrayList<TextureAtlas.AtlasRegion>();

        switch (level)
        {
            case 0:
                property.speed = Settings.BASE_MOVEMENT_SPEED;
                property.health = 1;
                property.score = 100;
                textureIndex = 1;

                left_animationTextures.addAll(Arrays.asList(atlas.findRegions("white" + textureIndex + "_left").toArray()));
                top_animationTextures.addAll(Arrays.asList(atlas.findRegions("white" + textureIndex + "_top").toArray()));
                right_animationTextures.addAll(Arrays.asList(atlas.findRegions("white" + textureIndex + "_right").toArray()));
                bottom_animationTextures.addAll(Arrays.asList(atlas.findRegions("white" + textureIndex + "_bottom").toArray()));

                break;

            case 1:
                property.speed = Settings.BASE_MOVEMENT_SPEED * 1.5f;
                property.health = 1;
                property.score = 200;
                textureIndex = 5;

                left_animationTextures.addAll(Arrays.asList(atlas.findRegions("white" + textureIndex + "_left").toArray()));
                top_animationTextures.addAll(Arrays.asList(atlas.findRegions("white" + textureIndex + "_top").toArray()));
                right_animationTextures.addAll(Arrays.asList(atlas.findRegions("white" + textureIndex + "_right").toArray()));
                bottom_animationTextures.addAll(Arrays.asList(atlas.findRegions("white" + textureIndex + "_bottom").toArray()));
                break;

            case 2:
                property.speed = Settings.BASE_MOVEMENT_SPEED * 1.5f;
                property.health = 2;
                property.score = 300;
                textureIndex = 6;

                left_animationTextures.addAll(Arrays.asList(atlas.findRegions("white" + textureIndex + "_left").toArray()));
                left_animationTextures.addAll(Arrays.asList(atlas.findRegions("red" + textureIndex + "_left").toArray()));

                top_animationTextures.addAll(Arrays.asList(atlas.findRegions("white" + textureIndex + "_top").toArray()));
                top_animationTextures.addAll(Arrays.asList(atlas.findRegions("red" + textureIndex + "_top").toArray()));

                right_animationTextures.addAll(Arrays.asList(atlas.findRegions("white" + textureIndex + "_right").toArray()));
                right_animationTextures.addAll(Arrays.asList(atlas.findRegions("red" + textureIndex + "_right").toArray()));

                bottom_animationTextures.addAll(Arrays.asList(atlas.findRegions("white" + textureIndex + "_bottom").toArray()));
                bottom_animationTextures.addAll(Arrays.asList(atlas.findRegions("red" + textureIndex + "_bottom").toArray()));
                break;

            case 3:
                property.speed = Settings.BASE_MOVEMENT_SPEED * 0.75f;
                property.health = 4;
                property.score = 400;
                textureIndex = 8;

                left_animationTextures.addAll(Arrays.asList(atlas.findRegions("white" + textureIndex + "_left").toArray()));
                left_animationTextures.addAll(Arrays.asList(atlas.findRegions("red" + textureIndex + "_left").toArray()));

                top_animationTextures.addAll(Arrays.asList(atlas.findRegions("white" + textureIndex + "_top").toArray()));
                top_animationTextures.addAll(Arrays.asList(atlas.findRegions("red" + textureIndex + "_top").toArray()));

                right_animationTextures.addAll(Arrays.asList(atlas.findRegions("white" + textureIndex + "_right").toArray()));
                right_animationTextures.addAll(Arrays.asList(atlas.findRegions("red" + textureIndex + "_right").toArray()));

                bottom_animationTextures.addAll(Arrays.asList(atlas.findRegions("white" + textureIndex + "_bottom").toArray()));
                bottom_animationTextures.addAll(Arrays.asList(atlas.findRegions("red" + textureIndex + "_bottom").toArray()));
                break;

            default:
                Gdx.app.log("Error", "Invalid tank level");
                return null;
        }

        property.leftAnimation = new Animation(Settings.ANIMATION_FRAME_DURATION, left_animationTextures.toArray());
        property.rightAnimation = new Animation(Settings.ANIMATION_FRAME_DURATION, right_animationTextures.toArray());
        property.topAnimation = new Animation(Settings.ANIMATION_FRAME_DURATION, top_animationTextures.toArray());
        property.bottomAnimation = new Animation(Settings.ANIMATION_FRAME_DURATION, bottom_animationTextures.toArray());
        property.spawningAnimation = new Animation(ANIMATION_FRAME_DURATION * 0.5f,
                atlas.findRegion("spawn_animation_1"),
                atlas.findRegion("spawn_animation_2"),
                atlas.findRegion("spawn_animation_3"),
                atlas.findRegion("spawn_animation_4"));

        property.leftAnimation.setPlayMode(Animation.PlayMode.LOOP);
        property.rightAnimation.setPlayMode(Animation.PlayMode.LOOP);
        property.topAnimation.setPlayMode(Animation.PlayMode.LOOP);
        property.bottomAnimation.setPlayMode(Animation.PlayMode.LOOP);

        return property;
    }
}
