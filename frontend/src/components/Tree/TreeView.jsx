import { Box, Flex, Link, SegmentedControl, Text } from "@radix-ui/themes";
import Tree from "react-d3-tree";

const getDomain = (url) => {
  const urlObj = url && url.startsWith("http") ? new URL(url) : null;
  return urlObj ? urlObj.hostname : null;
};

const mergeByDomain = (nodes) => {
  const domainMap = {};

  nodes.forEach((node) => {
    const domain = getDomain(node.url);

    if (!domain) return;

    if (!domainMap[domain]) {
      domainMap[domain] = { ...node, children: [] };
    }

    const filteredChildren = node.children.filter(
      (child) => getDomain(child.url) !== domain,
    );
    domainMap[domain].children.push(...filteredChildren);
  });

  return Object.values(domainMap).map((node) => ({
    ...node,
    children: mergeByDomain(node.children),
  }));
};

const TreeViewChange = ({ isDomainMode, setDomainMode }) => {
  const handleValueChange = (value) => {
    setDomainMode(value === "domains");
  };

  return (
    <SegmentedControl.Root
      value={isDomainMode ? "domains" : "websites"}
      onValueChange={handleValueChange}
      radius="large"
      className="TreeViewChange"
      size="3"
    >
      <SegmentedControl.Item value="websites">Websites</SegmentedControl.Item>
      <SegmentedControl.Item value="domains">Domains</SegmentedControl.Item>
    </SegmentedControl.Root>
  );
};

const TreeNode = ({ nodeDatum }) => {
  const formattedStart = new Date(nodeDatum.start).toLocaleString();
  const formattedEnd = nodeDatum.end
    ? new Date(nodeDatum.end).toLocaleString()
    : "Loading...";

  return (
    <foreignObject className="TreeNode" width="200" height="120">
      <Flex className="Container" direction="column" gap="1">
        <Flex direction="row" justify="between" align="center">
          <Text truncate size="2" className="Text SiteTitle">
            {nodeDatum.title}
          </Text>
          <Text truncate size="1" className="Text">
            {nodeDatum.type}
          </Text>
        </Flex>

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

const TreeView = ({ rootNode, isDomainMode, setDomainMode }) => {
  const convertToTreeData = (node) => ({
    type: node.type,
    title: node.title,
    url: node.url,
    start: node.start,
    end: node.end,
    children: node.nodes.map(convertToTreeData),
  });

  if (!rootNode.node) {
    return (
      <Box className="TreeView" p="4">
        Execution not runned{" "}
      </Box>
    );
  }

  let treeData = convertToTreeData(rootNode.node);

  if (isDomainMode) {
    treeData = mergeByDomain([treeData])[0];
  }

  return (
    <Box className="TreeView">
      <TreeViewChange
        isDomainMode={isDomainMode}
        setDomainMode={setDomainMode}
      />
      <Tree
        data={treeData}
        pathFunc="step"
        nodeSize={{ x: 200, y: 300 }}
        orientation="vertical"
        initialScale={0.1}
        renderCustomNodeElement={(rd3tProps) => <TreeNode {...rd3tProps} />}
      />
    </Box>
  );
};

export default TreeView;
