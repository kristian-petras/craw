import {useEffect, useState} from 'react';

const useGraphStream = (source, setSelected, backendLoading, setBackendLoading) => {
    const [graph, setGraph] = useState();

    useEffect(() => {
        let stream;
        let retryTimeout;

        const connectToStream = () => {
            stream = new EventSource(source);

            stream.onmessage = (event) => {
                setGraph(JSON.parse(event.data));
                console.log(graph)
                setBackendLoading(false);
            };

            stream.onerror = (err) => {
                setSelected(null);
                stream.close();
                setBackendLoading(true);
                retryTimeout = setTimeout(connectToStream, 5000);
            };
        };

        connectToStream();

        return () => {
            if (stream) {
                stream.close();
            }
            clearTimeout(retryTimeout);
        };
    }, [source]);

    return {graph, loading: backendLoading};
};

export default useGraphStream;
