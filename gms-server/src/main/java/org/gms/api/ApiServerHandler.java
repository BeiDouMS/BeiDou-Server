package org.gms.api;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.gms.api.dto.BaseNettyRequest;
import org.gms.api.dto.BaseNettyResponse;

@Slf4j
public class ApiServerHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext context, String msg) throws Exception {
        log.info("request:{}", msg);
        BaseNettyRequest<?> baseNettyRequest = JSONObject.parseObject(msg, new TypeReference<>() {
        });
        BaseController baseController = ControllerType.ofController(baseNettyRequest.getRequestKey());
        BaseNettyResponse<?> baseNettyResponse;
        if (baseController == null) {
            baseNettyResponse = BaseNettyResponse.builder()
                    .responseId(baseNettyRequest.getRequestId())
                    .responseCode("error")
                    .responseMessage("controller not found")
                    .build();
        } else {
            baseNettyResponse = baseController.request(baseNettyRequest);
        }
        String response = JSONObject.toJSONString(baseNettyResponse);
        log.info("response:{}", response);
        context.writeAndFlush(response);
    }
}
