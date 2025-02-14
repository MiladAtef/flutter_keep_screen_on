package dev.craftsoft.keepscreenon

import android.app.Activity
import android.util.Log
import android.view.WindowManager
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

class KeepScreenOnPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
  private lateinit var channel: MethodChannel
  private var activity: Activity? = null

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, CHANNEL_NAME)
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    when (call.method) {
      "isOn" -> onMethodCallIsOn(result)
      "turnOn" -> onMethodCallTurnOn(call, result)
      "isAllowLockWhileScreenOn" -> onMethodCallIsAllowLockWhileScreenOn(result)
      "addAllowLockWhileScreenOn" -> onMethodCallAddAllowLockWhileScreenOn(call, result)
      else -> result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    activity = binding.activity
  }

  override fun onDetachedFromActivityForConfigChanges() {
    activity = null
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    activity = binding.activity
  }

  override fun onDetachedFromActivity() {
    activity = null
  }

  private fun onMethodCallIsOn(result: Result) {
    val window = activity?.window ?: return result.error("not-found-activity", "Activity not found.", null)
    val hasKeepScreenOn = (window.attributes.flags and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) != 0
    result.success(hasKeepScreenOn)
  }

  private fun onMethodCallTurnOn(call: MethodCall, result: Result) {
    val window = activity?.window ?: return result.error("not-found-activity", "Activity not found.", null)
    val on = call.argument<Boolean>("on") ?: false
    val withAllowLockWhileScreenOn = call.argument<Boolean>("withAllowLockWhileScreenOn") ?: false

    val flag = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            if (withAllowLockWhileScreenOn) WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON else 0

    Log.d(TAG, "flag=$flag")

    if (on) {
      window.addFlags(flag)
    } else {
      window.clearFlags(flag)
    }

    result.success(true)
  }

  private fun onMethodCallIsAllowLockWhileScreenOn(result: Result) {
    val window = activity?.window ?: return result.error("not-found-activity", "Activity not found.", null)
    val hasAllowLockWhileScreenOn = (window.attributes.flags and WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON) != 0
    result.success(hasAllowLockWhileScreenOn)
  }

  private fun onMethodCallAddAllowLockWhileScreenOn(call: MethodCall, result: Result) {
    val window = activity?.window ?: return result.error("not-found-activity", "Activity not found.", null)
    val on = call.argument<Boolean>("on") ?: false
    val flag = WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON

    if (on) {
      window.addFlags(flag)
    } else {
      window.clearFlags(flag)
    }

    result.success(true)
  }

  companion object {
    const val TAG = "KeepScreenOnPlugin"
    const val CHANNEL_NAME = "dev.craftsoft/keep_screen_on"
  }
}
