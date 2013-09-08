package org.qii.weiciyuan.ui.preference.filter;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * User: qii
 * Date: 13-6-17
 */
public class CommonAppDefinedFilterList {
    public static Set<String> getDefinedFilterKeywordAndUserList() {
        Set<String> result = new LinkedHashSet<String>();
        Collections.addAll(result, "十二星座", "成功学", "不转不是中国人", "经典语录");
        Collections.addAll(result, "白羊座女人", "白羊座男人", "金牛座女人", "金牛座男人",
                "双子座女人", "双子座男人", "巨蟹座女人", "巨蟹座男人", "狮子座女人", "狮子座男人",
                "处女座女人", "处女座男人", "天秤座女人", "天秤座男人", "摩羯座女人", "摩羯座男人",
                "水瓶座女人", "水瓶座男人", "双鱼座女人", "双鱼座男人", "天蝎座女人", "天蝎座男人",
                "射手座女人", "射手座男人");
        Collections.addAll(result, "白羊女", "白羊男", "金牛女", "金牛男",
                "双子女", "双子男", "巨蟹女", "巨蟹男", "狮子女", "狮子男",
                "处女女", "处女男", "天秤女", "天秤男", "摩羯女", "摩羯男",
                "水瓶女", "水瓶男", "双鱼女", "双鱼男", "天蝎女", "天蝎男",
                "射手女", "射手男");
        return result;
    }

    public static Set<String> getDefinedFilterSourceList() {
        Set<String> result = new LinkedHashSet<String>();
        Collections.addAll(result, "皮皮时光机", "脉搏网", "在线求签网");
        return result;
    }
}
