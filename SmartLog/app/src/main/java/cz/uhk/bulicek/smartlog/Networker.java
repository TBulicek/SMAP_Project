package cz.uhk.bulicek.smartlog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.*;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.WIFI_SERVICE;

/**
 * Class that handles all the networking.
 */

public class Networker {
    private Context context;

    public Networker(Context c) { this.context = c; }

    /**
     * Method to get local info, returning array, where
     * [0] is SSID of the network device is connected to, and
     * [1] is IP of the device on that network
     */
    public String[] getLocalInfo() {
        String[] localInfo = new String[2];

        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);

        /* *
         * get SSID, store in localInfo[0]
         */
        String SSID = wifiManager.getConnectionInfo().getSSID();
        if (SSID.equals("<unknown ssid>")) {
            localInfo[0] = SSID;
        } else {
            localInfo[0] = SSID.substring(SSID.indexOf("\"") + 1, SSID.lastIndexOf("\""));
        }

        /* *
         * get IP as string, store in localInfo[1]
         */
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        // Convert little-endian to big-endian if needed
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            ipAddressString = "Unable to get host address.: " + ex;
        }
        localInfo[1] = ipAddressString;

        return localInfo;
    }

    /**
     * Method that returns a list of networks available to the device.
     */
    public List<String> getAvailibleNetworks() {
        List<String> resultStrings = new ArrayList<>();
        WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        mWifiManager.startScan();
        List<ScanResult> mScanResults = mWifiManager.getScanResults();

        for (ScanResult res : mScanResults) {
            resultStrings.add(res.SSID);
        }
        return resultStrings;
    }

    /**
     * Method that returns an array list of devices that are currently responding on
     * local network. This method manages the threads, and uses PingLooper class to
     * do all the actual pinging.
     */
    public ArrayList<InetAddress> searchDevices(String ipAddr) {
        ArrayList<InetAddress> addrs = new ArrayList<>();

        Thread[] threads = new Thread[51];
        for (int i = 0; i<51; i++ ) {
            threads[i] = new Thread(new PingLooper(i * 5, (i + 1) * 5 -1, ipAddr, addrs));
        }
        for (Thread l : threads) {
            l.start();
        }
        try {
            for (Thread l : threads) {
                l.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return addrs;
    }

    /**
     * Method that returns a MAC address for a given IP address.
     */
    public String getMacFromArpCache(String ip) {
        if (ip == null)
            return null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4 && ip.equals(splitted[0])) {
                    String mac = splitted[3];
                    if (mac.matches("..:..:..:..:..:..")) {
                        return mac;
                    } else {
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Method to check if MAC is on the network. First uses the default IP, if that
     * does not match, then tries to search all devices (searchDevices method).
     */
    public boolean findMACOnNetwork(String defaultIP, String currentIP, String MAC) {
        //first pass-trough: check MAC on defaultIP
        try {
            Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 -w 50 " + defaultIP);
            boolean reachable = (p1.waitFor() == 0);
            if (reachable) {
                if (MAC.equals(getMacFromArpCache(defaultIP))) {
                    return true;
                }
            }
        } catch (IOException ex) {
        } catch (InterruptedException ex) {
        }

        //if defaultIP MAC fails, try searching all devices again
        //and look for MAC address match
        ArrayList<InetAddress> addrs = searchDevices(currentIP);
        for (InetAddress addr : addrs) {
            String foundMac = getMacFromArpCache(addr.getHostAddress());
            if (foundMac.equals(MAC)) {
                return true;
            }
        }
        return false;
    }
}
