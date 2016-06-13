package org.primftpd;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

public class FtpPrefsActivity extends PreferenceActivity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//프래그먼트로 화면 구성 ( 자바소스로 구현 )
		getFragmentManager().beginTransaction().replace(android.R.id.content,new FtpPrefsFragment()).commit();
		getActionBar().setDisplayHomeAsUpEnabled(true); // 액션바의 아이콘 옆에 재생 버튼 만들기
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) // 버튼 클릭시 버튼에 해당 액티브 실시
	{
		super.onOptionsItemSelected(item);
		Intent intent = null;
		switch (item.getItemId()){
		case android.R.id.home:
			intent = new Intent(this, PrimitiveFtpdActivity.class);
			startActivity(intent);
			break;
		}
		return true;
	}
}
