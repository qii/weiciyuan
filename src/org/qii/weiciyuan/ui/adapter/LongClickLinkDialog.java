package org.qii.weiciyuan.ui.adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.utils.GlobalContext;
import org.qii.weiciyuan.support.utils.Utility;
import org.qii.weiciyuan.support.utils.WebBrowserSelector;
import org.qii.weiciyuan.ui.userinfo.UserInfoActivity;

/**
 * User: qii
 * Date: 13-3-26
 */
public class LongClickLinkDialog extends DialogFragment {


    private Uri uri;

    public LongClickLinkDialog() {

    }


    public LongClickLinkDialog(Uri uri) {
        this.uri = uri;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("uri", uri);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
            this.uri = savedInstanceState.getParcelable("uri");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        CharSequence[] strangerItems = {getString(R.string.open), getString(R.string.copy)};

        builder.setTitle(getStringContent())
                .setItems(strangerItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Context context = getActivity();
                                if (uri.getScheme().startsWith("http")) {
                                    String url = uri.toString();
                                    if (Utility.isWeiboAccountIdLink(url)) {
                                        Intent intent = new Intent(context, UserInfoActivity.class);
                                        intent.putExtra("id", Utility.getIdFromWeiboAccountLink(url));
                                        context.startActivity(intent);
                                    } else if (Utility.isWeiboAccountDomainLink(url)) {
                                        Intent intent = new Intent(context, UserInfoActivity.class);
                                        intent.putExtra("domain", Utility.getDomainFromWeiboAccountLink(url));
                                        context.startActivity(intent);
                                    } else {
                                        WebBrowserSelector.openLink(context, uri);
                                    }
                                } else {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                    intent.putExtra(Browser.EXTRA_APPLICATION_ID, context.getPackageName());
                                    context.startActivity(intent);
                                }
                                break;
                            case 1:
                                ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                cm.setPrimaryClip(ClipData.newPlainText("sinaweibo", getStringContent()));
                                Toast.makeText(GlobalContext.getInstance(), String.format(GlobalContext.getInstance().getString(R.string.have_copied), getStringContent()), Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });


        return builder.create();
    }

    private String getStringContent() {
        String d = uri.toString();
        String newValue = "";
        if (d.startsWith("org.qii.weiciyuan")) {
            int index = d.lastIndexOf("/");
            newValue = d.substring(index + 1);
        } else if (d.startsWith("http")) {
            newValue = d;
        }
        return newValue;
    }
}