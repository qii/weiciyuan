package org.qii.weiciyuan.ui.browser;

import android.app.ActionBar;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import org.qii.weiciyuan.R;
import org.qii.weiciyuan.bean.MessageBean;
import org.qii.weiciyuan.bean.RepostListBean;
import org.qii.weiciyuan.dao.send.RepostNewMsgDao;
import org.qii.weiciyuan.dao.timeline.RepostsTimeLineByIdDao;
import org.qii.weiciyuan.support.error.WeiboException;
import org.qii.weiciyuan.support.utils.AppConfig;
import org.qii.weiciyuan.ui.Abstract.AbstractAppActivity;
import org.qii.weiciyuan.ui.Abstract.IToken;
import org.qii.weiciyuan.ui.Abstract.IWeiboMsgInfo;
import org.qii.weiciyuan.ui.actionmenu.RepostSingleChoiceModeListener;
import org.qii.weiciyuan.ui.adapter.StatusesListAdapter;
import org.qii.weiciyuan.ui.basefragment.AbstractTimeLineFragment;
import org.qii.weiciyuan.ui.send.RepostNewActivity;
import org.qii.weiciyuan.ui.widgets.SendProgressFragment;

import java.util.List;

/**
 * User: qii
 * Date: 12-8-13
 */
public class RepostsByIdTimeLineFragment extends AbstractTimeLineFragment<RepostListBean> {


    private MessageBean msg;

    private EditText et;

    private LinearLayout quick_repost;

    protected void clearAndReplaceValue(RepostListBean value) {
        bean.getReposts().clear();
        bean.getReposts().addAll(value.getReposts());
        bean.setTotal_number(value.getTotal_number());
    }


    public RepostListBean getList() {
        return bean;
    }

    private String token;
    private String id;

    public RepostsByIdTimeLineFragment(String token, String id, MessageBean msg) {
        this.token = token;
        this.id = id;
        this.msg = msg;
    }

    public RepostsByIdTimeLineFragment() {

    }

    //restore from activity destroy
    public void load() {
        if ((bean == null || bean.getReposts().size() == 0) && newTask == null) {
            if (listView != null) {
                refresh();
            }

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("bean", bean);
        outState.putString("id", id);
        outState.putString("token", token);
        outState.putSerializable("msg", msg);
    }


    private boolean canSend() {

        boolean haveToken = !TextUtils.isEmpty(token);
        boolean contentNumBelow140 = (et.getText().toString().length() < 140);

        if (haveToken && contentNumBelow140) {
            return true;
        } else {
            if (!haveToken) {
                Toast.makeText(getActivity(), getString(R.string.dont_have_account), Toast.LENGTH_SHORT).show();
            }

            if (!contentNumBelow140) {
                et.setError(getString(R.string.content_words_number_too_many));
            }

        }

        return false;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        commander = ((AbstractAppActivity) getActivity()).getCommander();
         if (savedInstanceState != null && bean.getReposts().size() == 0) {
            clearAndReplaceValue((RepostListBean) savedInstanceState.getSerializable("bean"));
            token = savedInstanceState.getString("token");
            id = savedInstanceState.getString("id");
            msg = (MessageBean) savedInstanceState.getSerializable("msg");
            timeLineAdapter.notifyDataSetChanged();
            refreshLayout(bean);
        }

        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (position - 1 < getList().getSize() && position - 1 >= 0) {
                    if (mActionMode != null) {
                        mActionMode.finish();
                        mActionMode = null;
                        listView.setItemChecked(position, true);
                        timeLineAdapter.notifyDataSetChanged();
                        mActionMode = getActivity().startActionMode(new RepostSingleChoiceModeListener(listView, (StatusesListAdapter)timeLineAdapter, RepostsByIdTimeLineFragment.this, quick_repost, bean.getReposts().get(position - 1)));
                        return true;
                    } else {
                        listView.setItemChecked(position, true);
                        timeLineAdapter.notifyDataSetChanged();
                        mActionMode = getActivity().startActionMode(new RepostSingleChoiceModeListener(listView, (StatusesListAdapter)timeLineAdapter, RepostsByIdTimeLineFragment.this, quick_repost, bean.getReposts().get(position - 1)));
                        return true;
                    }


                }
                return false;
            }

        });


    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bean = new RepostListBean();
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_repost_listview_layout, container, false);
        empty = (TextView) view.findViewById(R.id.empty);
        progressBar = (ProgressBar) view.findViewById(R.id.progressbar);
        quick_repost = (LinearLayout) view.findViewById(R.id.quick_repost);
        listView = (ListView) view.findViewById(R.id.listView);
        listView.setScrollingCacheEnabled(false);
        headerView = inflater.inflate(R.layout.fragment_listview_header_layout, null);
        listView.addHeaderView(headerView);
        listView.setHeaderDividersEnabled(false);
        footerView = inflater.inflate(R.layout.fragment_listview_footer_layout, null);
        listView.addFooterView(footerView);

        if (bean.getReposts().size() == 0) {
            footerView.findViewById(R.id.listview_footer).setVisibility(View.GONE);
        }


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (mActionMode != null) {
                    listView.clearChoices();
                    mActionMode.finish();
                    mActionMode = null;
                    return;
                }
                listView.clearChoices();
                if (position - 1 < getList().getSize() && position - 1 >= 0) {

                    listViewItemClick(parent, view, position - 1, id);
                } else if (position - 1 >= getList().getSize()) {

                    listViewFooterViewClick(view);
                }
            }
        });

        if (savedInstanceState == null && msg != null) {
            if (msg.getRetweeted_status() == null) {
                quick_repost.setVisibility(View.VISIBLE);
            }
        } else if (savedInstanceState != null) {
            msg = (MessageBean) savedInstanceState.getSerializable("msg");
            if (msg.getRetweeted_status() == null) {
                quick_repost.setVisibility(View.VISIBLE);
            }
        }

        et = (EditText) view.findViewById(R.id.content);
        view.findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRepost();
            }
        });
        buildListAdapter();
        return view;
    }

    protected void buildListAdapter() {
        timeLineAdapter = new StatusesListAdapter(getActivity(), ((AbstractAppActivity) getActivity()).getCommander(), getList().getReposts(), listView, false);
        listView.setAdapter(timeLineAdapter);
    }

    private void sendRepost() {
        if (canSend()) {
            new SimpleTask().execute();
        }
    }


    class SimpleTask extends AsyncTask<Void, Void, MessageBean> {
        WeiboException e;

        SendProgressFragment progressFragment = new SendProgressFragment();

        @Override
        protected void onPreExecute() {
            progressFragment.onCancel(new DialogInterface() {

                @Override
                public void cancel() {
                    SimpleTask.this.cancel(true);
                }

                @Override
                public void dismiss() {
                    SimpleTask.this.cancel(true);
                }
            });

            progressFragment.show(getFragmentManager(), "");

        }

        @Override
        protected MessageBean doInBackground(Void... params) {

            String content = et.getText().toString();
            if (TextUtils.isEmpty(content)) {
                content = getString(R.string.repost);
            }

            RepostNewMsgDao dao = new RepostNewMsgDao(token, id);
            dao.setStatus(content);
            try {
                return dao.sendNewMsg();
            } catch (WeiboException e) {
                this.e = e;
                cancel(true);
                return null;
            }
        }

        @Override
        protected void onCancelled(MessageBean s) {
            super.onCancelled(s);
            if (this.e != null) {
                Toast.makeText(getActivity(), e.getError(), Toast.LENGTH_SHORT).show();

            }
        }

        @Override
        protected void onPostExecute(MessageBean s) {
            if (progressFragment != null)
                progressFragment.dismissAllowingStateLoss();
            if (s != null) {
                et.setText("");
                refresh();
            } else {
                Toast.makeText(getActivity(), getString(R.string.send_failed), Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(s);

        }
    }


    protected void listViewItemClick(AdapterView parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), BrowserWeiboMsgActivity.class);
        intent.putExtra("msg", bean.getReposts().get(position));
        intent.putExtra("token", ((IToken) getActivity()).getToken());
        startActivity(intent);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.repostsbyidtimelinefragment_menu, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.repostsbyidtimelinefragment_repost:
                Intent intent = new Intent(getActivity(), RepostNewActivity.class);
                intent.putExtra("token", token);
                intent.putExtra("id", id);
                intent.putExtra("msg", ((IWeiboMsgInfo) getActivity()).getMsg());
                startActivity(intent);
                break;

            case R.id.repostsbyidtimelinefragment_repost_refresh:

                refresh();

                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void invlidateTabText() {
        Activity activity = getActivity();
        if (activity != null) {
            ActionBar.Tab tab = activity.getActionBar().getTabAt(2);
            String num = getString(R.string.repost) + "(" + bean.getTotal_number() + ")";
            tab.setText(num);

        }
    }


    @Override
    protected RepostListBean getDoInBackgroundNewData() throws WeiboException {
        RepostsTimeLineByIdDao dao = new RepostsTimeLineByIdDao(token, id);

        if (getList().getReposts().size() > 0) {
            dao.setSince_id(getList().getReposts().get(0).getId());
        }

        return dao.getGSONMsgList();
    }

    @Override
    protected RepostListBean getDoInBackgroundOldData() throws WeiboException {
        RepostsTimeLineByIdDao dao = new RepostsTimeLineByIdDao(token, id);
        if (getList().getReposts().size() > 0) {
            dao.setMax_id(getList().getReposts().get(getList().getReposts().size() - 1).getId());
        }
        return dao.getGSONMsgList();

    }


    @Override
    protected void newMsgOnPostExecute(RepostListBean newValue) {
        if (newValue != null) {
            if (newValue.getReposts().size() == 0) {
                Toast.makeText(getActivity(), getString(R.string.no_new_message), Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getActivity(), getString(R.string.total) + newValue.getReposts().size() + getString(R.string.new_messages), Toast.LENGTH_SHORT).show();
                if (newValue.getReposts().size() < AppConfig.DEFAULT_MSG_NUMBERS) {
                    newValue.getReposts().addAll(getList().getReposts());
                }

                clearAndReplaceValue(newValue);
                timeLineAdapter.notifyDataSetChanged();
            }
            invlidateTabText();
        }
    }

    @Override
    protected void oldMsgOnPostExecute(RepostListBean newValue) {
        if (newValue != null && newValue.getReposts().size() > 1) {
            List<MessageBean> list = newValue.getReposts();
            getList().getReposts().addAll(list.subList(1, list.size() - 1));

        }

        timeLineAdapter.notifyDataSetChanged();
        invlidateTabText();
    }


}

