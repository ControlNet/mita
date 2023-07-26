import { configureStore } from "@reduxjs/toolkit";
import updateIntervalReducer from "./updateIntervalSlice";
import roleReducer from "./roleSlice";

export default configureStore({
  reducer: {
    updateInterval: updateIntervalReducer,
    role: roleReducer,
  },
});
