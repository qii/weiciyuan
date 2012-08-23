package org.qii.weiciyuan.bean;

import android.text.TextUtils;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.utils.GlobalContext;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * User: Jiang Qi
 * Date: 12-8-2
 * Time: 下午3:30
 */
public class CommentBean implements Serializable {
    private String created_at;
    private String id;
    private String text;
    private String source;
    private String mid;
    private UserBean user;
    private MessageBean status;

    public String getCreated_at() {
        if (!TextUtils.isEmpty(created_at)) {
            SimpleDateFormat format = new SimpleDateFormat("kk:mm");
            return format.format(new Date(created_at));
        }
        return "";
    }

    public String getListviewItemShowTime() {

           if (!TextUtils.isEmpty(created_at)) {
               Calendar cal = Calendar.getInstance();
               int nowMonth = cal.get(Calendar.MONTH) + 1;
               int nowDay = cal.get(Calendar.DAY_OF_MONTH);
               int nowHour = cal.get(Calendar.HOUR);
               int nowMinute = cal.get(Calendar.MINUTE);
               int nowSeconds = cal.get(Calendar.SECOND);


               Calendar messageCal = Calendar.getInstance();
               messageCal.setTime(new Date(created_at));
               int month = messageCal.get(Calendar.MONTH) + 1;
               int day = messageCal.get(Calendar.DAY_OF_MONTH);
               int hour = messageCal.get(Calendar.HOUR);
               int minute = messageCal.get(Calendar.MINUTE);
               int seconds = messageCal.get(Calendar.SECOND);

               if (nowMonth > month)
                   return "" + (nowMonth - month) + GlobalContext.getInstance().getString(R.string.month);

               if (nowDay > day)
                   return "" + (nowDay - day) + GlobalContext.getInstance().getString(R.string.day);

               if (nowHour > hour)
                   return "" + (nowHour - hour) + GlobalContext.getInstance().getString(R.string.hour);

               if (nowMinute > minute)
                   return "" + (nowMinute - minute) + GlobalContext.getInstance().getString(R.string.min);

               if (nowMinute == minute)
                   return GlobalContext.getInstance().getString(R.string.justnow);


               return "";
           }

           return "";
       }


    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public UserBean getUser() {
        return user;
    }

    public void setUser(UserBean user) {
        this.user = user;
    }

    public MessageBean getStatus() {
        return status;
    }

    public void setStatus(MessageBean status) {
        this.status = status;
    }
}
