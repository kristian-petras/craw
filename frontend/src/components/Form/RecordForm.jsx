import {Card, Flex, Button, TextArea, Text, Checkbox, TextField} from "@radix-ui/themes";
import * as Dialog from '@radix-ui/react-dialog';

const FormField = ({label, value, onChange, placeholder, component: Component = TextField.Root}) => (
    <Flex width="100%" direction="column" gap="1">
        <Text size="1">{label}</Text>
        <Component color={!value ? "red" : "gray"} size="3" placeholder={placeholder} value={value}
                   onChange={(e) => onChange(e.target.value)}/>
    </Flex>
);

const RecordForm = ({title, description, formData, handleChange, handleSubmit, isLoading, buttonText}) => {
    return (
        <Card className="DialogCard" size="3">
            <Flex gap="3" direction="column" align="start">
                <Flex direction="row" justify="between" width="100%">
                    <Dialog.Title className="DialogTitle">{title}</Dialog.Title>
                    <Dialog.Close>Close</Dialog.Close>
                </Flex>

                <Dialog.Description className="DialogDescription">
                    {description}
                </Dialog.Description>

                <FormField
                    label="Label"
                    value={formData.label}
                    placeholder="example"
                    onChange={(value) => handleChange('label', value)}
                />
                <FormField
                    label="URL"
                    value={formData.url}
                    placeholder="https://example.com"
                    onChange={(value) => handleChange('url', value)}
                />
                <FormField
                    label="Regexp"
                    value={formData.regexp}
                    placeholder="zaklady"
                    onChange={(value) => handleChange('regexp', value)}
                />
                <FormField
                    label="Periodicity"
                    value={formData.periodicity}
                    placeholder="PT10M"
                    onChange={(value) => handleChange('periodicity', value)}
                />
                <FormField
                    label="Tags"
                    value={formData.tags}
                    placeholder="tag1|tag2|tag3|…|tagN"
                    component={TextArea}
                    onChange={(value) => handleChange('tags', value)}
                />

                <Flex width="100%" direction="row" gap="3" align="center">
                    <Text size="3">Active</Text>
                    <Checkbox
                        checked={formData.isActive}
                        onCheckedChange={(checked) => handleChange('isActive', checked)}
                    />
                </Flex>

                <Flex width="100%" direction="row" justify="end">
                    <Dialog.Close asChild onClick={handleSubmit}>
                        <Button
                            disabled={
                                isLoading ||
                                !formData.label ||
                                !formData.url ||
                                !formData.regexp ||
                                !formData.periodicity ||
                                !formData.tags
                            }
                            style={{ marginTop: "8px", width: "120px" }}
                            size="3"
                        >
                            {buttonText}
                        </Button>
                    </Dialog.Close>
                </Flex>
            </Flex>
        </Card>
    );
};

export default RecordForm;
