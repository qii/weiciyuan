package org.qii.weiciyuan.support.http;


import android.content.Context;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.utils.GlobalContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * get the string meaning of those int value return from server
 */

public final class URLManager {
    private static final String URL_FORMAT = "http://%s:%s%s";
    private static Properties properties = new Properties();

    public final static String getUrl(String name) {
        try {
            if (properties.isEmpty()) {
                Context context = GlobalContext.getInstance();
                InputStream inputStream = context.getResources().openRawResource(R.raw.url);
                properties.load(inputStream);
            }
        } catch (IOException e) {
//            Debug.Log(e);
        }

        return properties.get(name).toString();
    }

//    /**
//	 * 获得HTTP全路径地址
//	 * 
//	 * @param urlContent
//	 * @return
//	 */
//	public static String getRealUrl(String urlContent) {
//		if(!GeneralUtils.validateString(urlContent))
//			return null;
//		if(!urlContent.startsWith("/")){
//			urlContent = "/" + urlContent;
//		}
//		
//		return String.format(URL_FORMAT, GlobalHttpSetting.remoteHost,
//                GlobalHttpSetting.remoteHostPort, urlContent);
//	}
}
