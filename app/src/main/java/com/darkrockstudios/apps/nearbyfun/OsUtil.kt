package com.darkrockstudios.apps.nearbyfun


/**
 * Created by adamw on 8/27/2017.
 */
object OsUtil
{
	/**
	 * @return True if the version of Android that we're running on is at least M
	 * *  (API level 23).
	 */
	val isAtLeast23_M: Boolean by lazy { apiVersion >= android.os.Build.VERSION_CODES.M }

	/**
	 * @return True if the version of Android that we're running on is at least N
	 * *  (API level 24).
	 */
	val isAtLeast24_N: Boolean by lazy { apiVersion >= android.os.Build.VERSION_CODES.N }

	/**
	 * @return True if the version of Android that we're running on is at least O
	 * *  (API level 26).
	 */
	val isAtLeast26_O: Boolean by lazy { apiVersion >= android.os.Build.VERSION_CODES.O }

	/**
	 * @return The Android API version of the OS that we're currently running on.
	 */
	val apiVersion: Int by lazy { android.os.Build.VERSION.SDK_INT }
}