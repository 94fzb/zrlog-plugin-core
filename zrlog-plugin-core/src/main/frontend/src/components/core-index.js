import React from "react";
import {Badge, Button, Card, Col, Divider, Image, Row, Spin} from "antd";
import Title from "antd/es/typography/Title";
import {DeleteOutlined, DownloadOutlined, SettingOutlined} from "@ant-design/icons";
import axios from "axios";
import Meta from "antd/es/card/Meta";
import Constants from "../utils/constants";
import {Link} from "react-router-dom";
import {Content} from "antd/es/layout/layout";


class CoreIndex extends React.Component {

    state = {
        loading: true,
        plugins: [],
        version: "",
        pluginCenter: ""
    }

    load() {
        axios.get("/admin/plugins/api/plugins").then(e => {
            console.info(e.data);
            this.setState({
                plugins: e.data.plugins,
                version: "v" + e.data.pluginVersion,
                pluginCenter: e.data.pluginCenter,
                loading: false
            })
        })
    }

    componentDidMount() {
        this.load();
    }

    delete(pluginName) {
        axios.get("/admin/plugins/api/uninstall?name=" + pluginName).then(e => {
            this.load();
        })
    }

    render() {
        return (
            <Spin spinning={this.state.loading} style={{height: "100vh"}}>
                <Content hidden={this.state.loading} style={{height: "100vh"}}>
                    <Title className='page-header' level={3}>插件管理 <span
                        style={{fontSize: 16}}>({this.state.version})</span></Title>
                    <Divider/>
                    <Row gutter={[4, 4]} style={{margin: 4}}>
                        {this.state.plugins.map((plugin) => {
                            return (
                                <Col md={8} xxl={4} xs={24}>
                                    <Badge.Ribbon
                                        text={plugin.use ? this.state.res['admin.theme.inUse'] : plugin.preview ? this.state.res['admin.theme.inPreview'] : ""}
                                        style={{
                                            fontSize: 16,
                                            display: plugin.use || plugin.preview ? "" : "none"
                                        }}>
                                        <Card
                                            cover={
                                                <Image
                                                    preview={false}
                                                    fallback={Constants.getFillBackImg()}
                                                    style={{width: "100%", minHeight: 300}}
                                                    alt={plugin.name}
                                                    src={plugin.previewImageBase64}
                                                />
                                            }
                                            actions={[
                                                <a href={"/admin/plugins/" + plugin.shortName + "/"}>
                                                    <SettingOutlined key="preview"/>
                                                </a>,
                                                <Link onClick={e => this.delete(plugin.shortName)}>
                                                    <DeleteOutlined key="delete"/>
                                                </Link>
                                            ]}
                                        >
                                            <Meta
                                                title={plugin.name + ` (` + plugin.version + ')'}
                                                description={plugin.desc}
                                            />
                                        </Card>
                                    </Badge.Ribbon>
                                </Col>
                            )
                        })}
                    </Row>
                    <Divider/>
                    <a href={this.state.pluginCenter}>
                        <Button type='primary'><DownloadOutlined/> 下载</Button>
                    </a>
                </Content>
            </Spin>

        )
    }
}

export default CoreIndex;
