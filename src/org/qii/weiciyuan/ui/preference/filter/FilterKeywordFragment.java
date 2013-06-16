package org.qii.weiciyuan.ui.preference.filter;

import org.qii.weiciyuan.support.database.FilterDBTask;

import java.util.List;
import java.util.Set;

/**
 * User: qii
 * Date: 12-9-21
 */
public class FilterKeywordFragment extends AbstractFilterFragment {


    @Override
    protected List<String> getDBDataImpl() {
        return FilterDBTask.getFilterKeywordList(FilterDBTask.TYPE_KEYWORD);
    }

    @Override
    protected void addFilterImpl(String value) {
        FilterDBTask.addFilterKeyword(FilterDBTask.TYPE_KEYWORD, value);
    }

    @Override
    protected List<String> removeAndGetFilterListImpl(Set<String> set) {
        return FilterDBTask.removeAndGetNewFilterKeywordList(FilterDBTask.TYPE_KEYWORD, set);
    }

}
