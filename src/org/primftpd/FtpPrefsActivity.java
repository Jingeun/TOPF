package org.primftpd;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

public class FtpPrefsActivity extends PreferenceActivity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//�����׸�Ʈ�� ȭ�� ���� ( �ڹټҽ��� ���� )
		getFragmentManager().beginTransaction().replace(android.R.id.content,new FtpPrefsFragment()).commit();
		getActionBar().setDisplayHomeAsUpEnabled(true); // �׼ǹ��� ������ ���� ��� ��ư �����
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) // ��ư Ŭ���� ��ư�� �ش� ��Ƽ�� �ǽ�
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
