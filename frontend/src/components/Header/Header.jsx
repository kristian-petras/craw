import "../../styles/Header.css";
import { Flex, Heading, Select, Separator, Text } from "@radix-ui/themes";
import { FaSpider } from "react-icons/fa6";

export const Header = ({ children }) => (
  <header className="Header">
    <Flex
      className="HeaderItems"
      direction="row"
      justify="between"
      align="center"
      gap="4"
    >
      {children}
    </Flex>
  </header>
);

export const Logo = () => (
  <Flex className="HeaderTitle" justify="center" direction="row" gap="1">
    <FaSpider className="HeaderIcon" size={16} />
    <Heading className="HeaderItem">craw</Heading>
  </Flex>
);

export const Status = ({ host, status }) => (
  <Select.Root defaultValue={host} size="2">
    <Select.Trigger variant="ghost" />
    <Select.Content position="popper">
      <Select.Item key={host} value={host}>
        <Flex direction="row" gap="2" align="center">
          <Text weight="bold">Backend</Text>
          <Separator orientation="vertical" />
          <Text>{host}</Text>
          <Separator orientation="vertical" />
          <Text
            size="2"
            highContrast
            style={{ color: !status ? "inherit" : "#FF977D" }}
          >
            {!status ? "Online" : "Offline"}
          </Text>
        </Flex>
      </Select.Item>
    </Select.Content>
  </Select.Root>
);
