import { Hero } from "react-daisyui";
import ThemeButton from "../components/ThemeButton";
import { useEffect } from "react";
import { themeChange } from "theme-change";

export default function NotFound(props) {
  useEffect(() => {
    themeChange(false);
  }, []);

  return (
    <Hero {...props}>
      <Hero.Overlay className="bg-base-200">
        <div className="float-right p-10">
          <ThemeButton />
        </div>
      </Hero.Overlay>
      <Hero.Content className="text-center">
        <div className="max-w-md">
          <h1 className="text-5xl font-bold">404 Not Found</h1>
          <p className="py-6">The page you are looking for does not exist.</p>
        </div>
      </Hero.Content>
    </Hero>
  );
}
