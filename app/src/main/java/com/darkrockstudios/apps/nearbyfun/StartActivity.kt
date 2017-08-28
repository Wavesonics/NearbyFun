package com.darkrockstudios.apps.nearbyfun

import android.Manifest
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_start.*
import org.jetbrains.anko.startActivity

class StartActivity : AppCompatActivity(), PermissionListener
{
	private val TAG = StartActivity::class.simpleName

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_start)

		Dexter.withActivity(this)
				.withPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
				.withListener(this)
				.check()
	}

	override fun onPermissionGranted(response: PermissionGrantedResponse?)
	{
		Log.d(TAG, "onPermissionGranted")
		START_start_button.isEnabled = true
	}

	override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?)
	{
		Log.d(TAG, "onPermissionRationaleShouldBeShown")
	}

	override fun onPermissionDenied(response: PermissionDeniedResponse?)
	{
		Log.d(TAG, "onPermissionDenied")
	}

	fun onStartClicked(view: View)
	{
		val userNameValue = START_user_name.text
		if (!TextUtils.isEmpty(userNameValue))
		{
			val userNameStr = userNameValue.toString()

			startActivity<ClientOrHostActivity>("m_userName" to userNameStr)
		}
	}
}
