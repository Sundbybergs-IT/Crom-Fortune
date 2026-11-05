# Add project specific ProGuard / R8 rules here.

# Keep Yahoo Finance API classes and models used with reflection / Jackson parsing
-keep class yahoofinance.** { *; }

# Jackson rules (used by Yahoo Finance API)
-keepattributes *Annotation*,EnclosingMethod,InnerClasses,Signature
-keepnames class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.**

# SLF4J optional bindings
-dontwarn org.slf4j.**

# Kotlin Serialization rules
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
    @kotlinx.serialization.Serializer <fields>;
}
-keepclassmembers class * {
    public static *** Companion;
}
-keepclassmembers class * {
    public static *** serializer(...);
}

# Keep Parcelable CREATOR fields
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Navigation 3 NavKey implementations and serialization rules
-keep class com.sundbybergsit.cromfortune.main.navigation.** { *; }
-keep interface com.sundbybergsit.cromfortune.main.navigation.NavKey { *; }
-keep class * implements com.sundbybergsit.cromfortune.main.navigation.NavKey {
    <init>();
    ** Companion;
    ** serializer(...);
    <fields>;
}

