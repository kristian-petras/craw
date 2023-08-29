const container = document.getElementById("root");

const nodes = new vis.DataSet([])
const edges = new vis.DataSet([])

const network = new vis.Network(container, {nodes: nodes, edges: edges}, {})

if (!!window.EventSource) {
    const source = new EventSource('/graph-events');

    source.addEventListener('node', (e) => {
        const data = JSON.parse(e.data);
        nodes.update(data)
        console.log(data)
    })

    source.addEventListener('edge', (e) => {
        const data = JSON.parse(e.data);
        edges.update(data)
        console.log(data)
    })

    source.addEventListener('open', (e) => {
        console.log("Connection was opened.")
    })

    source.addEventListener('error', (e) => {
        if (e.readyState === EventSource.CLOSED) {
            console.log("Connection was closed.")
        } else {
            console.log(`Connection error: ${e}`)
        }
    })
} else {
    console.log("Event source not supported.")
}
