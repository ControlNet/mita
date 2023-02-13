import { Footer } from "react-daisyui";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faGithub } from "@fortawesome/free-brands-svg-icons";
import { GITHUB_URL } from "../utils/global";

export default function MitaFooter() {
  return (
    <Footer className="p-4 bg-base-300 text-base-content" center={true}>
      <div className="flex w-full">
        <div className="w-1" />
        <p className="h-5"></p>
        <div className="grow" />
        <a href={GITHUB_URL}>
          <p className="align-middle">GitHub</p>
        </a>
        <a href={GITHUB_URL}>
          <FontAwesomeIcon icon={faGithub} className="h-5 align-middle" />
        </a>
        <div className="w-1" />
      </div>
    </Footer>
  );
}
