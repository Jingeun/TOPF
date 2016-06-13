package org.primftpd;

import java.lang.ref.WeakReference;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.listener.ListenerFactory;
import org.primftpd.filesystem.AndroidFileSystemFactory;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Process;
import android.widget.Toast;

/**
 * FTP ���� ����
 */

public class FtpServerService extends Service
{
	public static final String BROADCAST_ACTION_COULD_NOT_START = "org.primftpd.CouldNotStartServer";

	protected static final int MSG_START = 1;
	protected static final int MSG_STOP = 2;

	private FtpServer ftpServer;
	private Looper serviceLooper;
	private ServiceHandler serviceHandler;
	private PrefsBean prefsBean;
	private WakeLock wakeLock;
	private NsdManager.RegistrationListener nsdRegistrationListener;

	/**
	 * FTP ������ ���� �� �ߴ��ϴ� �ڵ鷯
	 */
	private final static class ServiceHandler extends Handler{

		// WeakReference FtpserverService �� garbage collection �� �߻��ϸ� ������ ����
		private final WeakReference<FtpServerService> ftpServiceRef;

		public ServiceHandler(Looper looper, FtpServerService ftpService) { // Looper �����Ͽ� �޽��� ��ȯ
			super(looper);
			this.ftpServiceRef = new WeakReference<FtpServerService>(ftpService);
		}

		@Override
		public void handleMessage(Message msg) { // �޽���
			FtpServerService ftpService = ftpServiceRef.get();
			if (ftpService == null) {
				return;
			}

			int toDo = msg.arg1; // �޽��� �� ����
			if (toDo == MSG_START) {
				if (ftpService.ftpServer == null) {
					// �ٸ� ��ü���� IPv4 ��� ���� (����������)
					System.setProperty("java.net.preferIPv4Stack", "true");  
		    		ftpService.launchFtpServer();	
		    		if (ftpService.ftpServer != null) {
		    			ftpService.createStatusbarNotification(); // ����� ���¹ٿ� ��� �߰�
						PowerManager powerMgr =(PowerManager) ftpService.getSystemService(POWER_SERVICE);
						ftpService.wakeLock = powerMgr.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,"pFTPd");
						ftpService.wakeLock.acquire();

		    		} else {
		    			ftpService.stopSelf();

		    			// tell activity to update button states
		    			Intent intent = new Intent(BROADCAST_ACTION_COULD_NOT_START);
		    			ftpService.sendBroadcast(intent);
		    		}
				}

			} else if (toDo == MSG_STOP) {
				if (ftpService.ftpServer != null) {
					ftpService.ftpServer.stop();
					ftpService.ftpServer = null;
				}
				if (ftpService.ftpServer == null) {
					ftpService.removeStatusbarNotification();
				}
				if (ftpService.wakeLock != null) {
					ftpService.wakeLock.release();
					ftpService.wakeLock = null;
				}
				ftpService.stopSelf();
			}
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) { // onStartCommand �� ������ ���񽺷� ���� �ǹ��� ����
		return null;
	}

	@Override
	public void onCreate() { // ����
		HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
	    thread.start();
	    serviceLooper = thread.getLooper();
	    serviceHandler = new ServiceHandler(serviceLooper, this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null) { return START_REDELIVER_INTENT; }
		// get parameters
		Bundle extras = intent.getExtras(); // �Ķ���� ȹ��
		prefsBean = (PrefsBean)extras.get(PrimitiveFtpdActivity.EXTRA_PREFS_BEAN);
		
		// send start message
		Message msg = serviceHandler.obtainMessage(); // �޽��� ���� ����
		msg.arg1 = MSG_START;
		serviceHandler.sendMessage(msg);

		// we don't want the system to kill the ftp server
		//return START_NOT_STICKY;
		return START_STICKY;
	}

	@Override
	public void onDestroy() { // ����
		Message msg = serviceHandler.obtainMessage();
		msg.arg1 = MSG_STOP;
		serviceHandler.sendMessage(msg);
	}

	/**
	 * ����� ���¹� �κ� �ڵ�
	 */
	protected void createStatusbarNotification() {
		// create pending intent
		Intent notificationIntent = new Intent(this, PrimitiveFtpdActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		// create notification
		int icon = R.drawable.head;
		CharSequence tickerText = getText(R.string.serverRunning);
		CharSequence contentTitle = getText(R.string.notificationTitle);
		CharSequence contentText = tickerText;

		long when = System.currentTimeMillis();

		Notification notification = new Notification.Builder(getApplicationContext())
			.setTicker(tickerText)
			.setContentTitle(contentTitle)
			.setContentText(contentText)
			.setSmallIcon(icon)
			.setContentIntent(contentIntent)
			.setWhen(when)
			.build();
		// notification manager
		NotificationUtil.createStatusbarNotification(this, notification);
	}

	/**
	 * ���� ���� �κ�
	 */
	protected void removeStatusbarNotification() {
		NotificationUtil.removeStatusbarNotification(this);
	}

	/**
     * FTP ���� �۵� ����
     */
    protected void launchFtpServer() {
    	ListenerFactory listenerFactory = new ListenerFactory();	//�����Ͱ��� ����
    	listenerFactory.setPort(prefsBean.getPort());	//��Ʈ����

    	FtpServerFactory serverFactory = new FtpServerFactory();
    	serverFactory.addListener("default", listenerFactory.createListener());

    	// user manager & file system
    	serverFactory.setUserManager(new AndroidPrefsUserManager(prefsBean));
    	serverFactory.setFileSystem(new AndroidFileSystemFactory());

    	// do start server
    	ftpServer = serverFactory.createServer();
    	try {
    		ftpServer.start();
    	} catch (Exception e) {
    		// FuntimeExceptions ����ó��
			ftpServer = null;
			String msg = getText(R.string.serverCouldNotBeStarted).toString();
			msg += e.getLocalizedMessage();
			Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
		}
    }
}
