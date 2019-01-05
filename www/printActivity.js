var exec = require('cordova/exec');

exports.print = function( str, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, 'PrintActivity', 'print', str);
};
exports.readNfcCard = function( successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, 'PrintActivity', 'readNfcCard',[]);
};
exports.efawateerPrint = function( str, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, 'PrintActivity', 'efawateerPrint', str);
};
exports.efawateerPrint = function( str, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, 'PrintActivity', 'testPrint', []]);
};