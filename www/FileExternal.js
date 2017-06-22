
var exec = require('cordova/exec');

var PLUGIN_NAME = 'FileExternal';

var FileExternal = {
  dirChooser: function(){
    return new Promise(function(resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'dirChooser', []);
    });
  }
};

module.exports = FileExternal;
