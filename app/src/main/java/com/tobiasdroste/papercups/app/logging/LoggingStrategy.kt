package com.tobiasdroste.papercups.app.logging

import android.util.Log
import timber.log.Timber

/**
 * Logging strategy for the application.
 * 
 * Log Level Usage Guidelines:
 * - VERBOSE (v): Verbose logging for detailed tracing. Only use in development.
 * - DEBUG (d): Debug information useful during development. Not visible in production.
 * - INFO (i): General information about app state that's useful for verifying normal operation.
 * - WARN (w): Warnings that indicate potential issues but don't prevent normal operation.
 * - ERROR (e): Errors that prevent normal operation of a specific component.
 * - ASSERT/WTF (wtf): Critical errors that should never happen and require immediate attention.
 */
object LoggingStrategy {

    /**
     * Initializes the logging strategy for the application.
     * 
     * @param isDebugBuild Whether the application is running in debug mode.
     */
    fun init(isDebugBuild: Boolean) {
        if (isDebugBuild) {
            // In debug builds, log everything
            Timber.plant(Timber.DebugTree())
        } else {
            // In release builds, only log warnings and errors
            Timber.plant(ReleaseTree())
        }
    }

    /**
     * Custom Timber tree for release builds.
     * Only logs warnings and errors to avoid leaking sensitive information.
     */
    private class ReleaseTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority < Log.INFO) {
                // Skip verbose and debug logs in release builds
                return
            }

            // Format the log message with consistent structure
            val formattedMessage = formatLogMessage(tag, message)
            
            // Log to Android's logging system
            when (priority) {
                Log.INFO -> Log.i(tag ?: "PaperCups", formattedMessage)
                Log.WARN -> Log.w(tag ?: "PaperCups", formattedMessage, t)
                Log.ERROR, Log.ASSERT -> {
                    Log.e(tag ?: "PaperCups", formattedMessage, t)
                    // Here you would typically report to a crash reporting service
                    reportCrash(priority, tag, message, t)
                }
            }
        }

        /**
         * Formats a log message with a consistent structure.
         */
        private fun formatLogMessage(tag: String?, message: String): String {
            return "[$tag] $message"
        }

        /**
         * Reports a crash or error to a crash reporting service.
         * This is a placeholder for actual crash reporting implementation.
         */
        private fun reportCrash(priority: Int, tag: String?, message: String, t: Throwable?) {
            // TODO: Implement actual crash reporting service integration
            // For now, just log to Android's logging system
            if (priority == Log.ERROR || priority == Log.ASSERT) {
                Log.e("CrashReport", "Error that would be reported: [$tag] $message", t)
            }
        }
    }
}