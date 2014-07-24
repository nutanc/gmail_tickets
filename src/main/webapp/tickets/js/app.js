'use strict';

/* App Module */

var App=angular.module('phonecat', ['ngRoute','ngGrid','phonecatFilters', 'ticketServices','ui.tinymce','ngCkeditor','angular-loading-bar', 'ngAnimate','ui.bootstrap']).
  config(['$routeProvider', function($routeProvider) {
  $routeProvider.
      when('/tickets', {templateUrl: 'partials/ticket-list.htm', controller: TicketListCtrl}).
       when('/tickets/:ticketId', {templateUrl: 'partials/ticket.htm', controller: TicketCtrl}).
      otherwise({redirectTo: '/tickets'});
}]);



App.filter('isempty', function() {
    return function(input, replaceText) {
        if(input) return input;
        return replaceText;
    }
});

App.filter('capitalize', function() {
    return function(input, scope) {
        return input.substring(0,1).toUpperCase()+input.substring(1);
    }
});
