import { Button } from "react-daisyui";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faMoon } from "@fortawesome/free-solid-svg-icons";

export default function ThemeButton(props) {
  return (
    <Button {...props} data-toggle-theme="dracula,cupcake" shape="square">
      <FontAwesomeIcon icon={faMoon} className="fa-xl" />
    </Button>
  )
}