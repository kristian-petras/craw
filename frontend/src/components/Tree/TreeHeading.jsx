import {Flex, Heading, IconButton, Text} from "@radix-ui/themes";
import {TrashIcon} from '@radix-ui/react-icons';

const DeleteRecordButton = ({recordId, onDelete}) => {
    const handleDelete = async () => {
        const response = await fetch(`http://localhost:8080/${recordId}`, {
            method: 'DELETE',
        });

        if (!response.ok) {
            console.error('Failed to delete record');
            return;
        }

        if (onDelete) {
            onDelete(recordId);
        }
    };

    return (
        <IconButton size="4" onClick={handleDelete} aria-label="Delete record">
            <TrashIcon width="32" height="32"/>
        </IconButton>
    );
};

const TreeHeading = ({label, url, recordId, selected}) => {
    const handleRecordDelete = (id) => {
        console.log(`Record with id ${id} has been deleted.`);
        selected = null;
    };

    return (
        <Flex direction="row" className="TreeHeading" justify="between" align="center">
            <Flex direction="column">
                <Heading as="h1" size="8">{label}</Heading>
                <Text size="1">{recordId}</Text>
                <Text size="2">{url}</Text>
            </Flex>
            <Flex direction="row" align="center" justify="center" gap="3">
                <DeleteRecordButton recordId={recordId} onDelete={handleRecordDelete}/>
            </Flex>
        </Flex>
    );
};

export default TreeHeading;
