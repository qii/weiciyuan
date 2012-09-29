package org.qii.weiciyuan.ui.preference;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import org.qii.weiciyuan.R;

/**
 * User: qii
 * Date: 12-9-29
 */
public class AboutFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.about_pref);

    }



}
