import { useEffect, useMemo, useState } from "react";
import { deleteView, getView } from "../utils/api";
import _ from "lodash";
import TableLikeGroup from "./TableLikeGroup";
import LoggerGroup from "./LoggerGroup";
import { useSelector } from "react-redux";
import DeleteButton from "./DeleteButton";

export default function View(props) {
  const [groups, setGroups] = useState({});
  const interval = useSelector((state) => state.updateInterval.value);

  useEffect(() => {
    getView(props.viewName)
      .then((view) => _.groupBy(view, (d) => d.cls))
      .then(setGroups);
  }, [props.viewName]);

  useEffect(() => {
    const intervalTask = setInterval(() => {
      getView(props.viewName)
        .then((view) => _.groupBy(view, (d) => d.cls))
        .then(setGroups);
    }, interval);
    return () => clearInterval(intervalTask);
  }, [interval, props.viewName]);

  const variableGroup = useMemo(
    () => ("Variable" in groups ? groups["Variable"] : []),
    [groups]
  );
  const progressBarGroup = useMemo(
    () => ("ProgressBar" in groups ? groups["ProgressBar"] : []),
    [groups]
  );
  const needRenderTableLikeGroup = useMemo(
    () => variableGroup.length > 0 || progressBarGroup.length > 0,
    [variableGroup, progressBarGroup]
  );

  const needRenderLogGroup = useMemo(() => "Logger" in groups, [groups]);

  async function onClickDelete() {
    await deleteView(props.viewName);
  }

  return (
    <div className="m-2">
      <div className="flex flex-row my-2">
        <h1 className="text-2xl font-bold mb-5">{props.viewName}</h1>
        <div className="flex-grow" />
        <DeleteButton onClick={onClickDelete} />
      </div>

      {needRenderTableLikeGroup ? (
        <TableLikeGroup
          variableGroup={variableGroup}
          progressBarGroup={progressBarGroup}
          key={`${props.viewName}::TableLikeGroup`}
          parent={`${props.viewName}::TableLikeGroup`}
        />
      ) : undefined}

      {needRenderLogGroup ? (
        <LoggerGroup
          data={groups["Logger"]}
          parent={`${props.viewName}::Logger`}
          className="py-2"
        />
      ) : undefined}

      {/*{TODO: Image and LineChart}*/}
    </div>
  );
}
