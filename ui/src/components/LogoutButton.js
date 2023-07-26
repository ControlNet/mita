import { Button } from "react-daisyui";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faRightFromBracket } from "@fortawesome/free-solid-svg-icons";

export default function LogoutButton(props) {
  return (
    <Button {...props} shape="square">
      <FontAwesomeIcon icon={faRightFromBracket} className="fa-xl" />
    </Button>
  );
}
