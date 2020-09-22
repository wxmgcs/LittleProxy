package org.littleshoot.proxy.my_test;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

public class ReadAndModifyBodyTest {

    public static void main(String[] args) {
        HttpProxyServer server =
                DefaultHttpProxyServer.bootstrap()
                        .withPort(8888)
                        .withFiltersSource(new HttpFiltersSourceAdapter() {
                            @Override
                            public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
                                return new HttpFiltersAdapter(originalRequest) {
                                    @Override
                                    public HttpResponse clientToProxyRequest(HttpObject httpObject) {
                                        System.out.println("====>" + httpObject.toString());
                                        return null;
                                    }

                                    @Override
                                    public HttpObject serverToProxyResponse(HttpObject httpObject) {
                                        System.out.println("<=====" + httpObject.toString());
                                        return responsePre(httpObject);
                                    }

                                    public HttpObject responsePre(HttpObject httpObject) {
                                        System.out.println("Response is: " + httpObject);

                                        if (httpObject instanceof DefaultHttpContent) {
                                            HttpContent response = ((DefaultHttpContent) httpObject);

                                            String content = response.content().toString(CharsetUtil.UTF_8);
                                            System.out.println("result:" + content);
                                            HttpContent contentNew = modifyNeedCode(response, content);
                                            return contentNew;
                                        }
                                        return httpObject;
                                    }

                                    public HttpContent modifyNeedCode(HttpContent response, String content) {
                                        System.out.println("result:" + content);
                                        if (!"xxxx\n".equals(content)) {
                                            return response;
                                        }

                                        String contentNew = "yyyy\n";
                                        HttpContent newResponse = response.copy();
                                        newResponse.content().clear().writeBytes(contentNew.getBytes());
                                        return newResponse;
                                    }

                                    public HttpContent modifyHeader(HttpContent newResponse, String contentNew) {
                                        if (newResponse instanceof DefaultFullHttpResponse) {
                                            DefaultFullHttpResponse fullResponse = (DefaultFullHttpResponse) newResponse;
                                            fullResponse.headers().set("Content-Length", contentNew.length());
                                        }

                                        return newResponse;
                                    }
                                };
                            }
                        })
                        .start();
    }

}
