package server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * This is my first Server
 * 
 * @author xuwensheng
 */
public class HttpServer
{
    public static void main(String[] args)
    {
        int port = 8080;
        try
        {
            new HttpServer().bind(port);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void bind(int port)
        throws Exception
    {
        // 配置服务器端的NIO线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try
        {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(new ChildChannelHandler());
            // 绑定端口，同步等待成功
            ChannelFuture f = b.bind(port).sync();
            System.out.println("服务器正在监听端口：" + f.channel().localAddress().toString());
            
            // 等待服务端监听端口关闭
            f.channel().closeFuture().sync();
        }
        finally
        {
            // 优雅退出，释放线程池资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
    
    private class ChildChannelHandler extends ChannelInitializer<SocketChannel>
    {
        
        @Override
        protected void initChannel(SocketChannel channel)
            throws Exception
        {
            System.out.println("<===============================================>");
            System.out.println("接受到一条TCP连接： " + channel.remoteAddress().toString());
            channel.pipeline().addLast(new HttpServerHandler());
        }
        
    }
}
