package org.qii.weiciyuan.ui.interfaces;

import android.app.Fragment;
import android.os.Bundle;

/**
 * User: qii
 * Date: 12-12-30
 */
public class AbstractAppFragment extends Fragment {
    private boolean isFirstStartFlag = true;

    protected final int FIRST_TIME_START = 0; //when activity is first time start
    protected final int SCREEN_ROTATE = 1;    //when activity is destroyed and recreated because a configuration change, see setRetainInstance(boolean retain)
    protected final int ACTIVITY_DESTROY_AND_CREATE = 2;  //when activity is destroyed because memory is too low, recycled by android system

    protected int getCurrentState(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            isFirstStartFlag = false;
            return ACTIVITY_DESTROY_AND_CREATE;
        }


        if (!isFirstStartFlag) {
            return SCREEN_ROTATE;
        }

        isFirstStartFlag = false;
        return FIRST_TIME_START;
    }
}
