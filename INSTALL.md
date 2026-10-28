# CraftKart — Install Guide (Android 9 and newer)

Your APK: `releases/CraftKart-v1.0.0.apk` (signed release build, ~1.6 MB)

## Install on your phone

1. **Copy the APK to your phone**
   - Connect the phone with a USB cable and enable *File transfer* mode, then
     copy `releases/CraftKart-v1.0.0.apk` into the phone's *Downloads* folder.
     OR
   - Email the APK to yourself / use a cloud drive, and download it on the phone.

2. **Allow installing from unknown sources** (Android 9)
   - Open *Settings → Apps & notifications → Special app access →
     Install unknown apps*.
   - Choose your file manager / browser and switch **Allow from this source** ON.
   - (Android 8: *Settings → Security → Unknown sources*.)

3. **Install**
   - Open the APK (e.g. in *Files → Downloads*) and tap **Install**.
   - When it asks to open the app, tap **Open** (or find CraftKart in the app list).

## First steps after install

1. **Add your categories** — Inventory tab → tap the edit (pencil) icon in the
   top bar → add *Greeting Cards*, *Bookmarks*, *Gift Tags*, etc.
2. **Add your items** — Inventory tab → tap the **+** button → enter name,
   category, prices, stock, and a photo (camera or gallery).
3. **Sell** — New Sale tab → search or browse → tap items → tap the cart →
   set discount and payment (Cash / Card / UPI) → **Complete Sale**.

## Data & privacy
- Everything is stored **only on your phone** (SQLite database + photos).
- No internet connection is needed. Nothing is uploaded.
- To back up, copy the app's `databases/craftkart.db` and `item_images`
  folders (File Manager → Android/data/lk.craftkart.pos) to safe storage.

## Update to a newer version
- Just install the new APK over the old one — your data is kept.
