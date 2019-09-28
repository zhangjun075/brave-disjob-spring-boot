package com.brave.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * @author junzhang
 */
public class IpUtil {
    static String LOCAL_IP;

    static {
        LOCAL_IP = getLocalIP();
    }

    /**
     * 获得主机IP
     *
     * @return String
     */
    public static boolean isWindowsOS(){
        boolean isWindowsOS = false;
        String osName = System.getProperty("os.name");
        if(osName.toLowerCase().indexOf("windows")>-1){
            isWindowsOS = true;
        }
        return isWindowsOS;
    }

    /**
     * 获取本机ip地址，并自动区分Windows还是linux操作系统
     * @return String
     */
    public static String getLocalIP(){
        String sIP = "";
        InetAddress ip = null;
        try {
            //如果是Windows操作系统
            if(isWindowsOS()){
                ip = InetAddress.getLocalHost();
            }
            //如果是Linux操作系统
            else{
                boolean bFindIP = false;
                Enumeration<NetworkInterface> netInterfaces = (Enumeration<NetworkInterface>) NetworkInterface
                    .getNetworkInterfaces();
                while (netInterfaces.hasMoreElements()) {
                    if(bFindIP){
                        break;
                    }
                    NetworkInterface ni = (NetworkInterface) netInterfaces.nextElement();
                    //----------特定情况，可以考虑用ni.getName判断
                    //遍历所有ip
                    Enumeration<InetAddress> ips = ni.getInetAddresses();
                    while (ips.hasMoreElements()) {
                        ip = (InetAddress) ips.nextElement();
                        if( ip.isSiteLocalAddress()
                            && !ip.isLoopbackAddress()   //127.开头的都是lookback地址
                            && ip.getHostAddress().indexOf(":")==-1){
                            bFindIP = true;
                            break;
                        }
                    }

                }
            }
        }
        catch (Exception e) {
            //e.printStackTrace();
        }

        if(null != ip){
            sIP = ip.getHostAddress();
        }
        return sIP;
    }
}
