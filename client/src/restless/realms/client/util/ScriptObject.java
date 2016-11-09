package restless.realms.client.util;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.json.client.JSONObject;

public abstract class ScriptObject extends JavaScriptObject {
    protected ScriptObject() {
    }

    public static native ScriptObject create() /*-{
        return new Object();
    }-*/;

    public static native ScriptObject fromJson(String jsonString) /*-{
        return eval('(' + jsonString + ')');
    }-*/;

    public static native JsArray<ScriptObject> arrayFromJson(String jsonString) /*-{
        return eval('(' + jsonString + ')');
    }-*/;

    public final native boolean hasKey(String key) /*-{
        return this[key] != undefined;
    }-*/;

    public final native JsArrayString keys() /*-{
        var a = new Array();
        for (var p in this) { a.push(p); }
        return a;
    }-*/;

    public final native String get(String key) /*-{
        return "" + this[key];
    }-*/;

    public final native String get(String key, String defaultValue) /*-{
        return this[key] ? ("" + this[key]) : defaultValue;
    }-*/;

    public final native void set(String key, Object value) /*-{
        this[key] = value;
    }-*/;

    public final int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public final long getLong(String key) {
        return Long.parseLong(get(key));
    }

    public final boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    public final native boolean isObject(String key) /*-{
        return this[key].constructor == Object;
    }-*/;

    public final native ScriptObject getObject(String key) /*-{
        return this[key];
    }-*/;

    public final native boolean isArray(String key) /*-{
        return this[key].constructor == Array;
    }-*/;

    public final native JsArray<ScriptObject> getArray(String key) /*-{
        return this[key] ? this[key] : new Array();
    }-*/;

    public final native JsArrayInteger getIntArray(String key) /*-{
        return this[key] ? this[key] : new Array();
    }-*/;

    public final native JsArrayString getStringArray(String key) /*-{
        return this[key] ? this[key] : new Array();
    }-*/;

    public final String toDebugString() {
        JSONObject jsonValue = new JSONObject(this);
        return jsonValue.toString();
    }
}
