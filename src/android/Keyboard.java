package org.apache.cordova.labs.keyboard;

import android.app.Activity;
import android.content.Context;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.view.View;
import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

public class Keyboard extends CordovaPlugin implements View.OnLayoutChangeListener {
	private static final String TAG = "Keyboard";
	private static AndroidBug5497Workaround workaround = null;
	private int keyHeight = 0;
	private CordovaWebView webView=null;
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
        //阀值设置为屏幕高度的1/3
        keyHeight = cordova.getActivity().getWindowManager().getDefaultDisplay().getHeight()/3;
		webView.getView().addOnLayoutChangeListener(this);
		this.webView = webView;
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
                callbackContext.success(0);
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

    @Override
    public void onLayoutChange(View v, int left, int top, int right,
                               int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        //现在认为只要控件将Activity向上推的高度超过了1/3屏幕高，就认为软键盘弹起
        if(oldBottom != 0 && bottom != 0 &&(oldBottom - bottom > keyHeight)){
            this.webView.sendJavascript("Keyboard.fireOnShowing()");
            this.webView.sendJavascript("Keyboard.fireOnShow()");
        }else if(oldBottom != 0 && bottom != 0 &&(bottom - oldBottom > keyHeight)){
            this.webView.sendJavascript("Keyboard.fireOnHiding()");
            this.webView.sendJavascript("Keyboard.fireOnHide()");
        }
    }
}
