package cn.cpuboom.geniusbarassistant;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.util.ArrayList;

import cn.cpuboom.geniusbarassistant.adapter.GeniusBarQueueAdapter;
import cn.cpuboom.geniusbarassistant.message.HandlerMessage;
import cn.cpuboom.geniusbarassistant.model.FixInfoModel;
import cn.cpuboom.geniusbarassistant.model.FixQueueItemModel;
import cn.cpuboom.geniusbarassistant.utilities.SimpleHTTPUtility;

public class MainActivity extends AppCompatActivity {

    MyApplication mMyApp;

    RecyclerView mRecyclerView;
    SwipeRefreshLayout mRefreshLayout;
    GeniusBarQueueAdapter mAdapter;
    CoordinatorLayout mCoordinator;

    TextView mTextFixHr,mTextFixMin,mTextFixInfo,mTextFixDescription,mTextFixStaffId;
    Button mButtonFixOk;
    View mHideTime,mBgLayout,mCurrentFixMainLayout;

    ArrayList<FixQueueItemModel> mData=new ArrayList<>();

    private Handler mHandler = new Handler()
    {
        public void handleMessage(android.os.Message msg)
        {
            switch (msg.what)
            {
                case HandlerMessage.REFRESH_COMPLETE:
                    mAdapter.notifyDataSetChanged();
                    mRefreshLayout.setRefreshing(false);
                    break;
                case HandlerMessage.REFRESH_REQUESTED:
                    new GBQueueWaitingAsyncTask().execute(mMyApp.getUrl(R.string.json_src_gb_queue_waiting));
                    new GBCurrentFixAsyncTask().execute(mMyApp.getUrl(R.string.json_src_gb_my_fixing)+"?staffid="+Integer.toString(mMyApp.getStaffId()));
                    mRefreshLayout.setRefreshing(true);
                    break;
                case HandlerMessage.TIME_UPDATE:
                    if (mMyApp.currentFix!=null){
                        Long currentElapsedTime=System.currentTimeMillis()/1000-mMyApp.currentFix.fixStartTime;
                        long hr=currentElapsedTime/3600;
                        long min=currentElapsedTime%3600/60;
                        mTextFixHr.setText(Long.toString(hr));
                        mTextFixMin.setText(Long.toString(min));
                    }
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMyApp=(MyApplication)getApplication();

        mRecyclerView=(RecyclerView)findViewById(R.id.recycler);
        mRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.refresh);
        mCoordinator=(CoordinatorLayout)findViewById(R.id.coordinator);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter=new GeniusBarQueueAdapter(this,mData,mMyApp,mCoordinator,mHandler);
        mRecyclerView.setAdapter(mAdapter);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mHandler.sendEmptyMessage(HandlerMessage.REFRESH_REQUESTED);
            }
        });
        mRefreshLayout.setColorSchemeResources(R.color.material_red_500,R.color.material_orange_500,R.color.material_green_500,R.color.material_light_blue_500);

        mTextFixHr=(TextView)findViewById(R.id.fix_current_time_hr);
        mTextFixMin=(TextView)findViewById(R.id.fix_current_time_min);
        mTextFixInfo=(TextView)findViewById(R.id.fix_current_main_info);
        mTextFixDescription=(TextView)findViewById(R.id.fix_current_description);
        mTextFixStaffId=(TextView)findViewById(R.id.fix_current_staff_id);
        mButtonFixOk=(Button)findViewById(R.id.fix_current_button_ok);
        mHideTime=findViewById(R.id.fix_current_time_hide);
        mBgLayout=(LinearLayout)findViewById(R.id.fix_current_bg_layout);
        mButtonFixOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mMyApp.currentFix!=null)
                    new GBFixFinishedAsyncTask().execute(mMyApp.getUrl(R.string.json_src_gb_queue_finish));
            }
        });
        mCurrentFixMainLayout=findViewById(R.id.fix_current_main_layout);
        mHandler.sendEmptyMessage(HandlerMessage.REFRESH_REQUESTED);
    }

    void setCardContent(byte mode, FixQueueItemModel info){

        mTextFixStaffId.setText(Integer.toString(info.fixStaff));
        switch (mode){
            case FixQueueItemModel.FIX_STATUS_DOING:
                mButtonFixOk.setEnabled(true);
                mHideTime.setVisibility(View.VISIBLE);
                Long currentElapsedTime=System.currentTimeMillis()/1000-info.fixStartTime;
                long hr=currentElapsedTime/3600;
                long min=currentElapsedTime%3600/60;
                mTextFixHr.setText(Long.toString(hr));
                mTextFixMin.setText(Long.toString(min));
                mTextFixInfo.setText(info.userName+" 的 "+info.productName);
                mTextFixStaffId.setVisibility(View.VISIBLE);
                mTextFixDescription.setText(info.productDescription);
                mBgLayout.setBackgroundColor(Color.parseColor("#f44336"));
                mButtonFixOk.setTextColor(getResources().getColor(R.color.colorAccent));
                mCurrentFixMainLayout.setOnClickListener(new FixItemDetailOnClickListener(info));
                break;
            case FixQueueItemModel.FIX_STATUS_NONE:
                mHideTime.setVisibility(View.GONE);
                mButtonFixOk.setEnabled(false);
                mTextFixHr.setText(R.string.gb_fix_none);
                mTextFixInfo.setText("...");
                mTextFixStaffId.setVisibility(View.GONE);
                mTextFixDescription.setText(R.string.gb_fix_none_description);
                //mButtonFixOk.setOnClickListener(new OnDelClickListener(info.id));
                mBgLayout.setBackgroundColor(Color.parseColor("#4caf50"));
                mButtonFixOk.setTextColor(getResources().getColor(R.color.material_grey_200));
                mCurrentFixMainLayout.setOnClickListener(null);
                break;
        }
    }


    class FixItemDetailOnClickListener implements View.OnClickListener {

        FixInfoModel info;

        FixItemDetailOnClickListener(FixInfoModel i){
            info=i;
        }

        @Override
        public void onClick(View view) {
            Intent intent=new Intent(getBaseContext(), GeniusBarItemDetailActivity.class);
            intent.putExtra("data",info);
            startActivity(intent);
        }
    }

    class GBQueueWaitingAsyncTask extends AsyncTask<String, Void, ArrayList<FixQueueItemModel>>
    {

        @Override
        protected ArrayList<FixQueueItemModel> doInBackground(String... params) {
            ArrayList<FixQueueItemModel> data=new ArrayList<>();
            try{
                String resultJson=new SimpleHTTPUtility(params[0],"").get();
                Type type=new TypeToken<ArrayList<FixQueueItemModel>>(){}.getType();
                Gson gson=new Gson();
                data=gson.fromJson(resultJson,type);
            } catch (MalformedURLException e) {
                Log.e("ProductsFragment", "getJsonData: ", e);
                Snackbar.make(mRefreshLayout, "MalformedURLException", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } catch (IOException e) {
                Log.e("ProductsFragment", "getJsonData: ", e);
                Snackbar.make(mRefreshLayout, "IOException", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } catch (JsonParseException e) {
                Log.e("ProductsFragment", "getJsonData: ", e);
                Snackbar.make(mRefreshLayout, "JsonParseException", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }catch(SimpleHTTPUtility.HttpResponseException e){
                Log.e("ProductsFragment", "getJsonData: ", e);
                Snackbar.make(mRefreshLayout, "HttpResponseException", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            return data;
        }

        @Override
        protected void onPostExecute(ArrayList<FixQueueItemModel> result) {
            super.onPostExecute(result);
            mData.clear();
            mData.addAll(result);
            //mHandler.sendEmptyMessage(HandlerMessage.REFRESH_COMPLETE);
        }

    }

    class GBCurrentFixAsyncTask extends AsyncTask<String, Void, FixQueueItemModel>
    {

        @Override
        protected FixQueueItemModel doInBackground(String... params) {
            FixQueueItemModel data=null;
            try{
                String resultJson=new SimpleHTTPUtility(params[0],"").get();
                Type type=new TypeToken<ArrayList<FixQueueItemModel>>(){}.getType();
                Gson gson=new Gson();
                data=gson.fromJson(resultJson,FixQueueItemModel.class);
            } catch (MalformedURLException e) {
                Log.e("ProductsFragment", "getJsonData: ", e);
                Snackbar.make(mRefreshLayout, "MalformedURLException", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } catch (IOException e) {
                Log.e("ProductsFragment", "getJsonData: ", e);
                Snackbar.make(mRefreshLayout, "IOException", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } catch (JsonParseException e) {
                Log.e("ProductsFragment", "getJsonData: ", e);
                Snackbar.make(mRefreshLayout, "JsonParseException", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }catch(SimpleHTTPUtility.HttpResponseException e){
                Log.e("ProductsFragment", "getJsonData: ", e);
                Snackbar.make(mRefreshLayout, "HttpResponseException", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            return data;
        }

        @Override
        protected void onPostExecute(FixQueueItemModel result) {
            super.onPostExecute(result);
            if (result!=null) {
                if (result.fixStatus == FixQueueItemModel.FIX_STATUS_NONE) {
                    mMyApp.currentFix = null;
                } else {
                    mMyApp.currentFix = result;
                }
                setCardContent(result.fixStatus, result);
            }else{
                Snackbar.make(mRefreshLayout, R.string.get_data_fail, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            mHandler.sendEmptyMessage(HandlerMessage.REFRESH_COMPLETE);
        }

    }

    class GBFixFinishedAsyncTask extends AsyncTask<String, Void, String>
    {

        @Override
        protected String doInBackground(String... params) {
            try{
                String finalUrl=params[0]+"?staffid="+Integer.toString(mMyApp.getStaffId())+"&fixid="+Integer.toString(mMyApp.currentFix.id);
                String result=new SimpleHTTPUtility(finalUrl,"").get();
                return  result;
            } catch (MalformedURLException e) {
                Log.e("ProductsFragment", "getJsonData: ", e);
                Snackbar.make(mRefreshLayout, "MalformedURLException", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } catch (IOException e) {
                Log.e("ProductsFragment", "getJsonData: ", e);
                Snackbar.make(mRefreshLayout, "IOException", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } catch (JsonParseException e) {
                Log.e("ProductsFragment", "getJsonData: ", e);
                Snackbar.make(mRefreshLayout, "JsonParseException", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }catch(SimpleHTTPUtility.HttpResponseException e){
                Log.e("ProductsFragment", "getJsonData: ", e);
                Snackbar.make(mRefreshLayout, "HttpResponseException", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            mHandler.sendEmptyMessage(HandlerMessage.REFRESH_REQUESTED);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class TimerThread extends Thread{
        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                int count=0;
                do
                {

                /* 重要关键程序:取得时间后发出信息给Handler */
                    Message msg=new Message();
                    msg.what= HandlerMessage.TIME_UPDATE;
                    mHandler.sendMessage(msg);/* 重要关键程序:取得时间后发出信息给Handler */
                    if (count==30) {
                        msg.what= HandlerMessage.REFRESH_REQUESTED;
                        mHandler.sendMessage(msg);
                        count=0;
                    }
                    count++;
                    //count=(count+1)%30;

                    Thread.sleep(1000);
                }
                while(Thread.interrupted()==false);/* 当系统发出中断信息时停止本循环*/
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }

        }
    }
}
