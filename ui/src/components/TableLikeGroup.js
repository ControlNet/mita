import { Table } from "react-daisyui";
import { useEffect, useRef, useState } from "react";
import "../stores/updateIntervalSlice";
import { useSelector } from "react-redux";

export default function TableLikeGroup(props) {
  return (
    <Table
      className={props.className + " w-full rounded-corners"}
      compact={true}
      zebra={true}
    >
      <Table.Head>
        <span>Name</span>
        <span>Value</span>
      </Table.Head>

      <colgroup>
        <col className="w-64" />
        <col className="w-auto" />
      </colgroup>

      <Table.Body>
        {props.variableGroup.map((d) => (
          <VariableTableRow
            parent={props.parent}
            d={d}
            key={props.parent + "::" + d.name}
          />
        ))}
        {props.progressBarGroup.map((d) => (
          <ProgressBarTableRow
            parent={props.parent}
            d={d}
            key={props.parent + "::" + d.name}
          />
        ))}
      </Table.Body>
    </Table>
  );
}

function VariableTableRow(props) {
  return (
    <Table.Row key={props.parent + "::" + props.d.name} hover={true}>
      <span>{props.d.name}</span>
      <span>{props.d.value}</span>
    </Table.Row>
  );
}

function ProgressBarTableRow(props) {
  const [color, setColor] = useState("progress-warning");
  const value = useState(props.d.value)[0];

  const prevValue = useRef(value);
  const [timer, setTimer] = useState(0);
  const updateInterval = useSelector((state) => state.updateInterval.value);

  // for same time interval, change color based on value change
  useEffect(() => {
    const prev = prevValue.current;
    if (timer >= (2 * updateInterval) / 1000) {
      setTimer(0);
      if (value > prev) {
        setColor("progress-success");
      } else if (value < prev) {
        setColor("progress-error");
      } else {
        setColor("progress-warning");
      }
      prevValue.current = value;
    }
  }, [timer, value, updateInterval]);

  return (
    <Table.Row key={props.parent + "::" + props.d.name} hover={true}>
      <span>{props.d.name}</span>
      <span>
        <div className="flex flex-row items-center">
          <p className="basis-1/6">
            {value} / {props.d.total}
          </p>
          {/*The Progress in daisy-ui-react is buggy, use vanilla progress instead*/}
          <progress
            className={`progress ${color} basis-5/6 TableLikeGroup-progress`}
            value={value}
            max={props.d.total}
          ></progress>
        </div>
      </span>
    </Table.Row>
  );
}
