let _password =
  window.localStorage.getItem("password") ||
  process.env.REACT_APP_PASSWORD ||
  "";

export async function getViewList() {
  const response = await fetch(`/api/listViews?pw=${_password}`);
  return await response.json();
}

export async function getView(name) {
  const response = await fetch(`/api/views/${name}?pw=${_password}`);
  return await response.json();
}

export async function auth() {
  const response = await fetch(`/api/auth?pw=${_password}`);
  return response.status === 200;
}

export function setAuth(password) {
  _password = password;
  window.localStorage.setItem("password", password);
}
