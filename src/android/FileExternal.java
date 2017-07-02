/**
 */
package de.solvis;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.AssetManager;
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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

class FileExternalEntry {
  public DocumentFile fileEntry;
  public String rootUri;
  public String extPath;
};

public class FileExternal extends CordovaPlugin {
  private static final String TAG = "FileExternal";
  private static final int ACTION_GET_FILES = 56;
  private CallbackContext _callbackContext;

  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);

    Log.d(TAG, "Initializing FileExternal");
  }

  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    boolean result = false;

    PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
    pluginResult.setKeepCallback(true);
    callbackContext.sendPluginResult(pluginResult);
    _callbackContext = callbackContext; // only used for onActivityResult

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
      final String rootUri = args.getString(0);
      final String extPath = args.getString(1);
      cordova.getThreadPool().execute(new Runnable() {
        @Override
        public void run() {
          try {
            FileExternalEntry dirFile = getExternalEntry(rootUri, extPath);
            JSONArray resultJson = listDir(dirFile);
            callbackContext.success(resultJson);
          } catch (IOException e) {
            callbackContext.error(e.getMessage());
            e.printStackTrace();
          } catch (JSONException e) {
            callbackContext.error(e.getMessage());
            e.printStackTrace();
          }
        }
      });
      result = true;
    }
    else if(action.equals("readFile")) {
      final String rootUri = args.getString(0);
      final String extPath = args.getString(1);
      cordova.getThreadPool().execute(new Runnable() {
        @Override
        public void run() {
          try {
            FileExternalEntry file = getExternalEntry(rootUri, extPath);
            String content = readFile(file);
            callbackContext.success(content);
          } catch (IOException e) {
            callbackContext.error(e.getMessage());
            e.printStackTrace();
          }
        }
      });
      result = true;
    }
    else if(action.equals("readFileBinary")) {
      final String rootUri = args.getString(0);
      final String extPath = args.getString(1);
      cordova.getThreadPool().execute(new Runnable() {
        @Override
        public void run() {
          try {
            FileExternalEntry file = getExternalEntry(rootUri, extPath);
            byte[] content = readFileBinary(file);
            callbackContext.success(content);
          } catch (IOException e) {
            callbackContext.error(e.getMessage());
            e.printStackTrace();
          }
        }
      });
      result = true;
    }
    else if(action.equals("remove")) {
      final String rootUri = args.getString(0);
      final String extPath = args.getString(1);
      cordova.getThreadPool().execute(new Runnable() {
        @Override
        public void run() {
          try {
            FileExternalEntry entry = getExternalEntry(rootUri, extPath);
            remove(entry);
            callbackContext.success();
          } catch (IOException e) {
            callbackContext.error(e.getMessage());
            e.printStackTrace();
          }
        }
      });
      result = true;
    }
    else if(action.equals("createDir")) {
      final String rootUri = args.getString(0);
      final String extPath = args.getString(1);
      final String dirName = args.getString(2);
      cordova.getThreadPool().execute(new Runnable() {
        @Override
        public void run() {
          try {
            FileExternalEntry entry = getExternalEntry(rootUri, extPath);
            createDir(entry, dirName);
            callbackContext.success();
          } catch (IOException e) {
            callbackContext.error(e.getMessage());
            e.printStackTrace();
          }
        }
      });
      result = true;
    }
    else if(action.equals("writeFile")) {
      final String rootUri = args.getString(0);
      final String extPath = args.getString(1);
      final String fileName = args.getString(2);
      final String data = args.getString(3);
      cordova.getThreadPool().execute(new Runnable() {
        @Override
        public void run() {
          try {
            FileExternalEntry entry = getExternalEntry(rootUri, extPath);
            writeFile(entry, fileName, data);
            callbackContext.success();
          } catch (IOException e) {
            callbackContext.error(e.getMessage());
            e.printStackTrace();
          }
        }
      });
      result = true;
    }
    else if(action.equals("copyAssetsToExternal")) {
      final String assetPath = args.getString(0);
      final String rootUri = args.getString(1);
      final String extPath = args.getString(2);
      cordova.getThreadPool().execute(new Runnable() {
        @Override
        public void run() {
          try {
            FileExternalEntry extTarget = getExternalEntry(rootUri, extPath);
            copyAssetsToExternal(assetPath, extTarget);
            callbackContext.success();
          } catch (IOException e) {
            callbackContext.error(e.getMessage());
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

  private FileExternalEntry getExternalEntry(String rootUri, String extPath) throws IOException {
    FileExternalEntry resultEntry = new FileExternalEntry();
    resultEntry.rootUri = rootUri;
    resultEntry.extPath = extPath;
    resultEntry.fileEntry = DocumentFile.fromTreeUri(
        cordova.getActivity().getApplicationContext(), Uri.parse(rootUri));

    if(!extPath.equals("")){
      String[] parts = extPath.split("/");
      for(String part: parts) {
        resultEntry.fileEntry = resultEntry.fileEntry.findFile(part);
        if(resultEntry.fileEntry == null ){
          Log.i(TAG, "entry does not exist");
          throw new IOException("not found");
        }
      }
    }

    if(!resultEntry.fileEntry.exists()){
      Log.i(TAG, "entry does not exist");
      throw new IOException("not found");
    }
    return resultEntry;
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    if(requestCode == ACTION_GET_FILES
        && resultCode == Activity.RESULT_OK
        && this._callbackContext != null
        && intent != null){
      Activity activity = cordova.getActivity();
      DocumentFile externalFile = DocumentFile.fromTreeUri(
          activity.getApplicationContext(), intent.getData());
      Log.i(TAG, "external directory to get files: " + externalFile.getUri());

      this._callbackContext.success(externalFile.getUri().toString());
    }
  }

  private JSONArray listDir(FileExternalEntry sourceDir) throws IOException, JSONException {
    if(!sourceDir.fileEntry.isDirectory()) {
      Log.i(TAG, "entry does not exist");
      throw new IOException("not a directory");
    }

    Log.i(TAG, "list files from  " + sourceDir.fileEntry.getName());

    JSONArray filesDataJson = new JSONArray();

    for (DocumentFile sourceFile : sourceDir.fileEntry.listFiles()) {
      long lastModified = sourceFile.lastModified();
      Log.i(TAG, "lastMod " + sourceFile.getName() + " is " + lastModified);
      String extPath = (sourceDir.extPath.equals(""))? sourceFile.getName(): sourceDir.extPath + "/" + sourceFile.getName();

      JSONObject jsonObject = new JSONObject();
      jsonObject.put("extPath", extPath);
      jsonObject.put("name", sourceFile.getName());
      jsonObject.put("isFile", sourceFile.isFile());
      jsonObject.put("modificationDate", lastModified);
      filesDataJson.put(jsonObject);
    }

    return filesDataJson;
  }

  private String readFile(FileExternalEntry file) throws IOException{
    if(!file.fileEntry.isFile()){
      Log.i(TAG, "not a file");
      throw new IOException("not a file");
    }
    Log.i(TAG, "read file: " + file.fileEntry.getName());

    ContentResolver ctx = cordova.getActivity().getContentResolver();
    InputStream is = ctx.openInputStream(file.fileEntry.getUri());

    BufferedReader r = new BufferedReader(new InputStreamReader(is));
    StringBuilder content = new StringBuilder();
    String line;
    while ((line = r.readLine()) != null) {
      content.append(line).append('\n');
    }

    return content.toString();
  }

  private byte[] readFileBinary(FileExternalEntry file) throws IOException{
    if(!file.fileEntry.isFile()){
      Log.i(TAG, "not a file");
      throw new IOException("not a file");
    }
    Log.i(TAG, "read file: " + file.fileEntry.getName());

    ContentResolver ctx = cordova.getActivity().getContentResolver();
    InputStream is = ctx.openInputStream(file.fileEntry.getUri());

    ByteArrayOutputStream output = new ByteArrayOutputStream(); 
    byte[] buffer = new byte[1024*32];
    int bytesRead;

    while ((bytesRead = is.read(buffer)) != -1) {
      output.write(buffer, 0, bytesRead);
    }
    return output.toByteArray();
  }

  private void remove(FileExternalEntry entry) throws IOException{
    Log.i(TAG, "remove: " + entry.fileEntry.getUri());
    entry.fileEntry.delete();
  }

  private void createDir(FileExternalEntry path, String dirName) throws IOException {
    if(!path.fileEntry.isDirectory()) {
      Log.i(TAG, "entry is not a directory");
      throw new IOException("not a directory");
    }

    path.fileEntry.createDirectory(dirName);
  }

  private void writeFile(FileExternalEntry target, String fileName, String data) throws IOException {
    if(!target.fileEntry.isDirectory()) {
      Log.i(TAG, "entry is not a directory");
      throw new IOException("not a directory");
    }

    Log.i(TAG, "write file " + fileName);

    InputStream is = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));

    ContentResolver ctx = cordova.getActivity().getContentResolver();
    DocumentFile file = target.fileEntry.createFile(null, fileName);
    OutputStream os = ctx.openOutputStream(file.getUri());

    FileExternal.copyInputToOutputStream(is, os);
  }

  private void copyAssetsToExternal(String assetPath, FileExternalEntry target) throws IOException{
    if(!target.fileEntry.isDirectory()) {
      Log.i(TAG, "entry is not a directory");
      throw new IOException("not a directory");
    }

    Log.i(TAG, "copy assets in " + assetPath + " to " + target.fileEntry.getName());

    ContentResolver ctx = cordova.getActivity().getContentResolver();
    AssetManager asm = cordova.getActivity().getAssets();

    FileExternal.copyAssets(ctx, asm, assetPath, target.fileEntry);
  }

  private static void copyAssets(ContentResolver ctx, AssetManager asm, String assetPath, DocumentFile target) throws IOException {
    String[] assets = asm.list(assetPath);

    if(assets.length == 0){
      FileExternal.copyAssetFile(ctx, asm, assetPath, target);
    }
    else{
      String dirName = new File(assetPath).getName();
      DocumentFile subDir = target.createDirectory(dirName);

      for (String asset : assets) {
        FileExternal.copyAssets(ctx, asm, assetPath + "/" + asset, subDir);
      }
    }
  }

  private static void copyAssetFile(ContentResolver ctx, AssetManager asm,String assetPath, DocumentFile target) throws IOException {
    String fileName = new File(assetPath).getName();
    InputStream is = asm.open(assetPath);

    DocumentFile targetFile = target.createFile(null, fileName);
    OutputStream os = ctx.openOutputStream(targetFile.getUri());

    Log.i(TAG, "copy assets file " + assetPath + " to " + target.getUri().toString());

    FileExternal.copyInputToOutputStream(is, os);
  }

  private static void copyInputToOutputStream(InputStream is, OutputStream os) throws IOException {
    byte[] buffer = new byte[1024*32];
    int bytesRead;

    while ((bytesRead = is.read(buffer)) != -1) {
      os.write(buffer, 0, bytesRead);
    }

    os.flush();
    is.close();
    os.close();
  }

}
