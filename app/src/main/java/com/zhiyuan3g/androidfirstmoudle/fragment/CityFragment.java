package com.zhiyuan3g.androidfirstmoudle.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ContentFrameLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zhiyuan3g.androidfirstmoudle.R;
import com.zhiyuan3g.androidfirstmoudle.adapter.CityAdapter;
import com.zhiyuan3g.androidfirstmoudle.db.CityDB;
import com.zhiyuan3g.androidfirstmoudle.utils.ContractUtils;
import com.zhiyuan3g.androidfirstmoudle.utils.OkHttpCallBack;
import com.zhiyuan3g.androidfirstmoudle.utils.OkHttpUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by kkkkk on 2017/8/23.
 */

public class CityFragment extends Fragment {
    @BindView(R.id.tv_province_title)
    TextView tvProvinceTitle;
    @BindView(R.id.province_toolBar)
    Toolbar provinceToolBar;
    @BindView(R.id.province_recyclerView)
    RecyclerView provinceRecyclerView;

    private FragmentManager fragmentManager;
    private int id;
    private List<CityDB> cityDBList;

    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_province, container, false);
        ButterKnife.bind(this, view);
        Bundle bundle = getArguments();
        id = bundle.getInt("id");
        String name = bundle.getString("name");
        initToolBar(name);
        initFindDB();
        return view;
    }

    private void initFindDB() {
        final List<CityDB> cityDBs = DataSupport.where("provinceCode = ?", String.valueOf(id)).find(CityDB.class);
        if(cityDBs.isEmpty()){
            initHttp();
        }else{
            CityAdapter cityAdapter = new CityAdapter(getActivity(),cityDBs);
            LinearLayoutManager manager = new LinearLayoutManager(getActivity());
            provinceRecyclerView.setLayoutManager(manager);
            provinceRecyclerView.setAdapter(cityAdapter);
            cityAdapter.setOnItemClick(new CityAdapter.OnItemClick() {
                @Override
                public void itemClick(int position) {
                    //此处碎片使用new的形式，因为如果使用匿名对象的话，传递Bundle会出现崩溃
                    CountryFragment countryFragment = new CountryFragment();
                    //给另一个碎片传递消息
                    Bundle bundle = new Bundle();
                    //传递键值对
                    bundle.putInt("id",cityDBs.get(position).getCityCode());
                    bundle.putInt("provinceId",cityDBs.get(position).getProvinceCode());
                    bundle.putString("name",cityDBs.get(position).getName());
                    //传递bundle对象
                    countryFragment.setArguments(bundle);

                    //替换碎片
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.replace(R.id.main_container,countryFragment);
                    transaction.commit();
                }
            });
        }
    }


    private void initHttp() {
        //设置dialog标题内容
        progressDialog.setTitle("提示");
        progressDialog.setMessage("请等待...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        OkHttpUtils.sendRequestGETMethod(getActivity(), ContractUtils.URL_CITY + id, new OkHttpCallBack() {
            @Override
            public void Success(String result) {
                try {
                    //此处使用源生解析，因为Gson会自动生成id，会覆盖数据中的id字段
                    JSONArray jsonArray = new JSONArray(result);
                    for (int i=0;i<jsonArray.length();i++){
                        JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                        //给实体类赋值
                        CityDB cityDB = new CityDB();
                        cityDB.setCityCode(jsonObject.getInt("id"));
                        cityDB.setName(jsonObject.getString("name"));
                        cityDB.setProvinceCode(id);
                        cityDB.save();
                        //把这个实体类添加进入集合
                        cityDBList.add(cityDB);
                    }
                    CityAdapter cityAdapter = new CityAdapter(getActivity(),cityDBList);
                    LinearLayoutManager manager = new LinearLayoutManager(getActivity());
                    provinceRecyclerView.setLayoutManager(manager);
                    provinceRecyclerView.setAdapter(cityAdapter);
                    progressDialog.dismiss();
                    cityAdapter.setOnItemClick(new CityAdapter.OnItemClick() {
                        @Override
                        public void itemClick(int position) {
                            //此处碎片使用new的形式，因为如果使用匿名对象的话，传递Bundle会出现崩溃
                            CountryFragment countryFragment = new CountryFragment();
                            //给另一个碎片传递消息
                            Bundle bundle = new Bundle();
                            //传递键值对
                            bundle.putInt("id",cityDBList.get(position).getCityCode());
                            bundle.putInt("provinceId",cityDBList.get(position).getProvinceCode());
                            bundle.putString("name",cityDBList.get(position).getName());
                            //传递bundle对象
                            countryFragment.setArguments(bundle);

                            //替换碎片
                            FragmentManager fragmentManager = getFragmentManager();
                            FragmentTransaction transaction = fragmentManager.beginTransaction();
                            transaction.replace(R.id.main_container,countryFragment);
                            transaction.commit();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void Failure(String failure) {
                progressDialog.dismiss();
            }
        });
    }

    private void initToolBar(String name) {
        progressDialog = new ProgressDialog(getActivity());

        cityDBList = new ArrayList<>();
        //这行代码会让你的Fragment中的toolbar生效
        setHasOptionsMenu(true);
        fragmentManager = getFragmentManager();
        //因为此setSupportActionBar方法只存在Activity中，所以需要强制转换。
        ((AppCompatActivity)getActivity()).setSupportActionBar(provinceToolBar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if(actionBar!=null){
            //隐藏原有标题栏
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        tvProvinceTitle.setText(name);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.main_container,new ProvinceFragment());
                transaction.commit();
                break;
        }
        //return 的值此处为false，因为返回true的话。意外以后的toolbar点击事件不再往下传递。
        return super.onOptionsItemSelected(item);
    }
}
