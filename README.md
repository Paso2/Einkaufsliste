# Einkaufsliste (Android)

Eine minimalistische, schnelle Android-Einkaufslisten-App (Kotlin & Jetpack Compose, Material 3, Dunkelmodus).

## APK Download (Neueste Version)

👉 **[Neueste APK direkt herunterladen (Einkaufsliste.apk)](https://github.com/Paso2/Einkaufsliste/releases/latest/download/Einkaufsliste.apk)**

Alle Releases findest du außerdem auf der [GitHub Releases-Seite](https://github.com/Paso2/Einkaufsliste/releases).

> **Hinweis zu Updates:** Sobald ein neues Release erscheint, kannst du einfach die neue `Einkaufsliste.apk` herunterladen und installieren – deine bestehenden Einkaufslisten, Kategorien, Produkte und Häufigkeiten bleiben dabei vollständig erhalten (sofern alle Releases mit demselben Keystore signiert sind, siehe unten).

---

## Funktionen

- **Umfangreicher Produktkatalog (520+ Artikel)**: Lebensmittel, Haushalts- und Drogerieprodukte ohne unnötige Markenvarianten, lokal in SQLite/Room gespeichert. Produkte können jederzeit manuell hinzugefügt, bearbeitet oder gelöscht werden.
- **21 Standard-Kategorien & freie Sortierung**: Kategorien (z. B. Obst, Gemüse & Kräuter, Backwaren & Brot, Milchprodukte & Eier, …) sind frei anpassbar, hinzufügbar, löschbar und in ihrer Reihenfolge verschiebbar. Die Einkaufsliste sortiert offene Artikel exakt nach dieser Reihenfolge.
- **Notizen & Mengen pro Artikel**: Beim Hinzufügen lassen sich optionale Mengen, Einheiten (`l`, `ml`, `g`, `kg`, `Stk`, `Pkg`, …) und Notizen (z. B. *Magerquark*, *Pfefferminze*) angeben – direkt sichtbar in der Einkaufsansicht.
- **Duplikat-Erkennung & Mengen-Zusammenführung**: Steht ein Produkt bereits auf der Liste, fragt ein Dialog, ob die Einträge zusammengeführt werden sollen (inkl. Einheiten-Umrechnung wie `1 l + 2 l = 3 l` oder `500 g + 1 kg = 1,5 kg`).
- **Einkaufsmodus**: Ein Tipp markiert Artikel als gekauft und schiebt sie nach ganz unten; erneutes Antippen stellt sie wieder an den Platz ihrer Kategorie.
- **Mehrere Listen & Häufigkeits-Vorschläge**: Beliebig viele benennbare Listen. Beim Erstellen einer neuen Liste und beim Hinzufügen werden häufig gekaufte Artikel direkt vorgeschlagen.
- **Dunkelmodus**: Wählbar zwischen Systemstandard, Hell und Dunkel.

---

## Einrichtung für nahtlose APK-Updates (Keystore in GitHub Secrets)

Damit Android neue Versionen direkt über eine bereits installierte APK aktualisieren kann (ohne Deinstallation und Datenverlust), müssen alle Releases mit demselben Keystore signiert werden:

1. Einmalig lokal einen Keystore erzeugen:
   ```bash
   keytool -genkey -v -keystore release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias einkaufsliste
   ```
2. In den GitHub Repository-Einstellungen unter **Settings → Secrets and variables → Actions** folgende Secrets anlegen:
   - `KEYSTORE_BASE64`: Inhalt von `base64 -w 0 release.jks`
   - `KEYSTORE_PASSWORD`: Dein Keystore-Passwort
   - `KEY_ALIAS`: `einkaufsliste`
   - `KEY_PASSWORD`: Dein Key-Passwort
3. Neues Release veröffentlichen (z. B. `git tag v1.0.0 && git push origin v1.0.0`) – GitHub Actions baut und veröffentlicht automatisch `Einkaufsliste.apk`.