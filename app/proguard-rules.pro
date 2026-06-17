# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-dontwarn androidx.room.paging.**

# Koin
-keep class org.koin.** { *; }
-keep class * extends org.koin.core.module.Module { *; }

# Kotlinx Serialization
-keepclassmembers class kotlinx.serialization.json.** { *; }
-keep,includedescriptorclasses class com.zarnth.savr.**$$serializer { *; }
-keepclassmembers class com.zarnth.savr.** {
    *** Companion;
}
-keepclasseswithmembers class com.zarnth.savr.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Coil
-keep class coil3.** { *; }

# Jsoup
-keep class org.jsoup.** { *; }

# Keep R
-keep class **.R { *; }
-keep class **.R$* { *; }

# Missing classes
-dontwarn com.google.re2j.Matcher
-dontwarn com.google.re2j.Pattern

# Compose
-keep class androidx.compose.** { *; }
