package server.httptools;

import io.netty.buffer.ByteBuf;

public class JerryRequest
{
    private String request = null;
    
    public JerryRequest(Object msg)
        throws Exception
    {
        ByteBuf buf = (ByteBuf) msg;
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        String req = new String(bytes, "UTF-8");
        this.request = req;
        System.out.println("Thread ID : [" + Thread.currentThread().getId() + "]'s JerryRequest has been inited.");
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
