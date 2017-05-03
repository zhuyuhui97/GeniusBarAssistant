package cn.cpuboom.geniusbarassistant;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import cn.cpuboom.geniusbarassistant.model.FixInfoModel;


public class GeniusBarItemDetailActivity extends AppCompatActivity {

    TextView pname,sn,imei,cname,ccontact,desc;
    ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genius_bar_item_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        pname = (TextView) findViewById(R.id.gb_item_detail_name);
        sn = (TextView) findViewById(R.id.gb_item_detail_sn);
        imei = (TextView) findViewById(R.id.gb_item_detail_imei);
        cname = (TextView) findViewById(R.id.gb_item_detail_username);
        ccontact = (TextView) findViewById(R.id.gb_item_detail_contact);
        desc = (TextView) findViewById(R.id.gb_item_detail_description);
        FixInfoModel info = (FixInfoModel) getIntent().getSerializableExtra("data");
        pname.setText(info.productName);
        sn.setText(info.productSn);
        imei.setText(info.productImei);
        cname.setText(info.userName);
        ccontact.setText(info.userContact);
        desc.setText(info.productDescription);
        mActionBar=getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }
}
