import {Box} from "@radix-ui/themes";
import TreeHeading from './TreeHeading';
import TreeView from './TreeView';

const TreeSection = ({rootNodes, selected}) => {
    const rootNode = rootNodes.find(node => node.record.recordId === selected);

    const convertToTreeData = (node) => ({
        type: node.type,
        title: node.title,
        url: node.url,
        start: node.start,
        end: node.end,
        children: node.nodes.map(convertToTreeData),
    });

    const treeData = convertToTreeData(rootNode.node);

    return (
        <Box className="TreeSection">
            <TreeHeading
                label={rootNode.record.label}
                url={rootNode.node.url}
                recordId={rootNode.record.recordId}
                selected={selected}
            />
            <TreeView treeData={treeData}/>
        </Box>
    );
};

export default TreeSection;
