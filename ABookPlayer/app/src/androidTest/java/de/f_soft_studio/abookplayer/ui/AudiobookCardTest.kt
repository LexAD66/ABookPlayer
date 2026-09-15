package de.f_soft_studio.abookplayer.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.f_soft_studio.abookplayer.domain.model.Audiobook
import de.f_soft_studio.abookplayer.ui.library.components.AudiobookItemCard
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation-Test (Compose UI) für Bibliothekskarten.
 */
@RunWith(AndroidJUnit4::class)
class AudiobookCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testAudiobookItemCard_rendersTitleAuthorAndHandlesClick() {
        var clicked = false
        val testBook = Audiobook(
            id = 1L,
            title = "Der Steppenwolf",
            author = "Hermann Hesse",
            narrator = "Ulrich Matthes",
            filePath = "/storage/emulated/0/ABook/steppenwolf.abook",
            coverUri = null,
            duration = 3600000L,
            currentPosition = 1800000L,
            lastPlayed = System.currentTimeMillis(),
            isFavorite = true
        )

        composeTestRule.setContent {
            AudiobookItemCard(
                audiobook = testBook,
                isSelected = false,
                isFavorite = true,
                isCurrentlyPlaying = false,
                onClick = { clicked = true },
                onOpenDetails = {}
            )
        }

        // Überprüfe Sichtbarkeit der Texte
        composeTestRule.onNodeWithText("Der Steppenwolf").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hermann Hesse").assertIsDisplayed()
        composeTestRule.onNodeWithText("Gelesen von Ulrich Matthes").assertIsDisplayed()
        composeTestRule.onNodeWithText("50% gehört").assertIsDisplayed()

        // Klick-Aktion prüfen
        composeTestRule.onNodeWithText("Der Steppenwolf").performClick()
        assertTrue("Karte muss Klick-Event auslösen", clicked)
    }
}
