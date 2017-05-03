package cn.cpuboom.geniusbarassistant.model;

import java.io.Serializable;

/**
 * Created by zhuyuhui on 2016/8/24.
 */
public class FixQueueItemModel extends FixInfoModel implements Serializable {
    public static final byte FIX_STATUS_TODO=0;//维修正在排队
    public static final byte FIX_STATUS_DOING=1;//维修正在进行
    public static final byte FIX_STATUS_DONE=2;//维修已完成，等待app端处理
    public static final byte FIX_STATUS_NONE=3;//目前暂无维修记录
    public static final byte FIX_STATUS_CANCELLED=4;//维修已被取消
    public static final byte FIX_STATUS_FINAL=5;//维修已在app端确认完成
    public int id;
    public int fixStaff;
    public long fixStartTime;
    public byte fixStatus;
    public FixQueueItemModel(String code,String sn,String imei,String userName,String contact,String description,int xid,int staff,long startTime){
        super(code,sn,imei,userName,contact,description);
        id=xid;
        fixStaff=staff;
        fixStartTime=startTime;
        fixStatus=FIX_STATUS_TODO;
    }

}
