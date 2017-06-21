
var exec = require('cordova/exec');

var PLUGIN_NAME = 'FileExtra';

var FileExtra = {
  dirChooser: function(){
    return new Promise(function(resolve, reject) {
      exec(resolve, reject, PLUGIN_NAME, 'dirChooser', []);
    });
  }
};

module.exports = FileExtra;
