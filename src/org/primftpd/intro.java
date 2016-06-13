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

		//인트로 시작
		Handler handler = new Handler();
		handler.postDelayed(new Runnable(){
			public void run(){
				Intent intent = new Intent(intro.this,PrimitiveFtpdActivity.class);
				startActivity(intent);
				finish();
			}
		},1030);
		if(!iswifi())
			Toast.makeText(this, "Wi-Fi에서만 작동합니다.", Toast.LENGTH_SHORT).show();
		else{
			Toast.makeText(this, "Wi-Fi를 사용중입니다.", Toast.LENGTH_SHORT).show();
		}
		//인트로 끝 

	}

	public boolean iswifi() { // wifi 연결 체크
		ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE); // 안드로이드
		boolean isWifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
				.isConnectedOrConnecting(); // wifi 연결 체크
		return isWifi;
	}
}
