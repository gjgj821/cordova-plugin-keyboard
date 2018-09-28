package org.apache.cordova.labs.keyboard;

import android.app.Activity;
import android.content.Context;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.view.View;
import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

public class Keyboard extends CordovaPlugin {
	private static final String TAG = "Keyboard";
	private static AndroidBug5497Workaround workaround = null;
	/**
	 * Sets the context of the Command. This can then be used to do things like
	 * get file paths associated with the Activity.
	 *
	 * @param cordova The context of the main Activity.
	 * @param webView The CordovaWebView Cordova is running in.
	 */
	@Override
	public void initialize(final CordovaInterface cordova, CordovaWebView webView) {
		LOG.v(TAG, "StatusBar: initialization");
		super.initialize(cordova, webView);
        workaround = AndroidBug5497Workaround.assistActivity(this.cordova.getActivity());

		this.cordova.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// Read 'KeyboardShrinksView' from config.xml, default is #000000.
				setKeyboardShrinksView(preferences.getBoolean("KeyboardShrinksView", false));
			}
		});

	}
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Activity activity = this.cordova.getActivity();
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);

        View view;
        try {
            view = (View)webView.getClass().getMethod("getView").invoke(webView);
        }
        catch (Exception e){
            view = (View)webView;
        }

        if("show".equals(action)){
            imm.showSoftInput(view, 0);
            callbackContext.success();
            return true;
        }
        else if("hide".equals(action)){
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            callbackContext.success();
            return true;
        }
        else if("shrinkView".equals(action)){
            setKeyboardShrinksView(args.getBoolean(0));
        }
        else if("navigationBar".equals(action)){
            if(workaround.hasNavigationBar(this.cordova.getActivity())){
                callbackContext.success(workaround.getNavigationBarHeight(this.cordova.getActivity()));
            }else{
                callbackContext.success(0);
            }
        }
        callbackContext.error(action + " is not a supported action");
        return false;
    }

    public void setKeyboardShrinksView(boolean flag){
        workaround.switchResize(flag);
	}
}
