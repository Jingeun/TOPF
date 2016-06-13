package org.primftpd;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.primftpd.util.StringUtils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity to display network info and to start FTP service.
 */
public class PrimitiveFtpdActivity extends Activity {
	
	private String C;
	
	//중지를 위해 수신 브로드캐스팅 객체 생성
	private BroadcastReceiver receiver = new BroadcastReceiver() { // 수신 브로드캐스팅
		@Override
		public void onReceive(Context context, Intent intent) {
			if (FtpServerService.BROADCAST_ACTION_COULD_NOT_START.equals(intent.getAction())) {
				updateButtonStates();
			}
		}
	};

	private BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			createIfaceTable();
		}
	};

	protected static final String SERVICE_CLASS_NAME = "org.primftpd.FtpServerService";
	public static final String EXTRA_PREFS_BEAN = "prefs.bean";

	private PrefsBean prefsBean;
	private String md5Fingerprint;
	private String sha1Fingerprint;

	//처음 Activity가 생성될때 호출
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (hasFocus) { updateButtonStates(); }
	}

	@Override
	protected void onStart() {
		super.onStart();
		loadPrefs();	//ID, PW, PORT생성
		createPortsTable();	//메인화면 테이
		createUsernameTable();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		// broadcast receiver to update buttons
		IntentFilter filter = new IntentFilter();
		filter.addAction(FtpServerService.BROADCAST_ACTION_COULD_NOT_START);
		this.registerReceiver(this.receiver, filter);

		// register listener to reprint interfaces table when network connections change
		filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(this.networkStateReceiver, filter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// unregister broadcast receivers
		this.unregisterReceiver(this.receiver);
		this.unregisterReceiver(this.networkStateReceiver);
	}

	/**
	 * Creates table containing network interfaces.
	 */
	protected void createIfaceTable() {
		TableLayout table = (TableLayout)findViewById(R.id.ifacesTable);

		// clear old entries
		table.removeAllViews();
		
		try {
			Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
			while (ifaces.hasMoreElements()) {
				NetworkInterface iface = ifaces.nextElement();
				String ifaceDispName = iface.getDisplayName();
				String ifaceName = iface.getName();
				Enumeration<InetAddress> inetAddrs = iface.getInetAddresses();

				while (inetAddrs.hasMoreElements()) {
					InetAddress inetAddr = inetAddrs.nextElement();
					String hostAddr = inetAddr.getHostAddress();

					if (inetAddr.isLoopbackAddress())
						continue; // 루프백(127.0.0.1)이아닐때 컨티뉴
					if (hostAddr.indexOf("::") >= 0)
						continue; // v6 출력하지 않게 컨티뉴
					if (ifaceDispName.indexOf("wlan") >= 0 && iswifi()){
						createTableRow(table, ifaceDispName, hostAddr); //아이피 출력
						C = hostAddr;
					}
				}

			}
		} catch (SocketException e) {
			String msg = getText(R.string.ifacesError) + e.getLocalizedMessage();
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
	}

	public boolean iswifi() { // wifi 연결 체크
		ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE); // 안드로이드
		boolean isWifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting(); // wifi 연결 체크
		return isWifi;
	}

	protected void createTableRow(TableLayout table, CharSequence label, CharSequence value){
		TableRow row = new TableRow(table.getContext());
		table.addView(row);
		row.setPadding(1, 1, 1, 5);

		TextView valueView = new TextView(row.getContext());
		row.addView(valueView);

		LayoutParams params = new LayoutParams();
		params.height = LayoutParams.WRAP_CONTENT;

		valueView.setLayoutParams(params);
		valueView.setGravity(Gravity.LEFT);
	}

	/*** Creates UI table showing ports. */
	protected void createPortsTable() {
		TableLayout table = (TableLayout)findViewById(R.id.portsTable);

		// clear old entries
		table.removeAllViews();
	}

	protected void createUsernameTable() {
		TableLayout table = (TableLayout)findViewById(R.id.usernameTable);

		// clear old entries
		table.removeAllViews();
	}

	protected void createFingerprintTable() {
		// note: HTML required for line breaks
		TableLayout table = (TableLayout)findViewById(R.id.fingerprintsTable);
		createTableRow(table,"MD5",Html.fromHtml(md5Fingerprint)); // MD5
		createTableRow(table,"SHA1",Html.fromHtml(sha1Fingerprint)); // 
	}

	/*** @return True if {@link FtpServerService} is running. */
	protected boolean checkServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		List<RunningServiceInfo> runningServices = manager.getRunningServices(Integer.MAX_VALUE);
		for (RunningServiceInfo service : runningServices) {
			if (SERVICE_CLASS_NAME.equals(service.service.getClassName())) { return true; }
		}
		return false;
	}

	/*** Updates enabled state of start/stop buttons. */

	protected void updateButtonStates() { // ftp  시작과 중지
		if (startIcon == null || stopIcon == null) return;
		boolean serviceRunning = checkServiceRunning();
		startIcon.setVisible(!serviceRunning);
		stopIcon.setVisible(serviceRunning);		
		// remove status bar notification if server not running
		if (!serviceRunning) NotificationUtil.removeStatusbarNotification(this);
	}

	//와이파이 버튼 생성//
	protected MenuItem startIcon;
	protected MenuItem stopIcon;
	ImageButton onbt;
	ImageButton offbt;

	
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.pftpd, menu);
		startIcon = menu.findItem(R.id.menu_start); //시작버튼, 메뉴
		stopIcon = menu.findItem(R.id.menu_stop);
		onbt = (ImageButton) findViewById(R.id.on);
		offbt = (ImageButton) findViewById(R.id.off);

		// to avoid icon flicker when invoked via notification
		updateButtonStates();
		return true;
	}

	//information
	public void infor(CharSequence value, boolean state){
		if(state){
			ArrayList<String> arraylist = new ArrayList<String>();

			arraylist.add("Address : ftp://"+(String)value+":"+prefsBean.getPortStr());
			arraylist.add("ID (Name) : "+prefsBean.getUserName());
			arraylist.add("PassWord : "+prefsBean.getPassword());
			arraylist.add("Directory : "+prefsBean.getDirectory());
			
			ArrayAdapter<String> Adapter;
			Adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arraylist);

			ListView list = (ListView)findViewById(R.id.listView);
			list.setAdapter(Adapter);
		}
	}


	//와이파이 onclick 액션
	public void Onclick0(View v){
		if(iswifi()){
			handleStart(startIcon, stopIcon);
			infor(C,true);	//리스트뷰
		}
		else {
			Toast toast = Toast.makeText(this, "Wi-Fi에서만 사용 가능합니다.",Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER_HORIZONTAL| Gravity.CENTER_VERTICAL, 0, 0);
			toast.show();
		}
	}

	public void Onclick1(View v){
		handleStop(startIcon, stopIcon);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_start:
			if (iswifi()){
				handleStart(startIcon, stopIcon);
				infor(C,true);
			}else {
				Toast toast = Toast.makeText(this, "Wi-Fi에서만 사용 가능합니다.",Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
				toast.show();
			}
			break;
		case R.id.menu_stop:
			handleStop(startIcon, stopIcon);
			break;
		case R.id.menu_prefs:
			handlePrefs();
			break;
		}

		updateButtonStates();
		return super.onOptionsItemSelected(item);
	}

	protected void handleStart(MenuItem startIcon, MenuItem stopIcon){
		if (StringUtils.isBlank(prefsBean.getPassword())){
			Toast.makeText(getApplicationContext(), R.string.haveToSetPassword, Toast.LENGTH_LONG).show();
		} else {
			offbt.setVisibility(View.GONE);
			onbt.setVisibility(View.VISIBLE);
			startIcon.setVisible(false);
			stopIcon.setVisible(true);
			Intent intent = createFtpServiceIntent();
			startService(intent);
		}
	}

	protected void handleStop(MenuItem startIcon, MenuItem stopIcon) {
		Intent intent = createFtpServiceIntent();
		stopService(intent);
		startIcon.setVisible(true);
		stopIcon.setVisible(false);
		onbt.setVisibility(View.GONE);
		offbt.setVisibility(View.VISIBLE);
	}

	protected void handlePrefs() {
		Intent intent = new Intent(this, FtpPrefsActivity.class);
		startActivity(intent);
	}

	/*** @return Intent to start/stop {@link FtpServerService}. */
	protected Intent createFtpServiceIntent() {
		Intent intent = new Intent(this, FtpServerService.class);
		intent.putExtra(EXTRA_PREFS_BEAN, prefsBean);
		return intent;
	}

	private static final int PORT_DEFAULT_VAL = 12345;		//초기 port값
	private static final String PORT_DEFAULT_VAL_STR = String.valueOf(PORT_DEFAULT_VAL);
	
	private static final int SSL_PORT_DEFAULT_VAL = 1234;
	@SuppressWarnings("unused") // XXX SSL
	private static final String SSL_PORT_DEFAULT_VAL_STR = String.valueOf(SSL_PORT_DEFAULT_VAL);

	/*** @return Android {@link SharedPreferences} object. */
	// return key 해당 값
	protected SharedPreferences getPrefs() {
		return PreferenceManager.getDefaultSharedPreferences(getBaseContext());
	}
	
	public static final String PREF_KEY_USER = "userNamePref";
	public static final String PREF_KEY_PASSWORD = "passwordPref";
	public static final String PREF_KEY_PORT = "portPref";
	public static final String PREF_KEY_SSL_PORT = "sslPortPref";
	public static final String PREF_KEY_DIRECTORY = "directoryPref";
	public static final String PREF_KEY_ANNOUNCE = "announcePref";

	public String directory123;
	
	/*** Loads preferences and stores in member {@link #prefsBean}. */
	protected void loadPrefs() {

		SharedPreferences prefs = getPrefs();
		String userName = prefs.getString(PREF_KEY_USER, "TOPF");
		// load password
		String password = prefs.getString(PREF_KEY_PASSWORD, null);
		// load announcement setting
		String directory123 = prefs.getString(PREF_KEY_DIRECTORY, "/");

		// load port
		int port = loadAndValidatePort(prefs,PREF_KEY_PORT,PORT_DEFAULT_VAL,PORT_DEFAULT_VAL_STR);
		
		// create prefsBean
		PrefsBean oldPrefs = prefsBean;
		prefsBean = new PrefsBean(userName, password, port,directory123);

		// TODO oldPrefs is null when user navigates via action bar,
		// find other way to figure out if prefs have changed
		if (oldPrefs != null) {
			if (!oldPrefs.equals(prefsBean) && checkServiceRunning()) {
				Toast.makeText(
						getApplicationContext(),
						R.string.restartServer,
						Toast.LENGTH_LONG).show();
			}
		}
	}

	protected int loadAndValidatePort(SharedPreferences prefs, String prefsKey, int defaultVal, String defaultValStr){
		// load port
		int port = defaultVal;
		String portStr = prefs.getString(prefsKey, defaultValStr);
		try {
			port = Integer.valueOf(portStr);
		} catch (NumberFormatException e) {
		}

		// validate port
		// I would prefer to do this in a prefsChangeListener, but that seems not to work
		if (!validatePort(port)) {
			Toast.makeText(getApplicationContext(),	R.string.portInvalid, Toast.LENGTH_LONG).show();
			port = defaultVal;
			Editor prefsEditor = prefs.edit();
			prefsEditor.putString(prefsKey, defaultValStr); 
			prefsEditor.commit();
		}
		return port;
	}

	/*** @param port * @return True if port is valid, false if invalid. */
	protected boolean validatePort(int port) {
		if (port > 1024 && port < 64000) {
			return true;
		}
		return false;
	}
}
