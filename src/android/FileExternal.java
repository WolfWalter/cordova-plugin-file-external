/**
 */
package de.solvis;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

public class FileExternal extends CordovaPlugin {
  private static final String TAG = "FileExternal";
  private static final int ACTION_GET_FILES = 56;
  private CallbackContext callback;

  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);

    Log.d(TAG, "Initializing FileExternal");
  }

  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    boolean result = false;

    PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
    pluginResult.setKeepCallback(true);
    callbackContext.sendPluginResult(pluginResult);
    callback = callbackContext;

    if(action.equals("dirChooser")) {
      cordova.getThreadPool().execute(new Runnable() {
        @Override
        public void run() {
          openDirChooser();
        }
      });

      result = true;
    }

    return result;
  }

  private void openDirChooser() {
    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
    cordova.startActivityForResult(this, intent, ACTION_GET_FILES);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    if(requestCode == ACTION_GET_FILES
            && resultCode == Activity.RESULT_OK
            && callback != null
            && intent != null){
      Activity activity = cordova.getActivity();
      DocumentFile externalFile = DocumentFile.fromTreeUri(
              activity.getApplicationContext(), intent.getData());
      Log.i(TAG, "external directory to get files: " + externalFile.getUri());

      callback.success(externalFile.getUri().toString());
    }
  }

}
