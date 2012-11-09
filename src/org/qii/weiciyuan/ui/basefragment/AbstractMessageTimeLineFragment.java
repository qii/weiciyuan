package org.qii.weiciyuan.ui.basefragment;

import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.ListBean;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.dao.destroy.DestroyStatusDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.lib.MyAsyncTask;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.ui.adapter.StatusListAdapter;
import org.qii.weiciyuan.ui.interfaces.AbstractAppActivity;
import org.qii.weiciyuan.ui.interfaces.IRemoveItem;
import org.qii.weiciyuan.ui.interfaces.IToken;
import org.qii.weiciyuan.ui.actionmenu.StatusSingleChoiceModeListener;

/**
 * User: qii
 * Date: 12-7-29
 */
public abstract class AbstractMessageTimeLineFragment extends AbstractTimeLineFragment<ListBean<MessageBean>> implements IRemoveItem {

    private RemoveTask removeTask;

    protected void showNewMsgToastMessage(ListBean<MessageBean> newValue) {
        if (newValue != null && getActivity() != null) {
            if (newValue.getSize() == 0) {
                Toast.makeText(getActivity(), getString(R.string.no_new_message), Toast.LENGTH_SHORT).show();
            } else if (newValue.getSize() > 0) {
                Toast.makeText(getActivity(), getString(R.string.total) + newValue.getSize() + getString(R.string.new_messages), Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void clearAndReplaceValue(ListBean<MessageBean> value) {
        bean.getItemList().clear();
        bean.getItemList().addAll(value.getItemList());
        bean.setTotal_number(value.getTotal_number());
    }


    @Override
    protected void newMsgOnPostExecute(ListBean<MessageBean> newValue) {
        if (newValue != null && getActivity() != null) {
            if (newValue.getSize() == 0) {

            } else if (newValue.getSize() > 0) {
//                Toast.makeText(getActivity(), getString(R.string.total) + newValue.getStatuses().size() + getString(R.string.new_messages), Toast.LENGTH_SHORT).show();
                if (newValue.getItemList().size() < AppConfig.DEFAULT_MSG_NUMBERS) {
                    //for speed, add old data after new data
                    newValue.getItemList().addAll(getList().getItemList());
                } else {
                    //null is flag means this position has some old messages which dont appear
                    if (getList().getSize() > 0) {
                        newValue.getItemList().add(null);
                    }
                    newValue.getItemList().addAll(getList().getItemList());
                }
                clearAndReplaceValue(newValue);
                timeLineAdapter.notifyDataSetChanged();
                getListView().setSelectionAfterHeaderView();

            }
        }
        afterGetNewMsg();
    }


    @Override
    protected void oldMsgOnPostExecute(ListBean<MessageBean> newValue) {
        if (newValue != null && newValue.getSize() > 1) {

            getList().getItemList().addAll(newValue.getItemList().subList(1, newValue.getSize()));
            getList().setTotal_number(newValue.getTotal_number());

        } else {
            Toast.makeText(getActivity(), getString(R.string.older_message_empty), Toast.LENGTH_SHORT).show();

        }

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bean = new MessageListBean();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                if (position - 1 < getList().getSize() && position - 1 >= 0 && timeLineAdapter.getItem(position - 1) != null) {
                    if (mActionMode != null) {
                        mActionMode.finish();
                        mActionMode = null;
                        getListView().setItemChecked(position, true);
                        timeLineAdapter.notifyDataSetChanged();
                        mActionMode = getActivity().startActionMode(new StatusSingleChoiceModeListener(getListView(), (StatusListAdapter) timeLineAdapter, AbstractMessageTimeLineFragment.this, bean.getItemList().get(position - 1)));
                        return true;
                    } else {
                        getListView().setItemChecked(position, true);
                        timeLineAdapter.notifyDataSetChanged();
                        mActionMode = getActivity().startActionMode(new StatusSingleChoiceModeListener(getListView(), (StatusListAdapter) timeLineAdapter, AbstractMessageTimeLineFragment.this, bean.getItemList().get(position - 1)));
                        return true;
                    }
                }
                return false;
            }
        }

        );
    }

    @Override
    protected void buildListAdapter() {
        timeLineAdapter = new StatusListAdapter(this, ((AbstractAppActivity) getActivity()).getCommander(), getList().getItemList(), getListView(), true);
        getListView().setAdapter(timeLineAdapter);
    }


    @Override
    public void removeItem(int position) {
        clearActionMode();
        if (removeTask == null || removeTask.getStatus() == MyAsyncTask.Status.FINISHED) {
            removeTask = new RemoveTask(((IToken) getActivity()).getToken(), bean.getItemList().get(position).getId(), position);
            removeTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void removeCancel() {
        clearActionMode();
    }


    class RemoveTask extends MyAsyncTask<Void, Void, Boolean> {

        String token;
        String id;
        int positon;
        WeiboException e;

        public RemoveTask(String token, String id, int positon) {
            this.token = token;
            this.id = id;
            this.positon = positon;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            DestroyStatusDao dao = new DestroyStatusDao(token, id);
            try {
                return dao.destroy();
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
                return false;
            }
        }

        @Override
        protected void onCancelled(Boolean aBoolean) {
            super.onCancelled(aBoolean);
            if (this.e != null) {
                Toast.makeText(getActivity(), e.getError(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                ((StatusListAdapter) timeLineAdapter).removeItem(positon);
            }
        }
    }
}
