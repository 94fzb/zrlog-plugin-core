import React from 'react';
import {Route, Routes} from "react-router";
import CoreIndex from "./components/CoreIndex";
import DownloadResult from "./components/DownloadResult";
import PluginStarted from "./components/PluginStarted";


class AppBase extends React.Component {

    render() {
        return (
            <Routes>
                <Route path='/admin/plugins/downloadResult' element={<DownloadResult/>}/>
                <Route path='/downloadResult' element={<DownloadResult/>}/>
                <Route path='/admin/plugins/pluginStarted' element={<PluginStarted/>}/>
                <Route path='/pluginStarted' element={<PluginStarted/>}/>
                <Route path="*" element={<CoreIndex/>}/>
            </Routes>
        );
    }
}

export default AppBase;
