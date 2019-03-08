package com.encamy.battlecity;

import com.badlogic.gdx.Game;
import com.encamy.battlecity.network.AndroidInterface;
import com.encamy.battlecity.screens.MainMenuScreen;

public class BattleCityGame extends Game
{
    private AndroidInterface m_androidAPI;

    public BattleCityGame(AndroidInterface androidAPI)
    {
        m_androidAPI = androidAPI;
    }

    @Override
    public void create()
    {
        this.setScreen(new MainMenuScreen(this, m_androidAPI));
    }
}
