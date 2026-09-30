# ProGuard rules for Image Compressor – MRPS

# Keep Compose runtime
-keep class androidx.compose.** { *; }

# Keep data classes used in state
-keepclassmembers class in.mrps.imagecompressor.engine.CompressionConfig { *; }
-keepclassmembers class in.mrps.imagecompressor.engine.CompressionResult { *; }
-keepclassmembers class in.mrps.imagecompressor.data.ImageInfo { *; }

# Standard Android rules
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
