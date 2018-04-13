package server.httptools;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class JerryRequest
{
    private String request = null;
    
    public JerryRequest(InputStream socketIn)
        throws Exception
    {
        Long threadId = Thread.currentThread().getId();
        int size = socketIn.available();
        byte[] buffer = new byte[size];
        socketIn.read(buffer);
        String req = new String(buffer, "UTF-8");
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
        if (request != null && request.length() > 0)
            return request.substring(0, request.indexOf("\r\n"));
        return "";
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
        if (request != null && request.length() > 0)
        {
            int locate = request.indexOf("\r\n\r\n");
            return request.substring(locate + 4, request.length());
        }
        return "";
    }
    
    public String getRequestHeaders()
    {
        if (request != null && request.length() > 0)
        {
            return request.substring(request.indexOf("\r\n") + 2, request.indexOf("\r\n\r\n"));
        }
        return "";
    }
    
    public String getHeader(String name)
    {
        String headers = getRequestHeaders();
        BufferedReader br = new BufferedReader(new StringReader(headers));
        String data = null;
        try
        {
            while ((data = br.readLine()) != null)
            {
                if (data.indexOf(name) != -1)
                {
                    int len = name.length() + 2; // key后面有": "
                    return data.substring(data.indexOf(name + ":") + len, data.length());
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return "";
    }
    
    public Map<String, String> saveUpload(String path) throws UnsupportedEncodingException
    {
        Map<String, String> map = new HashMap<>();
        String contentType = getHeader("Content-Type");
        // 边界
        String boundary =
            contentType.substring(contentType.indexOf("boundary=") + 9, contentType.length()) + "\r\n";
        if (boundary.length() > 2)
        {
            int index1OfBoundary = request.indexOf(boundary);
            int index2OfBoundary = request.indexOf(boundary, index1OfBoundary + boundary.length());
            int index3OfBoundary = request.indexOf(boundary, index2OfBoundary + boundary.length());
            int beforeOfFilePart = request.indexOf("\r\n\r\n", index2OfBoundary) + 3;
            int afterOfFilePart = index3OfBoundary - 4;
            int afterOfFilePartLine1 = request.indexOf("\r\n", index2OfBoundary + boundary.length());
            String header2OfFilePart =
                request.substring(index2OfBoundary + boundary.length(), afterOfFilePartLine1);
            String fileName = header2OfFilePart.substring(header2OfFilePart.lastIndexOf("filename=\"") + 10,
                header2OfFilePart.length() - 1);
            map.put("fileName", fileName);
            int len1 = request.substring(0, beforeOfFilePart + 1).getBytes().length;
            int len2 = request.substring(afterOfFilePart, request.length()).getBytes().length;
            int fileLen = request.getBytes("UTF-8").length - len1 - len2;
            map.put("fileLen", fileLen + "");
            try(FileOutputStream fos = new FileOutputStream(path + fileName))
            {
                fos.write(request.getBytes("UTF-8"), len1, fileLen);
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
                map.put("result", "0");
            }
            catch (IOException e)
            {
                e.printStackTrace();
                map.put("result", "0");
            }
            map.put("result", "1");
        }
        else
        {
            map.put("result", "0");
        }
        return map;
    }
}
