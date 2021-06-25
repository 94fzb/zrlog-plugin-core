import EnvUtils from "./utils/env-utils";
import AsyncDarkApp from "./DarkApp";
import AsyncLightApp from "./LightApp";
import React from "react";

class ThemeIndex extends React.Component {
    render() {
        return EnvUtils.isDarkMode() ? <AsyncDarkApp/> : <AsyncLightApp/>;
    }
}

export default ThemeIndex;
