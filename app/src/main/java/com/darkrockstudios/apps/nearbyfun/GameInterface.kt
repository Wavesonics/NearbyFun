package com.darkrockstudios.apps.nearbyfun

import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.nearby.connection.ConnectionInfo

/**
 * Created by adamw on 8/27/2017.
 */
interface GameInterface
{
	fun confirmServerConnection(endpointId: String, connectionInfo: ConnectionInfo, googleApiClient: GoogleApiClient, payloadHandler: GameService.PayloadHandler)
	fun onHosting()
	fun onConnection()

	fun onMessageReceived(message: String)
}