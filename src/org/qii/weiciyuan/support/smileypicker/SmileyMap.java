package org.qii.weiciyuan.support.smileypicker;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * User: qii
 * Date: 13-3-4
 * 42+6=48
 */
public class SmileyMap {
    private static SmileyMap instance = new SmileyMap();
    private Map<String, String> map = new LinkedHashMap<String, String>();

    private SmileyMap() {
        map.put("[挖鼻屎]", "kbsa_org.png");
        map.put("[泪]", "sada_org.png");
        map.put("[亲亲]", "qq_org.png");
        map.put("[晕]", "dizzya_org.png");
        map.put("[可爱]", "tza_org.png");
        map.put("[花心]", "hsa_org.png");
        map.put("[汗]", "sweata_org.png");
        map.put("[衰]", "cry.png");
        map.put("[偷笑]", "heia_org.png");
        map.put("[打哈欠]", "k_org.png");
        map.put("[睡觉]", "sleepa_org.png");
        map.put("[哼]", "hatea_org.png");
        map.put("[可怜]", "kl_org.png");
        map.put("[右哼哼]", "yhh_org.png");
        map.put("[酷]", "cool_org.png");
        map.put("[生病]", "sb_org.png");
        map.put("[馋嘴]", "cza_org.png");
        map.put("[害羞]", "shamea_org.png");
        map.put("[怒]", "angrya_org.png");
        map.put("[闭嘴]", "bz_org.png");
        map.put("[钱]", "money_org.png");
        map.put("[嘻嘻]", "tootha_org.png");
        map.put("[左哼哼]", "zhh_org.png");
        map.put("[委屈]", "wq_org.png");
        map.put("[鄙视]", "bs2_org.png");
        map.put("[吃惊]", "cj_org.png");
        map.put("[吐]", "t_org.png");
        map.put("[懒得理你]", "ldln_org.png");
        map.put("[思考]", "sk_org.png");
        map.put("[怒骂]", "nm_org.png");
        map.put("[哈哈]", "laugh.png");
        map.put("[抓狂]", "crazya_org.png");
        map.put("[抱抱]", "bba_org.png");
        map.put("[爱你]", "lovea_org.png");
        map.put("[鼓掌]", "gza_org.png");
        map.put("[悲伤]", "bs_org.png");
        map.put("[嘘]", "x_org.png");
        map.put("[呵呵]", "smilea_org.png");
        map.put("[感冒]", "gm.png");
        map.put("[黑线]", "hx.png");
        map.put("[愤怒]", "face335.png");
        map.put("[失望]", "face032.png");
        map.put("[做鬼脸]", "face290.png");
        map.put("[阴险]", "face105.png");
        map.put("[困]", "face059.png");
        map.put("[拜拜]", "face062.png");
        map.put("[疑问]", "face055.png");


        map.put("[赞]", "face329.png");
        map.put("[心]", "hearta_org.png");
        map.put("[伤心]", "unheart.png");
        map.put("[囧]", "j_org.png");
        map.put("[奥特曼]", "otm_org.png");
        map.put("[蜡烛]", "lazu_org.png");
        map.put("[蛋糕]", "cake.png");
        map.put("[弱]", "sad_org.png");
        map.put("[ok]", "ok_org.png");
        map.put("[威武]", "vw_org.png");
        map.put("[猪头]", "face281.png");
        map.put("[月亮]", "face18.png");
        map.put("[浮云]", "face229.png");
        map.put("[咖啡]", "face74.png");
        map.put("[爱心传递]", "face221.png");
        map.put("[来]", "face277.png");


        map.put("[熊猫]", "face002.png");
        map.put("[帅]", "face94.png");
        map.put("[不要]", "face274.png");
        map.put("[熊猫]", "face002.png");
    }

    public static SmileyMap getInstance() {
        return instance;
    }

    public Map<String, String> get() {
        return map;
    }
}
