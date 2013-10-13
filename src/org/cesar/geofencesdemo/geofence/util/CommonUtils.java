package org.cesar.geofencesdemo.geofence.util;

import android.content.Context;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

public class CommonUtils {

    public static final String LOG_TAG = CommonUtils.class.getSimpleName();

    public static void showShortToast(final Context context, final String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void showShortToast(final Context context, final int resId) {
        Toast toast = Toast.makeText(context, resId, Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void hideSoftKeyboard(final Context context, final EditText editText) {
        ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                editText.getWindowToken(), 0);
        Log.d(LOG_TAG, "Hidding keyboard");
    }
}
