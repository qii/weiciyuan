package org.qii.weiciyuan.ui.basefragment;

import android.os.Bundle;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.adapter.StatusesListAdapter;

/**
 * User: qii
 * Date: 12-7-29
 */
public abstract class AbstractMessageTimeLineFragment extends AbstractTimeLineFragment<MessageListBean> {

    protected void showNewMsgToastMessage(MessageListBean newValue) {
        if (newValue != null && getActivity() != null) {
            if (newValue.getStatuses().size() == 0) {
                Toast.makeText(getActivity(), getString(R.string.no_new_message), Toast.LENGTH_SHORT).show();
            } else if (newValue.getStatuses().size() > 0) {
                Toast.makeText(getActivity(), getString(R.string.total) + newValue.getStatuses().size() + getString(R.string.new_messages), Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void clearAndReplaceValue(MessageListBean value) {
        bean.getStatuses().clear();
        bean.getStatuses().addAll(value.getStatuses());
        bean.setTotal_number(value.getTotal_number());
    }

    @Override
    protected void newMsgOnPostExecute(MessageListBean newValue) {
        if (newValue != null && getActivity() != null) {
            if (newValue.getStatuses().size() == 0) {
//                Toast.makeText(getActivity(), getString(R.string.no_new_message), Toast.LENGTH_SHORT).show();
            } else if (newValue.getStatuses().size() > 0) {
//                Toast.makeText(getActivity(), getString(R.string.total) + newValue.getStatuses().size() + getString(R.string.new_messages), Toast.LENGTH_SHORT).show();
                if (newValue.getStatuses().size() < AppConfig.DEFAULT_MSG_NUMBERS) {
                    //for speed, add old data after new data
                    newValue.getStatuses().addAll(getList().getStatuses());
                }
                clearAndReplaceValue(newValue);
                timeLineAdapter.notifyDataSetChanged();
                listView.setSelectionAfterHeaderView();

            }
        }
        afterGetNewMsg();
    }

    @Override
    protected void oldMsgOnPostExecute(MessageListBean newValue) {
        if (newValue != null && newValue.getSize() > 1) {

            int index = newValue.getStatuses().size() - 1;

            if (index > 1) {

                getList().getStatuses().addAll(newValue.getStatuses().subList(1, index));
            }

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
    }

    @Override
    protected void buildListAdapter() {
        timeLineAdapter = new StatusesListAdapter(getActivity(), ((AbstractAppActivity) getActivity()).getCommander(), getList().getStatuses(), listView);
        listView.setAdapter(timeLineAdapter);
    }
}
