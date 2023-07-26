import { Card } from "react-daisyui";

export default function LoggerGroup(props) {
  return (
    <div className={props.className}>
      {props.data.map((d) => (
        <Card key={props.parent + "::" + d.name} className="bg-base-200">
          <Card.Body>
            <Card.Title tag="h2">{d.name}</Card.Title>
            {d.value.map((v, j) => (
              <p key={props.parent + "::" + d.name + "::" + j}>{v}</p>
            ))}
          </Card.Body>
        </Card>
      ))}
    </div>
  );
}
