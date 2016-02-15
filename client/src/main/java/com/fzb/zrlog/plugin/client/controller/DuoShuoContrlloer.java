package com.fzb.zrlog.plugin.client.controller;

import com.fzb.common.util.IOUtil;
import com.fzb.zrlog.plugin.IMsgPacketCallBack;
import com.fzb.zrlog.plugin.IOSession;
import com.fzb.zrlog.plugin.common.IdUtil;
import com.fzb.zrlog.plugin.data.codec.ContentType;
import com.fzb.zrlog.plugin.data.codec.HttpRequestInfo;
import com.fzb.zrlog.plugin.data.codec.MsgPacket;
import com.fzb.zrlog.plugin.data.codec.MsgPacketStatus;
import com.fzb.zrlog.plugin.type.ActionType;
import com.lyncode.jtwig.JtwigModelMap;
import com.lyncode.jtwig.JtwigTemplate;
import com.lyncode.jtwig.exception.CompileException;
import com.lyncode.jtwig.exception.ParseException;
import com.lyncode.jtwig.exception.RenderException;
import flexjson.JSONDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xiaochun on 2016/2/13.
 */
public class DuoShuoContrlloer {

    private IOSession session;
    private MsgPacket requestPacket;
    private HttpRequestInfo requestInfo;

    public DuoShuoContrlloer(IOSession session, MsgPacket requestPacket, HttpRequestInfo requestInfo) {
        this.session = session;
        this.requestPacket = requestPacket;
        this.requestInfo = requestInfo;
    }

    public void update() {
        session.sendMsg(new MsgPacket(requestInfo.simpleParam(), ContentType.JSON, MsgPacketStatus.SEND_REQUEST, IdUtil.getInt(), ActionType.SET_WEBSITE.name()), new IMsgPacketCallBack() {
            @Override
            public void handler(MsgPacket msgPacket) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("success", true);
                session.sendMsg(new MsgPacket(map, ContentType.JSON, MsgPacketStatus.RESPONSE_SUCCESS, requestPacket.getMsgId(), requestPacket.getMethodStr()));
            }
        });
    }

    public void index() {
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put("key", "duoshuo_short_name,duoshuo_secret,user_comment_plugin");
        session.sendJsonMsg(keyMap, ActionType.GET_WEBSITE.name(), IdUtil.getInt(), MsgPacketStatus.SEND_REQUEST, new IMsgPacketCallBack() {
            @Override
            public void handler(MsgPacket msgPacket) {
                String templateStr = IOUtil.getStringInputStream(DuoShuoContrlloer.class.getResourceAsStream("/templates/index.twig.html"));
                JtwigTemplate jtwigTemplate = JtwigTemplate.fromString(templateStr);
                JtwigModelMap modelMap = new JtwigModelMap();
                try {
                    Map map = new JSONDeserializer<Map>().deserialize(msgPacket.getDataStr());
                    modelMap.putAll(map);
                    session.sendMsg(new MsgPacket(jtwigTemplate.output(modelMap).getBytes(), ContentType.BYTE, MsgPacketStatus.RESPONSE_SUCCESS, requestPacket.getMsgId(), requestPacket.getMethodStr()));
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (CompileException e) {
                    e.printStackTrace();
                } catch (RenderException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
