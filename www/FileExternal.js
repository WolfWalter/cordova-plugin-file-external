var exec = require('cordova/exec');

var PLUGIN_NAME = 'FileExternal';

var FileExternal = {
  dirChooser: function(){
    return new Promise(function(resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'dirChooser', []);
    });
  },
  listDir: function(path, dir){
    return new Promise(function(resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'listDir', [path, dir]);
    });
  }
};

module.exports = FileExternal;
