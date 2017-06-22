/**
 */
package de.solvis;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

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
    else if(action.equals("listDir")) {
      final DocumentFile dirFile = getFile(args.getString(0), args.getString(1));
      cordova.getThreadPool().execute(new Runnable() {
        @Override
        public void run() {
          try {
            JSONArray resultJson = listDir(dirFile);
            callback.success(resultJson);
          } catch (IOException e) {
            callback.error(e.getMessage());
            e.printStackTrace();
          } catch (JSONException e) {
            callback.error(e.getMessage());
            e.printStackTrace();
          }
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

  private DocumentFile getFile(String path, String subDir) {
    DocumentFile resultDir;
    DocumentFile pathDir = DocumentFile.fromTreeUri(
            cordova.getActivity().getApplicationContext(), Uri.parse(path));
    resultDir = (subDir.equals(""))? pathDir: pathDir.findFile(subDir);

    return resultDir;
  }

  private JSONArray listDir(DocumentFile sourceDir) throws IOException, JSONException {
    if(sourceDir == null || !sourceDir.exists()){
      Log.i(TAG, "sourceDir does not exist");
      throw new IOException("not found");
    }

    Log.i(TAG, "list files from  " + sourceDir.getName());

    JSONArray filesDataJson = new JSONArray();

    for (DocumentFile sourceFile : sourceDir.listFiles()) {
      long lastModified = sourceFile.lastModified();
      Log.i(TAG, "lastMod " + sourceFile.getName() + " is " + lastModified);

      JSONObject jsonObject = new JSONObject();
      jsonObject.put("path", sourceFile.getUri());
      jsonObject.put("name", sourceFile.getName());
      jsonObject.put("isFile", sourceFile.isFile());
      jsonObject.put("modificationDate", lastModified);
      filesDataJson.put(jsonObject);
    }

    return filesDataJson;
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
