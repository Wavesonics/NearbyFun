package com.darkrockstudios.apps.nearbyfun

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.ActivityCompat
import android.support.v4.app.NotificationCompat
import android.text.TextUtils
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Status
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import java.nio.charset.Charset


class GameService : Service(),
                    GoogleApiClient.ConnectionCallbacks,
                    GoogleApiClient.OnConnectionFailedListener
{
	companion object
	{
		val TAG = GameService::class.simpleName

		val ACTION_STARTED = GameService::class.qualifiedName + "STARTED"

		val SERVICE_ID = "NearbyFun"
		val EXTRA_IS_HOST = "is_host"
		val EXTRA_USER_NAME = "username"
		val NOTIFICATION_CHANNEL_ID = "game_service"
		val NOTIFICATION_ID = 1

		fun startHost(activity: Activity, userName: String)
		{
			startService(activity, true, userName)
		}

		fun startClient(activity: Activity, userName: String)
		{
			startService(activity, false, userName)
		}

		private fun startService(activity: Activity, isHost: Boolean, userName: String)
		{
			ActivityCompat.startForegroundService(activity, createIntent(activity, isHost, userName))
		}

		private fun createIntent(activity: Activity, isHost: Boolean, userName: String): Intent
		{
			val intent = Intent(activity, GameService::class.java)
			intent.putExtra(EXTRA_IS_HOST, isHost)
			intent.putExtra(EXTRA_USER_NAME, userName)
			return intent
		}

		fun intent(activity: Activity): Intent = Intent(activity, GameService::class.java)
	}

	private val m_googleApiClient: GoogleApiClient by lazy {
		GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(Nearby.CONNECTIONS_API)
				.build()
	}

	private val m_notificationBuilder: NotificationCompat.Builder by lazy {
		NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
				.setSmallIcon(R.drawable.ic_launcher_background)
				.setContentTitle(getString(R.string.SERVICE_notification_title))
	}

	private var m_isHost: Boolean = false
	private lateinit var m_connectionId: String
	private lateinit var m_userName: String

	var gameInterface: GameInterface? = null

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
	{
		parseIntent(intent)
		createNotificationChannel()
		goForeground()

		m_googleApiClient.connect()

		sendBroadcast(Intent(ACTION_STARTED))

		return Service.START_NOT_STICKY
	}

	override fun onDestroy()
	{
		super.onDestroy()

		if (m_googleApiClient.isConnected)
		{
			m_googleApiClient.disconnect()
		}
	}

	private fun parseIntent(intent: Intent?)
	{
		intent?.getBooleanExtra(EXTRA_IS_HOST, false)?.let {
			m_isHost = it
		}

		intent?.getStringExtra(EXTRA_USER_NAME)?.let {
			m_userName = it
		}
	}

	private fun createNotificationChannel()
	{
		if (OsUtil.isAtLeast26_O)
		{
			val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

			val channelName = "Service"
			val importance = NotificationManager.IMPORTANCE_LOW
			val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, importance)
			notificationManager.createNotificationChannel(notificationChannel)
		}
	}

	private fun goForeground()
	{
		if (m_isHost)
		{
			//builder.setContentText(getString(R.string.SERVICE_notification_subtitle_host))
			m_notificationBuilder.setContentText("host")
		}
		else
		{
			m_notificationBuilder.setContentText(getString(R.string.SERVICE_notification_subtitle_client))
		}

		startForeground(NOTIFICATION_ID, m_notificationBuilder.build())
	}

	override fun onBind(intent: Intent): IBinder? = LocalBinder()

	override fun onConnected(p0: Bundle?)
	{
		Log.d(TAG, "onConnected")

		if (m_isHost)
		{
			startAdvertising()
		}
		else
		{
			startDiscovery()
		}
	}

	override fun onConnectionSuspended(p0: Int)
	{
		Log.d(TAG, "onConnectionSuspended")
	}

	override fun onConnectionFailed(p0: ConnectionResult)
	{
		Log.d(TAG, "onConnectionFailed")
	}

	private fun startAdvertising()
	{
		Nearby.Connections.startAdvertising(
				m_googleApiClient,
				m_userName,
				SERVICE_ID,
				mConnectionLifecycleCallback,
				AdvertisingOptions(Strategy.P2P_STAR))
				.setResultCallback(
						{ result ->
							if (result.status.isSuccess)
							{
								Log.d(TAG, "We're advertising")

								gameInterface?.onHosting()
							}
							else
							{
								Log.d(TAG, "We were unable to start advertising")
							}
						})
	}

	private fun startDiscovery()
	{
		Nearby.Connections.startDiscovery(
				m_googleApiClient,
				SERVICE_ID,
				mEndpointDiscoveryCallback,
				DiscoveryOptions(Strategy.P2P_STAR))
				.setResultCallback({
					                   status: Status ->
					                   if (status.isSuccess)
					                   {
						                   Log.d(TAG, "We're discovering!")
					                   }
					                   else
					                   {
						                   Log.d(TAG, "We were unable to start discovering.")
					                   }
				                   })
	}

	private val mConnectionLifecycleCallback = object : ConnectionLifecycleCallback()
	{
		override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo)
		{
			Log.d(TAG, "onConnectionInitiated: " + endpointId)

			if (m_isHost)
			{
				Nearby.Connections.acceptConnection(m_googleApiClient, endpointId, PayloadHandler())
			}
			else
			{
				gameInterface?.confirmServerConnection(endpointId, connectionInfo, m_googleApiClient, PayloadHandler())
			}
		}

		override fun onConnectionResult(endpointId: String, connectionInfo: ConnectionResolution)
		{
			Log.d(TAG, endpointId + " onConnectionResult: " + connectionInfo.status)

			if (connectionInfo.status.isSuccess)
			{
				m_connectionId = endpointId

				gameInterface?.onConnection()
			}
		}

		override fun onDisconnected(endpointId: String)
		{
			Log.d(TAG, "onDisconnected: " + endpointId)
		}
	}

	private val mEndpointDiscoveryCallback = object : EndpointDiscoveryCallback()
	{
		override fun onEndpointFound(endpointId: String, discoveredEndpointInfo: DiscoveredEndpointInfo)
		{
			Log.d(TAG, "An endpoint was found! " + endpointId)

			Nearby.Connections.requestConnection(
					m_googleApiClient,
					m_userName,
					endpointId,
					mConnectionLifecycleCallback)
					.setResultCallback { status ->
						if (status.isSuccess)
						{
							// We successfully requested a connection. Now both sides
							// must accept before the connection is established.
							Log.d(TAG, "Connection requested: " + endpointId)
						}
						else
						{
							// Nearby Connections failed to request the connection.
							Log.d(TAG, "Failed to request connection: " + endpointId)
						}
					}
		}

		override fun onEndpointLost(endpointId: String)
		{
			Log.d(TAG, "A previously discovered endpoint has gone away. : " + endpointId)
		}
	}

	inner class PayloadHandler : PayloadCallback()
	{
		val TAG = PayloadHandler::class.simpleName

		override fun onPayloadReceived(endpointId: String?, payload: Payload?)
		{
			Log.d(TAG, endpointId + " onPayloadReceived")

			payload?.let {
				it.asBytes()?.let { bytes ->
					val message = String(bytes, Charset.defaultCharset())
					if (!TextUtils.isEmpty(message))
					{
						gameInterface?.onMessageReceived(message)
						Log.d(TAG, "message: " + message)
					}
				}
			}
		}

		override fun onPayloadTransferUpdate(endpointId: String?, update: PayloadTransferUpdate?)
		{
			Log.d(TAG, endpointId + " onPayloadTransferUpdate " + update?.status)
		}
	}

	inner class LocalBinder : Binder()
	{
		internal // Return this instance of LocalService so clients can call public methods
		val service: GameService
			get() = this@GameService
	}

	fun sendMessage(message: String)
	{
		if (!TextUtils.isEmpty(message))
		{
			Log.d(TAG, "Sending message to : " + m_connectionId)
			Nearby.Connections.sendPayload(m_googleApiClient, m_connectionId, Payload.fromBytes(message.toByteArray()))
		}
	}
}
