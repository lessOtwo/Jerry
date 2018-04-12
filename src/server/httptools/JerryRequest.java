package server.httptools;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class JerryRequest
{
    private String request = null;
    
    private static final Set<Long> THREADS = new HashSet<>();
    
    public JerryRequest(InputStream socketIn)
        throws Exception
    {
        Long threadId = Thread.currentThread().getId();
        if (THREADS.contains(threadId))
        {
            throw new RuntimeException("该方法只能被一个线程调用一次.");
        }
        THREADS.add(threadId);
        int size = socketIn.available();
        byte[] buffer = new byte[size];
        socketIn.read(buffer);
        String req = new String(buffer);
        this.request = req;
        System.out.println("Thread ID : [" + threadId + "]'s JerryRequest has been inited.");
    }
    
    public String getRequestString()
    {
        return this.request;
    }
    
    /** 获取HTTP请求类型 */
    public String getRequestMethod()
    {
        String firstLineOfRequest = getRequestFirstLine();
        String[] parts = firstLineOfRequest.split(" ");
        if (parts.length == 3)
        {
            return parts[0];
        }
        return "";
    }
    
    public String getRequestUri()
    {
        String firstLineOfRequest = getRequestFirstLine();
        String[] parts = firstLineOfRequest.split(" ");
        if (parts.length == 3)
        {
            return parts[1];
        }
        return "";
    }
    
    public String getRequestProtocol()
    {
        String firstLineOfRequest = getRequestFirstLine();
        String[] parts = firstLineOfRequest.split(" ");
        if (parts.length == 3)
        {
            return parts[2];
        }
        return "";
    }
    
    public String getRequestFirstLine()
    {
        return request.substring(0, request.indexOf("\r\n"));
    }
    
    public String getParameter(String name)
    {
        String method = getRequestMethod();
        String uri = getRequestUri();
        if ("get".equalsIgnoreCase(method) && uri.indexOf(name) != -1)
        {
            String parameters = uri.substring(uri.indexOf("?") + 1, uri.length());
            return getValueByKey(parameters, name);
        }
        if ("post".equalsIgnoreCase(method))
        {
            String content = getRequestContent();
            if (content.indexOf(name + "=") != -1)
            {
                return getValueByKey(content, name);
            }
        }
        return "";
    }
    
    private String getValueByKey(String parameters, String name)
    {
        String[] parts = parameters.split("&");
        if (parts.length > 1)
        {
            for (String part : parts)
            {
                String key = part.split("=")[0];
                if (name.equals(key))
                {
                    return part.split("=")[1];
                }
            }
        }
        return parts[0].split("=")[1];
    }
    
    /** 获取HTTP请求正文 */
    public String getRequestContent()
    {
        int locate = request.indexOf("\r\n\r\n");
        return request.substring(locate + 4, request.length());
    }
}
