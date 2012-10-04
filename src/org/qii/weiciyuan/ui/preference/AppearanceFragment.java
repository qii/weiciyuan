package org.qii.weiciyuan.ui.preference;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import org.qii.weiciyuan.R;

/**
 * User: qii
 * Date: 12-10-4
 */
public class AppearanceFragment extends PreferenceFragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.appearance_pref);

        setRetainInstance(true);
    }


}
