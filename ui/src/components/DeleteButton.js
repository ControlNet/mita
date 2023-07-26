import { Button } from "react-daisyui";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faTrash } from "@fortawesome/free-solid-svg-icons";
import { useSelector } from "react-redux";

export default function DeleteButton(props) {
  const role = useSelector((state) => state.role.value);
  switch (role) {
    case "admin":
      return (
        <Button {...props} shape="square">
          <FontAwesomeIcon icon={faTrash} className="fa-xl" />
        </Button>
      );
    case "guest":
    default:
      return <></>;
  }
}
