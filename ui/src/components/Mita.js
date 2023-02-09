import { Component } from "react";
import { getViewList } from "../utils/api";
import View from "./View";

export default class Mita extends Component {
    constructor(props) {
        super(props);

        this.state = {viewNames: []};
    }

    async componentDidMount() {
        const viewNames = await getViewList();
        this.setState({viewNames});
    }

    render() {
        return (
            <div>
                {this.state.viewNames.map(name => <View viewName={name} key={name}/>)}
            </div>
        )
    }
}
