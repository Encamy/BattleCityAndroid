package com.encamy.battlecity;

import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.encamy.battlecity.screens.GameScreen;

public class AndroidLauncher extends AndroidApplication
{
	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useImmersiveMode = true;
        config.useAccelerometer = false;
        config.useGyroscope = false;
        config.useCompass = false;

		Point size = new Point();
		getWindowManager().getDefaultDisplay().getSize(size);
		int width = size.x;
		int height = size.y;

		//display size on OP 6T = 2135x1080
        Log.d("ApplicationTagName", "Application have started");
		Log.d("ApplicationTagName", "Display size: " + width + ":" + height);

		initialize(new BattleCityGame(), config);
	}
}
