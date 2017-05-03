package cn.cpuboom.geniusbarassistant.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by zhuyuhui on 2016/8/17.
 */
public class SimpleHTTPUtility {
    String mUrl,mJsonStr;
    public SimpleHTTPUtility(String url, String jsonStr){
        mUrl=url;
        mJsonStr=jsonStr;
    }
    public String post() throws HttpResponseException,MalformedURLException,IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(mUrl).openConnection();
        conn.setConnectTimeout(5000);
        conn.setDoOutput(true);//设置允许输出
        conn.setRequestMethod("POST");
        conn.setRequestProperty("User-Agent", "Fiddler");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Charset", "UTF-8");
        OutputStream os = conn.getOutputStream();
        os.write(mJsonStr.getBytes());
        os.close();
        int code=conn.getResponseCode();
        switch (code) {
            case 200:
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String retData = null;
                String responseData = "";
                while ((retData = in.readLine()) != null) {
                    responseData += retData;
                }
                return responseData;

            default:
                throw new HttpResponseException(code);
        }
    }

    public String get() throws HttpResponseException,MalformedURLException,IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(mUrl).openConnection();
        conn.setConnectTimeout(5000);
        conn.setDoOutput(false);//设置允许输出
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Fiddler");
        conn.setRequestProperty("Charset", "UTF-8");
        int code=conn.getResponseCode();
        switch (code) {
            case 200:
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String retData = null;
                String responseData = "";
                while ((retData = in.readLine()) != null) {
                    responseData += retData;
                }
                return responseData;

            default:
                throw new HttpResponseException(code);
        }
    }

    public class HttpResponseException extends Exception{
        public int mCode;
        public HttpResponseException(int code){
            super("HTTP connection responsed with code "+code);
            mCode=code;
        }
    }
}
