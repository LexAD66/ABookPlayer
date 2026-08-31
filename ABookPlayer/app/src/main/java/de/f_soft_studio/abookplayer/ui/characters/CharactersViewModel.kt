package de.f_soft_studio.abookplayer.ui.characters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.f_soft_studio.abookplayer.data.repository.AudiobookRepository
import de.f_soft_studio.abookplayer.domain.model.Audiobook
import de.f_soft_studio.abookplayer.domain.model.BookCharacter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel für den CharactersScreen (Personenregister / Buchfiguren).
 */
class CharactersViewModel(
    private val repository: AudiobookRepository
) : ViewModel() {

    private val _audiobook = MutableStateFlow<Audiobook?>(null)
    val audiobook: StateFlow<Audiobook?> = _audiobook.asStateFlow()

    private val _characters = MutableStateFlow<List<BookCharacter>>(emptyList())
    val characters: StateFlow<List<BookCharacter>> = _characters.asStateFlow()

    fun loadAudiobook(audiobookId: Long) {
        viewModelScope.launch {
            _audiobook.value = repository.getAudiobookById(audiobookId)
            repository.getCharactersForAudiobook(audiobookId).collect { list ->
                _characters.value = list
            }
        }
    }

    fun addOrUpdateCharacter(name: String, role: String, description: String, relationship: String, isPrimary: Boolean, characterId: Long = 0) {
        val currentBook = _audiobook.value ?: return
        viewModelScope.launch {
            val char = BookCharacter(
                id = characterId,
                audiobookId = currentBook.id,
                name = name,
                role = role,
                description = description,
                relationship = relationship,
                isPrimary = isPrimary
            )
            repository.saveCharacter(char)
        }
    }

    fun deleteCharacter(characterId: Long) {
        viewModelScope.launch {
            repository.deleteCharacter(characterId)
        }
    }
}
