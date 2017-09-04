package com.darkrockstudios.apps.nearbyfun

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionInfo
import kotlinx.android.synthetic.main.activity_client_or_host.*
import org.jetbrains.anko.startActivity


class ClientOrHostActivity : BaseGameActivity(), GameInterface
{
	private val TAG = ClientOrHostActivity::class.simpleName

	private val m_userName: String by IntentExtraString()

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_client_or_host)

		user_name_view.text = m_userName

		registerStartReceiver()
	}

	override fun onDestroy()
	{
		super.onDestroy()

		unregisterStartReceiver()
		unbindFromGameService()
	}

	private fun registerStartReceiver()
	{
		val filter = IntentFilter()
		filter.addAction(GameService.ACTION_STARTED)

		m_serviceStartReceiverRegistered = true
		registerReceiver(m_serviceStartReceiver, filter)
	}

	private fun unregisterStartReceiver()
	{
		if (m_serviceStartReceiverRegistered)
		{
			unregisterReceiver(m_serviceStartReceiver)
			m_serviceStartReceiverRegistered = false
		}
	}

	private var m_serviceStartReceiverRegistered = false
	private val m_serviceStartReceiver = object : BroadcastReceiver()
	{
		override fun onReceive(context: Context, intent: Intent)
		{
			bindToGameService()
		}
	}

	private fun disableButtons()
	{
		advertise_button.isEnabled = false
		discover_button.isEnabled = false
	}

	fun onAdvertiseClicked(view: View)
	{
		disableButtons()

		GameService.startHost(this, m_userName)
	}

	fun onDiscoverClicked(view: View)
	{
		disableButtons()

		GameService.startClient(this, m_userName)
	}

	/********************************************************
	 * Game Interface
	 */

	override fun confirmServerConnection(endpointId: String,
	                                     connectionInfo: ConnectionInfo,
	                                     googleApiClient: GoogleApiClient,
	                                     payloadHandler: GameService.PayloadHandler)
	{
		AlertDialog.Builder(this)
				.setTitle("Accept connection to " + connectionInfo.getEndpointName())
				.setMessage("Confirm if the code " + connectionInfo.getAuthenticationToken() + " is also displayed on the other device")
				.setPositiveButton("Accept", { dialog, which ->
					// The user confirmed, so we can accept the connection.
					Nearby.Connections.acceptConnection(googleApiClient, endpointId, payloadHandler)
				})
				.setNegativeButton(android.R.string.cancel, { dialog, which ->
					// The user canceled, so we should reject the connection.
					Nearby.Connections.rejectConnection(googleApiClient, endpointId)
				})
				.setIcon(android.R.drawable.ic_dialog_alert)
				.show()
	}

	override fun onHosting()
	{
		startActivity<GameActivity>()
		finish()
	}

	override fun onConnection()
	{
		startActivity<GameActivity>()
		finish()
	}

	override fun onMessageReceived(message: String)
	{
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
}
