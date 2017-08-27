package com.darkrockstudios.apps.nearbyfun

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.TextView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Status
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import kotlinx.android.synthetic.main.activity_main.*
import java.nio.charset.Charset


class MainActivity : AppCompatActivity(),
                     GoogleApiClient.ConnectionCallbacks,
                     GoogleApiClient.OnConnectionFailedListener
{
	private val TAG = MainActivity::class.simpleName

	private var m_googleApiClient: GoogleApiClient? = null

	private val SERVICE_ID = "NearbyFun"

	private val m_userName: String by IntentExtraString()

	private var m_connectionId: String? = null

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		user_name_view.text = m_userName

		m_googleApiClient = GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(Nearby.CONNECTIONS_API)
				.build()
	}

	public override fun onStart()
	{
		super.onStart()
		m_googleApiClient?.connect()
	}

	public override fun onStop()
	{
		super.onStop()

		m_googleApiClient?.let {
			if (it.isConnected)
			{
				it.disconnect()
			}
		}
	}

	override fun onConnected(p0: Bundle?)
	{
		Log.d(TAG, "onConnected")

		advertise_button.isEnabled = true
		discover_button.isEnabled = true
	}

	private fun disableButtons()
	{
		advertise_button.isEnabled = false
		discover_button.isEnabled = false
	}

	fun onAdvertiseClicked(view: View)
	{
		disableButtons()
		startAdvertising()
	}

	fun onDiscoverClicked(view: View)
	{
		disableButtons()
		startDiscovery()
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
							}
							else
							{
								Log.d(TAG, "We were unable to start advertising")
							}
						})
	}

	private val mConnectionLifecycleCallback = object : ConnectionLifecycleCallback()
	{
		override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo)
		{
			Log.d(TAG, "onConnectionInitiated: " + endpointId)

			AlertDialog.Builder(this@MainActivity)
					.setTitle("Accept connection to " + connectionInfo.getEndpointName())
					.setMessage("Confirm if the code " + connectionInfo.getAuthenticationToken() + " is also displayed on the other device")
					.setPositiveButton("Accept", { dialog, which ->
						// The user confirmed, so we can accept the connection.
						Nearby.Connections.acceptConnection(m_googleApiClient, endpointId, PayloadHandler())
					})
					.setNegativeButton(android.R.string.cancel, { dialog, which ->
						// The user canceled, so we should reject the connection.
						Nearby.Connections.rejectConnection(m_googleApiClient, endpointId)
					})
					.setIcon(android.R.drawable.ic_dialog_alert)
					.show()
		}

		override fun onConnectionResult(endpointId: String, connectionInfo: ConnectionResolution)
		{
			Log.d(TAG, endpointId + " onConnectionResult: " + connectionInfo.status)

			if (connectionInfo.status.isSuccess)
			{
				m_connectionId = endpointId
			}
		}

		override fun onDisconnected(endpointId: String)
		{
			Log.d(TAG, "onDisconnected: " + endpointId)
		}
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

	fun onSendClick(view: View)
	{
		val message = TEST_send_mesage.text.toString()
		TEST_send_mesage.setText("")

		m_connectionId?.let { connectionId ->
			if (!TextUtils.isEmpty(message) && m_connectionId != null)
			{
				showMessage(message)

				Log.d(TAG, "Sending message to : " + connectionId)
				Nearby.Connections.sendPayload(m_googleApiClient, connectionId, Payload.fromBytes(message.toByteArray()))
			}
		}
	}

	fun showMessage(message: String)
	{
		Log.d(TAG, "message: " + message)
		//Snackbar.make(user_name_view, message, Snackbar.LENGTH_LONG).show()
		val curText = TEST_chat_list.text

		TEST_chat_list.setText(message + "\n" + curText, TextView.BufferType.NORMAL)
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
						showMessage(message)
					}
				}
			}
		}

		override fun onPayloadTransferUpdate(endpointId: String?, update: PayloadTransferUpdate?)
		{
			Log.d(TAG, endpointId + " onPayloadTransferUpdate " + update?.status)
		}

	}
}
