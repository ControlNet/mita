import { createSlice } from "@reduxjs/toolkit";
import { INITIAL_UPDATE_INTERVAL } from "../utils/global";

export const updateIntervalSlice = createSlice({
  name: "updateInterval",
  initialState: {
    value: INITIAL_UPDATE_INTERVAL,
  },
  reducers: {
    set: (state, action) => {
      state.value = action.payload;
    },
  },
});

export const { set: setUpdateInterval } = updateIntervalSlice.actions;

export default updateIntervalSlice.reducer;
