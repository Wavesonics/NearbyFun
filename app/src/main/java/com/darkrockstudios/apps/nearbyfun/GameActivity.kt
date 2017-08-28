package com.darkrockstudios.apps.nearbyfun

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.nearby.connection.ConnectionInfo
import kotlinx.android.synthetic.main.activity_game.*

class GameActivity : BaseGameActivity()
{
	private val TAG = GameActivity::class.simpleName

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_game)
	}

	override fun onStart()
	{
		super.onStart()

		bindToGameService()
	}

	override fun onStop()
	{
		super.onStop()

		unbindFromGameService()
	}

	fun onSendClick(view: View)
	{
		val message = TEST_send_mesage.text.toString()
		TEST_send_mesage.setText("")

		showMessage(message)
		m_service?.sendMessage(message)
	}

	fun showMessage(message: String)
	{
		Log.d(TAG, "message: " + message)
		//Snackbar.make(user_name_view, message, Snackbar.LENGTH_LONG).show()
		val curText = TEST_chat_list.text

		TEST_chat_list.setText(message + "\n" + curText, TextView.BufferType.NORMAL)
	}

	/********************************************************
	 * Game Interface
	 */

	override fun confirmServerConnection(endpointId: String, connectionInfo: ConnectionInfo, googleApiClient: GoogleApiClient, payloadHandler: GameService.PayloadHandler)
	{

	}

	override fun onHosting()
	{

	}

	override fun onConnection()
	{

	}

	override fun onMessageReceived(message: String)
	{
		showMessage(message)
	}
}
