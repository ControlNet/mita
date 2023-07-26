import { createSlice } from "@reduxjs/toolkit";

export const roleSlice = createSlice({
  name: "role",
  initialState: {
    value: undefined,
  },
  reducers: {
    setRole: (state, action) => {
      state.value = action.payload;
    },
  },
});

export const { setRole } = roleSlice.actions;
export default roleSlice.reducer;
