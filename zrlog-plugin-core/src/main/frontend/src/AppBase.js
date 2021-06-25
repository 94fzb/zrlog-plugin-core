import React from 'react';
import {Route, Switch} from "react-router";
import CoreIndex from "./components/core-index";
import DownloadResult from "./components/download-result";
import PluginStarted from "./components/plugin-started";


class AppBase extends React.Component {

    render() {
        return (
            <Switch>
                <Route path='/admin/plugins/downloadResult' component={DownloadResult}/>
                <Route path='/admin/plugins/pluginStarted' component={PluginStarted}/>
                <Route path="/admin/plugins/" component={CoreIndex}/>
            </Switch>
        );
    }
}

export default AppBase;
