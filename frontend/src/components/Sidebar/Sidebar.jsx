import {Box, Card, Flex, Text, Theme, TextField, Button, Checkbox, TextArea} from "@radix-ui/themes";
import SidebarScrollArea from "./SidebarScrollArea.jsx";
import '../../styles/Sidebar.css';
import {SidebarTitle} from "./SidebarItem.jsx";
import * as Dialog from '@radix-ui/react-dialog';
import {useState} from "react";
import backends from "../../dummy/backends.js";
import config from "../../config.js";


const FormField = ({label, value, onChange, placeholder, component: Component = TextField.Root}) => (
    <Flex width="100%" direction="column" gap="1">
        <Text size="1">{label}</Text>
        <Component color={!value ? "red" : "gray"} size="3" placeholder={placeholder} value={value}
                   onChange={(e) => onChange(e.target.value)}/>
    </Flex>
);

const AddRecordDialog = () => {
    const [label, setLabel] = useState();
    const [url, setUrl] = useState();
    const [regexp, setRegexp] = useState();
    const [periodicity, setPeriodicity] = useState();
    const [tags, setTags] = useState();
    const [isActive, setIsActive] = useState(true);

    const [buttonText, setButtonText] = useState("Create");
    const [isLoading, setIsLoading] = useState(false);

    const handleSubmit = async (event) => {
        const recordData = {label, url, regexp, periodicity, tags: tags.split('|'), active: isActive};
        setIsLoading(true);
        setButtonText("Loading...");
        console.log(recordData);
        event.preventDefault();

        try {
            const response = await fetch(config.backendHost + "/record", {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(recordData)
            });

            if (response.ok) {
                setButtonText("Success");
                setTimeout(() => setIsLoading(false), 1000); // Wait a second before closing
                setIsLoading(false);
            } else {
                setButtonText("Error");
                setIsLoading(false);
            }
        } catch (error) {
            setButtonText("Error");
            setIsLoading(false);
        }
    };

    return (
        <Card className="DialogCard" size="3">
            <Flex gap="3" direction="column" align="start">
                <Flex direction="row" justify="between" width="100%">
                    <Dialog.Title className="DialogTitle">New Record</Dialog.Title>
                    <Dialog.Close>Close</Dialog.Close>
                </Flex>

                <Dialog.Description className="DialogDescription">New record to be crawled.</Dialog.Description>

                <FormField label="label" value={label} onChange={setLabel} placeholder="example"/>
                <FormField label="url" value={url} onChange={setUrl} placeholder="https://example.com"/>
                <FormField label="regexp" value={regexp} onChange={setRegexp} placeholder="zaklady"/>
                <FormField label="periodicity" value={periodicity} onChange={setPeriodicity} placeholder="PT10M"/>
                <FormField label="tags" value={tags} onChange={setTags} placeholder="tag1|tag2|tag3|…|tagN"
                           component={TextArea}/>

                <Flex width="100%" direction="row" gap="3" align="center">
                    <Text size="3">active</Text>
                    <Checkbox defaultChecked checked={isActive} onCheckedChange={(checked) => setIsActive(checked)}/>
                </Flex>

                <Flex width="100%" direction="row" justify="end">
                    <Dialog.Close asChild onClick={handleSubmit}>
                        <Button disabled={isLoading || !label || !url || !regexp || !periodicity || !tags}
                                style={{marginTop: '8px', width: '120px'}}
                                size="3">
                            {buttonText}
                        </Button>
                    </Dialog.Close>
                </Flex>
            </Flex>
        </Card>
    );
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
                    <AddRecordDialog/>
                </Theme>
            </Dialog.Content>
        </Dialog.Portal>
    </Dialog.Root>
);

const Sidebar = ({children}) => {
    return (
        <Box className="Sidebar">
            <SidebarScrollArea>
                <SidebarTitle>CRAWLER</SidebarTitle>
                <Flex className="SidebarItems" direction="column">
                    <AddRecord/>
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
