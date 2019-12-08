package info.nightscout.androidaps.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle
import info.nightscout.androidaps.logging.L
import org.slf4j.LoggerFactory

object ActivityMonitor : Application.ActivityLifecycleCallbacks {
    private val log = LoggerFactory.getLogger(L.CORE)
    override fun onActivityPaused(activity: Activity?) {
        val name = activity?.javaClass?.simpleName ?: return
        val resumed = SP.getLong("Monitor_" + name + "_" + "resumed", 0)
        if (resumed == 0L) {
            log.debug("onActivityPaused: $name resumed == 0")
            return
        }
        val elapsed = DateUtil.now() - resumed
        val total = SP.getLong("Monitor_" + name + "_total", 0)
        if (total == 0L) {
            SP.putLong("Monitor_" + name + "_start", DateUtil.now())
        }
        SP.putLong("Monitor_" + name + "_total", total + elapsed)
        log.debug("onActivityPaused: $name elapsed=$elapsed total=${total + elapsed}")
    }

    override fun onActivityResumed(activity: Activity?) {
        val name = activity?.javaClass?.simpleName ?: return
        log.debug("onActivityResumed: $name")
        SP.putLong("Monitor_" + name + "_" + "resumed", DateUtil.now())
    }

    override fun onActivityStarted(activity: Activity?) {
    }

    override fun onActivityDestroyed(activity: Activity?) {
    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
    }

    override fun onActivityStopped(activity: Activity?) {
    }

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
    }

    fun toText(): String {
        val keys: Map<String, *> = SP.getAll()
        var result = ""
        for ((key, value) in keys)
            if (key.startsWith("Monitor") && key.endsWith("total")) {
                val activity = key.split("_")[1]
                val seconds = value as Long / 1000
                val start = SP.getLong(key.replace("total", "start"), 0)
                val days = T.msecs(DateUtil.now() - start).days()
                result += "$activity: $seconds s in $days days\n"
            }
        return result
    }

    fun reset() {
        val keys: Map<String, *> = SP.getAll()
        for ((key, _) in keys)
            if (key.startsWith("Monitor") && key.endsWith("total")) {
                SP.remove(key)
                SP.remove(key.replace("total", "start"))
                SP.remove(key.replace("total", "resumed"))
            }
    }
}