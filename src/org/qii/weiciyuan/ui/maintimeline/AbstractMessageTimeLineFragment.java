package org.qii.weiciyuan.ui.maintimeline;

import android.os.Bundle;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.MessageListBean;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.userinfo.StatusesListAdapter;

/**
 * User: qii
 * Date: 12-7-29
 */
public abstract class AbstractMessageTimeLineFragment extends AbstractTimeLineFragment<MessageListBean> {


    protected void clearAndReplaceValue(MessageListBean value) {
        bean.getStatuses().clear();
        bean.getStatuses().addAll(value.getStatuses());
    }

    @Override
    protected void newMsgOnPostExecute(MessageListBean newValue) {
        if (newValue != null && getActivity() != null) {
            if (newValue.getStatuses().size() == 0) {
                Toast.makeText(getActivity(), getString(R.string.no_new_message), Toast.LENGTH_SHORT).show();
            } else if (newValue.getStatuses().size() > 0) {
                Toast.makeText(getActivity(), getString(R.string.total) + newValue.getStatuses().size() + getString(R.string.new_messages), Toast.LENGTH_SHORT).show();
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
        if (newValue != null) {
            Toast.makeText(getActivity(), getString(R.string.total) + newValue.getStatuses().size() + getString(R.string.old_messages), Toast.LENGTH_SHORT).show();

            getList().getStatuses().addAll(newValue.getStatuses().subList(1, newValue.getStatuses().size() - 1));

        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bean = new MessageListBean();
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    protected void buildListAdapter() {
        timeLineAdapter = new StatusesListAdapter(getActivity(), ((AbstractAppActivity) getActivity()).getCommander(), getList(), listView);
        listView.setAdapter(timeLineAdapter);
    }
}
