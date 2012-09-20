package org.qii.weiciyuan.ui.preference;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.database.DatabaseManager;
import org.qii.weiciyuan.support.lib.MyAsyncTask;

import java.util.ArrayList;
import java.util.List;

/**
 * User: qii
 * Date: 12-9-21
 */
public class FilterFragment extends ListFragment  {

    ArrayAdapter<String> adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, new ArrayList<String>());
        setListAdapter(adapter);
        new DBTask().executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String word = adapter.getItem(position);
                List<String> newWordList = DatabaseManager.getInstance().removeAndGetNewFilterList(word);
                adapter.clear();
                adapter.addAll(newWordList);
                return true;
            }
        });
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.filterfragment_menu, menu);
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
        new DBTask().executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
    }

    class DBTask extends MyAsyncTask<Void, List<String>, List<String>> {

        @Override
        protected List<String> doInBackground(Void... params) {
            List<String> set = DatabaseManager.getInstance().getFilterList();

            return set;
        }

        @Override
        protected void onPostExecute(List<String> set) {
            super.onPostExecute(set);
            adapter.clear();
            adapter.addAll(set);
        }
    }
}
