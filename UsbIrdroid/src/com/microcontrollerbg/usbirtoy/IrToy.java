package com.microcontrollerbg.usbirtoy;

import java.util.LinkedList;
import java.util.Queue;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;

public class IrToy {
	
	// variables
	private FTDriver mSerial = null;
	private boolean ready = false;
	private boolean mUsbReceiverRegistered = false;
	private int SERIAL_BAUDRATE;
	private Activity activity;
	private Queue<byte[]> sendQueue = new LinkedList<byte[]>();
	
	private final int IRTOY_BUFFER_SIZE = 62;
	
	private final byte[] CMD_RESET = new byte[] { 0, 0, 0, 0, 0 };  	  // 5 mal 0x00
	private final byte[] CMD_SAMPLEMODE = new byte[] { 's' };       	  // 'c'
	private final byte[] CMD_TRANSMIT = getCommandBytes("03");      	  // 0x03
	private final byte[] CMD_BYTE_COUNT_REPORT = getCommandBytes("24");   // 0x24           
	private final byte[] CMD_NOTIFY_ON_COMPLETE = getCommandBytes("25");  // 0x25
	private final byte[] CMD_HANDSHAKE = getCommandBytes("26");			  // 0x26
	
	/*
	 * Public API
	 */

	/**
	 * constructor
	 */
	public IrToy(Activity acitivity) {
		this.activity = acitivity;
	}

	/**
	 * clean up
	 */
	public void Close() {
		if (mSerial != null) {
			mSerial.end();
			mSerial = null;
		}
		if (mUsbReceiverRegistered) {
			activity.unregisterReceiver(mUsbReceiver);
		}
	}
	
	/**
	 *  Initializes the connection. Returns whether the connection was successful.
	 */
	public boolean init() {
		return init(FTDriver.BAUD115200);
	}
	
	/**
	 * Initializes the connection. Returns whether the connection was successful.
	 */
	public boolean init(int baudrate) {
		if (ready)
			throw new RuntimeException("IrToy is ready and initialized.");

		if (mSerial == null) {
			MainActivity.log("IrToy, init(), SERIAL_BAUDRATE = " + baudrate);
			SERIAL_BAUDRATE = baudrate;
			mSerial = new FTDriver((UsbManager)activity.getSystemService(Context.USB_SERVICE));
			
			
			String intentName = activity.getApplicationInfo().packageName + ".USB_PERMISSION";
		//	MainActivity.log("intentName = " + intentName);
			System.out.println("intentName = " + intentName);
			PendingIntent permissionIntent = PendingIntent.getBroadcast(activity, 0, new Intent(intentName), 0);
		//	PendingIntent permissionIntent = PendingIntent.getBroadcast(activity, 0, new Intent(ACTION_USB_PERMISSION), 0);
			mSerial.setPermissionIntent(permissionIntent);
	
			// listen for new devices
			IntentFilter filter = new IntentFilter();
			filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
			filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
			activity.registerReceiver(mUsbReceiver, filter);
			mUsbReceiverRegistered = true;
		}
		
		if(mSerial.begin(SERIAL_BAUDRATE)) {
			initConnection();
			MainActivity.log("IrToy, init(), Success!");
			return true;
		} else {
			MainActivity.log("IrToy, init(), Fail!");
			return false;
		}
	}
	
	/**
	 * Resets the connection through, useful for unexpected errors.
	 */
	public void reset() {
		ready = false;
		initConnection();
	}
	
	/**
	 * Sends a command in the format '00 01 a3 ff ff 89 e8 bf ac '. He must necessarily conclude with 'ff ff'!
	 */
	public void sendCommandAsync(String command) {
		MainActivity.log("Command: " + command);
		sendInternal( getCommandBytes(command) );
	}
	
	/*
	 * internal methods
	 */
	
	private void initConnection() {
		if (ready) {
			throw new RuntimeException("IrToy is already ready (init was carried out again?)");
		}
		
		mSerial.write(CMD_RESET);
		MainActivity.log("sent reset");

		mSerial.write(CMD_SAMPLEMODE);
		MainActivity.log("Sent Sample Mode");
		readSampleMode();

		mSerial.write(CMD_BYTE_COUNT_REPORT);
		MainActivity.log("byte count report sent");
		
		mSerial.write(CMD_NOTIFY_ON_COMPLETE);
		MainActivity.log("notify on complete sent");
		
		mSerial.write(CMD_HANDSHAKE);
		MainActivity.log("handshake sent");
		
		ready = true;
	}

	private byte[] readAnswer() {
		// TODO Somehow better, or can this continue?
		byte[] buffer = new byte[4096];
		int len = mSerial.read(buffer);
		if (len == 0) {
			MainActivity.log("no reply");
			return new byte[0];
		}
		// Start debug
		String text = "";
		String bytes = "";
		String hex = "";
		for (int i = 0; i < len; i++) {
			text = text + (char)buffer[i];
			bytes = bytes + buffer[i] + " ";
			hex = hex + String.format("0x%02X", (buffer[i])) + " ";
		}
		MainActivity.log("Receive : " + hex + ", \"" + text + "\"");
//		MainActivity.log("Receive (byte): " + bytes);
//		MainActivity.log("Receive (hex) : " + hex);
		if (len ==6) {
			// 6x 0xFF --> error
			MainActivity.log("received ERROR");
		}
		// End debug
		
		byte[] reply = new byte[len];
		System.arraycopy(buffer, 0, reply, 0, len);
		return reply;
	}
	
	private void readSampleMode() {
		byte[] reply = readAnswer();
		String text = new String(reply);
		if (!text.equals("S01")) {
			throw new RuntimeException("Sample Mode has not responded with S01.");
		}
	}
	
	private void readHandshake() {
		byte[] reply = readAnswer();
		if (reply.length != 1 || reply[0] != IRTOY_BUFFER_SIZE) {
			throw new RuntimeException("The handshake has not responded as expected.");
		}
	}
	
	private void readTransmitCount(int expectedLength) {
		byte[] reply = readAnswer();
		if (reply.length != 3 || reply[0] != 't') {
			throw new RuntimeException("The transmit count has not responded as expected.");
		}
		int sendCount = reply[2] < 0 ? reply[2] + 256 : reply[2];
		int iterator = reply[1];
		for (int i = 0; i < iterator; i++) {
			expectedLength = expectedLength - 256;
		}
		if (sendCount != expectedLength) {
			throw new RuntimeException("The transmit count has not responded as expected:" + sendCount + "/" + expectedLength);
		}
	}
	
	private void readNotifyOnComplete() {
		byte[] reply = readAnswer();
		if (reply.length != 1 || reply[0] != 'C') {
			throw new RuntimeException("The Notify on complete has not responded as expected.");
		}
	}
	
	// Converts a command in hex String format into a byte array to
	private byte[] getCommandBytes(String cmd) {
		String[] partSplit = cmd.split(" ");
		byte bytes[] = new byte[partSplit.length];
		for (int i = 0; i < partSplit.length; i++) {
			bytes[i] = (byte) ((Character.digit(partSplit[i].charAt(0), 16) << 4) + Character.digit(partSplit[i].charAt(1), 16));
		}
		return bytes;
	}
	
	// Asynchronously sends a command
	private void sendInternal(final byte[] command) {
		if (!ready)
			throw new RuntimeException("IrToy is not ready (Init was successful?)");
		
		if (command[command.length-1] != -1 && command[command.length-2] != -1) {
			throw new RuntimeException("The command does not end with 'ff ff'.");
		}

		// Add command to the queue. The execution is asynchronous.
		sendQueue.add(command);

		new Thread(new Runnable() {
			@Override
			public void run() {
				sendCommandFromQueue();
			}
		}).start();
	}
	
	private synchronized void sendCommandFromQueue() {
		byte[] command = sendQueue.poll();
		try {
			internalSendCommand(command);
		} catch (Exception e) {
			MainActivity.log("Exception: " + e.getMessage());
			MainActivity.log("Hole remaining buffer from!");
			byte[] buffer;
			do {
				buffer = readAnswer();
			} while(buffer.length != 0);
			MainActivity.log("Send Fail.");
		}
	}
	
	private void internalSendCommand(byte[] command) {
		if (command != null) {
			mSerial.write(CMD_TRANSMIT);
			MainActivity.log("transmit sent");
			readHandshake();
			
			int iFull = command.length / IRTOY_BUFFER_SIZE;
			int iRest = command.length % IRTOY_BUFFER_SIZE;
			
			// Send Buffer Full
			for(int i = 0; i < iFull; i++) {
				byte[] buffer = new byte[IRTOY_BUFFER_SIZE];
				System.arraycopy(command, i * IRTOY_BUFFER_SIZE, buffer, 0, IRTOY_BUFFER_SIZE);
				mSerial.write(buffer);
//				MainActivity.log("fullbuffer sent: " + IRTOY_BUFFER_SIZE);
				MainActivity.log("fullbuffer sent: 0x3E");
				readHandshake();
			}
			
			// Send The Rest
			if (iRest != 0) {
				byte[] buffer = new byte[iRest];
				System.arraycopy(command, iFull * IRTOY_BUFFER_SIZE, buffer, 0, iRest);
	
				mSerial.write(buffer);
				MainActivity.log("remaining buffer sent: " + iRest);
				readHandshake();
			}
			
			readTransmitCount(command.length);
			
			readNotifyOnComplete();
			
			MainActivity.log("Commands sent successfully");
		}
	}

	// BroadcastReceiver when insert/remove the device USB plug into/from a USB port
	private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
        	MainActivity.log("mUsbReceiver onReceive");
			String action = intent.getAction();
			if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
    			MainActivity.log("mUsbReceiver usbAttached");
				mSerial.usbAttached(intent);
				mSerial.begin(SERIAL_BAUDRATE);
				initConnection();
			} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
    			MainActivity.log("mUsbReceiver usbDetached");
				ready = false;
				mSerial.usbDetached(intent);
				mSerial.end();
				mSerial = null;
			}
		}
	};

}
