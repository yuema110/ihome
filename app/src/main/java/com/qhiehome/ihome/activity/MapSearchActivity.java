package com.qhiehome.ihome.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.qhiehome.ihome.R;
import com.qhiehome.ihome.adapter.SearchMapAdapter;
import com.qhiehome.ihome.application.IhomeApplication;
import com.qhiehome.ihome.persistence.MapSearch;
import com.qhiehome.ihome.persistence.MapSearchDao;
import com.qhiehome.ihome.util.CommonUtil;
import com.qhiehome.ihome.util.LogUtil;
import com.qhiehome.ihome.view.SearchRecyclerView;

import org.greenrobot.greendao.query.Query;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MapSearchActivity extends BaseActivity {

    private static final String TAG = MapSearchActivity.class.getSimpleName();

    @BindView(R.id.et_search)
    EditText mEtSearch;
    @BindView(R.id.iv_search)
    ImageView mIvSearch;
    @BindView(R.id.tv_search_tip)
    TextView mTvSearchTip;
    @BindView(R.id.tv_search_clear)
    TextView mTvSearchClear;
    @BindView(R.id.rv_search)
    SearchRecyclerView mRvSearch;
//    @BindView(R.id.floating_search_view)
//    FloatingSearchView mFloatingSearchView;

    private Context mContext;
    private SearchMapAdapter mAdapter;
    private SuggestionSearch mSuggestionSearch = SuggestionSearch.newInstance();
    private SuggestionResult mSuggestionResult;
    private String mCity;
    private boolean isHistory;
    private Handler mHandler;
    private int mPosition;

    private MapSearchDao mSearchDao;
    private Query<MapSearch> mSearchQuery;

    private static final int BACK_MSG = 1;

    @OnClick(R.id.iv_finish)
    public void onClickFinish() {
        this.finish();
    }

    private static class SearchHandler extends Handler {
        private final WeakReference<MapSearchActivity> mActivity;

        private SearchHandler(MapSearchActivity mapSearchActivity) {
            mActivity = new WeakReference<>(mapSearchActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            MapSearchActivity mapSearchActivity = mActivity.get();
            if (mapSearchActivity != null) {
                switch (msg.what) {
                    case BACK_MSG:
                        mapSearchActivity.deliverData(mapSearchActivity.mPosition);
//                        mapSearchActivity.mFloatingSearchView.setSearchFocused(false);
                        mapSearchActivity.finish();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_search);
        ButterKnife.bind(this);
        mContext = this;
        mHandler = new SearchHandler(this);
        mCity = getIntent().getStringExtra("city");
        LogUtil.d(TAG, "city is " + mCity);
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        mCity = bundle.getString("city");

        mSearchDao = ((IhomeApplication) getApplication()).getDaoSession().getMapSearchDao();

        init();
        queryData("");

        OnGetSuggestionResultListener listener = new OnGetSuggestionResultListener() {
            @Override
            public void onGetSuggestionResult(SuggestionResult suggestionResult) {
                if (suggestionResult == null || suggestionResult.error == SuggestionResult.ERRORNO.RESULT_NOT_FOUND) {
                    return;
                }
                if (suggestionResult.error == SearchResult.ERRORNO.NO_ERROR) {
                    mSuggestionResult = suggestionResult;
                    List<String> list = new ArrayList<>();
                    for (int i = 0; i < suggestionResult.getAllSuggestions().size(); i++) {
                        list.add(suggestionResult.getAllSuggestions().get(i).key);
                    }
                    mAdapter.setmSearchResults(list);
                    mAdapter.notifyDataSetChanged();
                    if (mPosition >= 0) {
                        mHandler.sendEmptyMessage(BACK_MSG);
                    }
                }
            }
        };
        mSuggestionSearch.setOnGetSuggestionResultListener(listener);
    }

    private void init() {

        mRvSearch.setLayoutManager(new LinearLayoutManager(this));
        mRvSearch.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        List<String> initList = new ArrayList<>();
        mAdapter = new SearchMapAdapter(mContext, initList);
        mAdapter.setOnItemClickListener(new SearchMapAdapter.OnClickListener() {
            @Override
            public void onClick(View view, int i, String name) {
                boolean tmp;
                if (isHistory) {
                    tmp = true;
                } else {
                    tmp = false;
                }
                mEtSearch.setText(name);
                isHistory = tmp;        //改变et内容
                if (!hasData(name)) {
                    insertData(name);
                }                       //添加到数据库
                if (isHistory) {
                    mPosition = i;
                } else {
                    mPosition = 0;
                }
                //判断历史还是联想
                suggestionSearch(name);
            }
        });
        mRvSearch.setAdapter(mAdapter);
//        mFloatingSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
//            @Override
//            public void onSearchTextChanged(String oldQuery, String newQuery) {
//
//                if (newQuery.length() == 0) {
//                    mTvSearchTip.setText("搜索历史");
//                    isHistory = true;
//                } else {
//                    mTvSearchTip.setText("搜索结果");
//                    isHistory = false;
//                    mPosition = -1;
//                    suggestionSearch(newQuery);
//                }
//            }
//        });
//        mFloatingSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
//            @Override
//            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
//
//            }
//
//            @Override
//            public void onSearchAction(String currentQuery) {
//                if (TextUtils.isEmpty(currentQuery)){
//                    return;
//                }
//                boolean hasData = hasData(mFloatingSearchView.getQuery());
//                if (!hasData) {
//                    insertData(mFloatingSearchView.getQuery());
//                    queryData("");
//                }
//                mPosition = 0;
//                suggestionSearch(mFloatingSearchView.getQuery());
//            }
//        });

        mEtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //有内容则显示联想结果，无内容则显示历史记录
                String tempName = mEtSearch.getText().toString();
                if (s.toString().trim().length() == 0) {
                    mTvSearchTip.setText("搜索历史");
                    isHistory = true;
                } else {
                    mTvSearchTip.setText("搜索结果");
                    isHistory = false;
                    mPosition = -1;
                    suggestionSearch(mEtSearch.getText().toString().trim());
                }


            }
        });
        mEtSearch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (TextUtils.isEmpty(mEtSearch.getText().toString().trim())) {
                        return false;
                    }
                    boolean hasData = hasData(mEtSearch.getText().toString().trim());
                    if (!hasData) {
                        insertData(mEtSearch.getText().toString().trim());
                        queryData("");
                    }
                    mPosition = 0;
                    suggestionSearch(mEtSearch.getText().toString().trim());

                }
                return false;
            }
        });
        /*********列表点击**********/
//        mRvSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                TextView textView = (TextView) view.findViewById(android.R.id.text1);
//                String name = textView.getText().toString();
//                boolean tmp;
//                if (isHistory) {
//                    tmp = true;
//                } else {
//                    tmp = false;
//                }
////                mFloatingSearchView.setSearchText(name);
//                //mEtSearch.setText(name);
//                isHistory = tmp;
//                if (!hasData(name)) {
//                    insertData(name);
//                }
//                if (isHistory) {
//                    mPosition = position;
//                } else {
//                    mPosition = 0;
//                }
//                //判断历史还是联想
//                suggestionSearch(name);
//            }
//        });
        /*********按钮点击**********/
        mIvSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!hasData(mEtSearch.getText().toString().trim())) {
                    insertData(mEtSearch.getText().toString().trim());
                }
                mPosition = 0;
                suggestionSearch(mEtSearch.getText().toString().trim());
            }
        });
    }

    @OnClick(R.id.tv_search_clear)
    public void onViewClicked() {
        //清空数据库
        deleteData();
        queryData("");
    }


    /*模糊查询数据 并显示在ListView列表上*/
//    private void queryData(String tempName) {
//
//        //模糊搜索
//        Cursor cursor = mSQLHelper.getReadableDatabase().rawQuery(
//                "select id as _id,name from history where name like '%" + tempName + "%' order by id desc ", null);
//        // 创建adapter适配器对象,装入模糊搜索的结果
//        mAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursor, new String[]{"name"},
//                new int[]{android.R.id.text1}, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
//        // 设置适配器
//        mRvSearch.setAdapter(mAdapter);
//        mAdapter.notifyDataSetChanged();
//    }
    private void queryData(String search) {
        if (TextUtils.isEmpty(search)) {
            mSearchQuery = mSearchDao.queryBuilder().orderDesc(MapSearchDao.Properties.Id).build();
        }
        List<MapSearch> searchList = mSearchQuery.list();
        List<String> list = new ArrayList<>();
        for (int i = 0; i < searchList.size(); i++) {
            list.add(searchList.get(i).getName());
        }
        mAdapter.setmSearchResults(list);
        mAdapter.notifyDataSetChanged();

    }

//    private void deleteData() {
//        mDB = mSQLHelper.getWritableDatabase();
//        mDB.execSQL("delete from history");
//        mDB.close();
//    }

    private void deleteData() {
        mSearchDao.deleteAll();
    }

//    private boolean hasData(String tempName) {
//        //从Record这个表里找到name=tempName的id
//        Cursor cursor = mSQLHelper.getReadableDatabase().rawQuery(
//                "select id as _id,name from history where name =?", new String[]{tempName});
//        //判断是否有下一个
//        return cursor.moveToNext();
//    }

    private boolean hasData(String search) {
        mSearchQuery = mSearchDao.queryBuilder().where(MapSearchDao.Properties.Name.eq(search)).build();
        return mSearchQuery.list().size() > 0;
    }

    /*插入数据*/
//    private void insertData(String tempName) {
//        mDB = mSQLHelper.getWritableDatabase();
//        mDB.execSQL("insert into history(name) values('" + tempName + "')");
//        mDB.close();
//    }
    public void insertData(String search) {
        mSearchDao.insert(new MapSearch(null, search));
    }

    private void suggestionSearch(String input) {
        mSuggestionSearch.requestSuggestion(new SuggestionSearchOption()
                .keyword(input)
                .city(mCity));
    }

    private void deliverData(int position) {
        Bundle data = new Bundle();
        data.putString("name", mSuggestionResult.getAllSuggestions().get(position).key);
        try {
            data.putDouble("latitude", mSuggestionResult.getAllSuggestions().get(position).pt.latitude);
            data.putDouble("longitude", mSuggestionResult.getAllSuggestions().get(position).pt.longitude);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent backIntent = new Intent();
        backIntent.putExtras(data);
        setResult(RESULT_OK, backIntent);
    }


}
