import {Box, Flex, Text} from "@radix-ui/themes";
import TreeHeading from './TreeHeading';
import TreeView from './TreeView';
import {useState} from "react";
import ExecutionsView from "../Execution/ExecutionsView.jsx";


const TreeSection = ({rootNodes, selected}) => {
    const [isDomainMode, setDomainMode] = useState(false);
    const rootNode = rootNodes.find(node => node.record.recordId === selected);

    if (!rootNode) {
        return <Box>Select record</Box>
    }

    return (
        <Box className="TreeSection">
            <TreeHeading
                label={rootNode.record.label}
                url={rootNode.record.url}
                record={rootNode.record}
                selected={selected}
            />
            <Flex height="100%" gap="3">
                <TreeView rootNode={rootNode} isDomainMode={isDomainMode} setDomainMode={setDomainMode}/>
                <Flex direction="column" className="ExecutionsView">
                    <Text size="3" weight="bold">Executions</Text>
                    <ExecutionsView executions={rootNode.execution}/>
                </Flex>
            </Flex>
        </Box>
    );
};

export default TreeSection;
