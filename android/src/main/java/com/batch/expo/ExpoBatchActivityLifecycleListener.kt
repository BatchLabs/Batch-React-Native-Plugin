package expo.modules.batch

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log;
import expo.modules.core.interfaces.ReactActivityLifecycleListener

class ExpoBatchActivityLifecycleListener : ReactActivityLifecycleListener {

    override fun onCreate(activity: Activity?, savedInstanceState: Bundle?) {
        Log.warn("BatchExpoPackage", "hi from expo modules")
    }

    override fun onNewIntent(intent: Intent?): Boolean {
        Log.warn("BatchExpoPackage", "hi from expo modules onNewIntent")
        return true
    }
}
