import { useCallback, useEffect, useState } from "react";
import { deleteAll, getViewList } from "../utils/api";
import View from "./View";
import { Button, Divider, Modal, Range } from "react-daisyui";
import { setUpdateInterval } from "../stores/updateIntervalSlice";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import ThemeButton from "./ThemeButton";
import DeleteButton from "./DeleteButton";
import LogoutButton from "./LogoutButton";

export default function Mita(props) {
  const [viewNames, setViewNames] = useState([]);
  const [modalVisible, setModalVisible] = useState(false);
  const dispatch = useDispatch();
  const interval = useSelector((state) => state.updateInterval.value);
  const navigate = useNavigate();

  const rangeLevelToInterval = (level) => {
    switch (level) {
      case 0:
        return 200;
      case 10:
        return 500;
      case 20:
        return 1000;
      case 30:
        return 2000;
      case 40:
        return 3000;
      case 50:
        return 5000;
      case 60:
        return 10000;
      case 70:
        return 30000;
      case 80:
        return 60000;
      case 90:
        return 300000;
      case 100:
        return 600000;
      default:
        return 5000;
    }
  };

  const fetchViewList = useCallback(() => {
    getViewList()
      .then(setViewNames)
      .catch(() => {
        navigate("/login");
      });
  }, [navigate]);

  useEffect(() => {
    fetchViewList();
  }, [fetchViewList]);

  useEffect(() => {
    const intervalTask = setInterval(() => {
      fetchViewList();
    }, interval);
    return () => clearInterval(intervalTask);
  }, [interval, fetchViewList]);

  function onIntervalChange(e) {
    dispatch(setUpdateInterval(rangeLevelToInterval(Number(e.target.value))));
  }

  async function onClickDeleteYes() {
    await deleteAll();
    setModalVisible(false);
  }

  function onClickDeleteNo() {
    setModalVisible(false);
  }

  function toggleVisible() {
    setModalVisible(!modalVisible);
  }

  function onClickLogout() {
    navigate("/login");
  }

  return (
    <div className={props.className + " flex flex-col p-10"}>
      <div className="flex flex-row">
        <h1 className="flex-none text-5xl font-bold mb-5 ml-1">Mita</h1>
        <div className="grow" />
        <p className="text-lg mr-2 w-64">Update interval: {interval} ms</p>
        <div className="w-64">
          <Range step={10} onChange={onIntervalChange} />
        </div>
        <div className="pl-4">
          <ThemeButton className="mr-2" />
          <DeleteButton className="mr-2" onClick={toggleVisible} />
          <LogoutButton className="mr-2" onClick={onClickLogout} />
          <Modal open={modalVisible}>
            <Modal.Header className="font-bold">
              Confirm to delete all views?
            </Modal.Header>

            <Modal.Actions>
              <Button onClick={onClickDeleteYes} color="error">
                Yes
              </Button>
              <Button onClick={onClickDeleteNo}>No</Button>
            </Modal.Actions>
          </Modal>
        </div>
      </div>

      {viewNames.map((name) => (
        <div key={`${name}-div`}>
          <Divider key={`${name}-key`} />
          <View viewName={name} key={name} />
        </div>
      ))}
    </div>
  );
}
