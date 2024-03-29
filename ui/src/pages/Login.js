import { useEffect, useState } from "react";
import * as api from "../utils/api";
import { useNavigate } from "react-router-dom";
import { Button, Form, Hero, Input, InputGroup } from "react-daisyui";
import ThemeButton from "../components/ThemeButton";
import { themeChange } from "theme-change";

export default function Login(props) {
  const [input, setInput] = useState("");
  const navigate = useNavigate();

  const [buttonDisabled, setButtonDisabled] = useState(false);
  const [color, setColor] = useState("primary");
  const [buttonText, setButtonText] = useState("LOGIN");

  useEffect(() => {
    themeChange(false);
  }, []);

  function setButtonStage(stage) {
    switch (stage) {
      case "ready":
        setButtonDisabled(false);
        setColor("primary");
        setButtonText("LOGIN");
        break;
      case "processing":
        setButtonDisabled(true);
        setColor("primary");
        setButtonText("LOGIN");
        break;
      case "error":
        setButtonDisabled(false);
        setColor("error");
        setButtonText("ERROR");
        break;
      default:
        throw new Error("Invalid stage");
    }
  }

  async function onClick() {
    setButtonStage("processing");
    api.removeToken();
    const response = await api.auth(input);
    if (response.status === 200) {
      const result = await response.json();
      api.setAuth(result.token);
      api.setRole(result.role);
      navigate("/");
    } else {
      setButtonStage("error");
    }
  }

  function onInputChange(e) {
    setInput(e.target.value);
    if (buttonText === "ERROR") {
      setButtonStage("ready");
    }
  }

  async function onEnterPressed(e) {
    if (e.key === "Enter" && !buttonDisabled) {
      await onClick();
    }
  }

  return (
    <Hero className={props.className}>
      <Hero.Overlay className="bg-base-200">
        <div className="float-right p-10">
          <ThemeButton />
        </div>
      </Hero.Overlay>
      <Hero.Content className="text-center">
        <div className="max-w-md">
          <h1 className="text-5xl font-bold">Mita</h1>
          <p className="py-6">Variable-based cluster monitoring system.</p>
          <Form>
            <InputGroup>
              <Input
                type="password"
                placeholder="Password"
                bordered={true}
                value={input}
                color={color}
                onChange={onInputChange}
                onKeyDown={onEnterPressed}
              />
              <Button
                color={color}
                onClick={onClick}
                disabled={buttonDisabled}
                children={buttonText}
              />
            </InputGroup>
          </Form>
        </div>
      </Hero.Content>
    </Hero>
  );
}
