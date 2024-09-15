import React, {useState} from 'react';
import {Header, Logo, Status} from "./components/Header/Header.jsx";
import './styles/Content.css';
import Sidebar from "./components/Sidebar/Sidebar.jsx";
import backends_placeholder from "./dummy/backends.js";
import usePingBackends from "./hooks/usePingBackends.js";
import useGraphStream from "./hooks/useGraphStream.js";

import {SidebarItem} from "./components/Sidebar/SidebarItem.jsx";
import Body from "./components/Body/Body.jsx";
import TreeSection from "./components/Tree/TreeSection.jsx";
import {Box} from "@radix-ui/themes";

function App() {
    const initialBackends = backends_placeholder;

    const availableBackends = usePingBackends(initialBackends);
    const [selectedRootNode, setSelectedRootNode] = useState();
    const {graph, loading} = useGraphStream('http://localhost:8080/graph', setSelectedRootNode);


    return (
        <div className="App">
            <Header>
                <Logo/>
                <Status availableBackends={availableBackends}/>
            </Header>

            <Body>
                <Sidebar>
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
