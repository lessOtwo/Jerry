package servlet;

import java.io.OutputStream;

import server.httptools.JerryRequest;

public interface Servlet
{
    public void init()
        throws Exception;
    
    public void service(JerryRequest request, OutputStream out)
        throws Exception;
}
