import { Component } from "react";
import { getView } from "../utils/api";

export default class View extends Component {
  constructor(props) {
    super(props);

    this.state = { view: {} };
  }

  async componentDidMount() {
    const view = await getView(this.props.viewName);
    this.setState({ view });
  }

  render() {
    return (
      <div className={"m-2"}>
        <h1>{this.props.viewName}</h1>
        <p>{JSON.stringify(this.state.view)}</p>
      </div>
    );
  }
}
