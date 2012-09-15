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
    //this error string is from sina weibo request return
    private String oriError;
    private int error_code;

    public String getError() {

        String result;

        if (!TextUtils.isEmpty(error)) {
            result = error;
        } else {

            String name = "code" + error_code;
            int i = GlobalContext.getInstance().getResources()
                    .getIdentifier(name, "string", GlobalContext.getInstance().getPackageName());

            try {
                result = GlobalContext.getInstance().getString(i);

            } catch (Resources.NotFoundException e) {

                if (!TextUtils.isEmpty(oriError)) {
                    result = oriError;
                } else {

                    result = GlobalContext.getInstance().getString(R.string.unknown_error_error_code) + error_code;
                }
            }
        }

        return result;
    }

    @Override
    public String getMessage() {
        return getError();
    }


    public void setError_code(int error_code) {
        this.error_code = error_code;
    }

    public int getError_code() {
        return error_code;
    }

    public WeiboException() {

    }

    public WeiboException(String detailMessage, Throwable throwable) {
        error = detailMessage;
    }


    public void setOriError(String oriError) {
        this.oriError = oriError;
    }

}
