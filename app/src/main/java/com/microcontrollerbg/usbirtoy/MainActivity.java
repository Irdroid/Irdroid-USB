package com.microcontrollerbg.usbirtoy;

//import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
//import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
//import java.util.Map.Entry;
//import java.util.Enumeration;
//import java.util.Hashtable;
//import android.util.Log;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
//import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
//import android.app.AlertDialog;
//import android.app.AlertDialog.Builder;
//import android.app.Dialog;
//import android.content.Context;
//import android.content.ContextWrapper;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.pm.ActivityInfo;
//import android.content.res.Configuration;
//import android.graphics.Color;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
//import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
//import android.text.Html;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
//import android.text.method.ArrowKeyMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
//import android.util.Log;
//import android.text.util.Linkify;
//import android.util.Log;
//import android.view.ContextMenu;
//import android.view.ContextMenu.ContextMenuInfo;
//import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
//import android.view.MenuInflater;
import android.view.MenuItem;
//import android.view.MenuItem;
//import android.view.MenuItem.OnMenuItemClickListener;
//import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
//import android.view.ViewGroup;
//import android.view.ViewGroup.LayoutParams;
//import android.view.ViewGroup;
import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
//import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.microcontrollerbg.usbirtoy.lirc.ConfParser;
import com.microcontrollerbg.usbirtoy.lirc.Remote;


public class MainActivity extends AppCompatActivity {
	public static final int ID_TEXT_VIEW_LOG = 0;
	public static final int ID_TEXT_VIEW_BUTTON_ARRAY = 1;
	public static boolean record_mode;
	// System Related Variables
	private static MainActivity mainActivity;
	private static OutputStream logfile = null;
	private IrToy irToy;
	private boolean resume;
	public static boolean success;
	private static String remotename;
	private static String remotecode;
	public static String rec_cmd;
	boolean bIrToyInit = false;
	private HashMap<String, Remote> remotes = null;
	SharedPreferences mPrefs;
	// UI Related Variables
	ScrollView scrollViewMain;
	ScrollView scrollViewButtonArray;
	LinearLayout llVerticalMain;
	LinearLayout.LayoutParams lllpMain;
	LinearLayout llVerticalButtonArray;

	TextView textViewLog;
	Button buttonInit;
	Button buttonClear;

	TextView textviewBrowse;
	Button buttonBrowse;

	ArrayAdapter<String> arrayadapterRemoteName;
	Spinner spinnerRemoteName;
    public static final int PERMISSIONS_REQUEST_CODE = 0;
	TableLayout tablelayoutButtonArray;
	TextView textviewButtonArray;
	private int intBtnArrayColCnt = 2;
 public  static ArrayList<Long> list2;
	private Vibrator m_vibBtnPressed;
	private String m_strConfFilename = null;
	private String m_strConfFileDirectory= null;
	private static String m_strAppDirectory= null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		list2 = new ArrayList<Long>();
		File appFolder;
		record_mode=false;
		success=false;
		resume = false;
        Intent intent = getIntent();
        remotename="SM.conf";
        remotecode="POWERON";
        remotename=intent.getStringExtra("remotename");
        remotecode=intent.getStringExtra("remotecode");

        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                //	showError();


            } else {
                ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSIONS_REQUEST_CODE);
            }
        }




			// Internal Memory Of Phone
			appFolder = new File("/data/data/" + getPackageName() + File.separator + getString(R.string.app_name));


		// Save Application Folder
		try {
			m_strAppDirectory = appFolder.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Create Application Folder
		appFolder.mkdirs();

		// Create Preference If Virgin
		firstRunPreferences();

		llVerticalMain = new LinearLayout(this);
		llVerticalMain.setOrientation(LinearLayout.VERTICAL);

		scrollViewButtonArray = new ScrollView(this);

//		scrollViewMain = new ScrollView(this);
//		scrollViewMain.addView(llVerticalMain);
//		setContentView(scrollViewMain);
		setContentView(llVerticalMain);

		lllpMain = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
//		lllpMain.setMargins(5, 5, 5, 5);
		lllpMain.weight = 1f;

		
		textViewLog = new TextView(this);
		textViewLog.setId(ID_TEXT_VIEW_LOG);
		textViewLog.setHeight(200);
		textViewLog.setBackgroundColor(0xFFEFE4B0);
		textViewLog.setTextColor(0xFF000000);
		textViewLog.setTextSize(10);
//		textViewLog.setVerticalScrollBarEnabled(true);
//		textViewLog.setSelected(true);
//		textViewLog.setVisibility(View.GONE);
		textViewLog.setMovementMethod(ScrollingMovementMethod.getInstance());

		LinearLayout.LayoutParams lllpTextViewLog = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//		lllpTextViewLog.setMargins(5, 5, 5, 5);
//		lllpTextViewLog.weight = 1f;
		llVerticalMain.addView(textViewLog, lllpTextViewLog);

		LinearLayout llHorizontalInitClear = new LinearLayout(this);
		llHorizontalInitClear.setOrientation(LinearLayout.HORIZONTAL);

		buttonInit = new Button(this);
		buttonInit.setText(R.string.str_btn_init_ir);
		buttonInit.setOnClickListener(clickButtonInit);
		llHorizontalInitClear.addView(buttonInit, lllpMain);
		
		buttonClear = new Button(this);
		buttonClear.setText(R.string.str_btn_clear);
		buttonClear.setOnClickListener(clickButtonClear);
		llHorizontalInitClear.addView(buttonClear, lllpMain);

		llVerticalMain.addView(llHorizontalInitClear);
		
		// draw Recording Mode button
		{
			Switch buttonRecording = new Switch(this);
			buttonRecording.setTextOff(getString(R.string.recording_mode_off));
			buttonRecording.setTextOn(getString(R.string.recording_mode_on));
			buttonRecording.setOnCheckedChangeListener(checkButtonRecordingMode);
			
			TableRow tablerowButtonArray = new TableRow(this);
			LayoutParams layoutparamsButtonArray = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			tablerowButtonArray.setLayoutParams(layoutparamsButtonArray);
			tablerowButtonArray.addView(buttonRecording);
	
			LayoutParams tablerowlayoutparamsButton = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			tablerowlayoutparamsButton.gravity = Gravity.FILL_HORIZONTAL;
			
			TableLayout tablelayoutButtonArray = new TableLayout(this);
			tablelayoutButtonArray.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
			tablelayoutButtonArray.setGravity(Gravity.CENTER);
			tablelayoutButtonArray.addView(tablerowButtonArray, tablerowlayoutparamsButton);
		
			LinearLayout.LayoutParams lllpButtonArray = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			llVerticalMain.addView(tablelayoutButtonArray, lllpButtonArray);
		}

		// Draw A Line 
		View viewLine = new View(this);
		viewLine.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 3));
		viewLine.setBackgroundColor(Color.rgb(51, 181, 229));
		llVerticalMain.addView(viewLine);

		TextView textviewBrowseInst = new TextView(this);
		textviewBrowseInst.setText(R.string.str_tvw_browse_inst);
		llVerticalMain.addView(textviewBrowseInst);
		
		LinearLayout llHorizontalBrowse = new LinearLayout(this);
		llHorizontalBrowse.setOrientation(LinearLayout.HORIZONTAL);

		textviewBrowse = new TextView(this);
		textviewBrowse.setGravity(Gravity.CENTER_VERTICAL);
		textviewBrowse.setTextColor(0xFFFFFFFF);
		textviewBrowse.setLayoutParams(lllpMain);
		llHorizontalBrowse.addView(textviewBrowse);

		buttonBrowse = new Button(this);
		buttonBrowse.setText(R.string.str_btn_browse);
		buttonBrowse.setOnClickListener(clickButtonBrowse);
		LinearLayout.LayoutParams lllpBrowse = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//		lllpBrowse.setMargins(2, 2, 2, 2);
		llHorizontalBrowse.addView(buttonBrowse, lllpBrowse);

		llVerticalMain.addView(llHorizontalBrowse);

		LinearLayout llHorizontalSelRemote = new LinearLayout(this);
		llHorizontalSelRemote.setOrientation(LinearLayout.HORIZONTAL);
		
		TextView textviewRemoteName = new TextView(this);
		textviewRemoteName.setText(R.string.str_tvw_sel_rmt);
		LinearLayout.LayoutParams layoutParamsRemoteName = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		llHorizontalSelRemote.addView(textviewRemoteName, layoutParamsRemoteName);
//		llVerticalMain.addView(textviewRemoteName);

		// Initialize Adapter For Remote Name Spinner
		arrayadapterRemoteName = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item);
		arrayadapterRemoteName.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// Initialize Remote Name Spinner
		spinnerRemoteName = new Spinner(this);
//		spinnerRemoteName.setLayoutParams(lllpMain);
		spinnerRemoteName.setPrompt(getString(R.string.str_tvw_sel_rmt));
		spinnerRemoteName.setAdapter(arrayadapterRemoteName);
		spinnerRemoteName.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				// Re-draw Direct Button Name
				String strRemoteName = spinnerRemoteName.getSelectedItem().toString();
				log("spinnerRemoteName.setOnItemSelectedListener(), onItemSelected(), strRemoteName=" + strRemoteName);
				drawUiButtonArray(strRemoteName);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});

		LinearLayout.LayoutParams layoutParamsSelRemote = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		llHorizontalSelRemote.addView(spinnerRemoteName, layoutParamsSelRemote);
		llVerticalMain.addView(llHorizontalSelRemote);

		tablelayoutButtonArray = new TableLayout(this);
		tablelayoutButtonArray.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
		tablelayoutButtonArray.setGravity(Gravity.CENTER);

		textviewButtonArray = new TextView(this);
		textviewButtonArray.setId(ID_TEXT_VIEW_BUTTON_ARRAY);
		textviewButtonArray.setText(R.string.str_tvw_press_btn);

		m_vibBtnPressed = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
		this.remotes = new HashMap<String, Remote>();

		mainActivity = this;

		// Retrieve Previous Button Column Count Setting
		intBtnArrayColCnt = getButtonColCnt();

		// Retrieve Last Lirc Config File If Any
		m_strConfFilename = getLastLircConfFile();
		if(m_strConfFilename != null){
			parseLircFile(m_strConfFilename);
		}
		else{
			//drawUiAbout();
		}

    irToy = new IrToy(this);


		final Handler handler3 = new Handler();
		handler3.postDelayed(new Runnable() {
			@Override
			public void run() {
				//Do something after 100ms

				initIrToy();
				//irTo	}



			}
		}, 1000);
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				//Do something after 100ms



				System.out.println(remotename);
                System.out.println(remotecode);
                if(remotename!=null) {


					sendButton(remotename, remotecode);
				//	record_mode = true;
				//	irToy.initRecordingMode();


				}

			}
		}, 1500);

		final Handler handler5 = new Handler();
		handler5.postDelayed(new Runnable() {
			@Override
			public void run() {
				//Do something after 100ms
				System.out.println(remotename);
				System.out.println(remotecode);
				if(remotename!=null) {


					irToy.initConnectionMain(true);



				}



			}
		}, 2000);

		final Handler handler7 = new Handler();
		handler7.postDelayed(new Runnable() {
			@Override
			public void run() {
				//Do something after 100ms

				System.out.println(remotename);
				System.out.println(remotecode);
				if(remotename!=null) {


					irToy.Close();
					irToy=null;
					try {
						logfile.flush();
						logfile.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					logfile = null;




				}



			}
		}, 2200);
		final Handler handler6 = new Handler();
		handler6.postDelayed(new Runnable() {
			@Override
			public void run() {
				//Do something after 100ms

				System.out.println(remotename);
				System.out.println(remotecode);
				if(remotename!=null) {



				//	moveTaskToBack(true);
					android.os.Process.killProcess(android.os.Process.myPid());


				}



			}
		}, 2500);


		log("class MainActivity(), onCreate() done");
	}

	@Override
	protected void onDestroy() {
		irToy.Close();
        irToy=null;
		try {
			logfile.flush();
			logfile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logfile = null;

		super.onDestroy();
	}

	OnClickListener clickButtonBrowse = new OnClickListener() {
		@Override
		public void onClick(View view) {
			m_vibBtnPressed.vibrate(50);
			// Pop Up Dialog Browse File
			drawUiBrowseFileDialog();
		}
	};

	OnClickListener clickButtonInit = new OnClickListener() {
		@Override
		public void onClick(View view) {
			m_vibBtnPressed.vibrate(50);
			// Init
			irToy.reset();
		}
	};

	OnClickListener clickButtonClear = new OnClickListener() {
		@Override
		public void onClick(View view) {
			m_vibBtnPressed.vibrate(50);
			// Clear
			TextView textView = ((TextView)findViewById(ID_TEXT_VIEW_LOG));
			textView.setText("");
		}
	};

	OnClickListener clickButtonRemoteKey = new OnClickListener() {
		@Override
		public void onClick(View view) {
			m_vibBtnPressed.vibrate(50);
			if (bIrToyInit == false) {
				Toast.makeText(getApplicationContext(), R.string.str_fail_reinit, Toast.LENGTH_SHORT).show();
				log("clickButtonRemoteKey(), onClick(), " + getString(R.string.str_fail_reinit));
				return;
			}

			Button btnPressed = (Button)view;
			String strRemoteName = spinnerRemoteName.getSelectedItem().toString();
			String strButttonName = btnPressed.getText().toString();
			log("clickButtonRemoteKey(), onClick(), strRemoteName=" + strRemoteName + " strButttonName=" + strButttonName);
			sendButton(strRemoteName, strButttonName);
		}
	};
	
	CompoundButton.OnCheckedChangeListener checkButtonRecordingMode = new CompoundButton.OnCheckedChangeListener() {
	    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	    	if (!bIrToyInit) {
	    		// revert the checked state
	    		buttonView.setChecked(!isChecked);

				Toast.makeText(getApplicationContext(), R.string.str_fail_reinit, Toast.LENGTH_SHORT).show();
				log("checkButtonRecordingMode(), onCheckedChanged(), " + getString(R.string.str_fail_reinit));
				return;
			}
	    	
	        if (isChecked) {
	        	irToy.initRecordingMode();
	        	record_mode=true;
	        	Toast.makeText(getApplicationContext(), R.string.recording_mode_on, Toast.LENGTH_SHORT).show();
				log(getString(R.string.recording_mode_on));
	        } else {
				record_mode=false;
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				irToy.reset();
	        	
	        	Toast.makeText(getApplicationContext(), R.string.recording_mode_off, Toast.LENGTH_SHORT).show();
				log(getString(R.string.recording_mode_off));
	        }
	    }
	};

	/**
	* Setting Up Preferences Storage
	*/
	protected void onNewIntent(Intent intent) {
		if (intent != null)
			setIntent(intent);
		remotename=intent.getStringExtra("remotename");
		remotecode=intent.getStringExtra("remotecode");
	}

/*	public void onResume(){
		super.onResume();
		// put your code here...

if(resume) {

	record_mode=false;

	System.out.println("Resumed");


	final Handler handler = new Handler();
	handler.postDelayed(new Runnable() {
		@Override
		public void run() {
			//Do something after 100ms

			System.out.println(remotename);
			System.out.println(remotecode);
			if (remotename != null) {


				irToy.reset();


			}
		}
	}, 200);

	final Handler handler5 = new Handler();
	handler.postDelayed(new Runnable() {
		@Override
		public void run() {
			//Do something after 100ms
			//	initIrToy();
			System.out.println(remotename);
			System.out.println(remotecode);
			if (remotename != null) {

				sendButton(remotename, remotecode);

			}
		}
	}, 600);

	final Handler handler4 = new Handler();
	handler4.postDelayed(new Runnable() {
		@Override
		public void run() {
			//Do something after 100ms
			//	initIrToy();
			System.out.println(remotename);
			System.out.println(remotecode);
			if (remotename != null) {


			//	MainActivity.record_mode = true;
			//	irToy.initRecordingMode();
				irToy.mainmode();
				//moveTaskToBack(true);
				android.os.Process.killProcess(android.os.Process.myPid());

			}
		}
	}, 900);

}else{
	resume=true;
}

	}*/
	public void firstRunPreferences() {
		Context mContext = this.getApplicationContext();
		mPrefs = mContext.getSharedPreferences("usbIrToyPrefs", MODE_PRIVATE);
	}

	/**
	* Store The First Run
	*/
	public void setRunned() {
		SharedPreferences.Editor edit = mPrefs.edit();
		edit.putBoolean("firstRun", false);
		edit.commit();
	}

	/**
	* Get If This Is The First Run
	* @return True, If This Is The First Run
	*/
	public boolean getFirstRun() {
		return mPrefs.getBoolean("firstRun", true);
	}

	public String getLastLircConfFile() {
		return mPrefs.getString("LastLircConfFile", null);
	}

	public void setLastLircConfFile(String strFilename) {
		SharedPreferences.Editor edit = mPrefs.edit();
		edit.putString("LastLircConfFile", strFilename);
		edit.commit();
		log("setLastLircConfFile(), strFilename=" + strFilename);
	}

	/**
	* Get Button Column Count Setting 
	* @return int
	*/
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if ((keyCode == KeyEvent.KEYCODE_BACK))
		{
			//irToy.Close();

			//irToy=null;
			final Handler handler3 = new Handler();
			handler3.postDelayed(new Runnable() {
				@Override
				public void run() {
					irToy.initConnectionMain(true);





				}
			}, 500);

			final Handler handler4 = new Handler();
			handler4.postDelayed(new Runnable() {
				@Override
				public void run() {
					//Do something after 100ms
				//	irToy.Close();
					android.os.Process.killProcess(android.os.Process.myPid());



				}
			}, 1000);

		//	moveTaskToBack (true);

		}
        if ((keyCode == KeyEvent.KEYCODE_N))
        {
            if(remotename!=null) {


                sendButton(remotename, "POWERON");
                //	record_mode = true;
                //	irToy.initRecordingMode();


            }
        }


        if ((keyCode == KeyEvent.KEYCODE_F))
        {
            if(remotename!=null) {


                sendButton(remotename, "POWEROFF");
                //	record_mode = true;
                //	irToy.initRecordingMode();


            }
        }
		return super.onKeyDown(keyCode, event);
	}
	public int getButtonColCnt() {
		return mPrefs.getInt("buttonColCnt", 2);
	}

	/**
	* Store Button Column Count Setting
	*/
	public void setButtonColCnt(int iBtnColCnt) {
		SharedPreferences.Editor edit = mPrefs.edit();
		edit.putInt("buttonColCnt", iBtnColCnt);
		edit.commit();
		log("setButtonColCnt(), iBtnColCnt=" + iBtnColCnt);
	}

	public static void log(final String text) {
		mainActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				String logtext = "";
					SimpleDateFormat dateFormat = new SimpleDateFormat("kk:mm:ss");
					String datum = dateFormat.format(new Date());
					logtext = " [" + datum + "] " + text;
					logtext = logtext + "\r\n";
				try {
					if (logfile == null) {
						logfile = new FileOutputStream(m_strAppDirectory + "/usbirtoy.txt");
					}
					logfile.write(logtext.getBytes());
					logfile.flush();
				} catch (Exception e) {
					e.printStackTrace();
				}

				TextView textView = ((TextView) mainActivity.findViewById(ID_TEXT_VIEW_LOG));
				textView.setText(textView.getText() + logtext);

				Spannable sText = (Spannable) textView.getText();
				Selection.setSelection(sText, sText.length());
			}
		});
	}

	public void initIrToy() {
		try {
			if (irToy.init() == true){
				bIrToyInit = true;
				Toast.makeText(getApplicationContext(), R.string.str_init_ok, Toast.LENGTH_SHORT).show();
				log(getString(R.string.str_init_ok));
			}
			else{
				Toast.makeText(getApplicationContext(), R.string.str_init_fail, Toast.LENGTH_SHORT).show();
				log(getString(R.string.str_init_fail));
			}
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
			log(e.getMessage());
		}
	}

	public void parseLircFile(String strFilename) {
		// Update UI
		textviewBrowse.setText(strFilename);
	
		// Parse Lirc Conf File
		discoverRemotes(strFilename);
	}

	public void discoverRemotes(String strFilename) {
		File file = new File(strFilename);

		// Save Browsed Directory
		m_strConfFileDirectory = file.getParent();
		setLastLircConfFile(strFilename);
		
		try {
			InputStreamReader isr = new InputStreamReader(new FileInputStream(file));
			ConfParser parser = new ConfParser(isr);
			log("discoverRemotes(), new ConfParser(isr)");
			this.remotes.clear();
			for (Remote remote : parser.Parse()) {
				String strRemoteName = remote.getName();
				this.remotes.put(strRemoteName, remote);
				log("discoverRemotes(), Added to remotes, strRemoteName=" + strRemoteName);
			}


			// Re-Populate Spinner Remote Name
			updateRemoteNameSpinner();

			// Re-Draw Direct Button Name
			String strRemoteName = spinnerRemoteName.getSelectedItem().toString();
			drawUiButtonArray(strRemoteName);

		} catch (UnsupportedEncodingException e) {
			Toast.makeText(getApplicationContext(), "UnsupportedEncodingException while parsing lirc file", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			log("discoverRemotes(), UnsupportedEncodingException, " + e.getMessage());
		} catch (FileNotFoundException e) {
			Toast.makeText(getApplicationContext(), "FileNotFoundException while parsing lirc file", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			log("discoverRemotes(), FileNotFoundException, " + e.getMessage());
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "Exception while parsing lirc file", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			log("discoverRemotes(), Exception, " + e.getMessage());
		}
	}

	public void sendButton(String strRemoteName, String strButtonName) {
		String strSendCmd = "";
		String strButtonCode = "";
		Remote remote;

		remote = this.remotes.get(strRemoteName);
		strButtonCode = remote.getButtonsCode(strButtonName);
	//	log("sendButton(), strButtonName=" + strButtonName);
	//	log("sendButton(), strButtonCode=" + strButtonCode);
	//	Toast.makeText(getApplicationContext(), "Send [" + strButtonName + ", " + strButtonCode + "]", Toast.LENGTH_SHORT).show();

		 ArrayList<Long> arraylistRawCode = remote.playButton(strButtonName);
		for (long lTmp : arraylistRawCode){
			if(lTmp < 1397861) {

					lTmp = (long) (lTmp / 21.33);
				System.out.println(lTmp);
				String strHexRawCode = String.format("%04X", (0xFFFF & lTmp));
				strSendCmd += strHexRawCode.substring(0, 2) + " " + strHexRawCode.substring(2, 4) + " ";
			}
		}

		// Must Necessarily Conclude With 'ff ff'!
		strSendCmd += "FF FF";
		System.out.println("Send command:"+strSendCmd);
		irToy.sendCommandAsync(strSendCmd);
	}

	// Re-Populate Spinner Remote Name
	public void updateRemoteNameSpinner() {
		arrayadapterRemoteName.clear();
		for (String strRemoteName : this.remotes.keySet()) {
			arrayadapterRemoteName.add(strRemoteName);
		}
		log("updateRemoteNameSpinner() done.");
	}	

	public void drawUiBrowseFileDialog() {
		// Create FileOpenDialog And Register A Callback
		SimpleFileDialog FileOpenDialog =  new SimpleFileDialog(MainActivity.this, "FileOpen", new SimpleFileDialog.SimpleFileDialogListener() {
			@Override
			// The Code In This Function Will Be Executed When The Dialog Ok Button Is Pushed
			public void onChosenDir(String chosenDir) {
				m_strConfFilename = chosenDir;
				parseLircFile(m_strConfFilename);
			}
		});
	
		// Default Filename Using The Public Variable "Default_File_Name"
		FileOpenDialog.Default_File_Name = "";
		if (m_strConfFileDirectory == null){
			log("discoverRemotes(), m_strConfFileDirectory=" + m_strConfFileDirectory);
			FileOpenDialog.chooseFile_or_Dir();
		}
		else{
			log("LAST discoverRemotes(), m_strConfFileDirectory=" + m_strConfFileDirectory);
			FileOpenDialog.chooseFile_or_Dir(m_strConfFileDirectory);
		}
	}
	public void onBackPressed() {
	//	moveTaskToBack (true);
		final Handler handler3 = new Handler();
		handler3.postDelayed(new Runnable() {
			@Override
			public void run() {
				//Do something after 100ms

						//record_mode = true;
						irToy.mainmode();
			//	android.os.Process.killProcess(android.os.Process.myPid());




			}
		}, 500);
		final Handler handler4 = new Handler();
		handler4.postDelayed(new Runnable() {
			@Override
			public void run() {
				//Do something after 100ms

				//initIrToy();
				android.os.Process.killProcess(android.os.Process.myPid());
				//irTo	}



			}
		}, 1000);
	}
	public void drawUndrawUiTextViewLog() {
		if(textViewLog.getVisibility() == View.GONE) {
			textViewLog.setVisibility(View.VISIBLE);
			buttonClear.setVisibility(View.VISIBLE);
		}
		else{
			textViewLog.setVisibility(View.GONE);
			buttonClear.setVisibility(View.GONE);
		}
	}

	public void drawUiDialogChooseBtnColCnt() {
		AlertDialog.Builder alertdialogbuilderChooseBtnColCnt = new AlertDialog.Builder(this);
		alertdialogbuilderChooseBtnColCnt.setTitle(R.string.str_menu_button_col_cnt_title);
		CharSequence[] strBtnColCnt = {"1", "2", "3", "4", "5", "6"};
		alertdialogbuilderChooseBtnColCnt.setItems(strBtnColCnt, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				intBtnArrayColCnt = which + 1;
				setButtonColCnt(intBtnArrayColCnt);

				// Re-Populate Spinner Button Name
				String strRemoteName = spinnerRemoteName.getSelectedItem().toString();

				// Re-Draw Direct Button Name
				drawUiButtonArray(strRemoteName);
			}
		});
		alertdialogbuilderChooseBtnColCnt.show();
	}

	public void drawUiButtonArray(String strRemoteName) {
		Remote remote = this.remotes.get(strRemoteName);
		ArrayList<String> arraylistButtonName = remote.getButtonsNames();
		Iterator<String> iteratorButtonName = arraylistButtonName.iterator();
		log("drawUiButtonArray(), strRemoteName=" + strRemoteName);
		
		llVerticalMain.removeView(scrollViewButtonArray);

//		tablelayoutButtonArray.removeAllViewsInLayout();
		tablelayoutButtonArray.removeAllViews();
		scrollViewButtonArray.removeAllViews();
//		llVerticalMain.removeView(tablelayoutButtonArray);

//		llVerticalMain.removeViewInLayout(tablelayoutButtonArray);

		llVerticalMain.removeView(textviewButtonArray);
		llVerticalMain.addView(textviewButtonArray);


		String strButtonText;
		// Loop For Vertically Number Of Row Of Button
		while (iteratorButtonName.hasNext()){
			TableRow tablerowButtonArray = new TableRow(this);
			LayoutParams layoutparamsButtonArray = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
//			layoutparamsButtonArray.weight = 1f;
			tablerowButtonArray.setLayoutParams(layoutparamsButtonArray);
			// Loop For Horizontally Number Of Button
			for (int j = 0; j < intBtnArrayColCnt; j++) {
				Button btn = new Button(this);
				if(iteratorButtonName.hasNext()){
					strButtonText = iteratorButtonName.next();
					btn.setText(strButtonText);
					btn.setOnClickListener(clickButtonRemoteKey);
					tablerowButtonArray.addView(btn);
				}
			}

			TableRow.LayoutParams tablerowlayoutparamsButton = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
			tablerowlayoutparamsButton.gravity = Gravity.FILL_HORIZONTAL;
			tablelayoutButtonArray.addView(tablerowButtonArray, tablerowlayoutparamsButton);
		}

		LinearLayout.LayoutParams lllpButtonArray = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		scrollViewButtonArray.addView(tablelayoutButtonArray, lllpButtonArray);
		llVerticalMain.addView(scrollViewButtonArray, lllpButtonArray);
	}

	public String drawUiAbout(){
		String strVersionName = "";
		try {
			strVersionName = " Version " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		AlertDialog.Builder alertdialogbuilderAbout = new AlertDialog.Builder(this);
		String strTitle = getString(R.string.app_name) + strVersionName;
		alertdialogbuilderAbout.setTitle(strTitle)
								.setIcon(R.drawable.ic_launcher)
								.setMessage(R.string.str_about_info)
								.setCancelable(true)
								.setNegativeButton(R.string.str_about_dismiss, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
			}
		});
		
		AlertDialog alertdialogAboutAlert = alertdialogbuilderAbout.create();
		alertdialogAboutAlert.show();
		// Make The Textview Clickable. Must Be Called After show()
		((TextView)alertdialogAboutAlert.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());

		return null;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, R.string.str_menu_reset).setIcon(android.R.drawable.ic_lock_power_off);
		menu.add(0, 1, 0, R.string.str_menu_browse_lirc).setIcon(android.R.drawable.ic_menu_add);
		menu.add(0, 2, 0, R.string.str_menu_button_col_cnt).setIcon(android.R.drawable.ic_menu_add);
		menu.add(0, 3, 0, R.string.str_menu_viewhide_log).setIcon(android.R.drawable.ic_menu_search);
		menu.add(0, 4, 0, R.string.str_menu_about).setIcon(android.R.drawable.ic_menu_help);

		return true;
	}


	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case 0:
				initIrToy();
				break;
			case 1:
				drawUiBrowseFileDialog();
				break;
			case 2:
				drawUiDialogChooseBtnColCnt();
				break;
			case 3:
				drawUndrawUiTextViewLog();
				break;
			case 4:
				drawUiAbout();
				break;
		}
		return false;
	}
}

