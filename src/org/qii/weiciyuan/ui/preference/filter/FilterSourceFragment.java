package org.qii.weiciyuan.ui.preference.filter;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.support.database.FilterDBTask;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * User: qii
 * Date: 13-6-16
 */
public class FilterSourceFragment extends AbstractFilterFragment {


    @Override
    protected List<String> getDBDataImpl() {
        return FilterDBTask.getFilterKeywordList(FilterDBTask.TYPE_SOURCE);
    }

    @Override
    protected void addFilterImpl(Collection<String> set) {
        FilterDBTask.addFilterKeyword(FilterDBTask.TYPE_SOURCE, set);
    }

    @Override
    protected List<String> removeAndGetFilterListImpl(Collection<String> set) {
        return FilterDBTask.removeAndGetNewFilterKeywordList(FilterDBTask.TYPE_SOURCE, set);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.actionbar_menu_filterkeywordfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_common:
                Set<String> words = CommonAppDefinedFilterList.getDefinedFilterSourceList();
                words.removeAll(list);
                addFilter(words);
                return true;

        }

        return super.onOptionsItemSelected(item);
    }
}
