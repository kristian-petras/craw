import {useEffect, useState} from "react";

const usePingBackends = (initialBackends) => {
    const [availableBackends, setAvailableBackends] = useState(initialBackends);

    useEffect(() => {
        const interval = setInterval(() => {
            setAvailableBackends(backends =>
                backends.map(backend => ({
                    ...backend,
                    ping: Math.floor(Math.random() * 30) + 1,
                }))
            );
        }, 1000);

        return () => clearInterval(interval); // Cleanup interval on component unmount
    }, []);

    return availableBackends;
};

export default usePingBackends;