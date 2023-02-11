import { Hero } from "react-daisyui";

export default function NotFound(props) {
  return (
    <Hero {...props}>
      <Hero.Content className="text-center">
        <div className="max-w-md">
          <h1 className="text-5xl font-bold">404 Not Found</h1>
          <p className="py-6">The page you are looking for does not exist.</p>
        </div>
      </Hero.Content>
    </Hero>
  );
}
