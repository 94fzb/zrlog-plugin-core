import { createRoot } from "react-dom/client";
import * as serviceWorker from './serviceWorker';
import zh_CN from "antd/es/locale/zh_CN";


import {legacyLogicalPropertiesTransformer, StyleProvider} from "@ant-design/cssinjs";
import {useEffect, useState} from "react";
import {App, ConfigProvider, theme} from "antd";
import {BrowserRouter} from "react-router-dom";
import AppBase from "./AppBase";

const {darkAlgorithm, defaultAlgorithm} = theme;

function getPreferredColorScheme() {
    if (window.matchMedia) {
        if (window.matchMedia('(prefers-color-scheme: dark)').matches) {
            return 'dark';
        }
    }
    return 'light';
}


export const isDarkMode = (): boolean => {
    return getPreferredColorScheme() === "dark";
}

const Index = () => {
    const [dark, setDark] = useState<boolean>(isDarkMode());

    useEffect(() => {
        const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
        const changeHandler = () => setDark(isDarkMode);

        mediaQuery.addEventListener('change', changeHandler);

        // 在组件卸载时移除事件监听器
        return () => mediaQuery.removeEventListener('change', changeHandler);
    }, []);

    return (
        <ConfigProvider
            locale={zh_CN}
            theme={{
                algorithm: dark ? darkAlgorithm : defaultAlgorithm,
            }}
            divider={{
                style:{
                    margin:"8px 0px"
                }
            }}
            table={
                {
                    style: {
                        whiteSpace: "nowrap"
                    },
                }}
        >
            <BrowserRouter>
                <StyleProvider transformers={[legacyLogicalPropertiesTransformer]}>
                    <App>
                        <AppBase/>
                    </App>
                </StyleProvider>
            </BrowserRouter>
        </ConfigProvider>
    );
};

const container = document.getElementById("app");
const root = createRoot(container!); // createRoot(container!) if you use TypeScript
root.render(<Index />);
// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: http://bit.ly/CRA-PWA
serviceWorker.unregister();