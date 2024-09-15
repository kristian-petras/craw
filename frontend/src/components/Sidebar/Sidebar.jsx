import {Box, Card, Flex, Text} from "@radix-ui/themes";
import SidebarScrollArea from "./SidebarScrollArea.jsx";
import '../../styles/Sidebar.css';
import {SidebarTitle} from "./SidebarItem.jsx";
import * as Dialog from '@radix-ui/react-dialog';
import {useState} from "react";

const AddRecordDialog = () => {
    const [isOpen, setIsOpen] = useState(false);

    return (
        <Dialog.Root open={isOpen} onOpenChange={setIsOpen}>
            <Dialog.Trigger asChild>
                <Text
                    size="2"
                    highContrast
                    className="AddRecordButton"
                    onClick={() => setIsOpen(true)}
                >
                    Add Record
                </Text>
            </Dialog.Trigger>

            <Dialog.Portal>
                <Dialog.Overlay className="DialogOverlay"/>
                <Dialog.Content className="DialogContent">
                    <Card>
                        <Text>Hello</Text>
                    </Card>
                </Dialog.Content>
            </Dialog.Portal>
        </Dialog.Root>
    );
};

const Sidebar = ({children}) => {
    return (
        <Box className="Sidebar">
            <SidebarScrollArea>
                <SidebarTitle>CRAWLER</SidebarTitle>
                <Flex className="SidebarItems" direction="column">
                    <Box className="SidebarItem">
                        <AddRecordDialog/>
                    </Box>
                </Flex>

                <SidebarTitle>RECORDS</SidebarTitle>
                <Flex className="SidebarItems" direction="column">
                    {children}
                </Flex>
            </SidebarScrollArea>
        </Box>
    );
};


export default Sidebar;
