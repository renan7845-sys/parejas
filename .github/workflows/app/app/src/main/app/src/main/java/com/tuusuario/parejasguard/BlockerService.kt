package com.tuusuario.parejasguard

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class BlockerService : AccessibilityService() {

    companion object {
        private const val FACEBOOK_PACKAGE = "com.facebook.katana"
        private val ALLOWED_KEYWORDS = listOf("Parejas", "Dating", "Match")
        private const val COOLDOWN_MS = 1500L
        private var lastKick = 0L
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.packageName != FACEBOOK_PACKAGE) return

        val now = System.currentTimeMillis()
        if (now - lastKick < COOLDOWN_MS) return

        val screenText = StringBuilder()
        rootInActiveWindow?.let { collectText(it, screenText) }

        val allowed = ALLOWED_KEYWORDS.any { screenText.contains(it, ignoreCase = true) }

        if (!allowed) {
            lastKick = now
            Log.d("Blocker", "Pantalla no permitida, sacando al usuario")
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    private fun collectText(node: AccessibilityNodeInfo, out: StringBuilder) {
        node.text?.let { out.append(it).append(" ") }
        node.contentDescription?.let { out.append(it).append(" ") }
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { collectText(it, out) }
        }
    }

    override fun onInterrupt() {}
}
