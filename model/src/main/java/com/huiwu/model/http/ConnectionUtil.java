package com.huiwu.model.http;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Created by HuiWu on 2015/9/23.
 */
public class ConnectionUtil {
    private final String BOUNDARY = UUID.randomUUID().toString();

    private int connectionTimeOut = 10000;

    private int readTimeOut = 10000;

    private String method = "POST";

    public ConnectionUtil() {
        super();
    }

    public ConnectionUtil(String method){
        super();
        this.method = method;
    }

    public ConnectionUtil(int connectionTimeOut,int readTimeOut){
        super();
        setConnectionTimeOut(connectionTimeOut);
        setReadTimeOut(readTimeOut);
    }

    public ConnectionUtil(int connectionTimeOut,int readTimeOut,String method){
        super();
        setConnectionTimeOut(connectionTimeOut);
        setMethod(method);
        setReadTimeOut(readTimeOut);
    }

    public int getConnectionTimeOut() {
        return connectionTimeOut;
    }

    public void setConnectionTimeOut(int connectionTimeOut) {
        this.connectionTimeOut = connectionTimeOut;
    }

    public int getReadTimeOut() {
        return readTimeOut;
    }

    public void setReadTimeOut(int readTimeOut) {
        this.readTimeOut = readTimeOut;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    private HttpURLConnection createConnection(String request_url) throws IOException {
        URL url = new URL(request_url);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(connectionTimeOut);
        connection.setReadTimeout(readTimeOut);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setRequestMethod(method);
        connection.setRequestProperty("connection", "keep-alive");
        connection.setRequestProperty("Charset", "UTF-8");
        connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
        return connection;
    }

    public String getConnection(String request_url) {
        try {
            HttpURLConnection urlConnection = createConnection(request_url);
            if (urlConnection.getResponseCode() != 200) {
                return null;
            } else {
                InputStream is = urlConnection.getInputStream();
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len1;
                while ((len1 = is.read(buffer)) != -1) {
                    os.write(buffer, 0, len1);
                }

                is.close();
                os.close();
                String result = new String(os.toByteArray());
                return result;
            }
        } catch (Exception var8) {
            return null;
        }
    }

    public String postParams(String request_url, Map<String, String> params) throws IOException {
        return postParams(request_url, params, null, false);
    }

    public String postParams(String request_url, Map<String, String> params, Map<String, File> files) throws IOException {
        return postParams(request_url, params, files, true);
    }

    public String postParams(String request_url, Map<String, String> params, Map<String, File> files, boolean needFile) throws IOException {
        HttpURLConnection connection = createConnection(request_url);
        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
        StringBuilder sb = new StringBuilder();
        Map.Entry end_data;
        Iterator iterator;
        if (params != null) {
            iterator = params.entrySet().iterator();

            while (iterator.hasNext()) {
                end_data = (Map.Entry) iterator.next();
                sb.append("--");
                sb.append(BOUNDARY);
                sb.append("\r\n");
                sb.append("Content-Disposition: form-data; name=\"" + end_data.getKey() + "\"" + "\r\n");
                sb.append("Content-Type: text/plain; charset=UTF-8\r\n");
                sb.append("Content-Transfer-Encoding: 8bit\r\n");
                sb.append("\r\n");
                sb.append((String) end_data.getValue());
                sb.append("\r\n");
            }

            outputStream.write(sb.toString().getBytes());
        }

        if (files != null) {
            iterator = files.entrySet().iterator();

            while (iterator.hasNext()) {
                end_data = (Map.Entry) iterator.next();
                sb = new StringBuilder();
                sb.append("--");
                sb.append(BOUNDARY);
                sb.append("\r\n");
                sb.append("Content-Disposition: form-data; name=\"" +  end_data.getKey() + "\"; filename=\"" + ((File) end_data.getValue()).getName() + "\"" + "\r\n");
                sb.append("Content-Type: application/octet-stream; charset=UTF-8\r\n");
                sb.append("\r\n");
                outputStream.write(sb.toString().getBytes());

                try {
                    FileInputStream br = new FileInputStream((File) end_data.getValue());
                    if (needFile) {
                        byte[] value = new byte[1024];

                        int len;
                        while ((len = br.read(value)) != -1) {
                            outputStream.write(value, 0, len);
                        }
                    }

                    br.close();
                    outputStream.write("\r\n".getBytes());
                } catch (Exception var12) {

                }
            }
        }

        byte[] end_data1 = ("--" + BOUNDARY + "--" + "\r\n").getBytes();
        outputStream.write(end_data1);
        outputStream.flush();

        String result = "";
        String value1;


        if (connection.getResponseCode() == 200) {
            for (BufferedReader br1 = new BufferedReader(new InputStreamReader(connection.getInputStream())); (value1 = br1.readLine()) != null; result = result + value1) {

            }
            try {
                String sessionStatus = connection.getHeaderField("sessionStatus");
                if (TextUtils.equals(sessionStatus, "clear")) {
                    return "clear";
                }
            } catch (Exception e) {

            }

        } else {
            result = null;
        }

        outputStream.close();
        connection.disconnect();
        return result;
    }
}