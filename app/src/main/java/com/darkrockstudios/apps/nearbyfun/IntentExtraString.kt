package com.darkrockstudios.apps.nearbyfun

import android.app.Activity
import kotlin.reflect.KProperty

/**
 * Created by adamw on 8/27/2017.
 */
class IntentExtraString
{
	operator fun getValue(activity: Activity, property: KProperty<*>): String =
			activity.intent.getStringExtra(property.name)

	operator fun setValue(activity: Activity, property: KProperty<*>, value: String)
	{
		activity.intent.putExtra(property.name, value)
	}
}