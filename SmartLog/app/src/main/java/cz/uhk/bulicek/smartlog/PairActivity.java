package cz.uhk.bulicek.smartlog;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class PairActivity extends AppCompatActivity {
    ListView devicesList;
    List<InetAddress> devices = new ArrayList<>();
    List<String> devicesStrings = new ArrayList<>();
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pair);
        devicesList = (ListView) findViewById(R.id.devicesList);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1,  devicesStrings);
        devicesList.setAdapter(adapter);

        Thread t = new Thread(new Runnable() {
            public void run() {
                Networker nw = new Networker(getApplicationContext());
                String ip = nw.getLocalInfo()[1];
                devices = nw.searchDevices(ip);
                try {
                    for (InetAddress addr : devices) {
                        String mac = nw.getMacFromArpCache(addr.getHostAddress());
                        if (mac == null) {mac = "Unknown";}
                        devicesStrings.add(addr.getHostAddress() + ": MAC " + mac);
                    }
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
                updateListView();
            }
        });
        t.start();
    }

    private void updateListView(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), "Got here.", Toast.LENGTH_LONG).show();
                for (String s:devicesStrings) {
                    Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
