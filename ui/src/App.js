import React from "react";

import { BrowserRouter, Route, Routes } from "react-router-dom";
import Home from "./pages/Home";
import Login from "./pages/Login";
import MitaFooter from "./components/MitaFooter";
import NotFound from "./pages/NotFound";

export default function App() {
  return (
    <div className="flex flex-col min-h-screen">
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Home className="flex-auto" />} />
          <Route path="/login" element={<Login className="flex-auto" />} />
          <Route path="/*" element={<NotFound className="flex-auto" />} />
        </Routes>
      </BrowserRouter>
      <MitaFooter />
    </div>
  );
}
