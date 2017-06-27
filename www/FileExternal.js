var exec = require('cordova/exec');

var PLUGIN_NAME = 'FileExternal';

var FileExternal = {
  dirChooser: function(){
    return new Promise(function(resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'dirChooser', []);
    });
  },
  listDir: function(extRootUri, extPath){
    return new Promise(function(resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'listDir', [extRootUri, extPath]);
    });
  },
  readFile: function(extRootUri, extFilePath){
    return new Promise(function(resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'readFile', [extRootUri, extFilePath]);
    });
  },
  remove: function(extRootUri, extPath){
    return new Promise(function(resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'remove', [extRootUri, extPath]);
    });
  },
  createDir: function(extRootUri, extPath, dir){
    return new Promise(function(resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'createDir', [extRootUri, extPath, dir]);
    });
  },
  writeFile: function(extRootUri, extPath, file, data){
    return new Promise(function(resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'writeFile', [extRootUri, extPath, file, data]);
    });
  },
  copyAssetsToExternal: function(assetsPath, extRootUri, extPath){
    return new Promise(function(resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'copyAssetsToExternal', [assetsPath, extRootUri, extPath]);
    });
  }
};

module.exports = FileExternal;
