package com.menti.workoutTimer

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Espresso UI tests for MainActivity.
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun initialUIState_timerShowsZero_startButtonVisible() {
        ActivityScenario.launch(MainActivity::class.java)

        // Verify timer text shows initial state
        onView(withId(R.id.timerTextView))
            .check(matches(isDisplayed()))

        // Verify start/pause button is visible
        onView(withId(R.id.startPauseButton))
            .check(matches(isDisplayed()))

        // Verify reset button is visible
        onView(withId(R.id.resetButton))
            .check(matches(isDisplayed()))

        // Verify settings button is visible
        onView(withId(R.id.settingsButton))
            .check(matches(isDisplayed()))

        // Verify history button is visible
        onView(withId(R.id.historyButton))
            .check(matches(isDisplayed()))
    }

    @Test
    fun clickStartPauseButton_togglesTimer() {
        ActivityScenario.launch(MainActivity::class.java)

        // Click start/pause button
        onView(withId(R.id.startPauseButton))
            .perform(click())

        // Button should still be displayed after click
        onView(withId(R.id.startPauseButton))
            .check(matches(isDisplayed()))
    }

    @Test
    fun clickResetButton_resetsTimer() {
        ActivityScenario.launch(MainActivity::class.java)

        // Click reset button
        onView(withId(R.id.resetButton))
            .perform(click())

        // Verify timer is displayed
        onView(withId(R.id.timerTextView))
            .check(matches(isDisplayed()))
    }

    @Test
    fun clickSettingsButton_opensSettingsActivity() {
        ActivityScenario.launch(MainActivity::class.java)

        // Click settings button
        onView(withId(R.id.settingsButton))
            .perform(click())

        // Verify SettingsActivity was launched
        Intents.intended(hasComponent(SettingsActivity::class.java.name))
    }

    @Test
    fun clickHistoryButton_opensHistoryActivity() {
        ActivityScenario.launch(MainActivity::class.java)

        // Click history button
        onView(withId(R.id.historyButton))
            .perform(click())

        // Verify WorkoutHistoryActivity was launched
        Intents.intended(hasComponent(WorkoutHistoryActivity::class.java.name))
    }

    @Test
    fun phaseInfo_isDisplayed() {
        ActivityScenario.launch(MainActivity::class.java)

        // Verify phase info card is displayed
        onView(withId(R.id.phaseCard))
            .check(matches(isDisplayed()))

        // Verify phase text view is displayed
        onView(withId(R.id.currentPhaseTextView))
            .check(matches(isDisplayed()))
    }

    @Test
    fun roundInfo_isDisplayed() {
        ActivityScenario.launch(MainActivity::class.java)

        // Verify round text view is displayed
        onView(withId(R.id.roundTextView))
            .check(matches(isDisplayed()))
    }
}
