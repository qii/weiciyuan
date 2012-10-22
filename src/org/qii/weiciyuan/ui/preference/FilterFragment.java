package org.qii.weiciyuan.ui.preference;

import android.app.ListFragment;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.*;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.database.DatabaseManager;
import org.qii.weiciyuan.support.lib.MyAsyncTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User: qii
 * Date: 12-9-21
 */
public class FilterFragment extends ListFragment {

    private BaseAdapter adapter;

    private DBTask task;

    private List<String> list = new ArrayList<String>();

    private RemoveFilterDBTask removeTask;


    @Override
    public void onDetach() {
        super.onDetach();
        if (task != null)
            task.cancel(true);

        if (removeTask != null)
            removeTask.cancel(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = new DraftAdapter();
        setListAdapter(adapter);
        if (task == null || task.getStatus() == MyAsyncTask.Status.FINISHED) {
            task = new DBTask();
            task.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);

        }
        getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        getListView().setMultiChoiceModeListener(new FilterMultiChoiceModeListener());
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ModifyFilterDialog dialog = new ModifyFilterDialog(list.get(position));
                dialog.setTargetFragment(FilterFragment.this,1);
                dialog.show(getFragmentManager(), "");
            }
        });

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.actionbar_menu_filterfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
                AddFilterDialog dialog = new AddFilterDialog();
                dialog.setTargetFragment(FilterFragment.this, 0);
                dialog.show(getFragmentManager(), "");
                break;

        }

        return true;
    }


    public void addFilter(String word) {
        DatabaseManager.getInstance().addFilterKeyword(word);
        if (task == null || task.getStatus() == MyAsyncTask.Status.FINISHED) {
            task = new DBTask();
            task.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);

        }
    }


    class FilterMultiChoiceModeListener implements AbsListView.MultiChoiceModeListener {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.contextual_menu_draftfragment, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_remove:
                    if (removeTask == null || removeTask.getStatus() == MyAsyncTask.Status.FINISHED) {
                        removeTask = new RemoveFilterDBTask();
                        removeTask.executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
                    }
                    mode.finish();
                    return true;
            }
            return false;
        }


        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            mode.setTitle(String.format(getString(R.string.have_selected), String.valueOf(getListView().getCheckedItemCount())));
            adapter.notifyDataSetChanged();
        }
    }

    private class RemoveFilterDBTask extends MyAsyncTask<Void, List<String>, List<String>> {

        Set<String> set = new HashSet<String>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            long[] ids = getListView().getCheckedItemIds();
            for (long id : ids) {
                set.add(list.get((int) id));
            }
        }

        @Override
        protected List<String> doInBackground(Void... params) {
            return DatabaseManager.getInstance().removeAndGetNewFilterList(set);
        }

        @Override
        protected void onPostExecute(List<String> result) {
            list = result;
            adapter.notifyDataSetChanged();
        }
    }

    class DBTask extends MyAsyncTask<Void, List<String>, List<String>> {

        @Override
        protected List<String> doInBackground(Void... params) {
            List<String> set = DatabaseManager.getInstance().getFilterList();

            return set;
        }

        @Override
        protected void onPostExecute(List<String> result) {
            super.onPostExecute(result);
            list = result;
            adapter.notifyDataSetChanged();
        }
    }

    class DraftAdapter extends BaseAdapter {

        int checkedBG;
        int defaultBG;

        public DraftAdapter() {
            defaultBG = getResources().getColor(R.color.transparent);

            int[] attrs = new int[]{R.attr.listview_checked_color};
            TypedArray ta = getActivity().obtainStyledAttributes(attrs);
            checkedBG = ta.getColor(0, 430);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = getActivity().getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false);
            TextView tv = (TextView) view;
            tv.setBackgroundColor(defaultBG);
            if (getListView().getCheckedItemPositions().get(position)) {
                tv.setBackgroundColor(checkedBG);
            }
            tv.setText(list.get(position));
            return view;
        }
    }
}
