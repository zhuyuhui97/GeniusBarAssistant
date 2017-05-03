package cn.cpuboom.geniusbarassistant.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.JsonParseException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import cn.cpuboom.geniusbarassistant.GeniusBarItemDetailActivity;
import cn.cpuboom.geniusbarassistant.MyApplication;
import cn.cpuboom.geniusbarassistant.R;
import cn.cpuboom.geniusbarassistant.message.HandlerMessage;
import cn.cpuboom.geniusbarassistant.model.FixQueueItemModel;
import cn.cpuboom.geniusbarassistant.utilities.SimpleHTTPUtility;


/**
 * Created by zhuyuhui on 2016/8/24.
 */
public class GeniusBarQueueAdapter extends RecyclerView.Adapter<GeniusBarQueueAdapter.MyViewHolder> {
    private ArrayList<FixQueueItemModel> mDataset;
    private Context mContext;
    private LayoutInflater inflater;
    private View mBaseView;
    private Handler mActivityHandler;
    private MyApplication mMyApp;

    public GeniusBarQueueAdapter(Context context, ArrayList<FixQueueItemModel> dataset, MyApplication app, View baseView, Handler handler){
        mContext=context;
        mDataset=dataset;
        mBaseView=baseView;
        inflater=LayoutInflater. from(mContext);
        mMyApp=app;
        mActivityHandler=handler;
    }

    public int getItemCount(){
        return mDataset.size();
    }

    public void onBindViewHolder(final MyViewHolder holder, int pos){
        final FixQueueItemModel currentItem= mDataset.get(pos);
        //setCardContent(holder,currentItem.fixStatus,currentItem);
        holder.tInfo.setText(currentItem.userName+" 的 "+currentItem.productName);
        holder.tDescription.setText(currentItem.productDescription);
        holder.bStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMyApp.currentFix==null) {
                    StartFixItemAsyncTask task = new StartFixItemAsyncTask(currentItem.id);
                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mMyApp.getUrl(R.string.json_src_gb_queue_start));
                }else{
                    Snackbar.make(mBaseView, R.string.gb_should_finish_current, Snackbar.LENGTH_LONG)
                            .show();
                }

            }
        });
        holder.mMainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(mContext, GeniusBarItemDetailActivity.class);
                intent.putExtra("data",currentItem);
                mContext.startActivity(intent);
            }
        });
        //holder.pPic=
    }

    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.card_fix_item_in_queue,parent, false);
        MyViewHolder holder= new MyViewHolder(view);
        return holder;
    }





    class StartFixItemAsyncTask extends AsyncTask<String,Void,String>{

        int mFixId;

        StartFixItemAsyncTask(int fixid){
            mFixId=fixid;
        }

        @Override
        protected String doInBackground(String... strings) {
            String finaUrl=strings[0]+"?fixid="+Integer.toString(mFixId)+"&staffid="+Integer.toString(mMyApp.getStaffId());
            String responseStr=null;
            try {
                responseStr=new SimpleHTTPUtility(finaUrl,"").get();
            }catch (MalformedURLException e) {
                Log.e("ProductsFragment", "getJsonData: ", e);
                Snackbar.make(mBaseView, "MalformedURLException", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } catch (IOException e) {
                Log.e("ProductsFragment", "getJsonData: ", e);
                Snackbar.make(mBaseView, "IOException", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } catch (JsonParseException e) {
                Log.e("ProductsFragment", "getJsonData: ", e);
                Snackbar.make(mBaseView, "JsonParseException", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }catch(SimpleHTTPUtility.HttpResponseException e){
                Log.e("ProductsFragment", "getJsonData: ", e);
                Snackbar.make(mBaseView, "HttpResponseException", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            return responseStr;
        }

        @Override
        protected void onPostExecute(String i) {
            super.onPostExecute(i);
            if (!i.isEmpty())
                Snackbar.make(mBaseView, i, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            mActivityHandler.sendEmptyMessage(HandlerMessage.REFRESH_REQUESTED);
        }
    }


    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView tInfo,tDescription;
        Button bStart;
        View mMainLayout;

        public MyViewHolder(View view)
        {
            super(view);
            tInfo=(TextView)view.findViewById(R.id.fix_item_main_info);
            tDescription=(TextView)view.findViewById(R.id.fix_item_description);
            bStart=(Button)view.findViewById(R.id.fix_item_button_start);
            mMainLayout=view.findViewById(R.id.fix_item_main_layout);
        }
    }
}
