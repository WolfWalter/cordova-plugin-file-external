var exec = require('cordova/exec');

var PLUGIN_NAME = 'FileExternal';

var FileExternal = {
  dirChooser: function(){
    return new Promise(function(resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'dirChooser', []);
    });
  },
  listDir: function(rootUri, dir){
    return new Promise(function(resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'listDir', [rootUri, dir]);
    });
  },
  readFile: function(rootUri, file){
    return new Promise(function(resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'readFile', [rootUri, file]);
    });
  },

};

module.exports = FileExternal;
