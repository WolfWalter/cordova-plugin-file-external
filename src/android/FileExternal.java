/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
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
import java.io.FileNotFoundException;
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
  private static final int ACTION_GET_FILES = 0;
  private CallbackContext _callbackContext;

  public static int NOT_FOUND_ERR = 1;
  public static int SECURITY_ERR = 2;
  public static int INVALID_STATE_ERR = 7;
  public static int INVALID_MODIFICATION_ERR = 9;
  public static int UNKNOWN_ERR = 1000;


  private interface FileOp {
    void run(JSONArray args) throws Exception;
  }
  
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    Log.d(TAG, "Initializing FileExternal");
  }

  public boolean execute(String action, final String rawArgs, final CallbackContext callbackContext) {
    if(action.equals("dirChooser")) {
      _callbackContext = callbackContext; // only used for onActivityResult
      threadhelper(new FileOp() {
        public void run(JSONArray args) {
          openDirChooser();
        }
      }, rawArgs, callbackContext);
    }
    else if(action.equals("listDir")) {
      threadhelper(new FileOp() {
        public void run(JSONArray args) throws JSONException, FileNotFoundException{
          String rootUri = args.getString(0);
          String extPath = args.getString(1);
          FileExternalEntry dirFile = getExternalEntry(rootUri, extPath);
          JSONArray resultJson = listDir(dirFile);
          callbackContext.success(resultJson);
        }
      }, rawArgs, callbackContext);
    }
    else if(action.equals("readFile")) {
      threadhelper(new FileOp() {
        public void run(JSONArray args) throws JSONException, IOException{
          String rootUri = args.getString(0);
          String extPath = args.getString(1);
          FileExternalEntry file = getExternalEntry(rootUri, extPath);
          String content = readFile(file);
          callbackContext.success(content);
        }
      }, rawArgs, callbackContext);
    }
    else if(action.equals("readFileBinary")) {
      threadhelper(new FileOp() {
        public void run(JSONArray args) throws JSONException, IOException{
          String rootUri = args.getString(0);
          String extPath = args.getString(1);
          FileExternalEntry file = getExternalEntry(rootUri, extPath);
          byte[] content = readFileBinary(file);
          callbackContext.success(content);
        }
      }, rawArgs, callbackContext);
    }
    else if(action.equals("remove")) {
      threadhelper(new FileOp() {
        public void run(JSONArray args) throws JSONException, IOException{
          String rootUri = args.getString(0);
          String extPath = args.getString(1);
          FileExternalEntry entry = getExternalEntry(rootUri, extPath);

          remove(entry);
          callbackContext.success();
        }
      }, rawArgs, callbackContext);
    }
    else if(action.equals("createDir")) {
      threadhelper(new FileOp() {
        public void run(JSONArray args) throws JSONException, FileNotFoundException{
          String rootUri = args.getString(0);
          String extPath = args.getString(1);
          String dirName = args.getString(2);
          FileExternalEntry entry = getExternalEntry(rootUri, extPath);
          createDir(entry, dirName);
          callbackContext.success();
        }
      }, rawArgs, callbackContext);
    }
    else if(action.equals("writeFile")) {
      threadhelper(new FileOp() {
        public void run(JSONArray args) throws JSONException, IOException{
          String rootUri = args.getString(0);
          String extPath = args.getString(1);
          String fileName = args.getString(2);
          String data = args.getString(3);
          FileExternalEntry entry = getExternalEntry(rootUri, extPath);
            writeFile(entry, fileName, data);
            callbackContext.success();
        }
      }, rawArgs, callbackContext);
    }
    else if(action.equals("copyAssetsToExternal")) {
      threadhelper(new FileOp() {
        public void run(JSONArray args) throws JSONException, IOException{
          String assetPath = args.getString(0);
          String rootUri = args.getString(1);
          String extPath = args.getString(2);
          FileExternalEntry extTarget = getExternalEntry(rootUri, extPath);

          copyAssetsToExternal(assetPath, extTarget);
          callbackContext.success();
        }
      }, rawArgs, callbackContext);
    }
    else{
      return false;
    }

    return true;
  }

  private void threadhelper(final FileOp f, final String rawArgs, final CallbackContext callbackContext){
    cordova.getThreadPool().execute(new Runnable() {
      public void run() {
        try {
          JSONArray args = new JSONArray(rawArgs);
          f.run(args);
        } catch ( Exception e) {
          if( e instanceof FileNotFoundException) {
            callbackContext.error(FileExternal.NOT_FOUND_ERR);
          } else if(e instanceof IOException ) {
            callbackContext.error(FileExternal.INVALID_MODIFICATION_ERR);
          } else if(e instanceof JSONException ) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
          } else if (e instanceof SecurityException) {
            callbackContext.error(FileExternal.SECURITY_ERR);
          } else {
            e.printStackTrace();
            callbackContext.error(FileExternal.UNKNOWN_ERR);
          }
        }
      }
    });
  }

  private void openDirChooser() {
    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
    cordova.startActivityForResult(this, intent, ACTION_GET_FILES);
  }

  private FileExternalEntry getExternalEntry(String rootUri, String extPath) throws FileNotFoundException {
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
          throw new FileNotFoundException();
        }
      }
    }

    if(!resultEntry.fileEntry.exists()){
      Log.i(TAG, "entry does not exist");
      throw new FileNotFoundException();
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

  private JSONArray listDir(FileExternalEntry sourceDir) throws FileNotFoundException, JSONException {
    if(!sourceDir.fileEntry.isDirectory()) {
      Log.i(TAG, "entry does not exist");
      throw new FileNotFoundException();
    }

    Log.i(TAG, "list files from  " + sourceDir.fileEntry.getName());

    JSONArray filesDataJson = new JSONArray();

    for (DocumentFile sourceFile : sourceDir.fileEntry.listFiles()) {
      long lastModified = sourceFile.lastModified();
      String sourceFileName = sourceFile.getName();
      String extPath = (sourceDir.extPath.equals(""))? sourceFileName: sourceDir.extPath + "/" + sourceFileName;

      JSONObject jsonObject = new JSONObject();
      jsonObject.put("extPath", extPath);
      jsonObject.put("name", sourceFileName);
      jsonObject.put("isFile", sourceFile.isFile());
      jsonObject.put("modificationDate", lastModified);
      filesDataJson.put(jsonObject);
    }

    return filesDataJson;
  }

  private String readFile(FileExternalEntry file) throws IOException{
    if(!file.fileEntry.isFile()){
      Log.i(TAG, "not a file");
      throw new FileNotFoundException();
    }
    Log.i(TAG, "read file: " + file.fileEntry.getName());

    ContentResolver ctx = cordova.getActivity().getContentResolver();
    InputStream is = ctx.openInputStream(file.fileEntry.getUri());

    BufferedReader r = new BufferedReader(new InputStreamReader(is));
    StringBuilder content = new StringBuilder();
    int value;
    while ((value = r.read()) != -1) {
      char c = (char)value;
      content.append(c);
    }

    is.close();

    return content.toString();
  }

  private byte[] readFileBinary(FileExternalEntry file) throws IOException{
    if(!file.fileEntry.isFile()){
      Log.i(TAG, "not a file");
      throw new FileNotFoundException();
    }
    Log.i(TAG, "read file: " + file.fileEntry.getName());

    ContentResolver ctx = cordova.getActivity().getContentResolver();
    InputStream is = ctx.openInputStream(file.fileEntry.getUri());

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024*32];
    int bytesRead;

    while ((bytesRead = is.read(buffer)) != -1) {
      os.write(buffer, 0, bytesRead);
    }

    is.close();
    os.close();

    return os.toByteArray();
  }

  private void remove(FileExternalEntry entry) throws IOException{
    Log.i(TAG, "remove: " + entry.fileEntry.getUri());

    if(!entry.fileEntry.delete()){
      Log.i(TAG, "could not delete");
      throw new IOException();
    }
  }

  private void createDir(FileExternalEntry path, String dirName) throws FileNotFoundException {
    if(!path.fileEntry.isDirectory()) {
      Log.i(TAG, "entry is not a directory");
      throw new FileNotFoundException();
    }

    path.fileEntry.createDirectory(dirName);
  }

  private void writeFile(FileExternalEntry target, String fileName, String data) throws IOException {
    if(!target.fileEntry.isDirectory()) {
      Log.i(TAG, "entry is not a directory");
      throw new FileNotFoundException();
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

    is.close();
    os.close();
  }

}
