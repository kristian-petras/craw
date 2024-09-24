import {Box, Card, Flex, Text, Theme} from "@radix-ui/themes";
import SidebarScrollArea from "./SidebarScrollArea.jsx";
import '../../styles/Sidebar.css';
import {SidebarTitle} from "./SidebarItem.jsx";
import * as Dialog from '@radix-ui/react-dialog';
import useRecordForm from "../../hooks/useRecordForm.js";
import config from "../../config.js";
import RecordForm from "../Form/RecordForm.jsx";
import ExecutionsView from "../Execution/ExecutionsView.jsx";


const AddRecordPopup = () => {
    const source = config.backendHost + "/record"
    const {formData, isLoading, buttonText, handleChange, handleSubmit} = useRecordForm({}, source, "POST");

    return <RecordForm
        title="New Record"
        description="New record to be crawled."
        formData={formData}
        handleChange={handleChange}
        handleSubmit={handleSubmit}
        isLoading={isLoading}
        buttonText={buttonText}
    />;
};


const AddRecord = () => (
    <Dialog.Root>
        <Dialog.Trigger asChild>
            <Box className="SidebarItem">
                <Text size="2" highContrast className="AddRecordButton">Add Record</Text>
            </Box>
        </Dialog.Trigger>

        <Dialog.Portal>
            <Dialog.Overlay className="DialogOverlay"/>
            <Dialog.Content className="DialogContent">
                <Theme accentColor="teal" appearance="dark" radius="large">
                    <AddRecordPopup/>
                </Theme>
            </Dialog.Content>
        </Dialog.Portal>
    </Dialog.Root>
);

const AllExecutionsPopup = ({graph}) => {
    if (!graph) {
        return (
            <Card className="DialogCard" size="3">
                <Text>No Executions Yet</Text>
            </Card>);
    }

    const executions = graph.nodes.map((node) => node.execution).flat()

    return (
        <Card className="DialogCard" size="3">
            <Dialog.Title className="DialogTitle">Executions</Dialog.Title>
            <Dialog.Description>All Executions</Dialog.Description>
            <ExecutionsView full={true} executions={executions}/>
        </Card>
    )
}

const AllExecutions = ({graph}) => (
    <Dialog.Root>
        <Dialog.Trigger asChild>
            <Box className="SidebarItem">
                <Text size="2" highContrast className="AddRecordButton">All Executions</Text>
            </Box>
        </Dialog.Trigger>

        <Dialog.Portal>
            <Dialog.Overlay className="DialogOverlay"/>
            <Dialog.Content className="DialogContent">
                <Theme accentColor="teal" appearance="dark" radius="large">
                    <AllExecutionsPopup graph={graph}/>
                </Theme>
            </Dialog.Content>
        </Dialog.Portal>
    </Dialog.Root>
)

export const Sidebar = ({children, graph}) => {
    return (
        <Box className="Sidebar">
            <SidebarScrollArea>
                <SidebarTitle>CRAWLER</SidebarTitle>
                <Flex className="SidebarItems" direction="column">
                    <AddRecord/>
                    <AllExecutions graph={graph}/>
                </Flex>

                <SidebarTitle>RECORDS</SidebarTitle>
                <Flex className="SidebarItems" direction="column">
                    {children}
                </Flex>
            </SidebarScrollArea>
        </Box>
    );
};
