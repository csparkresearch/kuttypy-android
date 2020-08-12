/* ExpEYES communication library.
   Library for ExpEYES (http://expeyes.in) under Android
   Copyright (C) 2014 Jithin B.P. , IISER Mohali (jithinbp@gmail.com)

   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2, or (at your option)
   any later version.
*/

package com.example.kuttypy;

import android.os.SystemClock;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialPort;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class comlib {
	private static final int VERSION_NUM = 99;
	private final int BUFSIZE = 16000;
   	private int timeout = 500;						// Timeout limit
    private final String TAG = "MCA library";
    protected final Object ejLock = new Object();
    public boolean sync=false,syncStart=false, syncStop = false;

	 //commands without arguments   (1 to 40)private final int GET_VERSION = 1;
	private final Integer GET_VERSION = 1;
	private final Integer READB = 2;
	private final Integer WRITEB = 3;
	private final Integer READI2C = 4;
	private final Integer WRITEI2C = 5;
	private final Integer SCANI2C = 6;


	private final Integer GET_COUNT = 3;
	private final Integer START_HISTOGRAM = 12;
	private final Integer STOP_HISTOGRAM = 13;
	private final Integer CLEAR_HISTOGRAM = 14;
	private final Integer GET_HISTOGRAM = 15;

	 // Responses from the device
	 public byte SUCCESS 		= (byte)'D';			// Command executed successfully
	 private byte WAITING 		= (byte)'W';			// Command under processing, for threaded version
	 private byte INVCMD		= (byte)'C';			// Invalid Command
	 private byte INVARG		= (byte)'A';			// Invalid input data
	 private byte INVBUFSIZE	= (byte)'B';			// Resulting data exceeds buffer size
	 private byte TIMEOUT		= (byte)'T';			// Time measurement timed out
	 private byte COMERR		= (byte)'S';			// Serial Communication error
	 private byte INVSIZE		= (byte)'Z';			// Size mismatch, result of capture


	 public UsbSerialPort port;
	 public int version;
	 public boolean connected = false;
	 public int commandStatus = 0, total_bins=1024;
	 public String message = new String();
	public Integer pos=0,splitting=50,samples=1024,points=0;
	public byte[] buffer = new byte[splitting*4 + 1];

	/*----------Constructor routine.  Load caldata?---------*/
	 public comlib() {
	 }

	 public comlib(UsbSerialPort globalport) {
		setPort(globalport);
	 }

	public void setPort(UsbSerialPort globalport){
		port = globalport;
		Log.d(TAG,"opened device");
		message = new String("Connected to CH340");
		connected=true;
		version = getVersion();
		if(version == VERSION_NUM){
			connected = true;
			Log.d(TAG,"Connected to device");
		}
		else{
			connected=false;
			Log.d(TAG, String.valueOf(version));
		}

	}

	/*--------------------retrieve the version number from the connected device------------*/
	public int getVersion(){
		if (systemBusy()) return 0;

		String version;
		try {
			port.setRTS(false);
			SystemClock.sleep(100);
			port.setRTS(true);
			SystemClock.sleep(250);
			sendByte(GET_VERSION);
			SystemClock.sleep(10);
			port.read(buffer,timeout);
			commandStatus = SUCCESS;
			return buffer[0]&0xFF;
		} catch (IOException e) {
			Log.e(TAG,"communication error");
			e.printStackTrace();
			connected = false;
			commandStatus = COMERR;
			return 0;
		}

	}


	/*--------------------retrieve the version number from the connected device------------*/
	public int readReg(int reg){
		if (systemBusy()) return 0;

		String version;
		try {
			port.write(new byte[] { (byte)(READB&0xFF), (byte) (reg&0xFF)}, 100);
			SystemClock.sleep(2);
			int bt = port.read(buffer, 100);
			if(bt==0){
				SystemClock.sleep(2);
				port.read(buffer, 1);
			}
			commandStatus = SUCCESS;
			return buffer[0]&0xFF;
		} catch (IOException e) {
			Log.e(TAG,"communication error");
			e.printStackTrace();
			commandStatus = COMERR;
			return 0;
		}

	}

    public boolean writeReg(int reg,int val){
		if (systemBusy()) return false;

        try {
			port.write(new byte[] { (byte)(WRITEB&0xFF), (byte) (reg),(byte) (val)}, 2);
            commandStatus = SUCCESS;
            return true;
        } catch (IOException e) {
            Log.e(TAG,"communication error");
            e.printStackTrace();
            commandStatus = COMERR;
            return false;
        }

    }


	public int readADC(int chan){
		int low=0,high=0,val=0;
		if (systemBusy()) return 0;
		try {
			port.write(new byte[] { (byte)(WRITEB&0xFF), (byte) (0x27),(byte) (64| (chan&0xF))}, 100); //ADCMUX, 64|chan
			port.write(new byte[] { (byte)(WRITEB&0xFF), (byte) (0x26),(byte) (197)}, 100); //ADCSRA,197
			SystemClock.sleep(1);
			commandStatus = SUCCESS;
			low = readReg(0x24);
			high = readReg(0x25);
            val = (high<<8)|low;
            if(val>1023)port.read(buffer,10); // error checking for extra byte
			return val;
		} catch (IOException e) {
			Log.e(TAG,"communication error");
            try {
                port.read(buffer,1);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
			commandStatus = COMERR;
			return 0;
		}

	}

	public List scanI2C(){
		List<Integer> arr = new ArrayList<Integer>(255);
		int got=0;
		if (systemBusy()) return arr;
		try {
			sendByte(SCANI2C);
			got = port.read(buffer,100);
			for(int i=0;i<got;i++){
				if( (buffer[i]&0xFF) <254)arr.add((int) buffer[i]&0xFF);
			}
			got = port.read(buffer,100);
			for(int i=0;i<got;i++){
				if((buffer[i]&0xFF) < 254)arr.add((int) buffer[i]&0xFF);
			}
			got = port.read(buffer,10);
			for(int i=0;i<got;i++){
				if( (buffer[i]&0xFF) < 254 )arr.add((int) buffer[i]&0xFF);
			}

			got = port.read(buffer,10);
			for(int i=0;i<got;i++){
				if( (buffer[i]&0xFF) < 254 )arr.add((int) buffer[i]&0xFF);
			}

			commandStatus = SUCCESS;
		} catch (IOException e) {
			Log.e(TAG,"communication error");
			e.printStackTrace();
			commandStatus = COMERR;
		}
		return arr;
	}


	public List readI2C(byte addr,byte reg,int numbytes){
		List<Integer> arr = new ArrayList<Integer>(255);
		int got=0,bt = 0,timeouts=0;
		if (systemBusy()) return arr;
		try {
			port.write(new byte[] { (byte)(READI2C&0xFF),addr,reg, (byte) numbytes}, 100);
			SystemClock.sleep(1);
			do{
				bt = port.read(buffer,100);
				if (bt==0)timeouts++;
				if(timeouts>5)break;
				for(int i=0;i<bt;i++,got++)arr.add((int) buffer[i]&0xFF);
			}while(got<numbytes);
			commandStatus = SUCCESS;
		} catch (IOException e) {
			Log.e(TAG,"communication error");
			e.printStackTrace();
			commandStatus = COMERR;
		}
		return arr;
	}


	public boolean writeI2C(byte addr,byte[] data){
		if (systemBusy()) return false;
		try {
			port.write(new byte[] { (byte)(WRITEI2C&0xFF),addr,(byte) data.length}, 50);
			port.write(data, 5);
			int bt = port.read(buffer, 10);
			commandStatus = SUCCESS;
			if (bt==0)return false;
			else return true;
		} catch (IOException e) {
			Log.e(TAG,"communication error");
			e.printStackTrace();
			commandStatus = COMERR;
		}
		return true;
	}



	public boolean close(){
		 try{
			 port.close();
			 connected=false;
			 return true;
		 }catch (IOException e){
			 Log.d(TAG,"Closed device!!");
		 }
		 return false;
	 }
	 
	public void setMessage(String msg){
		message = msg;
	}


	private void sendByte(Integer val) throws IOException{  // Sends lower byte of the received integer to the device
		if(!connected){
			throw new IOException("DEVICE NOT CONNECTED");
		}

		try {
			port.write(new byte[] {(byte) (val & 0xff)}, timeout);
		} catch (IOException e) {
			Log.e("ERROR","Failed to send data. check connections");
		}

		SystemClock.sleep(10);        // may not be required
	}

	private void sendInt(int val) throws IOException{		// Sends integer data as two bytes
		if(!connected){
			throw new IOException("DEVICE NOT CONNECTED");
		}

		try {
			port.write(new byte[] {(byte) (val & 0xff),(byte) ((val >> 8) & 0xff)}, timeout);
			Log.e("WRITE INT", String.valueOf(val));
		} catch (IOException e) {
			Log.e("INT FAILURE","Failed to send data. check connections");
			throw new IOException("DEVICE NOT CONNECTED");
		}

		SystemClock.sleep(10);        // may not be required
	}

	private boolean systemBusy(){
		if(commandStatus == WAITING ) {
			Log.d(TAG, "system busy"); return true;}
		else if(connected == false ) {Log.e(TAG, "Disconnected!"); commandStatus = COMERR; return true;}
		commandStatus = WAITING;
		return false;
	}


}
