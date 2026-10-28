# Room
-keep class lk.craftkart.pos.data.EntitiesKt { *; }
-keep class lk.craftkart.pos.data.** { *; }

# Coil
-dontwarn coil.**

# Keep enum names used in DB
-keepclassmembers enum lk.craftkart.pos.** { *; }
