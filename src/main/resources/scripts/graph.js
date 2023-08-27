// create an array with nodes
const nodes = new vis.DataSet([
    { id: 1, label: "Node 1" },
    { id: 2, label: "Node 2" },
    { id: 3, label: "Node 3" },
    { id: 4, label: "Node 4" },
    { id: 5, label: "Node 5" },
]);

// create an array with edges
const edges = new vis.DataSet([
    { from: 1, to: 3 },
    { from: 1, to: 2 },
    { from: 2, to: 4 },
    { from: 2, to: 5 },
    { from: 3, to: 3 },
]);

// create a network
const container = document.getElementById("root");
const data = {
    nodes: nodes,
    edges: edges,
};
const options = {};

const network = new vis.Network(container, data, options);


if (!!window.EventSource) {
    const source = new EventSource('/graph-events');
    console.log("!1")
    source.onmessage = function (e) {
        const data = JSON.parse(e.data);
        nodes.add(data)
        //document.body.innerHTML += e.data + '<br>';
    };

    source.addEventListener('open', function (e) {
        console.log("!3")
        // Connection was opened.
    }, false);

    source.addEventListener('error', function (e) {
        console.log("!4")
        if (e.readyState === EventSource.CLOSED) {
            // Connection was closed.
        }
    }, false);
}
