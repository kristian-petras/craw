import {Box, Flex, Link, Text} from "@radix-ui/themes";
import Tree from "react-d3-tree";

const TreeNode = ({nodeDatum}) => {
    const formattedStart = new Date(nodeDatum.start).toLocaleString();
    const formattedEnd = nodeDatum.end ? new Date(nodeDatum.end).toLocaleString() : 'Loading...';

    return (
        <foreignObject className="TreeNode" width="200" height="120">
            <Flex className="Container" direction="column" gap="1">
                <Text truncate size="2" className="Text SiteTitle">
                    {nodeDatum.title}
                </Text>
                <Link truncate size="1" className="Text Url" href={nodeDatum.name}>
                    {nodeDatum.url}
                </Link>
                <Text truncate size="1" className="Text Date">
                    {formattedStart}
                </Text>
                <Text truncate size="1" className="Text Date">
                    {formattedEnd}
                </Text>
            </Flex>
        </foreignObject>
    );
};

const TreeView = ({treeData}) => {
    return (
        <Box className="TreeView">
            <Tree
                data={treeData}
                pathFunc="step"
                nodeSize={{x: 200, y: 300}}
                orientation="vertical"
                initialScale={0.1}
                renderCustomNodeElement={(rd3tProps) => <TreeNode {...rd3tProps} />}
            />
        </Box>
    );
};

export default TreeView;
