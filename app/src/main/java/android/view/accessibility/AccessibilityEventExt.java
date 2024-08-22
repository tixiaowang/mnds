package android.view.accessibility;


import com.uu.txw.auto.UcEntity;

import java.util.ArrayList;
import java.util.List;

public class AccessibilityEventExt implements UcEntity {
    public int eventType;
    public List<CharSequence> text = new ArrayList<CharSequence>();
    public CharSequence className;
    public static final int TYPE_WINDOW_STATE_CHANGED = 0x00000020;
    public static final int TYPE_NOTIFICATION_STATE_CHANGED = 0x00000040;
    public static final int TYPE_VIEW_TEXT_SELECTION_CHANGED = 0x00002000;

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public CharSequence getClassName() {
        return className;
    }

    public void setClassName(CharSequence className) {
        this.className = className;
    }

    public List<CharSequence> getText() {
        return text;
    }

    public void setText(List<CharSequence> text) {
        this.text = text;
    }

    public static AccessibilityEventExt of(AccessibilityEvent accessibilityEvent) {
        AccessibilityEventExt r = new AccessibilityEventExt();
        r.setEventType(accessibilityEvent.getEventType());
        r.setClassName(accessibilityEvent.getClassName());
        return r;
    }
}
