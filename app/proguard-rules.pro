-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class de.paso2.einkaufsliste.data.**$$serializer { *; }
-keepclassmembers class de.paso2.einkaufsliste.data.** {
    *** Companion;
}
