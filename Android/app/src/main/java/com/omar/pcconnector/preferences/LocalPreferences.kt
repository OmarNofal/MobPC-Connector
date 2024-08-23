package com.omar.pcconnector.preferences

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.text.util.LocalePreferences


val LocalUserPreferences = staticCompositionLocalOf<UserPreferences> { throw IllegalStateException("No value set") }