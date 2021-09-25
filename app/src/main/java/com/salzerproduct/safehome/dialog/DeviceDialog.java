package com.salzerproduct.safehome.dialog;

import android.app.Dialog;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import com.salzerproduct.database.model.AddDevice;
import com.salzerproduct.database.model.AddDeviceDAO;
import com.salzerproduct.database.model.AppDatabase;
import com.salzerproduct.safehome.R;
import com.salzerproduct.safehome.adapter.DeviceAdapter;
import com.salzerproduct.safehome.basepojo.dDevice;

import java.util.ArrayList;
import java.util.List;

public class DeviceDialog extends Dialog {

    CallBack mCallBack;
    Context mContext;
    ListView pType;
    String ZoneDatas;
    String mData;
    //    String[] LiveData1;
    ArrayList<String> LiveData1 = new ArrayList<String>();
    String SelectedRegion;
    AddDeviceDAO mAddDeviceDAO;
    DeviceAdapter zoneadapter;

    public DeviceDialog(Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.device_dialog);

        mAddDeviceDAO = Room.databaseBuilder(mContext, AppDatabase.class, "db-devices")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries() //Allows room to do operation on main thread
                .build()
                .getAddDeviceDAO();

        pType = (ListView) findViewById(R.id.manufaturerList);
        filldata();

        pType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ZoneDatas = LiveData1.get(position);
                String Data = LiveData1.get(position).toString();
                mCallBack.GetDevices(Data);
            }
        });
    }

    private void filldata() {
        List<AddDevice> details = mAddDeviceDAO.getDevices();
        if (!details.isEmpty()) {
            LiveData1.clear();
            ArrayList<dDevice> ZoneData = new ArrayList<dDevice>();
            for (int u = 0; u < details.size(); u++) {
                dDevice vehcileManufacturer = new dDevice(details.get(u).getDevicename());
                LiveData1.add(details.get(u).getDevicename());
                ZoneData.add(vehcileManufacturer);
            }
            zoneadapter = new DeviceAdapter(mContext, ZoneData);
            pType.setAdapter(zoneadapter);
        }
    }

    public void setCallBack(CallBack mCallBackDialog) {
        this.mCallBack = mCallBackDialog;
    }

    public interface CallBack {
        public void GetDevices(String DeviceName);
    }
}
