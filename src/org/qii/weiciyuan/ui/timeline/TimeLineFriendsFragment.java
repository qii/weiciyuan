package org.qii.weiciyuan.ui.timeline;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.dao.TimeLineFriendsMsg;
import org.qii.weiciyuan.domain.TimeLineMsgList;
import org.qii.weiciyuan.ui.send.StatusNewActivity;

/**
 * Created with IntelliJ IDEA.
 * User: qii
 * Date: 12-7-29
 * Time: 下午12:03
 * To change this template use File | Settings | File Templates.
 */
public class TimeLineFriendsFragment extends TimeLineAbstractFragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        Bundle args = getArguments();

        View view = inflater.inflate(R.layout.timeline, container, false);
        listView = (ListView) view.findViewById(R.id.listView);
        timeLineAdapter = new TimeLineAdapter();
        listView.setAdapter(timeLineAdapter);
        listView.setOnItemLongClickListener(onItemLongClickListener);

        new TimeLineTask().execute();

        return view;
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return super.onContextItemSelected(item);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.friendstimelinefragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_new_weibo:

                startActivity(new Intent(getActivity(), StatusNewActivity.class));

                break;
            case R.id.menu_refresh_timeline:

                new TimeLineTask().execute();
                break;
        }
        return true;
    }

    AdapterView.OnItemLongClickListener onItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            view.setSelected(true);
            MyAlertDialogFragment.newInstance().setView(view).show(getFragmentManager(), "");

            return true;  //To change body of implemented methods use File | Settings | File Templates.
        }
    };

    static class MyAlertDialogFragment extends DialogFragment {

        View view;

        public static MyAlertDialogFragment newInstance() {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            frag.setRetainInstance(true);
            Bundle args = new Bundle();
            frag.setArguments(args);
            return frag;
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            view.setSelected(false);
        }

        public MyAlertDialogFragment setView(View view) {
            this.view = view;
            return this;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            String[] items = {getString(R.string.take_camera), getString(R.string.select_pic)};


            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.select))
                    .setItems(items, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            switch (which) {
                                case 0:

                                    break;
                                case 1:

                                    break;
                            }


                        }
                    });


            return builder.create();
        }
    }

    class TimeLineTask extends AsyncTask<Void, TimeLineMsgList, TimeLineMsgList> {

        DialogFragment dialogFragment = ProgressFragment.newInstance();

        @Override
        protected void onPreExecute() {
            dialogFragment.show(getFragmentManager(), "");
        }

        @Override
        protected TimeLineMsgList doInBackground(Void... params) {
            return new TimeLineFriendsMsg().getGSONMsgList();

        }

        @Override
        protected void onPostExecute(TimeLineMsgList o) {

            list = o;

            Toast.makeText(getActivity(), "" + list.getStatuses().size(), Toast.LENGTH_SHORT).show();

            dialogFragment.dismissAllowingStateLoss();

            timeLineAdapter.notifyDataSetChanged();

            super.onPostExecute(o);
        }
    }

    static class ProgressFragment extends DialogFragment {

        public static ProgressFragment newInstance() {
            ProgressFragment frag = new ProgressFragment();
            frag.setRetainInstance(true); //注意这句
            Bundle args = new Bundle();
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            ProgressDialog dialog = new ProgressDialog(getActivity());
            dialog.setMessage("刷新中");
            dialog.setIndeterminate(false);
            dialog.setCancelable(true);

            return dialog;
        }
    }

}

