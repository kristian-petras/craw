import { Flex } from "@radix-ui/themes";

const Body = ({ children }) => (
  <Flex className="Body" direction="row">
    {children}
  </Flex>
);

export default Body;
