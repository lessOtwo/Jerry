package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import server.httptools.JerryRequest;
import servlet.Servlet;

public class HttpServerHandler extends ChannelHandlerAdapter
{
    private static Map<String, Servlet> servletCache = new HashMap<>();
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
    {
        JerryRequest request = null;
        try
        {
            request = new JerryRequest(msg);
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }
        
        System.out.println(request.getRequestString());// 打印HTTP请求信息
        
        String uri = request.getRequestUri(); // URI
        
        String contentType = null;
        
        if (uri.indexOf("html") != -1 || uri.indexOf("htm") != -1)
        {
            contentType = "text/html";
        }
        else if (uri.indexOf("jpg") != -1 || uri.indexOf("jpeg") != -1)
        {
            contentType = "image/jpeg";
        }
        else if (uri.indexOf("gif") != -1)
        {
            contentType = "image/gif";
        }
        // 如果请求访问Servlet,则动态调用Servlet对象的service()方法
        else if (uri.indexOf("servlet") != -1)
        {
            // 获得Servlet的名字
            String servletName = null;
            if (uri.indexOf("?") != -1)
            {
                servletName = uri.substring(uri.indexOf("servlet/") + 8, uri.indexOf("?"));
            }
            else
            {
                servletName = uri.substring(uri.indexOf("servlet/") + 8, uri.length());
            }
            // 尝试从servletCache中获取Servlet对象
            Servlet servlet = (Servlet)servletCache.get(servletName);
            if (servlet == null)
            {
                try
                {
                    servlet = (Servlet)Class.forName("servlet." + servletName).newInstance();
                    servlet.init();
                }
                catch (InstantiationException e)
                {
                    e.printStackTrace();
                }
                catch (IllegalAccessException e)
                {
                    e.printStackTrace();
                }
                catch (ClassNotFoundException e)
                {
                    e.printStackTrace();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                
                servletCache.put(servletName, servlet);
            }
            try
            {
                servlet.service(request, ctx);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            contentType = "application/octet-stream"; // 字节流类型
        }
        
        // 非Servlet请求的响应方式
        if (contentType != null && contentType.length() > 0)
        {
            String responseFirstLine = "HTTP/1.1 200 OK\r\n";
            String responseHeader = "Content-Type:" + contentType + "\r\n\r\n";
            ctx.write(Unpooled.copiedBuffer(responseFirstLine.getBytes()));
            ctx.write(Unpooled.copiedBuffer(responseHeader.getBytes()));
            try(FileInputStream is = new FileInputStream(new File("src/resource" + uri)))
            {
                byte[] bytes = new byte[2048];
                int len;
                while((len=is.read(bytes)) != -1)
                {
                    ByteBuf buf = Unpooled.buffer(len);
                    buf.writeBytes(bytes,0,len);
                    ctx.write(buf);
                }
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            ctx.flush();
            ctx.close();
        }
        
    }
    
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx)
    {
        ctx.flush();
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
    {
        ctx.close();
    }
}
