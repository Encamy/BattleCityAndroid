package com.encamy.battlecity;

import com.badlogic.gdx.Game;
import com.encamy.battlecity.screens.MainMenuScreen;

public class BattleCityGame extends Game
{
    @Override
    public void create()
    {
        this.setScreen(new MainMenuScreen(this));
    }
}
