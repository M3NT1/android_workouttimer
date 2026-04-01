package com.menti.workoutTimer

import android.content.Context
import android.content.res.Configuration
import java.util.*

/**
 * Segédosztály a nyelvváltáshoz
 */
object LocaleHelper {

    fun applyLocale(context: Context): Context {
        val prefs = WorkoutPreferences(context)
        val language = prefs.language
        return updateResources(context, language)
    }

    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)
    }

    // Régebbi API verziókhoz, ha szükséges
    fun updateResourcesLegacy(context: Context, language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val resources = context.resources
        val configuration = resources.configuration
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }
}
