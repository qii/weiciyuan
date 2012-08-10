package org.qii.weiciyuan.ui.timeline;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.dao.MentionsTimeLineMsgDao;
import org.qii.weiciyuan.support.database.DatabaseManager;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.ui.Abstract.IAccountInfo;
import org.qii.weiciyuan.ui.browser.BrowserWeiboMsgActivity;
import org.qii.weiciyuan.ui.main.AvatarBitmapWorkerTask;
import org.qii.weiciyuan.ui.main.MainTimeLineActivity;
import org.qii.weiciyuan.ui.main.PictureBitmapWorkerTask;

import java.util.Map;
import java.util.Set;

/**
 * User: qii
 * Date: 12-7-29
 * Time: 上午12:52
 */
public class MentionsTimeLineFragment extends AbstractTimeLineFragment {

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("bean", bean);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainTimeLineActivity) getActivity()).setHomeListView(listView);
        if (savedInstanceState != null) {
            bean = (MessageListBean) savedInstanceState.getSerializable("bean");
            timeLineAdapter.notifyDataSetChanged();

            if (bean.getStatuses().size() != 0) {
                footerView.findViewById(R.id.listview_footer).setVisibility(View.VISIBLE);
            }
        } else {
            new SimpleTask().execute();
        }

    }

    private class SimpleTask extends AsyncTask<Object, Object, Object> {

        @Override
        protected Object doInBackground(Object... params) {
            bean = DatabaseManager.getInstance().getRepostLineMsgList(((IAccountInfo) getActivity()).getAccount().getUid());
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            timeLineAdapter.notifyDataSetChanged();
            if (bean.getStatuses().size() != 0) {
                footerView.findViewById(R.id.listview_footer).setVisibility(View.VISIBLE);
            }
            super.onPostExecute(o);
        }
    }

    @Override
    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), BrowserWeiboMsgActivity.class);
        intent.putExtra("msg", bean.getStatuses().get(position));
        startActivity(intent);
    }


    @Override
    protected void listViewFooterViewClick(View view) {
        if (!isBusying) {

            new TimeLineGetOlderMsgListTask().execute();

        }
    }


    public void refresh() {
        Map<String, AvatarBitmapWorkerTask> avatarBitmapWorkerTaskHashMap = ((MainTimeLineActivity) getActivity()).getAvatarBitmapWorkerTaskHashMap();
        Map<String, PictureBitmapWorkerTask> pictureBitmapWorkerTaskMap = ((MainTimeLineActivity) getActivity()).getPictureBitmapWorkerTaskMap();


        new TimeLineGetNewMsgListTask().execute();
        Set<String> keys = avatarBitmapWorkerTaskHashMap.keySet();
        for (String key : keys) {
            avatarBitmapWorkerTaskHashMap.get(key).cancel(true);
            avatarBitmapWorkerTaskHashMap.remove(key);
        }

        Set<String> pKeys = pictureBitmapWorkerTaskMap.keySet();
        for (String pkey : pKeys) {
            pictureBitmapWorkerTaskMap.get(pkey).cancel(true);
            pictureBitmapWorkerTaskMap.remove(pkey);
        }

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.mentionstimelinefragment_menu, menu);
        menu.add("weibo dont have messages group api");

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.mentionstimelinefragment_refresh:

                refresh();

                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected MessageListBean getDoInBackgroundNewData() {
        MentionsTimeLineMsgDao dao = new MentionsTimeLineMsgDao(((MainTimeLineActivity) getActivity()).getToken());
        if (getList().getStatuses().size() > 0) {
            dao.setSince_id(getList().getStatuses().get(0).getId());
        }
        MessageListBean result = dao.getGSONMsgList();
        if (result != null) {
            if (result.getStatuses().size() < AppConfig.DEFAULT_MSG_NUMBERS) {
                DatabaseManager.getInstance().addRepostLineMsg(result, ((IAccountInfo) getActivity()).getAccount().getUid());
            } else {
                DatabaseManager.getInstance().replaceRepostLineMsg(result, ((IAccountInfo) getActivity()).getAccount().getUid());
            }
        }
        return result;
    }


    @Override
    protected MessageListBean getDoInBackgroundOldData() {
        MentionsTimeLineMsgDao dao = new MentionsTimeLineMsgDao(((MainTimeLineActivity) getActivity()).getToken());
        if (getList().getStatuses().size() > 0) {
            dao.setMax_id(getList().getStatuses().get(getList().getStatuses().size() - 1).getId());
        }
        MessageListBean result = dao.getGSONMsgList();

        return result;
    }


}

