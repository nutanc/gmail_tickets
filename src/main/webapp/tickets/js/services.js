'use strict';

/* Services */
/*
angular.module('ticketServices', ['ngResource']).
    factory('Ticket', function($resource){
  return $resource('phones/tickets.json');
});
*/
angular.module('ticketServices', ['ngResource']).
    factory('Ticket', function($resource){
  /*return $resource('phones/tickets.json?ticketId=:ticketId',{},{
	  query: {method:'GET'}
	  });*/
	return $resource('../tickets.jsp?ticketId=:ticketId',{},{
	  query: {method:'GET'}
	  });
  //return $resource('phones/mps.json');
});
