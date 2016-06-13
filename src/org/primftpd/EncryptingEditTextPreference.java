package org.primftpd;

import org.primftpd.util.EncryptionUtil;
import org.primftpd.util.StringUtils;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

public class EncryptingEditTextPreference extends EditTextPreference{

	public EncryptingEditTextPreference(Context context){
		super(context);
	}

	public EncryptingEditTextPreference(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
	}

	public EncryptingEditTextPreference(Context context, AttributeSet attrs){
		super(context, attrs);
	}


	@Override
	public String getText(){ return ""; }

	@Override
	public void setText(String text){
		if (StringUtils.isBlank(text)) {
			super.setText(null);
			return;
		}
		super.setText(EncryptionUtil.encrypt(text));
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue){
		super.setText(restoreValue ? getPersistedString(null) : (String) defaultValue);
	}
}
