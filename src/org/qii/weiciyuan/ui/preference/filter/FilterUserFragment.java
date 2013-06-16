package org.qii.weiciyuan.ui.preference.filter;

import org.qii.weiciyuan.support.database.FilterDBTask;

import java.util.List;
import java.util.Set;

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
    protected void addFilterImpl(String value) {
        FilterDBTask.addFilterKeyword(FilterDBTask.TYPE_USER, value);
    }

    @Override
    protected List<String> removeAndGetFilterListImpl(Set<String> set) {
        return FilterDBTask.removeAndGetNewFilterKeywordList(FilterDBTask.TYPE_USER, set);
    }

}
