package com.encamy.battlecity;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.encamy.battlecity.network.AndroidInterface;
import com.encamy.battlecity.screens.GameScreen;

public class AndroidLauncher extends AndroidApplication implements AndroidInterface
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

		initialize(new BattleCityGame(this), config);
	}

	@Override
	public String getDeviceName()
	{
		return Settings.Secure.getString(getContentResolver(), "bluetooth_name");
	}

	@Override
	public void showToast(final String message)
	{
		runOnUiThread(new Runnable() {
			String inner_message = message;
			@Override
			public void run () {
				Toast.makeText(getContext(), inner_message,
						Toast.LENGTH_SHORT).show();}
			});
	}
}
