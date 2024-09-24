import {Header, Logo, Status} from "./components/Header/Header.jsx";
import './styles/Content.css';
import './styles/Tree.css';
import {Sidebar} from "./components/Sidebar/Sidebar.jsx";
import useGraphStream from "./hooks/useGraphStream.js";

import {SidebarItem} from "./components/Sidebar/SidebarItem.jsx";
import Body from "./components/Body/Body.jsx";
import TreeSection from "./components/Tree/TreeSection.jsx";
import {Box} from "@radix-ui/themes";
import config from "./config.js";
import {useState} from "react";

function App() {
    const [selectedRootNode, setSelectedRootNode] = useState();
    const [backendLoading, setBackendLoading] = useState(true);
    const {
        graph,
        backgroundGraph,
        setGraph, // Now we have access to setGraph here
        loading,
        cached,
        setCached
    } = useGraphStream(config.backendHost + "/graph", setSelectedRootNode, backendLoading, setBackendLoading);

    const handleCheckboxChange = () => {
        setCached(prevCached => {
            const newCached = !prevCached;

            // If unchecking (cached -> false), update the graph with the latest background data
            if (!newCached) {
                setGraph(backgroundGraph); // Show the latest data
                console.log(selectedRootNode);
            }

            return newCached;
        });
    };

    return (
        <div className="App">
            <Header>
                <Logo/>
                <Status host={config.backendHost} status={backendLoading}/>

                <div style={{opacity: 0.5}}>
                    <label>
                        <input
                            type="checkbox"
                            checked={cached}
                            onChange={handleCheckboxChange}
                        />
                        <span style={{fontWeight: 'bold'}}>Use Cached Data</span>
                    </label>
                </div>
            </Header>

            <Body>
                <Sidebar graph={graph ? graph : null}>
                    {graph && graph.nodes && graph.nodes.length > 0 ? (
                        graph.nodes.map(({record}) => (
                            <SidebarItem
                                key={record.recordId}
                                record={record}
                                isSelected={record.recordId === selectedRootNode}
                                onClick={() => setSelectedRootNode(record.recordId)}
                            />
                        ))
                    ) : (
                        <SidebarItem
                            key="no-nodes"
                            record={{recordId: 'none', label: 'No Nodes Available'}}
                            isSelected={false}
                        />
                    )}
                </Sidebar>

                <TreeSectionWrapper
                    graph={graph}
                    loading={loading}
                    selectedRootNode={selectedRootNode}
                />
            </Body>
        </div>
    );
}


function TreeSectionWrapper({graph, loading, selectedRootNode}) {
    if (loading) {
        return <Box className="TreeSection">Connecting to crawler...</Box>;
    }

    if (!graph || !graph.nodes || graph.nodes.length === 0) {
        return <Box className="TreeSection">No records currently crawled. Add one!</Box>;
    }

    if (!selectedRootNode) {
        return <Box className="TreeSection">Select record!</Box>;
    }

    return <TreeSection rootNodes={graph.nodes} selected={selectedRootNode}/>;
}

export default App;
