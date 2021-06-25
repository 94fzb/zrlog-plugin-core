import React from "react";
import Button from "antd/es/button";
import Result from "antd/es/result";

class DownloadResult extends React.Component {

    state = {
        viewLink: "",
        message: "",
    }

    componentDidMount() {
        const query = new URLSearchParams(this.props.location.search);
        const message = query.get("message");
        const pluginName = query.get("pluginName");
        this.setState({
            message: message,
            viewLink: "/admin/plugins/"+pluginName+"/"
        })
    }

    render() {
        return (
            <Result
                status="success"
                title={this.state.message}
                subTitle=''
                extra={
                    <a href={this.state.viewLink}>
                        <Button type='info'>查看</Button>
                    </a>
                }
            />
        );
    }
}

export default DownloadResult;
