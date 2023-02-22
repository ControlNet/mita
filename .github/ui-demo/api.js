let _password = 0
const PASSWORD = "123"

export async function getViewList() {
  return data.views;
}

export async function getView(name) {
  return data[name];
}

export async function auth(password) {
  return {
    status: password === PASSWORD ? 200 : 401,
    json: async () => {
      return {
        token: password
      }
    }
  }
}

export async function testAuth() {
  return {
    status: _password === PASSWORD ? 200 : 401
  }
}

export function setAuth(token) {
  _password = token
}

export function removeToken() {
}

export function deleteView(viewName) {
  delete data[viewName]
  data.views = data.views.filter(v => v !== viewName)
}

export function deleteAll() {
  for (const view of data.views) {
    deleteView(view)
  }
}

export function deleteComponent(viewName, componentName) {
  data[viewName] = data[viewName].filter(v => v.name !== componentName)
}

// mock data
const data = {
  "views": ["view1", "view2"],
  "view1": [
    {"cls": "Image", "name": "image", "value": ""},
    {
      "cls": "LineChart",
      "name": "Line Chart",
      "value": [
        {"x": 0, "y": 0, "label": "0"},
        {"x": 1, "y": 1, "label": "1"},
        {"x": 2, "y": 2, "label": "2"},
        {"x": 3, "y": 3, "label": "3"},
        {"x": 4, "y": 4, "label": "4"},
        {"x": 5, "y": 5, "label": "5"},
        {"x": 6, "y": 6, "label": "6"},
        {"x": 7, "y": 7, "label": "7"},
        {"x": 8, "y": 8, "label": "8"},
        {"x": 9, "y": 9, "label": "9"},
        {"x": 10, "y": 10, "label": "10"}
      ],
      "x_label": "x",
      "y_label": "y"
    },
    {"cls": "Variable", "name": "string", "value": ""},
    {"cls": "Variable", "name": "double", "value": 0},
    {"cls": "Logger", "name": "logger", "value": ["Hello", "World"]},
    {"cls": "ProgressBar", "name": "Progress Bar", "value": 0, "total": 100},
    {"cls": "Variable", "name": "int", "value": 0}
  ],
  "view2": [
    {"cls": "ProgressBar", "name": "process", "value": 5, "total": 20},
    {"cls": "Variable", "name": "x", "value": 35}
  ]
}

// https://stackoverflow.com/questions/1349404/generate-random-string-characters-in-javascript
function randomString(length) {
    let result = '';
    const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    const charactersLength = characters.length;
    let counter = 0;
    while (counter < length) {
      result += characters.charAt(Math.floor(Math.random() * charactersLength));
      counter += 1;
    }
    return result;
}

setInterval(() => {
  data.view1[2].value = randomString(16)
}, 1000)


setInterval(() => {
  data.view1[3].value = Math.random()
}, 1750)

setInterval(() => {
  data.view1[6].value += Math.floor(Math.random() * 10)
}, 730)

setInterval(() => {
  const random = Math.random()
  if (random < 0.5) {
    data.view1[5].value += 1
  } else if (random < 0.9 && data.view1[5].value > 0) {
    data.view1[5].value -= 1
  }
}, 1000)

setInterval(() => {
  data.view1[4].value.push(randomString(16))
}, 5000)
