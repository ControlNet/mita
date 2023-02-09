const PASSWORD = process.env.REACT_APP_PASSWORD;

export async function getViewList() {
  const response = await fetch("/api/listViews?pw=" + PASSWORD);
  return await response.json();
}

export async function getView(name) {
  const response = await fetch(`/api/views/${name}?pw=${PASSWORD}`);
  return await response.json();
}
