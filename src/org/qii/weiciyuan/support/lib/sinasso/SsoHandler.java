package org.qii.weiciyuan.support.lib.sinasso;

import android.app.Activity;
import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import com.sina.sso.RemoteSSO;
import org.qii.weiciyuan.dao.URLHelper;


/**
 * User: qii
 * Date: 13-6-18
 */
public class SsoHandler {
    private ServiceConnection conn = null;
    private static final int DEFAULT_AUTH_ACTIVITY_CODE = 32973;
    private static final String WEIBO_SIGNATURE = "30820295308201fea00302010202044b4ef1bf300d"
            + "06092a864886f70d010105050030818d310b300906035504061302434e3110300e0603550408130"
            + "74265694a696e673110300e060355040713074265694a696e67312c302a060355040a132353696e"
            + "612e436f6d20546563686e6f6c6f677920284368696e612920436f2e204c7464312c302a0603550"
            + "40b132353696e612e436f6d20546563686e6f6c6f677920284368696e612920436f2e204c746430"
            + "20170d3130303131343130323831355a180f32303630303130323130323831355a30818d310b300"
            + "906035504061302434e3110300e060355040813074265694a696e673110300e0603550407130742"
            + "65694a696e67312c302a060355040a132353696e612e436f6d20546563686e6f6c6f67792028436"
            + "8696e612920436f2e204c7464312c302a060355040b132353696e612e436f6d20546563686e6f6c"
            + "6f677920284368696e612920436f2e204c746430819f300d06092a864886f70d010101050003818"
            + "d00308189028181009d367115bc206c86c237bb56c8e9033111889b5691f051b28d1aa8e42b66b7"
            + "413657635b44786ea7e85d451a12a82a331fced99c48717922170b7fc9bc1040753c0d38b4cf2b2"
            + "2094b1df7c55705b0989441e75913a1a8bd2bc591aa729a1013c277c01c98cbec7da5ad7778b2fa"
            + "d62b85ac29ca28ced588638c98d6b7df5a130203010001300d06092a864886f70d0101050500038"
            + "181000ad4b4c4dec800bd8fd2991adfd70676fce8ba9692ae50475f60ec468d1b758a665e961a3a"
            + "edbece9fd4d7ce9295cd83f5f19dc441a065689d9820faedbb7c4a4c4635f5ba1293f6da4b72ed3"
            + "2fb8795f736a20c95cda776402099054fccefb4a1a558664ab8d637288feceba9508aa907fc1fe2"
            + "b1ae5a0dec954ed831c0bea4";

    private int mAuthActivityCode;
    private static String ssoPackageName = "";// "com.sina.weibo";
    private static String ssoActivityName = "";// "com.sina.weibo.MainTabActivity";
    private Activity mAuthActivity;
    private String[] authPermissions = {"friendships_groups_read", "friendships_groups_write"};


    public SsoHandler(Activity activity) {
        mAuthActivity = activity;
        conn = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {

            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                RemoteSSO remoteSSOservice = RemoteSSO.Stub
                        .asInterface(service);
                try {
                    ssoPackageName = remoteSSOservice.getPackageName();
                    ssoActivityName = remoteSSOservice.getActivityName();
                    boolean singleSignOnStarted = startSingleSignOn(
                            mAuthActivity, authPermissions,
                            mAuthActivityCode);

                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }
        };
    }


    public void authorize() {
        authorize(DEFAULT_AUTH_ACTIVITY_CODE);
    }

    private void authorize(
            int activityCode) {
        mAuthActivityCode = activityCode;

        boolean bindSucced = false;

        // Prefer single sign-on, where available.
        bindSucced = bindRemoteSSOService(mAuthActivity);

    }

    private boolean bindRemoteSSOService(Activity activity) {
        Context context = activity.getApplicationContext();
        Intent intent = new Intent("com.sina.weibo.remotessoservice");
        return context.bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    private boolean startSingleSignOn(Activity activity, String[] permissions, int activityCode) {
        boolean didSucceed = true;
        Intent intent = new Intent();
        intent.setClassName(ssoPackageName, ssoActivityName);
        intent.putExtra("appKey", URLHelper.APP_KEY);
        intent.putExtra("redirectUri", URLHelper.DIRECT_URL);

        if (permissions.length > 0) {
            intent.putExtra("scope", TextUtils.join(",", permissions));
        }

        // validate Signature
        if (!validateAppSignatureForIntent(activity, intent)) {
            return false;
        }

        try {
            activity.startActivityForResult(intent, activityCode);
        } catch (ActivityNotFoundException e) {
            didSucceed = false;
        }

        activity.getApplication().unbindService(conn);
        return didSucceed;
    }

    private boolean validateAppSignatureForIntent(Activity activity,
                                                  Intent intent) {
        ResolveInfo resolveInfo = activity.getPackageManager().resolveActivity(
                intent, 0);
        if (resolveInfo == null) {
            return false;
        }

        String packageName = resolveInfo.activityInfo.packageName;
        try {
            PackageInfo packageInfo = activity.getPackageManager()
                    .getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            for (Signature signature : packageInfo.signatures) {
                if (WEIBO_SIGNATURE.equals(signature.toCharsString())) {
                    return true;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }

        return false;
    }


}
