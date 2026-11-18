package fan.androidbase.widget;

import android.content.Context;
import android.util.AttributeSet;

/** Compatibility for Miuix layouts that still use the pre-migration class name. */
public final class ClearableEditText extends fan.androidbasewidget.widget.ClearableEditText {
    public ClearableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
