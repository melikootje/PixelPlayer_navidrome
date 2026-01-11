# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ========================================
# R8 MISSING CLASS WARNINGS - IGNORE THESE
# ========================================
# These classes are from optional dependencies or server-side libraries
# They're not needed on Android and can be safely ignored

# Netty native libraries (server-side SSL, not needed on Android)
-dontwarn io.netty.internal.tcnative.**
-dontwarn io.netty.handler.ssl.**

# Java Beans (desktop Java, not available on Android)
-dontwarn java.beans.**

# Other common server-side dependencies
-dontwarn javax.naming.**
-dontwarn javax.servlet.**
-dontwarn org.apache.log4j.**
-dontwarn org.slf4j.**

# ========================================
# R8 PERFORMANCE OPTIMIZATIONS
# ========================================
# These rules significantly speed up R8 processing
# Trade-off: Slightly less optimization, but 50-70% faster builds

# Disable aggressive optimizations (HUGE speed improvement)
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*,!code/allocation/variable

# Reduce optimization passes from default (5) to 1
-optimizationpasses 1

# Don't preverify (not needed for Android)
-dontpreverify

# Allow R8 to use more aggressive class merging (faster)
-allowaccessmodification

# Disable obfuscation - keep class/method names readable
# (App is open source, no need to hide code)
# This also speeds up R8 significantly
-dontobfuscate

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile


# Keep javax.lang.model classes (often needed by annotation processors or code generation libraries)
-keep class javax.lang.model.** { *; }
-keep interface javax.lang.model.** { *; }

# Keep javax.sound.sampled classes (for audio processing libraries like JFLAC)
-keep class javax.sound.sampled.** { *; }
-keep interface javax.sound.sampled.** { *; }

# Specific rules for JavaPoet if the above is not enough
-keep class com.squareup.javapoet.** { *; }
-keep interface com.squareup.javapoet.** { *; }

# Specific rules for AutoValue if it's directly used or a transitive dependency
# (though usually AutoValue is a compile-time dependency and shouldn't need this)
# -keep class com.google.auto.value.** { *; }
# -keep interface com.google.auto.value.** { *; }

# Rules for TagLib
-keep class com.kyant.taglib.** { *; }

# [NUEVO] Regla general para mantener metadatos de Kotlin, puede ayudar a R8
-keep class kotlin.Metadata { *; }

# ==== CRITICAL HILT/DAGGER RULES ====
# Keep all Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep class * extends dagger.hilt.android.internal.lifecycle.HiltViewModelFactory { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep @dagger.hilt.InstallIn class * { *; }
-keep @javax.inject.Inject class * { *; }

# Keep all @AndroidEntryPoint annotated classes
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }

# Keep all generated Hilt components
-keep class **_HiltComponents { *; }
-keep class **_HiltComponents$* { *; }
-keep class **_HiltModules { *; }
-keep class **_HiltModules$* { *; }
-keep class **Hilt_** { *; }
-keep class dagger.** { *; }

# Keep all DI modules and their provider methods
-keep @dagger.Module class * { *; }
-keep class com.theveloper.pixelplay.di.** { *; }
-keepclassmembers class com.theveloper.pixelplay.di.** {
    @dagger.Provides *;
    @javax.inject.Inject *;
}

# Keep all custom qualifiers
-keep @interface javax.inject.Qualifier
-keep,allowobfuscation @interface * extends javax.inject.Qualifier
-keep @interface com.theveloper.pixelplay.di.DeezerRetrofit
-keep @interface com.theveloper.pixelplay.di.FastOkHttpClient
-keep @interface com.theveloper.pixelplay.di.SubsonicRetrofit
-keepattributes *Annotation*

# Keep Application class and its Hilt wrapper
-keep class com.theveloper.pixelplay.PixelPlayApplication { *; }
-keep class com.theveloper.pixelplay.PixelPlayApplication_** { *; }

# Keep all Activities, Services, and their Hilt components
-keep class com.theveloper.pixelplay.MainActivity { *; }
-keep class com.theveloper.pixelplay.MainActivity_** { *; }
-keep class com.theveloper.pixelplay.ExternalPlayerActivity { *; }
-keep class com.theveloper.pixelplay.ExternalPlayerActivity_** { *; }
-keep class com.theveloper.pixelplay.data.service.MusicService { *; }
-keep class com.theveloper.pixelplay.data.service.MusicService_** { *; }

# Keep all ViewModel classes (they use reflection)
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class com.theveloper.pixelplay.presentation.viewmodel.** { *; }

# Keep all Hilt modules
-keep @dagger.Module class * { *; }
-keep @dagger.hilt.components.SingletonComponent class * { *; }

# ExoPlayer FFmpeg extension
-keep class androidx.media3.decoder.ffmpeg.** { *; }
-keep class androidx.media3.exoplayer.ffmpeg.** { *; }

# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.

# ==== ROOM DATABASE RULES ====
-keep class com.theveloper.pixelplay.data.local.** { *; }
-keep @androidx.room.Entity class *
-keepclassmembers class * {
  @androidx.room.* <methods>;
  @androidx.room.* <fields>;
}
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Database class *

# ==== KOTLINX SERIALIZATION ====
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.theveloper.pixelplay.**$$serializer { *; }
-keepclassmembers class com.theveloper.pixelplay.** {
    *** Companion;
}
-keepclasseswithmembers class com.theveloper.pixelplay.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ==== GSON RULES ====
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# ==== DATA CLASSES ====
-keep class com.theveloper.pixelplay.data.model.** { *; }

# ==== RETROFIT & OKHTTP ====
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keep,allowshrinking,allowobfuscation interface retrofit2.Call
-keep,allowshrinking,allowobfuscation class kotlin.coroutines.Continuation
-dontwarn org.codehaus.mojo.animal_sniffer.*

# Keep all Retrofit service interfaces
-keep interface com.theveloper.pixelplay.data.remote.** { *; }
-keep class com.theveloper.pixelplay.data.remote.** { *; }

# Keep Subsonic/Navidrome API classes
-keep interface com.theveloper.pixelplay.data.network.** { *; }
-keep class com.theveloper.pixelplay.data.network.** { *; }

# Keep Retrofit and OkHttp classes used in DI
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keepclassmembers,allowobfuscation class * {
  @retrofit2.http.* <methods>;
}

# Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items)
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# ==== COIL IMAGE LOADING ====
-keep class coil.** { *; }
-keep interface coil.** { *; }
-keep class * extends coil.** { *; }

# ==== MEDIA3 ====
-keep class androidx.media3.** { *; }
-keep interface androidx.media3.** { *; }
-dontwarn androidx.media3.**

# ==== GOOGLE CAST ====
-keep class com.google.android.gms.cast.** { *; }
-keep class com.google.android.gms.common.** { *; }
-dontwarn com.google.android.gms.**

# ==== NATIVE LIBRARIES (TagLib, VorbisJava) ====
-keep class com.kyant.taglib.** { *; }
-keep class org.gagravarr.** { *; }
-keepclassmembers class * {
    native <methods>;
}

# ==== WORKMANAGER & HILT WORKER ====
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.ListenableWorker {
    public <init>(...);
}
-keep class * extends androidx.work.Worker {
    public <init>(...);
}
-keep class * extends androidx.work.CoroutineWorker {
    public <init>(...);
}
-keep @androidx.hilt.work.HiltWorker class * {
    public <init>(...);
}

# ==== KOTLINX COROUTINES ====
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# [NUEVO] Reglas para solucionar el error de Ktor y R8
-dontwarn java.lang.management.**
-dontwarn reactor.blockhound.**

-dontwarn java.awt.Graphics2D
-dontwarn java.awt.Image
-dontwarn java.awt.geom.AffineTransform
-dontwarn java.awt.image.BufferedImage
-dontwarn java.awt.image.ImageObserver
-dontwarn java.awt.image.RenderedImage
-dontwarn javax.imageio.ImageIO
-dontwarn javax.imageio.ImageWriter
-dontwarn javax.imageio.stream.ImageInputStream
-dontwarn javax.imageio.stream.ImageOutputStream
-dontwarn javax.lang.model.SourceVersion
-dontwarn javax.lang.model.element.Element
-dontwarn javax.lang.model.element.ElementKind
-dontwarn javax.lang.model.type.TypeMirror
-dontwarn javax.lang.model.type.TypeVisitor
-dontwarn javax.lang.model.util.SimpleTypeVisitor8
-dontwarn javax.sound.sampled.AudioFileFormat$Type
-dontwarn javax.sound.sampled.AudioFileFormat
-dontwarn javax.sound.sampled.AudioFormat$Encoding
-dontwarn javax.sound.sampled.AudioFormat
-dontwarn javax.sound.sampled.AudioInputStream
-dontwarn javax.sound.sampled.UnsupportedAudioFileException
-dontwarn javax.sound.sampled.spi.AudioFileReader
-dontwarn javax.sound.sampled.spi.FormatConversionProvider
-dontwarn javax.swing.filechooser.FileFilter

-dontwarn io.netty.internal.tcnative.AsyncSSLPrivateKeyMethod
-dontwarn io.netty.internal.tcnative.AsyncTask
-dontwarn io.netty.internal.tcnative.Buffer
-dontwarn io.netty.internal.tcnative.CertificateCallback
-dontwarn io.netty.internal.tcnative.CertificateCompressionAlgo
-dontwarn io.netty.internal.tcnative.CertificateVerifier
-dontwarn io.netty.internal.tcnative.Library
-dontwarn io.netty.internal.tcnative.SSL
-dontwarn io.netty.internal.tcnative.SSLContext
-dontwarn io.netty.internal.tcnative.SSLPrivateKeyMethod
-dontwarn io.netty.internal.tcnative.SSLSessionCache
-dontwarn io.netty.internal.tcnative.SessionTicketKey
-dontwarn io.netty.internal.tcnative.SniHostNameMatcher
-dontwarn org.apache.log4j.Level
-dontwarn org.apache.log4j.Logger
-dontwarn org.apache.log4j.Priority
-dontwarn org.apache.logging.log4j.Level
-dontwarn org.apache.logging.log4j.LogManager
-dontwarn org.apache.logging.log4j.Logger
-dontwarn org.apache.logging.log4j.message.MessageFactory
-dontwarn org.apache.logging.log4j.spi.ExtendedLogger
-dontwarn org.apache.logging.log4j.spi.ExtendedLoggerWrapper
-dontwarn org.eclipse.jetty.npn.NextProtoNego$ClientProvider
-dontwarn org.eclipse.jetty.npn.NextProtoNego$Provider
-dontwarn org.eclipse.jetty.npn.NextProtoNego$ServerProvider
-dontwarn org.eclipse.jetty.npn.NextProtoNego