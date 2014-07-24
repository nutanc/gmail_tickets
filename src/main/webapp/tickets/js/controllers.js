'use strict';

/* Controllers */


function TicketListCtrl($scope, $routeParams, $timeout, $resource, Ticket) {
    console.log("hello");
    $scope.ticketId = $routeParams.ticketId;
    $scope.tickets = {};
    $scope.composeEmail = {};
    $scope.label = "open";
    $scope.selectedTicket = null;
    $scope.isComposePopupVisible = false;
    $scope.sendEmail = function() {
        $scope.isComposePopupVisible = false;
        $scope.composeEmail.from = "me";
        $scope.composeEmail.date = new Date();
        var SendMessage = $resource('../sendMessage.jsp', {});
        SendMessage.save({from: $scope.composeEmail.from, to: $scope.composeEmail.to, subject: $scope.composeEmail.subject, body: $scope.composeEmail.body}, function(sendMessage) {
            console.log("Sen message:" + sendMessage);
        });
    };

    $scope.showComposePopup = function() {
        console.log("Compose Mail");
        $scope.composeEmail = {};
        $scope.isComposePopupVisible = true;
        console.log("Compose Mail" + $scope.isComposePopupVisible);
    };

    // setup editor options
    $scope.tinymceOptions = {
        //toolbar: false,
        menubar: false,
        plugins: "autoresize",
        handle_event_callback: function(e) {
            // put logic here for keypress
        }
    };
    $scope.editorOptions = {
        language: 'en',
        uiColor: '#000000',
        toolbarGroups: [
            {name: 'basicstyles', groups: ['basicstyles', 'cleanup']},
            {name: 'paragraph', groups: ['list', 'indent', 'blocks', 'align', 'bidi']},
            {name: 'links'},
            {name: 'insert'},
        ]
    };
    $scope.ticketMessage = "";
    Ticket.get({ticketId: $routeParams.ticketId}, function(tickets) {
        console.log(tickets);
        $scope.tickets = tickets;
        //$timeout(tick, 300000);
    });
    $scope.getMessageDetails = function(ticket) {
        //console.log(ticket);
        $scope.selectedTicket = ticket;
        var TicketMessage = $resource('../ticketMessage.jsp?ticketId=:ticketId', {});
        TicketMessage.get({ticketId: ticket.threadId}, function(ticketMessage) {
            //console.log("message:"+ticketMessage.message);
            $scope.ticketMessage = "";
            $scope.ticketMessage = ticketMessage.body;
            //$scope.ticketMessage="got message";
            //$timeout(tick, 300000);
        });
    }
    $scope.loadMessages = function(label) {

        /*$http.get('../loadMessages.jsp?label=' + label).success(function(data, status, headers, config) {
         console.log(data);
         $scope.tickets.openTicketDetails = data.labelTicketDetails;
         });*/
        $scope.label=label;
        $scope.tickets.openTicketDetails={};
        var LabelMessage = $resource('../loadMessages.jsp?label=:label', {});
        LabelMessage.get({label: label}, function(labelMessage) {
            console.log(labelMessage);
            $scope.tickets.openTicketDetails = labelMessage.labelTicketDetails;
        });
    }


    $scope.trashMessage = function(ticket) {
        //console.log(ticket);
        $scope.selectedTicket = ticket;
        var TrashMessage = $resource('../trashMessage.jsp?ticketId=:ticketId', {});
        TrashMessage.get({ticketId: ticket.threadId}, function(trashMessage) {
            console.log("message:" + trashMessage);
            Ticket.get({}, function(tickets) {
                console.log("remaining tickets:" + tickets);
                $scope.tickets = tickets;
                $scope.ticketMessage = "";
            });

        });
    }

    $scope.setMessageLabel = function(ticket, label) {
        console.log(ticket + "label" + label);
        var LabelMessage = $resource('../setLabel.jsp?ticketId=:ticketId&label=:label', {});
        LabelMessage.get({ticketId: ticket.id, label: label}, function(labelMessage) {
            console.log("message:" + labelMessage);

            Ticket.get({}, function(tickets) {
                console.log("remaining tickets:" + tickets);
                $scope.tickets = tickets;
                $scope.ticketMessage = "";
            });
        });
    }


    $scope.refreshMessages = function() {
        Ticket.get({}, function(tickets) {
            console.log("remaining tickets:" + tickets);
            $scope.tickets = tickets;
            //$scope.ticketMessage = "";
        });
    }
}




function TicketCtrl($scope, $routeParams, $timeout, $resource, Ticket) {
    console.log("hello CTRL");
    $scope.ticketId = $routeParams.ticketId;
    $scope.ticket = {};
    // setup editor options
    $scope.tinymceOptions = {
        //toolbar: false,
        menubar: false,
        plugins: "autoresize",
        handle_event_callback: function(e) {
            // put logic here for keypress
        }
    };
    $scope.editorOptions = {
        language: 'en',
        uiColor: '#000000',
        toolbarGroups: [
            {name: 'basicstyles', groups: ['basicstyles', 'cleanup']},
            {name: 'paragraph', groups: ['list', 'indent', 'blocks', 'align', 'bidi']},
            {name: 'links'},
            {name: 'insert'},
        ]
    };
    $scope.ticketMessage = "";
    Ticket.get({ticketId: $routeParams.ticketId}, function(ticket) {
        console.log(ticket);
        $scope.ticket = ticket;
        $scope.ticketMessage = ticket.body;
        //$timeout(tick, 300000);
    });

    $scope.trashMessage = function(ticket) {
        //console.log(ticket);
        $scope.selectedTicket = ticket;
        var TrashMessage = $resource('../trashMessage.jsp?ticketId=:ticketId', {});
        TrashMessage.get({ticketId: ticket.id}, function(trashMessage) {
            console.log("message:" + trashMessage);
            $scope.actionMessage = trashMessage;
            $scope.ticketMessage = "";
            /*Ticket.get({}, function(tickets) {
             console.log("remaining tickets:" + tickets);
             $scope.tickets = tickets;
             $scope.ticketMessage = "";
             });*/

        });
    }

    $scope.setMessageLabel = function(ticket, label) {
        console.log(ticket + "label" + label);
        var LabelMessage = $resource('../setLabel.jsp?ticketId=:ticketId&label=:label', {});
        LabelMessage.get({ticketId: ticket.id, label: label}, function(labelMessage) {
            console.log("message:" + labelMessage);
            $scope.actionMessage = labelMessage;
            $scope.ticketMessage = "";
            /*Ticket.get({}, function(tickets) {
             console.log("remaining tickets:" + tickets);
             $scope.tickets = tickets;
             $scope.ticketMessage = "";
             });*/
        });
    }
}

