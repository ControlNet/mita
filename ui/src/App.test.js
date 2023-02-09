import React from "react";
import { createRoot } from "react-dom/client";
import { act } from "react-dom/test-utils";

import App from "./App";

globalThis.IS_REACT_ACT_ENVIRONMENT = true;

const render = () => {
  act(() => {
    const div = document.createElement("div");
    const root = createRoot(div);
    root.render(<App />);
  });
};

describe("App tests", () => {
  it("renders without crashing", () => {
    render();
  });
});
