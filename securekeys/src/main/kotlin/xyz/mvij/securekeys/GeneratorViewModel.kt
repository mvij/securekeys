package xyz.mvij.securekeys

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SliderState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/** Survives rotation; intentionally not persisted so the passphrase never touches saved state or disk. */
@OptIn(ExperimentalMaterial3Api::class)
class GeneratorViewModel : ViewModel() {
    val passphrase = TextFieldState()
    val length = SliderState(
        PasswordGenerator.MIN_LENGTH.toFloat(),
        PasswordGenerator.MAX_LENGTH - PasswordGenerator.MIN_LENGTH - 1,
        PasswordGenerator.MIN_LENGTH.toFloat()..PasswordGenerator.MAX_LENGTH.toFloat(),
    )
    var mixedCase by mutableStateOf(false)
    var special by mutableStateOf(false)
    var numbers by mutableStateOf(false)
    var password by mutableStateOf<String?>(null)

    /** Bumps on every Generate, so the key re-cuts even when the password comes out the same. */
    var generation by mutableIntStateOf(0)
        private set

    /** The generation whose cut has played, so coming back to the screen shows the key at rest. */
    var cutPlayed = 0
    var copied = false

    val canGenerate: Boolean get() = passphrase.text.isNotBlank()

    fun generate() {
        password = PasswordGenerator.generate(
            passphrase.text.toString(), length.value.toInt(), mixedCase, special, numbers,
        )
        generation++
    }
}
