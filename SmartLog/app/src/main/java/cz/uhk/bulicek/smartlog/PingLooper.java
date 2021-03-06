package cz.uhk.bulicek.smartlog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Class (implementing Runnable) that handles pinging IP address range.
 * See addConnectedDevices method below.
 */

public class PingLooper implements Runnable {
    private int start, end;
    private String IPAddr;
    private List<InetAddress> addrs;

    public PingLooper(int start, int end, String IPAddr, List<InetAddress> addrs){
        this.start = start;
        this.end = end;
        this.IPAddr = IPAddr;
        this.addrs = addrs;
    }

    /**
    Method that loops trough IP addresses in given range start-end, pings
    them and adds to addrs list, if no packets are lost (if reachable)
     */
    public void addConnectedDevices(String IPAddress, int start, int end) {
        try {
            InetAddress ip = InetAddress.getByName(IPAddress);
            NetworkInterface iFace = NetworkInterface.getByInetAddress(ip);
            //loop IPs from start to end
            for (int i = start; i <= end; i++) {
                String addr = IPAddress;

                //set last octet of IP to i, make it InetAddress
                addr = addr.substring(0, addr.lastIndexOf('.') + 1) + i;
                InetAddress pingAddr = InetAddress.getByName(addr);

                //skip the IP of 'this' device
                if (pingAddr.getHostAddress().equals(IPAddress)) { continue; }

                //build process
                Process p = null;
                try {
                    p = java.lang.Runtime.getRuntime().exec("ping -c 1 -w 1 " + addr);
                } catch (IOException ex) {
                }

                //process stream/reader
                InputStream inStream = p.getInputStream();
                InputStreamReader inStreamReader = new InputStreamReader(inStream);
                BufferedReader buffReader = new BufferedReader(inStreamReader);
                String readLine;

                //read, wait for " 0% packet loss", then add pingAddr to list
                while ((readLine = buffReader.readLine()) != null) {
                    CharSequence status = " 0% packet loss";
                    System.out.println(readLine);
                    if (readLine.contains(status)) {
                        addrs.add(pingAddr);
                    }
                }

            }
        } catch (UnknownHostException ex) {
            System.out.println(ex);
        } catch (SocketException ex) {
            System.out.println(ex);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    @Override
    public void run() {
        addConnectedDevices(IPAddr, start, end);
    }
}
