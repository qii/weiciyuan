package org.qii.weiciyuan.ui.discover;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.*;
import org.qii.weiciyuan.R;

/**
 * User: qii
 * Date: 12-9-19
 */
public class DiscoverFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.discoverfragment_layout, container, false);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.actionbar_menu_discoverfragment, menu);
    }


}
