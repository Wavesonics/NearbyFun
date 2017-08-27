package com.darkrockstudios.apps.nearbyfun

import android.app.Service
import android.content.Intent
import android.os.IBinder

class GameService : Service()
{
	companion object {
		public lateinit var s_userName: String;
	}

	override fun onBind(intent: Intent): IBinder?
	{
		// TODO: Return the communication channel to the service.
		throw UnsupportedOperationException("Not yet implemented")
	}
}
