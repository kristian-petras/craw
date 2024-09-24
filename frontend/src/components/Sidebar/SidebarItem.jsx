import { Box, Card, DataList, Link, Text } from "@radix-ui/themes";
import * as Tooltip from "@radix-ui/react-tooltip";

export const SidebarTitle = ({ children }) => {
  return (
    <Box className="SidebarTitle">
      <Text weight="bold" size="2">
        {children}
      </Text>
    </Box>
  );
};

export const SidebarItem = ({ record, onClick, isSelected }) => {
  const className = `SidebarItem ${isSelected ? "selected" : ""}`;

  return (
    <Tooltip.Provider>
      <Tooltip.Root>
        <Tooltip.Trigger asChild>
          <Box className={className} onClick={onClick}>
            <Text size="2" highContrast>
              {record.label}
            </Text>
          </Box>
        </Tooltip.Trigger>
        <Tooltip.Content className="TooltipContent" side="right" align="center">
          <SidebarItemTooltip record={record} />
        </Tooltip.Content>
      </Tooltip.Root>
    </Tooltip.Provider>
  );
};

const SidebarItemTooltip = ({ record }) => {
  return (
    <Card className="SidebarItemTooltip">
      <DataList.Root size="1">
        <DataList.Item>
          <DataList.Label minWidth="88px">identifier</DataList.Label>
          <DataList.Value>{record.identifier}</DataList.Value>
        </DataList.Item>
        <DataList.Item>
          <DataList.Label minWidth="88px">label</DataList.Label>
          <DataList.Value>{record.label}</DataList.Value>
        </DataList.Item>
        <DataList.Item>
          <DataList.Label minWidth="88px">url</DataList.Label>
          <DataList.Value>
            <Link href="">{record.node ? record.node.url : "missing"}</Link>
          </DataList.Value>
        </DataList.Item>
        <DataList.Item>
          <DataList.Label minWidth="88px">regexp</DataList.Label>
          <DataList.Value>{record.regexp}</DataList.Value>
        </DataList.Item>
        <DataList.Item>
          <DataList.Label minWidth="88px">active</DataList.Label>
          <DataList.Value>{record.active ? "true" : "false"}</DataList.Value>
        </DataList.Item>
      </DataList.Root>
    </Card>
  );
};
