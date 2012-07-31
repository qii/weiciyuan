package org.qii.weiciyuan.ui.timeline;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.*;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.TimeLineMsgList;

/**
 * Created with IntelliJ IDEA.
 * User: qii
 * Date: 12-7-29
 * Time: 下午12:03
 * To change this template use File | Settings | File Templates.
 */
public class FriendsTimeLineFragment extends AbstractTimeLineFragment {

    private Commander commander;

    public static interface Commander {
        public void getNewFriendsTimeLineMsg();

        public void replayTo(int position);

        public void newWeibo();
    }

    public FriendsTimeLineFragment setCommander(Commander commander) {
        this.commander = commander;
        return this;
    }

    @Override
    protected TimeLineMsgList getList() {
        return activity.getHomeList();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_listview_layout, container, false);
        listView = (ListView) view.findViewById(R.id.listView);
        timeLineAdapter = new TimeLineAdapter();
        listView.setAdapter(timeLineAdapter);
        listView.setOnItemLongClickListener(onItemLongClickListener);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                activity.setHomelist_position(firstVisibleItem);
            }
        });
        return view;
    }

    public void refresh() {
        timeLineAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.friendstimelinefragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.friendstimelinefragment_new_weibo:
                commander.newWeibo();
                break;
            case R.id.friendstimelinefragment_refresh:
                commander.getNewFriendsTimeLineMsg();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    AdapterView.OnItemLongClickListener onItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            view.setSelected(true);
            new MyAlertDialogFragment().setView(view).setPosition(position).show(getFragmentManager(), "");

            return true;
        }
    };

    class MyAlertDialogFragment extends DialogFragment {
        View view;
        int position;

        @Override
        public void onCancel(DialogInterface dialog) {
            view.setSelected(false);
        }

        public MyAlertDialogFragment setView(View view) {
            this.view = view;
            return this;
        }

        public MyAlertDialogFragment setPosition(int position) {
            this.position = position;
            return this;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            String[] items = {"刷新", "回复"};

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.select))
                    .setItems(items, onClickListener);

            return builder.create();
        }

        DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (which) {
                    case 0:
                        commander.getNewFriendsTimeLineMsg();
                        break;
                    case 1:
                        commander.replayTo(position);
                        break;
                }
            }
        };
    }


}

