import { useEffect, useState } from "react";

const useGraphStream = (
  source,
  setSelected,
  backendLoading,
  setBackendLoading,
) => {
  const [graph, setGraph] = useState();
  const [backgroundGraph, setBackgroundGraph] = useState();
  const [cached, setCached] = useState(false);

  useEffect(() => {
    let stream;
    let retryTimeout;

    const connectToStream = () => {
      stream = new EventSource(source);

      stream.onmessage = (event) => {
        const newGraph = JSON.parse(event.data);
        setBackgroundGraph(newGraph); // Always update the background graph with new data

        // Only update the visible graph if caching is off
        if (!cached) {
          setGraph(newGraph);
        }

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
  }, [source, cached]); // Add `cached` as a dependency to properly control updates

  return {
    graph,
    backgroundGraph,
    setGraph,
    loading: backendLoading,
    cached,
    setCached,
  };
};

export default useGraphStream;
