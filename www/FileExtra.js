
var exec = require('cordova/exec');

var PLUGIN_NAME = 'FileExtra';

var FileExtra = {
  dirChooser: function(cb) {
    exec(cb, null, PLUGIN_NAME, 'dirChooser', []);
  }
};

module.exports = FileExtra;
