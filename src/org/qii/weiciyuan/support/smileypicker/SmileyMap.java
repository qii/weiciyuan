package org.qii.weiciyuan.support.smileypicker;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * User: qii
 * Date: 13-3-4
 * 42+6=48
 */
public class SmileyMap {

    public static final int GENERAL_EMOTION_POSITION = 0;
    public static final int EMOJI_EMOTION_POSITION = 2;
    public static final int HUAHUA_EMOTION_POSITION = 1;

    private static SmileyMap instance = new SmileyMap();
    private Map<String, String> general = new LinkedHashMap<String, String>();
    private Map<String, String> huahua = new LinkedHashMap<String, String>();

    private SmileyMap() {

        /**
         * general emotion
         */
        general.put("[挖鼻屎]", "kbsa_org.png");
        general.put("[泪]", "sada_org.png");
        general.put("[亲亲]", "qq_org.png");
        general.put("[晕]", "dizzya_org.png");
        general.put("[可爱]", "tza_org.png");
        general.put("[花心]", "hsa_org.png");
        general.put("[汗]", "han.png");
        general.put("[衰]", "cry.png");
        general.put("[偷笑]", "heia_org.png");
        general.put("[打哈欠]", "k_org.png");
        general.put("[睡觉]", "sleepa_org.png");
        general.put("[哼]", "hatea_org.png");
        general.put("[可怜]", "kl_org.png");
        general.put("[右哼哼]", "yhh_org.png");
        general.put("[酷]", "cool_org.png");
        general.put("[生病]", "sb_org.png");
        general.put("[馋嘴]", "cza_org.png");
        general.put("[害羞]", "shamea_org.png");
        general.put("[怒]", "angrya_org.png");
        general.put("[闭嘴]", "bz_org.png");
        general.put("[钱]", "money_org.png");
        general.put("[嘻嘻]", "tootha_org.png");
        general.put("[左哼哼]", "zhh_org.png");
        general.put("[委屈]", "wq_org.png");
        general.put("[鄙视]", "bs2_org.png");
        general.put("[吃惊]", "cj_org.png");
        general.put("[吐]", "t_org.png");
        general.put("[懒得理你]", "ldln_org.png");
        general.put("[思考]", "sk_org.png");
        general.put("[怒骂]", "nm_org.png");
        general.put("[哈哈]", "laugh.png");
        general.put("[抓狂]", "crazya_org.png");
        general.put("[抱抱]", "bba_org.png");
        general.put("[爱你]", "lovea_org.png");
        general.put("[鼓掌]", "gza_org.png");
        general.put("[悲伤]", "bs_org.png");
        general.put("[嘘]", "x_org.png");
        general.put("[呵呵]", "smilea_org.png");
        general.put("[感冒]", "gm.png");
        general.put("[黑线]", "hx.png");
        general.put("[愤怒]", "face335.png");
        general.put("[失望]", "face032.png");
        general.put("[做鬼脸]", "face290.png");
        general.put("[阴险]", "face105.png");
        general.put("[困]", "face059.png");
        general.put("[拜拜]", "face062.png");
        general.put("[疑问]", "face055.png");

        general.put("[赞]", "face329.png");
        general.put("[心]", "hearta_org.png");
        general.put("[伤心]", "unheart.png");
        general.put("[囧]", "j_org.png");
        general.put("[奥特曼]", "otm_org.png");
        general.put("[蜡烛]", "lazu_org.png");
        general.put("[蛋糕]", "cake.png");
        general.put("[弱]", "sad_org.png");
        general.put("[ok]", "ok_org.png");
        general.put("[威武]", "vw_org.png");
        general.put("[猪头]", "face281.png");
        general.put("[月亮]", "face18.png");
        general.put("[浮云]", "face229.png");
        general.put("[咖啡]", "face74.png");
        general.put("[爱心传递]", "face221.png");
        general.put("[来]", "face277.png");

        general.put("[熊猫]", "face002.png");
        general.put("[帅]", "face94.png");
        general.put("[不要]", "face274.png");
        general.put("[熊猫]", "face002.png");

        /**
         * huahua emotion
         */
        huahua.put("[笑哈哈]", "lxh_xiaohaha.png");
        huahua.put("[好爱哦]", "lxh_haoaio.png");
        huahua.put("[噢耶]", "lxh_oye.png");
        huahua.put("[偷乐]", "lxh_toule.png");
        huahua.put("[泪流满面]", "lxh_leiliumanmian.png");
        huahua.put("[巨汗]", "lxh_juhan.png");
        huahua.put("[抠鼻屎]", "lxh_koubishi.png");
        huahua.put("[求关注]", "lxh_qiuguanzhu.png");
        huahua.put("[好喜欢]", "lxh_haoxihuan.png");
        huahua.put("[崩溃]", "lxh_bengkui.png");
        huahua.put("[好囧]", "lxh_haojiong.png");
        huahua.put("[震惊]", "lxh_zhenjing.png");
        huahua.put("[别烦我]", "lxh_biefanwo.png");
        huahua.put("[不好意思]", "lxh_buhaoyisi.png");
        huahua.put("[羞嗒嗒]", "lxh_xiudada.png");
        huahua.put("[得意地笑]", "lxh_deyidexiao.png");
        huahua.put("[纠结]", "lxh_jiujie.png");
        huahua.put("[给劲]", "lxh_feijin.png");
        huahua.put("[悲催]", "lxh_beicui.png");
        huahua.put("[甩甩手]", "lxh_shuaishuaishou.png");
        huahua.put("[好棒]", "lxh_haobang.png");
        huahua.put("[瞧瞧]", "lxh_qiaoqiao.png");
        huahua.put("[不想上班]", "lxh_buxiangshangban.png");
        huahua.put("[困死了]", "lxh_kunsile.png");
        huahua.put("[许愿]", "lxh_xuyuan.png");
        huahua.put("[丘比特]", "lxh_qiubite.png");
        huahua.put("[有鸭梨]", "lxh_youyali.png");
        huahua.put("[想一想]", "lxh_xiangyixiang.png");
        huahua.put("[躁狂症]", "lxh_kuangzaozheng.png");
        huahua.put("[转发]", "lxh_zhuanfa.png");
        huahua.put("[互相膜拜]", "lxh_xianghumobai.png");
        huahua.put("[雷锋]", "lxh_leifeng.png");
        huahua.put("[杰克逊]", "lxh_jiekexun.png");
        huahua.put("[玫瑰]", "lxh_meigui.png");
        huahua.put("[hold住]", "lxh_holdzhu.png");
        huahua.put("[群体围观]", "lxh_quntiweiguan.png");
        huahua.put("[推荐]", "lxh_tuijian.png");
        huahua.put("[赞啊]", "lxh_zana.png");
        huahua.put("[被电]", "lxh_beidian.png");
        huahua.put("[霹雳]", "lxh_pili.png");
    }

    public static SmileyMap getInstance() {
        return instance;
    }

    public Map<String, String> getGeneral() {
        return general;
    }

    public Map<String, String> getHuahua() {
        return huahua;
    }
}
