import {useEffect, useState} from 'react';

const useGraphStream = (source, setSelected) => {
    const [graph, setGraph] = useState();
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        let stream;
        let retryTimeout;

        const connectToStream = () => {
            stream = new EventSource(source);

            stream.onopen = (event) => {
                console.log(event);
            }

            stream.onmessage = (event) => {
                console.log(JSON.parse(event.data));
                setGraph(JSON.parse(event.data));
                setLoading(false);
            };

            stream.onerror = (err) => {
                setSelected(null);
                console.error('Stream error:', err);
                stream.close();
                setLoading(true);

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

    return {graph, loading};
};

export default useGraphStream;
