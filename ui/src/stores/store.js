import { configureStore } from "@reduxjs/toolkit";
import updateIntervalReducer from "./updateIntervalSlice";

export default configureStore({
  reducer: {
    updateInterval: updateIntervalReducer,
  },
});
