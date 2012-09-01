package org.qii.weiciyuan.ui.maintimeline;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.dao.maintimeline.MainMentionsTimeLineDao;
import org.qii.weiciyuan.support.database.DatabaseManager;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.ui.Abstract.IAccountInfo;
import org.qii.weiciyuan.ui.browser.BrowserWeiboMsgActivity;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;

/**
 * User: qii
 * Date: 12-7-29
 */
public class MentionsTimeLineFragment extends AbstractMessageTimeLineFragment {

    private String[] group = new String[3];

    private String filter_by_author = "0";
    private String filter_by_type = "0";

    private int selected = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        group[0] = getString(R.string.all_people);
        group[1] = getString(R.string.all_following);
        group[2] = getString(R.string.original);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("bean", bean);
    }

    @Override
    protected void newMsgOnPostExecute(MessageListBean newValue) {
        showNewMsgToastMessage(newValue);
        super.newMsgOnPostExecute(newValue);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainTimeLineActivity) getActivity()).setMentionsListView(listView);
        if (savedInstanceState != null) {
            clearAndReplaceValue((MessageListBean) savedInstanceState.getSerializable("bean"));
            timeLineAdapter.notifyDataSetChanged();
            refreshLayout(bean);

        } else {
            new SimpleTask().execute();
        }

    }

    private class SimpleTask extends AsyncTask<Object, Object, Object> {

        @Override
        protected Object doInBackground(Object... params) {
            clearAndReplaceValue(DatabaseManager.getInstance().getRepostLineMsgList(((IAccountInfo) getActivity()).getAccount().getUid()));
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            timeLineAdapter.notifyDataSetChanged();
            refreshLayout(bean);
            super.onPostExecute(o);
        }
    }

    @Override
    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), BrowserWeiboMsgActivity.class);
        intent.putExtra("msg", bean.getStatuses().get(position));
        intent.putExtra("token", ((MainTimeLineActivity) getActivity()).getToken());
        startActivity(intent);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.mentionstimelinefragment_menu, menu);
        menu.findItem(R.id.mentionstimelinefragment_group).setTitle(group[selected]);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.mentionstimelinefragment_refresh:

                refresh();

                break;
            case R.id.mentionstimelinefragment_group:
                if (newTask == null || newTask.getStatus() == MyAsyncTask.Status.FINISHED) {
                    GroupDialog dialog = new GroupDialog();
                    dialog.show(getFragmentManager(), "");
                }

        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected MessageListBean getDoInBackgroundNewData() {
        MainMentionsTimeLineDao dao = new MainMentionsTimeLineDao(((MainTimeLineActivity) getActivity()).getToken());
        if (getList().getStatuses().size() > 0) {
            dao.setSince_id(getList().getStatuses().get(0).getId());
        }
        dao.setFilter_by_author(filter_by_author);
        dao.setFilter_by_type(filter_by_type);
        MessageListBean result = dao.getGSONMsgList();
        if (result != null && selected == 0) {
            if (result.getStatuses().size() < AppConfig.DEFAULT_MSG_NUMBERS) {
                DatabaseManager.getInstance().addRepostLineMsg(result, ((IAccountInfo) getActivity()).getAccount().getUid());
            } else {
                DatabaseManager.getInstance().replaceRepostLineMsg(result, ((IAccountInfo) getActivity()).getAccount().getUid());
            }
        }
        return result;
    }

    @Override
    protected void afterGetNewMsg() {
        getActivity().getActionBar().getTabAt(1).setText(getString(R.string.mentions));
        NotificationManager notificationManager = (NotificationManager) getActivity()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    @Override
    protected MessageListBean getDoInBackgroundOldData() {
        MainMentionsTimeLineDao dao = new MainMentionsTimeLineDao(((MainTimeLineActivity) getActivity()).getToken());
        if (getList().getStatuses().size() > 0) {
            dao.setMax_id(getList().getStatuses().get(getList().getStatuses().size() - 1).getId());
        }
        dao.setFilter_by_author(filter_by_author);
        dao.setFilter_by_type(filter_by_type);
        MessageListBean result = dao.getGSONMsgList();

        return result;
    }


    private class GroupDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.select_group));
            builder.setSingleChoiceItems(group, selected, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            filter_by_author = "0";
                            filter_by_type = "0";

                            break;
                        case 1:
                            filter_by_author = "1";
                            filter_by_type = "0";

                            break;
                        case 2:
                            filter_by_author = "0";
                            filter_by_type = "1";

                            break;
                    }
                    if (selected != which) {
                        selected = which;
                        getList().getStatuses().clear();
                        timeLineAdapter.notifyDataSetChanged();
                        refresh();
                        getActivity().invalidateOptionsMenu();
                    }
                    dismiss();
                }

            });


            return builder.create();
        }
    }
}

