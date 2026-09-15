# ProGuard & R8 Rules for ABook Player

# --- General & Kotlin Attributes ---
-keepattributes *Annotation*,InnerClasses,Signature,EnclosingMethod
-dontwarn kotlinx.coroutines.**

# --- Room Database & Entities ---
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep class de.f_soft_studio.abookplayer.data.local.** { *; }
-dontwarn androidx.room.paging.**

# --- Domain Models ---
-keep class de.f_soft_studio.abookplayer.domain.model.** { *; }

# --- Kotlinx Serialization ---
-dontwarn kotlinx.serialization.**
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class * {
    *** Companion;
}

# --- Media3 / ExoPlayer / Playback Service ---
-keep class androidx.media3.exoplayer.** { *; }
-keep class androidx.media3.common.** { *; }
-keep class androidx.media3.session.** { *; }
-keep class de.f_soft_studio.abookplayer.player.service.AbookPlaybackService { *; }

# --- App Widgets ---
-keep class de.f_soft_studio.abookplayer.widget.** extends android.appwidget.AppWidgetProvider { *; }

# --- Coil Image Loading ---
-dontwarn coil.**
-keep class coil.** { *; }
