import React from "react";

import Result from "antd/es/result";
import Button from "antd/es/button";

class UnknownErrorPage extends React.Component {

    getSecondTitle() {
        return this.props.message;
    }

    render() {
        return (
            <Result
                status="500"
                title="500"
                subTitle={this.getSecondTitle()}
                extra={<Button type="primary">Unknown Error</Button>}
            />
        )
    }
}

export default UnknownErrorPage;
