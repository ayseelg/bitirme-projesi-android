# bitirmeiki

Android tabanli bir bitirme projesi uygulamasi.

Bu proje Kotlin ile gelistirilmistir ve Firebase (Auth, Firestore, Realtime Database) ile birlikte goruntu analizi tarafinda HTTP tabanli bir model servisine baglanir.

## Ozellikler

- E-posta/sifre ile kimlik dogrulama (Firebase Auth)
- Kullaniciya ozel veri ve gecmis kaydi (Firestore)
- Goruntu yukleme ve analiz islemi
- Model secimi (YOLOv8, YOLOv11, YOLOv12)
- Sonuc ve gecmis goruntuleme

## Teknolojiler

- Kotlin
- Android SDK (minSdk 26, targetSdk 36)
- Firebase Auth
- Firebase Firestore
- Firebase Realtime Database
- Retrofit / OkHttp
- TensorFlow Lite

## Gereksinimler

- Android Studio (guncel surum)
- JDK 11
- Android SDK 36
- Firebase projesi ve uygun `google-services.json`

## Kurulum

1. Projeyi klonlayin veya indirin.
2. Android Studio ile projeyi acin.
3. `app/google-services.json` dosyasinin dogru oldugunu kontrol edin.
4. `local.properties` icinde Android SDK yolunun tanimli oldugundan emin olun.
5. Gradle senkronizasyonunu tamamlayin.

## Calistirma

1. Bir emulator veya fiziksel cihaz baglayin.
2. Android Studio icinden `app` modulunu secin.
3. `Run` ile projeyi calistirin.

## Notlar

- Uygulama, analiz icin bir backend endpointine istek atar.
- Varsayilan endpoint kod icinde tanimlidir: `DetectionActivity`.
- Uretim ortami icin endpoint ve gizli bilgiler ortama gore yonetilmelidir.

## Lisans

Bu proje egitim/bitirme calismasi amaciyla olusturulmustur.
