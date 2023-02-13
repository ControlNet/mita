import { TOKEN_KEY } from "./global";

const _headers = {
  "Content-Type": "application/json",
};

_headers[TOKEN_KEY] = window.localStorage.getItem("token") || "";

export async function getViewList() {
  const response = await fetch(`/api/listViews`, {
    headers: _headers,
  });
  return await response.json();
}

export async function getView(name) {
  const response = await fetch(`/api/views/${name}`, {
    headers: _headers,
  });
  return await response.json();
}

export function auth(password) {
  return fetch("/api/auth", {
    method: "POST",
    headers: _headers,
    body: JSON.stringify({ password }),
  });
}

export function testAuth() {
  return fetch("/api/testAuth", {
    headers: _headers,
  });
}

export function setAuth(token) {
  _headers[TOKEN_KEY] = token;
  window.localStorage.setItem("token", token);
}
