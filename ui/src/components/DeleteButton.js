import { Button } from "react-daisyui";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faTrash } from "@fortawesome/free-solid-svg-icons";

export default function DeleteButton(props) {
  return (
    <Button {...props} shape="square">
      <FontAwesomeIcon icon={faTrash} className="fa-xl" />
    </Button>
  );
}
