import {useState} from 'react';

const useRecordForm = (initialValues, source, method, recordId = null) => {
    const [formData, setFormData] = useState({
        label: initialValues.label || '',
        url: initialValues.url || '',
        regexp: initialValues.regexp || '',
        periodicity: initialValues.periodicity || '',
        tags: initialValues.tags || '',
        isActive: initialValues.isActive || true,
    });

    const [buttonText, setButtonText] = useState(method === "POST" ? "Create" : "Edit");
    const [isLoading, setIsLoading] = useState(false);

    const handleChange = (name, value) => {
        setFormData({
            ...formData,
            [name]: value,
        });
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        setIsLoading(true);
        setButtonText("Loading...");

        const recordData = {
            ...formData,
            tags: formData.tags.split('|'),
            active: formData.isActive,
        };

        // Add recordId if method is PUT
        if (method === "PUT" && recordId) {
            recordData.recordId = recordId;
        }

        try {
            const response = await fetch(source, {
                method: method,
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(recordData),
            });

            if (response.ok) {
                setButtonText("Success");
                setTimeout(() => setIsLoading(false), 1000);

            } else {
                setButtonText("Error");
                setIsLoading(false);
            }
        } catch (error) {
            setButtonText("Error");
            setIsLoading(false);
        }
    };

    return {
        formData,
        isLoading,
        buttonText,
        handleChange,
        handleSubmit,
    };
};

export default useRecordForm;
