//package org.qii.weiciyuan.ui.timeline;
//
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.AdapterView;
//import org.qii.weiciyuan.R;
//import org.qii.weiciyuan.bean.TimeLineMsgList;
//
///**
// * Created with IntelliJ IDEA.
// * User: qii
// * Date: 12-7-29
// * Time: 上午12:52
// * To change this template use File | Settings | File Templates.
// */
//public class MentionsTimeLineFragment extends AbstractTimeLineFragment {
//
//    @Override
//    public void refresh() {
//        //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    @Override
//    public void refreshAndScrollTo(int positon) {
//        refresh();
//        listView.smoothScrollToPosition(positon);
//
//    }
//
//    @Override
//    protected TimeLineMsgList getList() {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    @Override
//    protected void scrollToBottom() {
//        //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    @Override
//    protected void listViewItemLongClick(AdapterView parent, View view, int position, long id) {
//        //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//
//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.mentionstimelinefragment_menu, menu);
//        super.onCreateOptionsMenu(menu, inflater);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//
//            case R.id.friendstimelinefragment_refresh:
//
//                break;
//        }
//        return true;
//    }
//
//}
//
