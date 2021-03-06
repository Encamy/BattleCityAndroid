package com.encamy.battlecity.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.encamy.battlecity.BattleCityGame;

public class DesktopLauncher
{
	public static void main (String[] arg)
	{
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1280;
		config.height = 720;

		//config.width = 640;
		//config.height = 360;

		new LwjglApplication(new BattleCityGame(null), config);
	}
}
