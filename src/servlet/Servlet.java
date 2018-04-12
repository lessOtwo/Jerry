package servlet;

import io.netty.channel.ChannelHandlerContext;
import server.httptools.JerryRequest;

public interface Servlet
{
    public void init()
        throws Exception;
    
    public void service(JerryRequest request, ChannelHandlerContext ctx)
        throws Exception;
}
