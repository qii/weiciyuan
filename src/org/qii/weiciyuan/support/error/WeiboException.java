package org.qii.weiciyuan.support.error;

import android.content.res.Resources;
import android.text.TextUtils;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.utils.GlobalContext;

/**
 * User: Jiang Qi
 * Date: 12-8-14
 */
public class WeiboException extends Exception {
    private String error;
    private int error_code;
    private String request;

    public String getError() {

        String name = "code" + error_code;
        int i = GlobalContext.getInstance().getResources()
                .getIdentifier(name, "string", GlobalContext.getInstance().getPackageName());
        String result = null;
        try {
            result = GlobalContext.getInstance().getString(i);
        } catch (Resources.NotFoundException e) {
            result = GlobalContext.getInstance().getString(R.string.unknown_error_error_code) + error_code;
        }
        if (!TextUtils.isEmpty(result)) {
            return result;
        }
        return error;
    }

    @Override
    public String getMessage() {
        return getError();
    }

    public void setError(String error) {
        this.error = error;
    }


    public void setError_code(int error_code) {
        this.error_code = error_code;
    }

    public int getError_code() {
        return error_code;
    }

}
