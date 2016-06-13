package org.primftpd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

public class intro extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.intro);

		//��Ʈ�� ����
		Handler handler = new Handler();
		handler.postDelayed(new Runnable(){
			public void run(){
				Intent intent = new Intent(intro.this,PrimitiveFtpdActivity.class);
				startActivity(intent);
				finish();
			}
		},1030);
		if(!iswifi())
			Toast.makeText(this, "Wi-Fi������ �۵��մϴ�.", Toast.LENGTH_SHORT).show();
		else{
			Toast.makeText(this, "Wi-Fi�� ������Դϴ�.", Toast.LENGTH_SHORT).show();
		}
		//��Ʈ�� �� 

	}

	public boolean iswifi() { // wifi ���� üũ
		ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE); // �ȵ���̵�
		boolean isWifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
				.isConnectedOrConnecting(); // wifi ���� üũ
		return isWifi;
	}
}
