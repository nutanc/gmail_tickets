'use strict';

/* Directives */

App.directive('peityBar', function() {
    return {
        // Restrict it to be an attribute in this case
        restrict: 'A',
        // responsible for registering DOM listeners as well as updating the DOM
        link: function(scope, element, attrs) {
            $(element).peity(attrs.peityBar);
        }
    };
});
App.directive('pieChart', function() {
    return{
        restrict: 'A',
        scope: {pieData: '=data'},
        link: function(scope, elem, attrs) {

            var chart = null,
                    opts = {
                series: {
                    pie: {
			            innerRadius: 0.3,
                        show: true
                    }
                },
                legend: {
                    show: false
                }
            };


            //var data = scope[attrs.ngModel];            

            scope.$watch("pieData", function(v) {
                //console.log("watching pie data");
                //console.log(v);
                if (!chart) {
                    chart = $.plot(elem, v, opts);
                } else {
                    chart.setData(v);
                    chart.setupGrid();
                    chart.draw();
                }
            });
        }
    };
});


App.directive('lineChart', function() {
    return{
        restrict: 'A',
        scope: {lineChartData: '=data'},
        link: function(scope, elem, attrs) {

            var chart = null,
                    opts = {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    ticks: 12
                }
            };

            scope.$watch("lineChartData", function(v) {
                console.log(v);
                if (!chart) {
                    chart = $.plot(elem, v, opts);
                } else {
                    chart.setData(v);
                    chart.setupGrid();
                    chart.draw();
                }
            });
        }
    };
});

App.directive('pivot', function() {
    return{
        restrict: 'A',
        scope: {pivotData1: '=data'},
        link: function(scope, elem, attrs) {
            scope.$watch("pivotData1", function(v) {
                console.log("watching pivot data");
                console.log(v);
					//have hardcoded the div id value, must get this dynamically
                    $("#pivotHolder").pivotUI(v, {
						//have hardcoded the rows and columns, must get this dynamically from attributes
                        rows: ["agentID"],
                        cols: ["feedback"],
                        effectsName: "Heatmap"
                    });

            });
        }
    };
});


App.directive('gridster', function() {
    return{
        restrict: 'A',
        link: function(scope, elem, attrs) {
					//have hardcoded the div id value, must get this dynamically
					    $(".gridster ul").gridster({
					widget_margins: [10, 10],
					widget_base_dimensions: [140, 140]
				});

/*    gridster = $(".gridster > ul").gridster({
        widget_margins: [10, 10],
        widget_base_dimensions: [140, 140],
        min_cols: 6
    }).data('gridster');
*/
        }
    };
});


