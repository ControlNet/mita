import { TOKEN_KEY } from "./global";
import store from "../stores/store";
import { setRole as setRoleAction } from "../stores/roleSlice";

const headers = {
  "Content-Type": "application/json",
};

headers[TOKEN_KEY] = window.localStorage.getItem("token") || "";

const role = window.localStorage.getItem("role") || "";
if (role === "") {
  headers[TOKEN_KEY] = "";
  window.localStorage.removeItem("token");
} else {
  store.dispatch(setRoleAction(role));
}

export async function getViewList() {
  const response = await fetch(`/api/listViews`, { headers });
  return await response.json();
}

export async function getView(name) {
  const response = await fetch(`/api/views/${name}`, { headers });
  return await response.json();
}

export function auth(password) {
  return fetch("/api/auth", {
    method: "POST",
    headers: headers,
    body: JSON.stringify({ password }),
  });
}

export function testAuth() {
  return fetch("/api/testAuth", {
    headers: headers,
  });
}

export function setAuth(token) {
  headers[TOKEN_KEY] = token;
  window.localStorage.setItem("token", token);
}

export function setRole(role) {
  window.localStorage.setItem("role", role);
  store.dispatch(setRoleAction(role));
}

export function removeToken() {
  delete headers[TOKEN_KEY];
  window.localStorage.removeItem("token");
}

export function deleteView(viewName) {
  return fetch(`/api/delete/${viewName}`, {
    method: "DELETE",
    headers: headers,
  });
}

export function deleteAll() {
  return fetch("/api/delete", {
    method: "DELETE",
    headers: headers,
  });
}

export function deleteComponent(viewName, componentName) {
  return fetch(`/api/delete/${viewName}/${componentName}`, {
    method: "DELETE",
    headers: headers,
  });
}
