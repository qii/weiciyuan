package org.qii.weiciyuan.ui.preference.filter;

import org.qii.weiciyuan.support.database.FilterDBTask;

import java.util.Collection;
import java.util.List;

/**
 * User: qii
 * Date: 13-6-16
 */
public class FilterUserFragment extends AbstractFilterFragment {


    @Override
    protected List<String> getDBDataImpl() {
        return FilterDBTask.getFilterKeywordList(FilterDBTask.TYPE_USER);
    }

    @Override
    protected void addFilterImpl(Collection<String> set) {
        FilterDBTask.addFilterKeyword(FilterDBTask.TYPE_USER, set);
    }

    @Override
    protected List<String> removeAndGetFilterListImpl(Collection<String> set) {
        return FilterDBTask.removeAndGetNewFilterKeywordList(FilterDBTask.TYPE_USER, set);
    }

}
