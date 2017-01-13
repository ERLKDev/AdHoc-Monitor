angular.module('MonitorApp', ['ngVis', 'chart.js', 'rzModule', 'angular-flot', 'toggle-switch', 'btford.socket-io']);

// Changing interpolation start/end symbols.
angular.module('MonitorApp').config(function($interpolateProvider, $httpProvider){         
    $interpolateProvider.startSymbol('[[').endSymbol(']]');
})