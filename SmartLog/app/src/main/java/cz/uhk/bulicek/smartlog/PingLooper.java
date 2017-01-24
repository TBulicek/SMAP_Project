package cz.uhk.bulicek.smartlog;

import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bulicek on 24. 1. 2017.
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

    public void addConnectedDevices(String IPAddress, int start, int end) {
        try {
            InetAddress ip = InetAddress.getByName(IPAddress);
            NetworkInterface iFace = NetworkInterface.getByInetAddress(ip);
            for (int i = start; i <= end; i++) {
                System.out.println(i);
                String addr = IPAddress;
                addr = addr.substring(0, addr.lastIndexOf('.') + 1) + i;
                InetAddress pingAddr = InetAddress.getByName(addr);
                try {
                    Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 -w 5 " + addr);
                    boolean reachable = (p1.waitFor() == 0);
                    if (reachable) {
                        addrs.add(pingAddr);
                    }
                } catch (IOException ex) {
                } catch (InterruptedException ex) {
                }

            }
        } catch (UnknownHostException ex) {
        } catch (SocketException ex) {
        }
    }

    @Override
    public void run() {
        addConnectedDevices(IPAddr, start, end);
    }
}
