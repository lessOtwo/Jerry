package servlet;

import java.io.OutputStream;
import java.util.Map;

import server.httptools.JerryRequest;

public class UploadServlet implements Servlet
{
    
    @Override
    public void init()
        throws Exception
    {
        System.out.println("UploadServlet is inited.");
    }
    
    @Override
    public void service(JerryRequest request, OutputStream out)
        throws Exception
    {
        out.write("HTTP/1.1 200 OK\r\n".getBytes());
        out.write("Content-Type:text/html\r\n\r\n".getBytes());
        Map<String,String> resultMap = request.saveUpload("src/resource/");
        if("0".equals(resultMap.get("result")))
        {
            out.write("Upload failed".getBytes());
        }
        else
        {
            out.write("Upload is finished.<br>".getBytes()); 
            out.write(("FileName: " + resultMap.get("fileName") + "<br>").getBytes()); 
            out.write(("FileSize: " + resultMap.get("fileLen") + "<br>").getBytes()); 
        }
        
    }
    
}
