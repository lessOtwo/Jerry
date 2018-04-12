package servlet;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import server.httptools.JerryRequest;

public class HelloServlet implements Servlet
{
    
    @Override
    public void init()
        throws Exception
    {
        System.out.println("HelloServlet is inited.");
    }
    
    @Override
    public void service(JerryRequest request, ChannelHandlerContext ctx) throws Exception
    {
        // 请求参数username
        String username = request.getParameter("username");
        ctx.write(Unpooled.copiedBuffer("HTTP/1.1 200 OK\r\n".getBytes()));
        ctx.write(Unpooled.copiedBuffer("Content-Type:text/html\r\n\r\n".getBytes()));
        ctx.write(Unpooled.copiedBuffer("<html><head><title>HelloWorld</title></head><body>".getBytes()));
        ctx.write(Unpooled.copiedBuffer(new String("<h1>Hello:" + username + "</h1></body></html>").getBytes()));
        ctx.flush();
        ctx.close();
    }
    
}
