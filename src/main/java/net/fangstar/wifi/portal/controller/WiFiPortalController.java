/*
 * Copyright (c) 2016, fangstar.com
 *
 * All rights reserved.
 */
package net.fangstar.wifi.portal.controller;

import java.io.PrintWriter;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.fangstar.wifi.portal.Server;
import net.fangstar.wifi.portal.controller.module.RespModule;
import net.fangstar.wifi.portal.controller.module.UserLoginModule;
import net.fangstar.wifi.portal.util.Codes;
import okhttp3.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * WiFiPortal 控制器.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Aug 1, 2016
 * @since 1.0.0
 */
@CrossOrigin
@Controller
@RequestMapping("/wifidog")
public class WiFiPortalController {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(WiFiPortalController.class);

    /**
     * WiFiDog 网关地址
     */
    private static final String GATEWAY_ADDR = Server.CONF.getString("gateway.addr");

    private static final String PORTAL_ADDR = Server.CONF.getString("portal.addr");

    private static final String TOKEN = "33";

//    var data = {
//            deviceCode: deviceCode,
//    deviceName: 'openwrt',
//    deviceOs: 'openwrt',
//    deviceType: 'linux',
//    flag: 'password',
//    password: encrypt(password),
//    username: username,
//};
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login (Model model, final HttpServletRequest request, final HttpServletResponse response) {
        logReq(request);
        String gw_address = request.getParameter("gw_address");
        String gw_port = request.getParameter("gw_port");
        String ip = request.getParameter("ip");
        String mac = request.getParameter("mac");
        LOGGER.debug("mac: " + mac);
        final HttpSession session = request.getSession();
        String visitURL = (String) session.getAttribute("url");
        LOGGER.debug(visitURL);
        if (StringUtils.isBlank(visitURL)) {
            visitURL = request.getParameter("url");
        }
        session.setAttribute("url", visitURL);
        session.setAttribute("gw_address", gw_address);
        session.setAttribute("gw_port", gw_port);
        session.setAttribute("mac", mac);
        return "login";
    }

//    @RequestMapping(value = "/login", method = RequestMethod.GET)
//    public void showLogin(final HttpServletRequest request, final HttpServletResponse response) {
//        logReq(request);
//        gatewayAddr = request.getParameter("gw_address");
//        gatewayPort = request.getParameter("gw_port");
//        LOGGER.debug(gatewayAddr + ":" + gatewayPort);
//        try (final PrintWriter writer = response.getWriter()) {
//            final HttpSession session = request.getSession();
//            String visitURL = (String) session.getAttribute("url");
//            LOGGER.debug(visitURL);
//            if (StringUtils.isBlank(visitURL)) {
//                visitURL = request.getParameter("url");
//            }
//            session.setAttribute("url", visitURL);
//
//            writer.write("<html>    \n"
//                    + "    <head>\n"
//                    + "        <meta charset=\"UTF-8\">\n"
//                    + "        <title>登录 - Portal</title>\n"
//                    + "    </head>\n"
//                    + "    <body>\n"
//                    + "        <form action=\"" + PORTAL_ADDR + "/wifidog/login\" method=\"POST\">\n"
//                    + "            <input type=\"text\" id=\"username\" name=\"username\" "
//                    + "                   placeholder=\"Username\">\n"
//                    + "            <input type=\"password\" id=\"password\" name=\"password\" "
//                    + "                   placeholder=\"Password\">\n"
//                    + "            <button type=\"submit\">登录</button>\n"
//                    + "        </form>\n"
//                    + "    </body>\n"
//                    + "</html>");
//
//            writer.flush();
//        } catch (final Exception e) {
//            LOGGER.error("Write response failed", e);
//        }
//    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<String> login(final HttpServletRequest request, final HttpServletResponse response) {
        logReq(request);

        String gw_address =  (String) request.getSession().getAttribute("gw_address");
        String gw_port =  (String) request.getSession().getAttribute("gw_port");
        String mac =  (String) request.getSession().getAttribute("mac");

        if (mac != null && mac.equals( "c8:94:02:ba:bb:a1")) {
            try {
                response.sendRedirect("http://" + gw_address + ":" + gw_port + "/wifidog/auth?token=" + TOKEN);
                //return new ResponseEntity<>("Success", HttpStatus.OK);
                return null;
            } catch (Exception e) {}
        }
        LOGGER.debug("address: " + gw_address + ":" + gw_port);

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        try {
            OkHttpClient client = new OkHttpClient();
            // 构造要传递的JSON对象
            ObjectMapper objectMapper = new ObjectMapper();
            UserLoginModule userLoginModule = new UserLoginModule();

            userLoginModule.setUsername(username);
            userLoginModule.setPassword(Codes.encrypt(password));
            userLoginModule.setDeviceCode("openwrt_web_portal");
            userLoginModule.setDeviceName("openwrt_web");
            userLoginModule.setDeviceType("linux");
            userLoginModule.setDeviceOs("openwrt");
            userLoginModule.setFlag("password");

            String json = objectMapper.writeValueAsString(userLoginModule);

            // 构造请求体
            // RequestBody body = RequestBody.create(MediaType.parse("application/json"), json);
            RequestBody body = RequestBody.Companion.create(json,MediaType.parse("application/json"));
            // 构造请求
            Request httpRequest = new Request.Builder()
                    .url("https://clientapi.turbo.zenlayer.net/turboClient/api/base/login")
                    .post(body)
                    .build();
            Response httpResponse = client.newCall(httpRequest).execute();
            String responseBody = httpResponse.body().string();

            LOGGER.debug("login resposnse" + responseBody);
            RespModule resp = JSON.parseObject(responseBody, RespModule.class);
            if (resp.isSuccess())  {
                String referer = request.getHeader("referer");
                if (referer.isEmpty()) {
                    LOGGER.error("referer is null");
                } else  {
                    // 将URL拆分为基本组成部分
                    int questionMarkIndex = referer.indexOf("?"); // 找到问号的位置
                    String queryString = referer.substring(questionMarkIndex + 1); // 获取问号后的字符串
                    String[] params = queryString.split("&"); // 将参数分割成数组
                    for (String param : params) {
                        String[] keyValue = param.split("="); // 将键值对分割成数组
                        String key = keyValue[0]; // 获取键
                        String value = keyValue[1]; // 获取值

                        if (key.equals("gw_address")) {
                            // 对 param1 做一些处理，比如输出到控制台
                            gw_address = value;
                            LOGGER.debug("find referer address " + gw_address);
                        } else if (key.equals("gw_port")) {
                            // 对 param2 做一些处理，比如输出到控制台
                            gw_port = value;
                            LOGGER.debug("find referer port " + gw_port);
                        }
                    }
                }
                LOGGER.debug("gateway address: " + gw_address + ":" + gw_port);
                try {
                    response.sendRedirect("http://" + gw_address + ":" + gw_port + "/wifidog/auth?token=" + TOKEN);
                    //return new ResponseEntity<>("Success", HttpStatus.OK);
                } catch (final Exception e) {
                    LOGGER.error("Write response failed", e);
                }
            } else {
                return new ResponseEntity<>(resp.getMsg(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return null;
        } catch ( Exception e) {
            return new ResponseEntity<>("Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/auth", method = {RequestMethod.POST, RequestMethod.GET})
    public void auth(final HttpServletRequest request, final HttpServletResponse response) {
        logReq(request);

        try (final PrintWriter writer = response.getWriter()) {
            final String token = request.getParameter("token");
            if (TOKEN.equals(token)) {
                writer.write("Auth: 1");
            } else {
                writer.write("Auth: 0");
            }
            writer.flush();
        } catch (final Exception e) {
            LOGGER.error("Write response failed", e);
        }
    }

    @RequestMapping(value = "portal", method = {RequestMethod.POST, RequestMethod.GET})
    public void portal(final HttpServletRequest request, final HttpServletResponse response) {
        logReq(request);

        try {
            final String visitURL = (String) request.getSession().getAttribute("url");
            response.sendRedirect("https://www.zenlayer.com/");
        } catch (final Exception e) {
            LOGGER.error("Write response failed", e);
        }
    }

    @RequestMapping(value = "ping", method = {RequestMethod.POST, RequestMethod.GET})
    public void ping(final HttpServletRequest request, final HttpServletResponse response) {
        logReq(request);

        try (final PrintWriter writer = response.getWriter()) {
            writer.write("Pong");
            writer.flush();
        } catch (final Exception e) {
            LOGGER.error("Write response failed", e);
        }
    }

    private void logReq(final HttpServletRequest request) {
        final StringBuilder reqBuilder = new StringBuilder("\nrequest [\n  URI=")
                .append(request.getRequestURI())
                .append("\n  method=").append(request.getMethod())
                .append("\n  remoteAddr=").append(request.getRemoteAddr());

        final String queryStr = request.getQueryString();
        if (StringUtils.isNotBlank(queryStr)) {
            reqBuilder.append("\n  queryStr=").append(queryStr);
        }

        final StringBuilder headerBuilder = new StringBuilder();
        final Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final String headerName = headerNames.nextElement();
            final String headerValue = request.getHeader(headerName);

            headerBuilder.append("    ").append(headerName).append("=").append(headerValue).append("\n");
        }
        headerBuilder.append("  ]");
        reqBuilder.append("\n  headers=[\n").append(headerBuilder.toString()).append("\n]");

        LOGGER.debug(reqBuilder.toString());
    }
}
