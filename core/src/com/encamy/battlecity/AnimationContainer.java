package com.encamy.battlecity;

import com.badlogic.gdx.graphics.g2d.Animation;

public class AnimationContainer
{
    private Animation left;
    private Animation top;
    private Animation right;
    private Animation bottom;
    private Animation spawnAnimation;
    private Animation invulnerabilityAnimation;

    public Animation getLeftAnimation()
    {
        return left;
    }

    public void setLeftAnimation(Animation left)
    {
        left.setPlayMode(Animation.PlayMode.LOOP);
        this.left = left;
    }

    public Animation getTopAnimation()
    {
        return top;
    }

    public void setTopAnimation(Animation top)
    {
        top.setPlayMode(Animation.PlayMode.LOOP);
        this.top = top;
    }

    public Animation getRightAnimation()
    {
        return right;
    }

    public void setRightAnimation(Animation right)
    {
        right.setPlayMode(Animation.PlayMode.LOOP);
        this.right = right;
    }

    public Animation getBottomAnimation()
    {
        return bottom;
    }

    public void setBottomAnimation(Animation bottom)
    {
        bottom.setPlayMode(Animation.PlayMode.LOOP);
        this.bottom = bottom;
    }

    public Animation getSpawnAnimation()
    {
        return spawnAnimation;
    }

    public void setSpawnAnimation(Animation spawnAnimation)
    {
        this.spawnAnimation = spawnAnimation;
    }

    public Animation getInvulnerabilityAnimation()
    {
        return invulnerabilityAnimation;
    }

    public void setInvulnerabilityAnimation(Animation invulnerabilityAnimation)
    {
        invulnerabilityAnimation.setPlayMode(Animation.PlayMode.LOOP);
        this.invulnerabilityAnimation = invulnerabilityAnimation;
    }
}
