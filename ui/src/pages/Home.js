import Mita from "../components/Mita";
import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import * as api from "../utils/api";

export default function Home(props) {
  const navigate = useNavigate();

  useEffect(() => {
    api.testAuth().then(response => {
      if (response.status !== 200) {
        navigate("/login");
      }
    });
  });
  return <Mita className={props.className} />;
}
