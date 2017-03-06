package cz.uhk.bulicek.smartlog;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * This Activity displays the list of devices found on local network, and
 * allows user to pair them with this device.
 */
public class PairActivity extends AppCompatActivity {
    private SharedPreferences shprefs;
    ListView devicesList;
    ArrayList<InetAddress> devices = new ArrayList<>();
    InetAddressAdapter adapter;
    TextView txtInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        shprefs = PreferenceManager.getDefaultSharedPreferences(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pair);
        devicesList = (ListView) findViewById(R.id.devicesList);
        adapter = new InetAddressAdapter(this, devices);
        devicesList.setAdapter(adapter);
        txtInfo = (TextView) findViewById(R.id.txt_info);

        Thread t = new Thread(new Runnable() {
            public void run() {
                Networker nw = new Networker(getApplicationContext());
                String ip = nw.getLocalInfo()[1];
                devices = nw.searchDevices(ip);
                updateListView();
            }
        });
        t.start();

        devicesList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
                InetAddress device = (InetAddress) adapter.getItemAtPosition(position);

                Networker nw = new Networker(getApplicationContext());
                SharedPreferences.Editor editor = shprefs.edit();
                editor.putString("mac_address", nw.getMacFromArpCache(device.getHostAddress()));
                editor.putString("ip_address", device.getHostAddress());
                editor.commit();
                finish();
                Toast.makeText(PairActivity.this, "Application paired with selected device.",Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateListView(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.addAll(devices);
                adapter.notifyDataSetChanged();
                if (adapter.getCount() > 0) {
                    txtInfo.setText("Devices loaded. Please select the device to pair.");
                } else {
                    txtInfo.setText("Unable to detect any devices. Check your WiFi connection.");
                }
            }
        });
    }
}
