package com.setImage;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import android.content.IntentSender.SendIntentException;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.view.Display;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ContextWrapper extends Context {
    public ContextWrapper(Context base) {
        throw new RuntimeException("Stub!");
    }

    protected void attachBaseContext(Context base) {
        throw new RuntimeException("Stub!");
    }

    public Context getBaseContext() {
        throw new RuntimeException("Stub!");
    }

    public AssetManager getAssets() {
        throw new RuntimeException("Stub!");
    }

    public Resources getResources() {
        throw new RuntimeException("Stub!");
    }

    public PackageManager getPackageManager() {
        throw new RuntimeException("Stub!");
    }

    public ContentResolver getContentResolver() {
        throw new RuntimeException("Stub!");
    }

    public Looper getMainLooper() {
        throw new RuntimeException("Stub!");
    }

    public Context getApplicationContext() {
        throw new RuntimeException("Stub!");
    }

    public void setTheme(int resid) {
        throw new RuntimeException("Stub!");
    }

    public Theme getTheme() {
        throw new RuntimeException("Stub!");
    }

    public ClassLoader getClassLoader() {
        throw new RuntimeException("Stub!");
    }

    public String getPackageName() {
        throw new RuntimeException("Stub!");
    }

    public ApplicationInfo getApplicationInfo() {
        throw new RuntimeException("Stub!");
    }

    public String getPackageResourcePath() {
        throw new RuntimeException("Stub!");
    }

    public String getPackageCodePath() {
        throw new RuntimeException("Stub!");
    }

    public SharedPreferences getSharedPreferences(String name, int mode) {
        throw new RuntimeException("Stub!");
    }

    public FileInputStream openFileInput(String name) throws FileNotFoundException {
        throw new RuntimeException("Stub!");
    }

    public FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException {
        throw new RuntimeException("Stub!");
    }

    public boolean deleteFile(String name) {
        throw new RuntimeException("Stub!");
    }

    public File getFileStreamPath(String name) {
        throw new RuntimeException("Stub!");
    }

    public String[] fileList() {
        throw new RuntimeException("Stub!");
    }

    public File getFilesDir() {
        throw new RuntimeException("Stub!");
    }

    public File getExternalFilesDir(String type) {
        throw new RuntimeException("Stub!");
    }

    public File[] getExternalFilesDirs(String type) {
        throw new RuntimeException("Stub!");
    }

    public File getObbDir() {
        throw new RuntimeException("Stub!");
    }

    public File[] getObbDirs() {
        throw new RuntimeException("Stub!");
    }

    public File getCacheDir() {
        throw new RuntimeException("Stub!");
    }

    public File getExternalCacheDir() {
        throw new RuntimeException("Stub!");
    }

    public File[] getExternalCacheDirs() {
        throw new RuntimeException("Stub!");
    }

    public File getDir(String name, int mode) {
        throw new RuntimeException("Stub!");
    }

    public SQLiteDatabase openOrCreateDatabase(String name, int mode, CursorFactory factory) {
        throw new RuntimeException("Stub!");
    }

    public SQLiteDatabase openOrCreateDatabase(String name, int mode, CursorFactory factory, DatabaseErrorHandler errorHandler) {
        throw new RuntimeException("Stub!");
    }

    public boolean deleteDatabase(String name) {
        throw new RuntimeException("Stub!");
    }

    public File getDatabasePath(String name) {
        throw new RuntimeException("Stub!");
    }

    public String[] databaseList() {
        throw new RuntimeException("Stub!");
    }

    public Drawable getWallpaper() {
        throw new RuntimeException("Stub!");
    }

    public Drawable peekWallpaper() {
        throw new RuntimeException("Stub!");
    }

    public int getWallpaperDesiredMinimumWidth() {
        throw new RuntimeException("Stub!");
    }

    public int getWallpaperDesiredMinimumHeight() {
        throw new RuntimeException("Stub!");
    }

    public void setWallpaper(Bitmap bitmap) throws IOException {
        throw new RuntimeException("Stub!");
    }

    public void setWallpaper(InputStream data) throws IOException {
        throw new RuntimeException("Stub!");
    }

    public void clearWallpaper() throws IOException {
        throw new RuntimeException("Stub!");
    }

    public void startActivity(Intent intent) {
        throw new RuntimeException("Stub!");
    }

    public void startActivity(Intent intent, Bundle options) {
        throw new RuntimeException("Stub!");
    }

    public void startActivities(Intent[] intents) {
        throw new RuntimeException("Stub!");
    }

    public void startActivities(Intent[] intents, Bundle options) {
        throw new RuntimeException("Stub!");
    }

    public void startIntentSender(IntentSender intent, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags) throws SendIntentException {
        throw new RuntimeException("Stub!");
    }

    public void startIntentSender(IntentSender intent, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, Bundle options) throws SendIntentException {
        throw new RuntimeException("Stub!");
    }

    public void sendBroadcast(Intent intent) {
        throw new RuntimeException("Stub!");
    }

    public void sendBroadcast(Intent intent, String receiverPermission) {
        throw new RuntimeException("Stub!");
    }

    public void sendOrderedBroadcast(Intent intent, String receiverPermission) {
        throw new RuntimeException("Stub!");
    }

    public void sendOrderedBroadcast(Intent intent, String receiverPermission, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        throw new RuntimeException("Stub!");
    }

    public void sendBroadcastAsUser(Intent intent, UserHandle user) {
        throw new RuntimeException("Stub!");
    }

    public void sendBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission) {
        throw new RuntimeException("Stub!");
    }

    public void sendOrderedBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        throw new RuntimeException("Stub!");
    }

    public void sendStickyBroadcast(Intent intent) {
        throw new RuntimeException("Stub!");
    }

    public void sendStickyOrderedBroadcast(Intent intent, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        throw new RuntimeException("Stub!");
    }

    public void removeStickyBroadcast(Intent intent) {
        throw new RuntimeException("Stub!");
    }

    public void sendStickyBroadcastAsUser(Intent intent, UserHandle user) {
        throw new RuntimeException("Stub!");
    }

    public void sendStickyOrderedBroadcastAsUser(Intent intent, UserHandle user, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        throw new RuntimeException("Stub!");
    }

    public void removeStickyBroadcastAsUser(Intent intent, UserHandle user) {
        throw new RuntimeException("Stub!");
    }

    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        throw new RuntimeException("Stub!");
    }

    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler) {
        throw new RuntimeException("Stub!");
    }

    public void unregisterReceiver(BroadcastReceiver receiver) {
        throw new RuntimeException("Stub!");
    }

    public ComponentName startService(Intent service) {
        throw new RuntimeException("Stub!");
    }

    public boolean stopService(Intent name) {
        throw new RuntimeException("Stub!");
    }

    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        throw new RuntimeException("Stub!");
    }

    public void unbindService(ServiceConnection conn) {
        throw new RuntimeException("Stub!");
    }

    public boolean startInstrumentation(ComponentName className, String profileFile, Bundle arguments) {
        throw new RuntimeException("Stub!");
    }

    public Object getSystemService(String name) {
        throw new RuntimeException("Stub!");
    }

    public int checkPermission(String permission, int pid, int uid) {
        throw new RuntimeException("Stub!");
    }

    public int checkCallingPermission(String permission) {
        throw new RuntimeException("Stub!");
    }

    public int checkCallingOrSelfPermission(String permission) {
        throw new RuntimeException("Stub!");
    }

    public void enforcePermission(String permission, int pid, int uid, String message) {
        throw new RuntimeException("Stub!");
    }

    public void enforceCallingPermission(String permission, String message) {
        throw new RuntimeException("Stub!");
    }

    public void enforceCallingOrSelfPermission(String permission, String message) {
        throw new RuntimeException("Stub!");
    }

    public void grantUriPermission(String toPackage, Uri uri, int modeFlags) {
        throw new RuntimeException("Stub!");
    }

    public void revokeUriPermission(Uri uri, int modeFlags) {
        throw new RuntimeException("Stub!");
    }

    public int checkUriPermission(Uri uri, int pid, int uid, int modeFlags) {
        throw new RuntimeException("Stub!");
    }

    public int checkCallingUriPermission(Uri uri, int modeFlags) {
        throw new RuntimeException("Stub!");
    }

    public int checkCallingOrSelfUriPermission(Uri uri, int modeFlags) {
        throw new RuntimeException("Stub!");
    }

    public int checkUriPermission(Uri uri, String readPermission, String writePermission, int pid, int uid, int modeFlags) {
        throw new RuntimeException("Stub!");
    }

    public void enforceUriPermission(Uri uri, int pid, int uid, int modeFlags, String message) {
        throw new RuntimeException("Stub!");
    }

    public void enforceCallingUriPermission(Uri uri, int modeFlags, String message) {
        throw new RuntimeException("Stub!");
    }

    public void enforceCallingOrSelfUriPermission(Uri uri, int modeFlags, String message) {
        throw new RuntimeException("Stub!");
    }

    public void enforceUriPermission(Uri uri, String readPermission, String writePermission, int pid, int uid, int modeFlags, String message) {
        throw new RuntimeException("Stub!");
    }

    public Context createPackageContext(String packageName, int flags) throws NameNotFoundException {
        throw new RuntimeException("Stub!");
    }

    public Context createConfigurationContext(Configuration overrideConfiguration) {
        throw new RuntimeException("Stub!");
    }

    public Context createDisplayContext(Display display) {
        throw new RuntimeException("Stub!");
    }

    public boolean isRestricted() {
        throw new RuntimeException("Stub!");
    }
}
