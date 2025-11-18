# Quick Start Checklist ✅

Use this checklist to get your app up and running quickly.

## Prerequisites
- [ ] Android Studio installed (Hedgehog or newer)
- [ ] JDK 11 or higher
- [ ] Android device or emulator (API 34+)
- [ ] Internet connection for Supabase setup

## Part 1: Supabase Setup (15 minutes)

### Step 1: Create Supabase Project
- [ ] Go to [supabase.com](https://supabase.com)
- [ ] Click "Start your project"
- [ ] Sign up or log in
- [ ] Click "New Project"
- [ ] Fill in project details:
  - [ ] Project name: `SupabaseProductos` (or your choice)
  - [ ] Database password: (save this securely)
  - [ ] Region: (choose closest to you)
- [ ] Click "Create new project"
- [ ] Wait for project initialization (2-3 minutes)

### Step 2: Create Database Tables
- [ ] In Supabase dashboard, go to **SQL Editor**
- [ ] Click "New Query"
- [ ] Copy the SQL from `SETUP_GUIDE.md` (section 1.2)
- [ ] Paste into SQL Editor
- [ ] Click "Run" or press `Ctrl+Enter`
- [ ] Verify: "Success. No rows returned"
- [ ] Go to **Table Editor**
- [ ] Confirm you see:
  - [ ] `categories` table
  - [ ] `products` table

### Step 3: Get API Credentials
- [ ] In Supabase, go to **Project Settings** (gear icon)
- [ ] Click **API** in sidebar
- [ ] Copy and save:
  - [ ] **Project URL**: `https://xxxxx.supabase.co`
  - [ ] **anon public key**: `eyJhbGc...` (long string)

## Part 2: Android Studio Setup (10 minutes)

### Step 4: Clone and Open Project
- [ ] Open Terminal/Command Prompt
- [ ] Run: `git clone https://github.com/lhuachl/SupabaseProductos.git`
- [ ] Open Android Studio
- [ ] Click "Open" and select the `SupabaseProductos` folder
- [ ] Wait for Gradle sync to complete

### Step 5: Configure Credentials
- [ ] In Android Studio, navigate to `app/build.gradle.kts`
- [ ] Find lines 22-23 (buildConfigField section)
- [ ] Replace `"https://your-project.supabase.co"` with your Project URL
- [ ] Replace `"your-anon-key"` with your anon public key
- [ ] Example:
```kotlin
buildConfigField("String", "SUPABASE_URL", "\"https://abcdefgh.supabase.co\"")
buildConfigField("String", "SUPABASE_KEY", "\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"")
```
- [ ] Save file (`Ctrl+S` or `Cmd+S`)
- [ ] Click "Sync Now" when prompted

### Step 6: Build Project
- [ ] Click **Build > Rebuild Project**
- [ ] Wait for build to complete (first build takes longer)
- [ ] Verify: "BUILD SUCCESSFUL" in Build Output

## Part 3: Run and Test (10 minutes)

### Step 7: Set Up Emulator or Device

**Option A: Emulator**
- [ ] Click **Device Manager** (phone icon in toolbar)
- [ ] Click "Create Device"
- [ ] Select "Pixel 6" or similar
- [ ] Select "API 34" system image (download if needed)
- [ ] Click "Finish"
- [ ] Click play button (▶️) to start emulator

**Option B: Physical Device**
- [ ] Enable Developer Options on your Android device
- [ ] Enable USB Debugging
- [ ] Connect device to computer via USB
- [ ] Approve USB debugging prompt on device
- [ ] Verify device appears in Android Studio toolbar

### Step 8: Run the App
- [ ] Select your device/emulator from dropdown
- [ ] Click "Run" button (▶️) or press `Shift+F10`
- [ ] Wait for app to install and launch
- [ ] App should open showing "Supabase Productos" screen

### Step 9: Test Basic Functionality

**Test Online Mode:**
- [ ] Verify green "En línea" banner at top
- [ ] Click FAB (+) button
- [ ] Create a test category:
  - [ ] Name: "Electrónica"
  - [ ] Description: "Productos electrónicos"
  - [ ] Click "Guardar"
- [ ] Verify you hear a sound
- [ ] Verify notification appears: "Categoría creada: Electrónica"
- [ ] See category in list
- [ ] Check Supabase Table Editor - category should appear there

**Test Products:**
- [ ] Switch to "Productos" tab
- [ ] Click FAB (+) button
- [ ] Create a test product:
  - [ ] Name: "Laptop"
  - [ ] Description: "Laptop gaming"
  - [ ] Price: "999.99"
  - [ ] Stock: "10"
  - [ ] Category: Select "Electrónica"
  - [ ] Click "Guardar"
- [ ] Verify sound and notification
- [ ] See product in list
- [ ] Check Supabase - product should appear

**Test Offline Mode:**
- [ ] **On Emulator**: Open Extended Controls (... button) > Settings > Disable WiFi/Mobile
- [ ] **On Device**: Enable Airplane Mode
- [ ] Verify banner changes to red "Sin conexión"
- [ ] Hear disconnect sound
- [ ] Create another category (e.g., "Ropa")
- [ ] Note the cloud icon "Pendiente de sincronización"
- [ ] Create another product
- [ ] Both should save locally

**Test Sync:**
- [ ] **On Emulator**: Re-enable WiFi/Mobile
- [ ] **On Device**: Disable Airplane Mode
- [ ] Verify banner turns green
- [ ] Hear connect sound
- [ ] See notification "Sincronización completada"
- [ ] Cloud icons should disappear from items
- [ ] Check Supabase - offline items should now appear

**Test Edit and Delete:**
- [ ] Click ⋮ menu on a category
- [ ] Click "Editar"
- [ ] Change name
- [ ] Click "Guardar"
- [ ] Verify update sound
- [ ] Click ⋮ menu again
- [ ] Click "Eliminar"
- [ ] Confirm deletion
- [ ] Verify delete sound
- [ ] Item removed from list

## Part 4: Verify Installation (5 minutes)

### Step 10: Final Checks
- [ ] All sounds working (7 different tones)
- [ ] Notifications animating smoothly
- [ ] Categories CRUD working
- [ ] Products CRUD working
- [ ] Offline mode working
- [ ] Online sync working
- [ ] Connection status banner updating
- [ ] Data persisting in Supabase
- [ ] No crashes or errors

## Troubleshooting

### ❌ Build fails with "Unable to resolve dependency"
**Solution:**
- Check internet connection
- File > Invalidate Caches / Restart
- Try: `./gradlew clean build --refresh-dependencies`

### ❌ "BuildConfig does not exist"
**Solution:**
- Verify `buildFeatures { buildConfig = true }` in build.gradle.kts
- Build > Clean Project, then Build > Rebuild Project

### ❌ App doesn't sync
**Solution:**
- Double-check Supabase URL and key in build.gradle.kts
- Verify tables exist in Supabase (Table Editor)
- Check Logcat for error messages
- Verify RLS policies are set (see SETUP_GUIDE.md)

### ❌ No sounds playing
**Solution:**
- Check device volume (notification volume specifically)
- Disable silent/vibrate mode
- On emulator, ensure audio is enabled

### ❌ Can't connect to Supabase
**Solution:**
- Verify URL starts with `https://`
- Check project is active in Supabase dashboard
- Try accessing URL in browser
- Verify INTERNET permission in AndroidManifest.xml

## Success! 🎉

If all checkboxes are checked, congratulations! Your app is fully set up and working.

## What's Next?

### Explore the Code
- [ ] Read `ARCHITECTURE.md` to understand the design
- [ ] Check `SYSTEM_FLOW.md` for visual flow diagrams
- [ ] Explore the codebase structure

### Customize
- [ ] Change app colors in `ui/theme/Color.kt`
- [ ] Modify sounds in `util/SoundManager.kt`
- [ ] Add new fields to entities
- [ ] Customize UI layouts

### Enhance
- [ ] Add user authentication (see SETUP_GUIDE.md security section)
- [ ] Implement search and filters
- [ ] Add product images
- [ ] Create reports and analytics
- [ ] Add barcode scanning

### Deploy
- [ ] Set up signing config for release build
- [ ] Configure ProGuard rules
- [ ] Generate signed APK/Bundle
- [ ] Publish to Google Play Store

## Need Help?

- 📚 Read the documentation:
  - `README.md` - Overview
  - `SETUP_GUIDE.md` - Detailed setup
  - `ARCHITECTURE.md` - Technical details
  - `SYSTEM_FLOW.md` - Flow diagrams
  - `IMPLEMENTATION_SUMMARY.md` - Feature list

- 🐛 Found a bug?
  - Check [Issues](https://github.com/lhuachl/SupabaseProductos/issues)
  - Create a new issue with details

- 💡 Have questions?
  - Check Troubleshooting section above
  - Review Supabase documentation
  - Check Android documentation

## Completion Time Estimate

- ⏱️ **Total Time**: ~40 minutes
  - Supabase setup: 15 min
  - Android Studio setup: 10 min
  - Testing: 10 min
  - Verification: 5 min

First-time setup may take longer due to downloads and learning curve.

---

**Happy Coding! 🚀**

Remember to star ⭐ the repo if you find it useful!
