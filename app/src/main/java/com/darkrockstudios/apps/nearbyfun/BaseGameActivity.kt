package com.darkrockstudios.apps.nearbyfun

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.util.Log

/**
 * Created by adamw on 8/27/2017.
 */
abstract class BaseGameActivity : AppCompatActivity(), GameInterface
{
	private val TAG = BaseGameActivity::class.simpleName

	protected var m_service: GameService? = null
	private var m_bound = false

	protected fun bindToGameService()
	{
		Log.d(TAG, "bindToGameService")

		bindService(GameService.intent(this),
		            m_connection,
		            Context.BIND_AUTO_CREATE)
	}

	protected fun unbindFromGameService()
	{
		Log.d(TAG, "unbindFromGameService")

		if (m_bound)
		{
			unbindService(m_connection)
			m_bound = false
		}
	}

	private val m_connection = object : ServiceConnection
	{
		override fun onServiceConnected(className: ComponentName, service: IBinder)
		{
			val binder = service as GameService.LocalBinder
			m_service = binder.service
			m_bound = true

			m_service?.let {
				it.gameInterface = this@BaseGameActivity
			}

			Log.d(TAG, "onServiceConnected")
		}

		override fun onServiceDisconnected(arg0: ComponentName)
		{
			m_bound = false
			m_service = null

			Log.d(TAG, "onServiceDisconnected")
		}
	}
}