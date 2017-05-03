package cn.cpuboom.geniusbarassistant.model;

import java.io.Serializable;

/**
 * Created by zhuyuhui on 2016/8/23.
 */
public class FixInfoModel implements Serializable {
    public String productName,productSn,productImei,userName,userContact,productDescription;
    public FixInfoModel(){};
    public FixInfoModel(String name,String sn,String imei,String uName,String contact,String description){
        productName=name;
        productSn=sn;
        productImei=imei;
        userContact=contact;
        userName=uName;
        productDescription=description;
    }
}
