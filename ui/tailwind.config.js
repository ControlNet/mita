module.exports = {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
    "node_modules/daisyui/dist/**/*.js",
    "node_modules/react-daisyui/dist/**/*.js",
  ],
  plugins: [require("daisyui")],
  daisyui: {
    themes: ["cupcake", "dracula"],
  },
};
