# GP Slideshow (100% vibe coded, use at own risk!)

Android-app som visar ett bildspel med bilder och videor från ditt Google
Photos-konto. Du väljer själv vilka medier som ska visas via Googles
Photo Picker, och hur länge varje bild och video ska visas.

Appen använder **Google Photos Picker API**. Det är den väg Google tillåter
för nya appar sedan 2025 – det äldre Library API:et är stängt för nya
applikationer. Priset är att du väljer ut medierna i en väljare som Google
drar, i stället för att appen kan läsa hela biblioteket.

## Funktioner

- Logga in med ditt Google-konto (OAuth, scope `photospicker.mediaitems.readonly`)
- Välj bilder och videor i Google Photos-väljaren (öppnas i webbläsaren)
- Bildspel i helskärm: bilder via Coil, videor via ExoPlayer (Media3)
- Inställningar:
  - Visningstid per bild, 1–120 sekunder
  - Max videolängd, 0–300 sekunder (0 = spela hela videon)
  - Upprepa bildspelet (loop)
- Skärmen hålls vaken medan bildspelet körs

## Kom igång

1. Öppna projektet i **Android Studio** (Koala eller nyare). Om Android
   Studio påpekar att Gradle-wrappern är ofullständig (jar-filen ingår inte
   i repot), kör `gradle wrapper --gradle-version 8.9` i projektroten eller
   låt Android Studio skapa/reparera wrappern. Låt sedan Gradle synka och
   bygg till telefon med Run ▶.

2. Innan inloggningen fungerar måste du konfigurera Google Cloud (nästa
   avsnitt) och byta ut projekt-ID:t i
   `app/src/main/java/com/staffang/gpslideshow/AppConfig.kt`.

## Konfigurera Google Cloud (engångsjobb)

1. Gå till [Google Cloud Console](https://console.cloud.google.com/) och
   skapa ett projekt (eller använd ett befintligt).
2. Aktivera **Google Photos Picker API**
   ([direktlänk](https://console.cloud.google.com/apis/library/photospicker.googleapis.com)).
3. Under **API:er och tjänster → OAuth consent screen**:
   - Välj *External*.
   - Lägg till scopet
     `https://www.googleapis.com/auth/photospicker.mediaitems.readonly`.
   - Publiceringsstatus: *Testing* räcker för privat bruk – lägg till din
     egen Google-adress som testanvändare.
4. Under **Credentials → Create credentials → OAuth client ID**:
   - Typ: **Android**.
   - Paketnamn: `com.staffang.gpslideshow`.
   - SHA-1: kör `./gradlew signingReport` i projektet och kopiera SHA-1 för
     debug-varianten (eller använd `keytool -list -v -keystore
     ~/.android/debug.keystore -alias androiddebugkey -password android`).
5. Kopiera projekt-ID:t (syns överst i Cloud Console) och klistra in det i
   `AppConfig.GOOGLE_CLOUD_PROJECT_ID`.

## Så här använder du appen

1. Tryck **Välj bilder och videor** – appen loggar in dig och öppnar
   Google Photos-väljaren i webbläsaren (den stänger sig själv när du är
   klar, tack vare `/autoclose`).
2. Välj de medier du vill visa och bekräfta.
3. Tryck **Starta bildspel**. Stäng av/ändra tiderna under **Inställningar**.

## Bra att veta

- **Bas-URL:er går ut efter 60 minuter.** Google ger appen tidsbegränsade
  länkar till varje bild/video. Kör bildspelet längre än så, starta om det
  (gå tillbaka och tryck Starta bildspel igen) så hämtas nya länkar. Ett
  naturligt nästa steg är att appen automatiskt uppdaterar länkarna
  medan bildspelet körs.
- **Sessioner gäller en gång.** När du bekräftat ett val i väljaren är den
  sessionen klar; nästa gång du trycker "Välj bilder och videor" skapas en
  ny session.
- Appen kan bara visa medier du uttryckligen valt – den kan inte bläddra i
  hela ditt bibliotek. Det är en medveten begränsning i Googles API.

## Projektstruktur

```
app/src/main/java/com/staffang/gpslideshow/
├── AppConfig.kt              # Projekt-ID och OAuth-scope
├── MainActivity.kt           # Enkel navigering mellan skärmarna
├── AppViewModel.kt           # Flödet: inloggning → session → medier
├── auth/AuthRepository.kt    # OAuth via Google Identity Services
├── picker/PickerRepository.kt# Anrop mot photospicker.googleapis.com
├── picker/PickerModels.kt
├── settings/SettingsRepository.kt # Inställningar i DataStore
└── ui/
    ├── HomeScreen.kt
    ├── SettingsScreen.kt
    ├── SlideshowScreen.kt    # Bildspel + ExoPlayer för videor
    └── theme/Theme.kt
```
