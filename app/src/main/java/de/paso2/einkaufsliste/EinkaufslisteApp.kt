package de.paso2.einkaufsliste

import android.app.Application
import de.paso2.einkaufsliste.data.AppDatabase
import de.paso2.einkaufsliste.data.AppPreferences

class EinkaufslisteApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val preferences: AppPreferences by lazy { AppPreferences(this) }
}
