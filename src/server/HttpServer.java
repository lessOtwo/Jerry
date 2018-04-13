package server;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import server.httptools.JerryRequest;
import servlet.Servlet;

/**
 * This is my first Server
 * 
 * @author xuwensheng
 */
public class HttpServer
{
    private static Map<String, Servlet> servletCache = new HashMap<>();
    
    public static void main(String[] args)
    {
        int port;
        
        try
        {
            port = Integer.parseInt(args[0]);
        }
        catch (Exception e)
        {
            port = 8080;
        }
        
        try (ServerSocket serverSocket = new ServerSocket(port))
        {
            System.out.println("服务器正在监听端口：" + serverSocket.getLocalPort());
            System.out.println();
            ExecutorService executor = Executors.newFixedThreadPool(8);
            for (;;)
            {
                try
                {
                    final Socket socket = serverSocket.accept();
                    /* 可以使用线程池来达到复用优化 */
                    executor.execute(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            System.out.println("<===============================================>");
                            System.out
                                .println("接受到一条TCP连接： " + socket.getInetAddress() + ":" + socket.getPort());
                            try
                            {
                                try
                                {
                                    service(socket);
                                }
                                finally
                                {
                                    socket.close();
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    });
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private static void service(Socket socket)
        throws Exception
    {
        InputStream socketIn = socket.getInputStream();
        OutputStream socketOut = socket.getOutputStream();
        
        JerryRequest request = new JerryRequest(socketIn);
        
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
                servlet = (Servlet)Class.forName("servlet." + servletName).newInstance();
                servlet.init();
                servletCache.put(servletName, servlet);
            }
            servlet.service(request, socketOut);
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
            InputStream in = HttpServer.class.getResourceAsStream("/resource" + uri);
            socketOut.write(responseFirstLine.getBytes());
            socketOut.write(responseHeader.getBytes());
            int len = 0;
            byte[] buffer = new byte[128];
            while ((len = in.read(buffer)) != -1)
            {
                socketOut.write(buffer, 0, len);
            }
        }
        
    }
    
}
