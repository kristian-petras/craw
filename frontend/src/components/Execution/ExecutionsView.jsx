import {Button, Flex, Text} from "@radix-ui/themes";
import {useState} from 'react';

const ExecutionItem = ({execution}) => (
    <Flex key={execution.executionId} className="ExecutionItem" direction="column">
        <Text size="2" weight="bold">{execution.type}</Text>
        <Text size="2">ID: {execution.executionId}</Text>
        <Text size="2">Start: {new Date(execution.start).toLocaleString()}</Text>
        <Text size="2">End: {new Date(execution.end).toLocaleString()}</Text>
        <Text size="2">Count: {execution.crawledCount}</Text>
    </Flex>
);

const Pagination = ({currentPage, totalPages, paginate}) => {
    const renderPageNumbers = () => {
        const pageNumbers = [];
        const maxPageDisplay = 5; // Max number of page buttons to display

        let startPage = Math.max(1, currentPage - Math.floor(maxPageDisplay / 2));
        let endPage = Math.min(totalPages, startPage + maxPageDisplay - 1);

        // Adjust if there are not enough pages to fill maxPageDisplay
        if (endPage - startPage + 1 < maxPageDisplay) {
            startPage = Math.max(1, endPage - maxPageDisplay + 1);
        }

        // Add first page and ellipsis if needed
        if (startPage > 1) {
            pageNumbers.push(1);
            if (startPage > 2) {
                pageNumbers.push("...");
            }
        }

        // Add visible page buttons
        for (let i = startPage; i <= endPage; i++) {
            pageNumbers.push(i);
        }

        // Add last page and ellipsis if needed
        if (endPage < totalPages) {
            if (endPage < totalPages - 1) {
                pageNumbers.push("...");
            }
            pageNumbers.push(totalPages);
        }

        return pageNumbers;
    };

    return (
        <Flex justify="center" gap="2">
            {/* Previous Button */}
            <Button
                onClick={() => paginate(currentPage - 1)}
                disabled={currentPage === 1}
            >
                Previous
            </Button>

            {/* Page Numbers */}
            {renderPageNumbers().map((pageNumber, index) =>
                typeof pageNumber === "number" ? (
                    <Button
                        key={index}
                        onClick={() => paginate(pageNumber)}
                        variant={currentPage === pageNumber ? 'solid' : 'outline'}
                    >
                        {pageNumber}
                    </Button>
                ) : (
                    <Text key={index} size="2">...</Text>
                )
            )}

            {/* Next Button */}
            <Button
                onClick={() => paginate(currentPage + 1)}
                disabled={currentPage === totalPages}
            >
                Next
            </Button>
        </Flex>
    );
};

// Main ExecutionsView Component
const ExecutionsView = ({executions}) => {
    const [currentPage, setCurrentPage] = useState(1);
    const executionsPerPage = 5; // Number of executions per page
    const totalPages = Math.ceil(executions.length / executionsPerPage);

    const indexOfLastExecution = currentPage * executionsPerPage;
    const indexOfFirstExecution = indexOfLastExecution - executionsPerPage;
    const currentExecutions = executions.slice(indexOfFirstExecution, indexOfLastExecution);

    const paginate = (pageNumber) => setCurrentPage(pageNumber);


    return (
        <Flex direction="column" gapY="4">
            {executions.length === 0 ? (
                <Text size="2">No executions available</Text>
            ) : (
                <>
                    {currentExecutions.map(execution => (
                        <ExecutionItem key={execution.executionId} execution={execution}/>
                    ))}

                    {/* Pagination controls */}
                    <Pagination
                        currentPage={currentPage}
                        totalPages={totalPages}
                        paginate={paginate}
                    />
                </>
            )}
        </Flex>
    );
};

export default ExecutionsView;
