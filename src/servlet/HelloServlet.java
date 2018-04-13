package servlet;

import java.io.OutputStream;

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
    public void service(JerryRequest request, OutputStream out) throws Exception
    {
        // 请求参数username
        String username = request.getParameter("username");
        out.write("HTTP/1.1 200 OK\r\n".getBytes());
        out.write("Content-Type:text/html\r\n\r\n".getBytes());
        out.write("<html><head><title>HelloWorld</title></head><body>".getBytes());
        out.write(new String("<h1>Hello:" + username + "</h1></body></html>").getBytes());
        out.close();
    }
    
}
