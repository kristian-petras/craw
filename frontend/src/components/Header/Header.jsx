import '../../styles/Header.css'
import {Flex, Heading, Select, Separator, Text} from "@radix-ui/themes";
import {FaSpider} from "react-icons/fa6";

export const Header = ({children}) => (
    <header className="Header">
        <Flex className="HeaderItems" direction="row" justify="between" align="center" gap="4">
            {children}
        </Flex>
    </header>
);


export const Logo = () => (
    <Flex className="HeaderTitle" justify="center" direction="row" gap="1">
        <FaSpider className="HeaderIcon" size={16}/>
        <Heading className="HeaderItem">craw</Heading>
    </Flex>
);

export const Status = ({availableBackends}) => (
    <Select.Root defaultValue={availableBackends[0]?.host.toString()} size="2">
        <Select.Trigger variant="ghost"/>
        <Select.Content position="popper">
            {availableBackends.map((backend) => (
                <Select.Item key={backend.host} value={backend.host.toString()}>
                    <Flex direction="row" gap="2" align="center">
                        <Text weight="bold">{backend.title}</Text>
                        <Separator orientation="vertical"/>
                        <Text>{backend.host}</Text>
                        <Separator orientation="vertical"/>
                        <Text size="1" highContrast>{backend.ping}ms</Text>
                    </Flex>
                </Select.Item>
            ))}
        </Select.Content>
    </Select.Root>
);