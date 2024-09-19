import {Box, Button, Card, Checkbox, Flex, Heading, IconButton, Text, TextArea, Theme} from "@radix-ui/themes";
import { TrashIcon, Pencil1Icon } from '@radix-ui/react-icons';
import useDeleteRecord from "../../hooks/useDeleteRecord.js";
import useRecordForm from "../../hooks/useRecordForm.js";
import config from "../../config.js";
import * as Dialog from "@radix-ui/react-dialog";
import RecordForm from "../Form/RecordForm.jsx";

const DeleteRecordButton = ({ recordId, onDelete }) => {
    const { deleteRecord, isLoading, error } = useDeleteRecord(config.backendHost);

    const handleDelete = () => {
        deleteRecord(recordId, onDelete);
    };

    return (
        <>
            <IconButton size="4" onClick={handleDelete} disabled={isLoading} aria-label="Delete record">
                <TrashIcon width="32" height="32" />
            </IconButton>
            {error && <Text size="2" color="red">{error}</Text>}
        </>
    );
};

const EditRecordPopup = ({record}) => {
    const source = config.backendHost + "/record"
    const initValues = {
        label: record.label,
        url: record.url,
        regexp: record.regexp,
        periodicity: record.periodicity,
        tags: record.tags,
        isActive: record.isActive,
    }
    const {formData, isLoading, buttonText, handleChange, handleSubmit} = useRecordForm(initValues, source, "PUT", record.recordId);

    return <RecordForm
        title="Edit Record"
        description="Edit record."
        formData={formData}
        handleChange={handleChange}
        handleSubmit={handleSubmit}
        isLoading={isLoading}
        buttonText={buttonText}
    />;
};

const EditRecord = ({record}) => (
    <Dialog.Root>
        <Dialog.Trigger asChild>
            <IconButton size="4">
                <Pencil1Icon width="32" height="32"/>
            </IconButton>
        </Dialog.Trigger>

        <Dialog.Portal>
            <Dialog.Overlay className="DialogOverlay"/>
            <Dialog.Content className="DialogContent">
                <Theme accentColor="teal" appearance="dark" radius="large">
                    <EditRecordPopup record={record}/>
                </Theme>
            </Dialog.Content>
        </Dialog.Portal>
    </Dialog.Root>
);

const TreeHeading = ({ label, url, record, selected }) => {
    const handleRecordDelete = () => {
        selected = null
    };

    return (
        <Flex direction="row" className="TreeHeading" justify="between" align="center">
            <Flex direction="column">
                <Heading as="h1" size="8">{label}</Heading>
                <Text size="1">{record.recordId}</Text>
                <Text size="2">{url}</Text>
            </Flex>
            <Flex direction="row" align="center" justify="center" gap="3">
                <EditRecord record={record} />
                <DeleteRecordButton recordId={record.recordId} onDelete={handleRecordDelete} />
            </Flex>
        </Flex>
    );
};

export default TreeHeading;
