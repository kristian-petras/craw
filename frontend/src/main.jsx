import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App.jsx";
import "@radix-ui/themes/styles.css";
import { Theme } from "@radix-ui/themes";

ReactDOM.createRoot(document.getElementById("root")).render(
  <Theme accentColor="teal" appearance="dark" radius="large">
    <App />
  </Theme>,
);
