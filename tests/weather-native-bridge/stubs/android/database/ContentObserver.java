package android.database;

import android.net.Uri;

public abstract class ContentObserver {
    public ContentObserver(Object handler) { }
    public void onChange(boolean selfChange, Uri uri) { }
}
