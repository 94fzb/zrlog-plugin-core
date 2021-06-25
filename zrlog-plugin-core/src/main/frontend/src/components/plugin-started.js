import React from "react";
import Result from "antd/es/result";
import Button from "antd/es/button";

class PluginStarted extends React.Component {

    render() {
        return (
            <Result
                status="error"
                title="插件已经在运行了"
                subTitle=''
                extra={<Button type="primary">Go Back</Button>}
            />
        )
    }
}

export default PluginStarted
