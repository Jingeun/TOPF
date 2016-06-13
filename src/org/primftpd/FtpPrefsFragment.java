package org.primftpd;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class FtpPrefsFragment extends PreferenceFragment
{
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences); // 환경설정 버튼클릭시 띄워지는 화면
	}
}
