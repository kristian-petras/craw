import {Box} from "@radix-ui/themes";
import TreeHeading from './TreeHeading';
import TreeView from './TreeView';
import {useState} from "react";


const getDomain = (url) => {
    const urlObj = url && url.startsWith('http') ? new URL(url) : null;
    return urlObj ? urlObj.hostname : null;
};

const mergeByDomain = (nodes) => {
    const domainMap = {};

    nodes.forEach(node => {
        const domain = getDomain(node.url);

        if (!domain) return;

        if (!domainMap[domain]) {
            domainMap[domain] = { ...node, children: [] };
        }

        const filteredChildren = node.children.filter(child => getDomain(child.url) !== domain);
        domainMap[domain].children.push(...filteredChildren);
    });

    return Object.values(domainMap).map(node => ({
        ...node,
        children: mergeByDomain(node.children),
    }));
};

const TreeSection = ({rootNodes, selected}) => {
    const rootNode = rootNodes.find(node => node.record.recordId === selected);

    const [isDomainMode, setDomainMode] = useState(true);

    const convertToTreeData = (node) => ({
        type: node.type,
        title: node.title,
        url: node.url,
        start: node.start,
        end: node.end,
        children: node.nodes.map(convertToTreeData),
    });

    let treeData= convertToTreeData(rootNode.node);

    if (isDomainMode) {
        console.log(treeData);
        treeData = mergeByDomain([treeData])[0];
    }

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
