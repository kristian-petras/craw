import {useEffect, useState} from 'react';

const useDeleteRecord = (source) => {
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);

    const deleteRecord = async (recordId, onDelete) => {
        setIsLoading(true);
        setError(null);

        try {
            const response = await fetch(`${source}/${recordId}`, {
                method: 'DELETE',
            });

            if (!response.ok) {
                throw new Error('Failed to delete record');
            }

            if (onDelete) {
                onDelete(recordId); // Notify the parent component
            }
        } catch (err) {
            setError(err.message);
            console.error(err.message);
        } finally {
            setIsLoading(false);
        }
    };

    return {deleteRecord, isLoading, error};
};

export default useDeleteRecord;