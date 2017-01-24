package cz.uhk.bulicek.smartlog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.net.InetAddress;
import java.util.ArrayList;

/**
 * Created by bulicek on 24. 1. 2017.
 */

public class InetAddressAdapter extends ArrayAdapter<InetAddress> {

    public InetAddressAdapter(Context context, ArrayList<InetAddress> items) {
        super(context, 0, items);
        System.out.println("Created Adapter");
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        System.out.println("Position: " + position);
        InetAddress addr = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_device, parent, false);
        }

        TextView tvMAC = (TextView) convertView.findViewById(R.id.txt_itemMAC);
        TextView tvIP = (TextView) convertView.findViewById(R.id.txt_itemIP);

        Networker nw = new Networker(getContext());
        String ip = addr.getHostAddress();
        String mac = nw.getMacFromArpCache(ip);
        if (mac == null) {mac = "Unknown";}
        tvMAC.setText(mac);
        tvIP.setText(ip);

        return convertView;
    }
}
