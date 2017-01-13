angular.module('MonitorApp').controller('MainCtrl', function ($scope, VisDataSet, $interval, $timeout, mySocket) {
    var network = null;
    $scope.selectedNode = null;
    $scope.toggleSpeedChart = false;
    $scope.toggleTotalCpuChart = false;

    mySocket.emit('delay', 0);

    $scope.refreshThread = $interval(function() {
        mySocket.emit('reqData');
    }, 500);


    mySocket.forward('nodeData', $scope);
    $scope.$on('socket:nodeData', function (ev, data) {
        data = JSON.parse(data);
        $scope.networkTime = data.time;
        updateNodes(data);
        updateEdges(data);
        updateCharts(data);
    });

    $scope.onLoaded = function (networkRef) {
        network = networkRef;
    };

    $scope.onSelectNode = function(event){
        $scope.selectedNode = event.nodes[0];
    }

    $scope.onDeselectNode = function(event){
        $scope.selectedNode = null;
    }

    $scope.onSelectEdge = function(event){
        $scope.selectedEdge = event.edges[0];
    }

    $scope.onDeselectEdge = function(event){
        $scope.selectedEdge = null;
    }


    function updateNodes(data){
        $scope.nodes = data.nodes;
        $scope.edges = data.edges;

        /* Update nodes. */
        var nodes = $scope.graphData.nodes.get();
        for (var i = 0; i < data.nodes.length; i++) {
            $scope.graphData.nodes.update({id: data.nodes[i].id, color: data.nodes[i].color, label: data.nodes[i].id, font: '14px arial white'});
        }

        /* Remove old nodes. */
        nodes = $scope.graphData.nodes.get();
        for (var i = 0; i < nodes.length; i++){
            var found = false;
            for (var j = 0; j < data.nodes.length; j++){
                if(nodes[i].id == data.nodes[j].id){
                    found = true;
                }
            }
            if(found == false){
                $scope.graphData.nodes.remove(nodes[i].id)
            }
        }

        /* Checks if a node is selected. */
        if($scope.selectedNode != null){
            node = null
            for (i = 0; i < data.nodes.length; i ++){
                if(data.nodes[i].id === $scope.selectedNode){
                    node = data.nodes[i];
                    break;
                }
            }
            $scope.node = node;
        }
    }

    function updateEdges(data){
        /* Update edges. */
        var edges = $scope.graphData.edges.get();
        for (var i = 0; i < data.edges.length; i++) {
            $scope.graphData.edges.update({to: data.edges[i].to, from: data.edges[i].from, id: data.edges[i].id, color: data.edges[i].color, width: 2, arrows: data.edges[i].arrows});
        }

        /* Remove old edges. */
        edges = $scope.graphData.edges.get();
        for (var i = 0; i < edges.length; i++){
            var found = false;
            for (var j = 0; j < data.edges.length; j++){
                if(edges[i].id == data.edges[j].id){
                    found = true;
                }
            }
            if(found == false){
                $scope.graphData.edges.remove(edges[i].id)
            }
        }

        /* Checks if a edge is selected. */
        if($scope.selectedEdge != null && $scope.selectedNode == null){
            edge = null
            for (i = 0; i < data.edges.length; i ++){
                if(data.edges[i].id === $scope.selectedEdge){
                    edge = data.edges[i];
                    break;
                }
            }
            $scope.edge = edge;
        }

    }

    function updateCharts(data){
        /* Update charts. */
        $scope.speedCharts = [];
        $scope.ioCharts = [];

        for(i = 0; i < data.nodes.length; i++){
            if($scope.selectedNode != null && $scope.selectedNode != data.nodes[i].id){
                continue;
            }

            /* Checks which chart is selected. */
            if($scope.toggleSpeedChart){
                $scope.speedCharts.push({ data: data.nodes[i].speedChart, yaxis: 1, xaxis: 1, label: data.nodes[i].id});
            }else{
                $scope.speedCharts.push({ data: data.nodes[i].cpuUsageChart, yaxis: 1, xaxis: 1, label: data.nodes[i].id});
                if($scope.toggleTotalCpuChart){
                    $scope.speedCharts.push({ data: data.nodes[i].cpuTotalUsageChart, yaxis: 1, xaxis: 1, label: data.nodes[i].id + " total"})
                }
            }
            
            /* Checks if a node is selected. */
            if($scope.selectedNode != null){
                $scope.ioCharts.push({ data: data.nodes[i].ioSendChart, yaxis: 1, xaxis: 1, label: "Bytes send"});
                $scope.ioCharts.push({ data: data.nodes[i].ioRecvChart, yaxis: 1, xaxis: 1, label: "Bytes received"});
            }else{
                $scope.ioCharts.push({ data: data.nodes[i].ioTotalChart, yaxis: 1, xaxis: 1, label: data.nodes[i].id});
            }
            
        }
    }

    /* Adds delay to the monitor. */
    $scope.setDelay = function(){
        mySocket.emit('delay', $scope.slider.value * 1000);
    }

    $scope.graphData = {
        nodes: new VisDataSet(),
        edges: new VisDataSet()
    };

    $scope.graphEvents = {
        onload: $scope.onLoaded,
        selectNode: $scope.onSelectNode,
        deselectNode: $scope.onDeselectNode,
        selectEdge: $scope.onSelectEdge,
        deselectEdge: $scope.onDeselectEdge
    };

    $scope.graphOptions = {
        height: '100%',
        width: '100%',
        layout: {
            randomSeed: 192763.02
        },
        edges : {
            physics: true,
            length: 200
        }
    };

    $scope.myChartOptions = {
        legend:{position: "nw"},
        xaxis: {
            mode: "time",
            timeformat: "%H:%M:%S",
            timezone: "browser"
        },
        yaxis: {
            min: 0
        },
        hierarchical: true
    };

    $scope.myChartOptionsCPU = {
        legend:{position: "nw"},
        xaxis: {
            mode: "time",
            timeformat: "%H:%M:%S",
            timezone: "browser"
        },
        yaxis: {
            min: 0,
            max: 100
        },
        hierarchical: true
    };

    $scope.slider = {
      value: 0,
      options: {
        floor: 0,
        ceil: 300,
        rightToLeft: true,
        onEnd: $scope.setDelay
      }
    };

});

angular.module('MonitorApp').factory('mySocket', function (socketFactory) {
  return socketFactory();
});