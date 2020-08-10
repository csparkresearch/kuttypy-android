package com.example.kuttypy;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private final int BUFSIZE = 2000;
    private byte[] buffer = new byte[BUFSIZE];		// for communication with the uC

    public byte SUCCESS 		= (byte)'D';			// Command executed successfully
    private byte WAITING 		= (byte)'W';			// Command under processing, for threaded version
    private byte INVCMD		= (byte)'C';			// Invalid Command
    private byte INVARG		= (byte)'A';			// Invalid input data
    private byte INVBUFSIZE	= (byte)'B';			// Resulting data exceeds buffer size
    private byte TIMEOUT		= (byte)'T';			// Time measurement timed out
    private byte COMERR		= (byte)'S';			// Serial Communication error
    private byte INVSIZE		= (byte)'Z';			// Size mismatch, result of capture
    private final String TAG = "KPY Library";

    private AppBarConfiguration mAppBarConfiguration;

    private UsbManager mUsbManager;
    private UsbDevice mDevice;
    private UsbDeviceConnection connection; // Open a connection to the first available driver.
    private UsbSerialDriver driver;
    public UsbSerialPort port;
    public comlib MCA;
    SwitchCompat mcaState;
    private Handler mHandler;

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private PendingIntent mPermissionIntent;

    IntentFilter filter;
    public AlertDialog.Builder about_dialog;
    boolean connected=false;
    private Integer timeout = 500;
    public int commandStatus = 0;
    private boolean running = false, disableLoop = false;
    private spectrumData model;
    private NavController navController;
    List<List<Integer>> writeThese = new ArrayList<List<Integer>>(20);
    private MPU6050 mpu6050;
    private BMP280 bmp280;
    public String dev = new String("IO");
    private boolean initSensor=false;
    private String command = new String("");
    private Handler messageHandler;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setTitle("KuttyPy ( Connect... )");
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });










        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_io, R.id.nav_adc,R.id.nav_mpu6050,R.id.nav_bmp280)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        model = new ViewModelProvider(this).get(spectrumData.class);

        model.getReg().observe(this, new Observer<List<Integer>>() {
            @Override
            public void onChanged(@Nullable List l) {
                writeThese.add(l);
            }
        });

        model.getSensor().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String name) {
                dev = name;
                initSensor = true;
            }
        });

        model.getCommand().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String name) {
                command = String.valueOf(name);
            }
        });





        about_dialog = new AlertDialog.Builder(this);
        about_dialog.setMessage("Contact Jithin B.P\ne-mail:jithinbp@gmail.com.\n https://github.com/csparkresearch\n\n Open Source Tools:\nAndroidPlot");
        about_dialog.setTitle("From CSpark Research");
        about_dialog.setCancelable(true);


        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        MCA = new comlib();

        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
        if (availableDrivers.isEmpty()) {
            return;
        }

        // Open a connection to the first available driver.
        driver = availableDrivers.get(0);
        connection = mUsbManager.openDevice(driver.getDevice());
        if (connection == null) {
            askForPermission();
            // add UsbManager.requestPermission(driver.getDevice(), ..) handling here
            return;
        } else {

            port = driver.getPorts().get(0); // Most devices have just one port (port 0)
            try {
                port.open(connection);
                port.setParameters(38400, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                port.purgeHwBuffers(true,true);
                MCA.setPort(port);
                if(MCA.connected){
                    Toast.makeText(getBaseContext(),"Device found: "+MCA.version,Toast.LENGTH_SHORT).show();
                    connected = true;
                    running = true;
                }
                else Toast.makeText(getBaseContext(),"Device not found!! Reconnect",Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        messageHandler = new Handler() {

            @Override public void handleMessage(Message msg) {
                String mString=(String)msg.obj;
                Toast.makeText(getBaseContext(), mString, Toast.LENGTH_LONG).show();
            }
            
        };

        HandlerThread thread = new HandlerThread("MyHandlerThread", Process.THREAD_PRIORITY_MORE_FAVORABLE);

        thread.start();
        mHandler = new Handler(thread.getLooper());
        //eventLoop.run();
        mHandler.postDelayed(eventLoop, 10);
    }




    public void askForPermission(){
        // Find all available drivers from attached devices.
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);

        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
        if (availableDrivers.isEmpty()) {
            Toast.makeText(getBaseContext(),"No device connected.",Toast.LENGTH_SHORT).show();
            return;
        }


        driver = availableDrivers.get(0);
        connection = mUsbManager.openDevice(driver.getDevice());
        mDevice = driver.getDevice();

        mUsbManager.requestPermission(mDevice, mPermissionIntent);


    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_reconnect:
                askForPermission();
                return true;
            case R.id.action_credits:
                about_dialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }




    /*---------------------REQUEST USB PERMISSION WITHIN THE APPLICATION--------------------------*/


    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) { //called when permission request reply received
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false) ) { //permission granted
                        try {
                            port = driver.getPorts().get(0); // Most devices have just one port (port 0)
                            port.open(connection);
                            port.setParameters(500000, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                            connected = true;
                            port.purgeHwBuffers(true,true);
                            Toast.makeText(getBaseContext(),"device connected. .",Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        if(mDevice != null){
                                MCA.setPort(port);
                                if(MCA.connected)Toast.makeText(getBaseContext(),"Device found: "+MCA.version,Toast.LENGTH_SHORT).show();
                             else Toast.makeText(getBaseContext(),"Device not found!!",Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {																		//permission denied
                        Toast.makeText(getBaseContext(),"Please grant permissions to access the device",Toast.LENGTH_LONG).show();
                        //Log.d("UH-OH", "permission denied for device " + mcp2200.mDevice);
                    }
                }
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }



    Runnable eventLoop = new Runnable() {
        @Override
        public void run() {

            while (true) {
                if ((!running) || disableLoop || MCA.connected == false || (MCA.commandStatus != MCA.SUCCESS)) {
                    model.setCount(0);
                    continue;
                }
                if(initSensor){
                    if(dev.equals("MPU6050")) {
                        mpu6050 = new MPU6050(MCA);
                    }else if(dev.equals("BMP280")) {
                        bmp280 = new BMP280(MCA);
                    }
                    initSensor=false;
                }
                // Pending register writes
                for (int i=0; i<writeThese.size(); i++) {
                    List<Integer> w = writeThese.get(i);
                    if(w.size() == 2) { // probably a setReg
                        MCA.writeReg((int) w.get(0), (int) w.get(1));
                    }
                }
                writeThese.clear();

                // Pending Commands
                if(command.equals("scan")){
                    Log.e("ERR","SCANNING");
                    List found = MCA.scanI2C();
                    Log.e("ERR","SCANNING"+found.toString());
                    Message msg = new Message();
                    msg.obj = " Devices at: " + found.toString();
                    messageHandler.sendMessage(msg);
                    command = "" ;
                }

                if(dev.equals("IO")){ //(navController.getCurrentDestination().getId() == R.id.nav_io){
                    List<Integer> arr = new ArrayList<Integer>(4);
                    arr.add(MCA.readReg(0x39)); //PINA
                    arr.add(MCA.readReg(0x36)); //PINB
                    arr.add(MCA.readReg(0x33)); //PINC
                    arr.add(MCA.readReg(0x30)); //PIND

                    //Log.e("STATUS", String.valueOf(counts));
                    model.setStates(arr);
                }else if(dev.equals("ADC")){ //(navController.getCurrentDestination().getId() == R.id.nav_adc){ //ADC page
                    for(int i=0;i<8;i++){
                        List<Integer> arr = new ArrayList<Integer>(8);
                        arr.add(i);
                        arr.add(MCA.readADC(i));
                        model.setSingleADC(arr);
                    }
                }else if(dev.equals("MPU6050")){ //navController.getCurrentDestination().getId() == R.id.nav_mpu6050){ //Sensors page
                        model.setI2C(mpu6050.getData());
                }else if(dev.equals("BMP280")){ //navController.getCurrentDestination().getId() == R.id.nav_bmp280){ //Sensors page
                    model.setI2C(bmp280.getData());
                }

                if (MCA.connected == false || (MCA.commandStatus != MCA.SUCCESS)) {
                    Message msg = new Message() ;
                    msg.obj = "Device disconnected.  Check connections and reconnect";
                    messageHandler.sendMessage(msg);
                    continue;
                }
            }
        }


    };






}
