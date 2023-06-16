package com.desay.iwan2.module;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.onekeyshare.OnekeyShare;
import com.actionbarsherlock.internal.view.menu.ActionMenuView;
import com.desay.fitband.R;
import com.desay.iwan2.common.app.broadcastreceiver.BaseBroadcastReceiver;
import com.desay.iwan2.common.app.fragment.BaseFragment;
import com.desay.iwan2.common.app.service.MyService;
import com.desay.iwan2.common.contant.PathContant;
import com.desay.iwan2.common.contant.SystemContant;
import com.desay.iwan2.common.db.DatabaseHelper;
import com.desay.iwan2.common.db.entity.*;
import com.desay.iwan2.common.server.*;
import com.desay.iwan2.common.server.ble.BleCallback;
import com.desay.iwan2.common.server.ble.BleManager;
import com.desay.iwan2.common.server.ble.handler.BandManager;
import com.desay.iwan2.module.band.BandManageActivity;
import com.desay.iwan2.module.dfu.DfuActivity;
import com.desay.iwan2.module.loadfile.DownLoadHandle;
import com.desay.iwan2.module.loadfile.UpdateManager;
import com.desay.iwan2.module.money.MoneyActivity;
import com.desay.iwan2.module.share.CutScreen;
import com.desay.iwan2.module.share.ShareContentCustomizeDemo;
import com.desay.iwan2.module.sleep.SleepActivity;
import com.desay.iwan2.module.sport.SportActivity;
import com.desay.iwan2.module.summary.widget.DoughnutProgressBar1;
import com.desay.iwan2.module.summary.widget.PlotGradientColorBarRuler;
import com.desay.iwan2.module.summary.widget.TrackProgressBar;
import com.desay.iwan2.module.upgrade.UpGradeDialog;
import com.desay.iwan2.util.SdCardUtil;
import com.desay.iwan2.util.TimeUtil;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import dolphin.tools.ble.BleConnectState;
import dolphin.tools.ble.BleServer;
import dolphin.tools.ble.BleUtil;
import dolphin.tools.common.download.DownloadManagerBroadcastReceiver;
import dolphin.tools.common.os.VoidAsyncTask;
import dolphin.tools.common.widget.DoughnutProgressBar;
import dolphin.tools.util.*;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


@SuppressLint("ValidFragment")
public class MainFragment extends BaseFragment implements OnClickListener {

    private static Activity act;
    private ShowSlidInterface showInterface;
    private ViewPager viewPager;
    View rootView = null;
    String iconPathString = null;
    TextView tv_main_time = null;
    ImageView main_conn_title = null;
    ImageView main_conn_title_anim = null;
    Animation operatingAnim = null;
    Calendar currentCalendar;
    int todayPage = 0;
    int allSize = 366;
    private Date[] dateRange;
    DayServer dayServer = null;
    private DatabaseHelper dbHelper;
    private Dao<Sleep, Integer> sleepDao;
    private Dao<Sport, Integer> sportDao;
    private Dao<Day, Integer> dayDao;
    SleepStateServer sleepStateServer = null;
    AimServer aimServer = null;
    Other sportOther = null;
    Other sleepOther = null;
    int aimStepNum = 10000;
    int sleepAimTime = 0;
    public static final String UPGRADE_ABLE = "com.desay.iwan2.upgrade_able";
    public static final String UPGRADE_APP = "com.desay.iwan2.upgrade_app";
    public static final String UPGRADE_NORDIC = "com.desay.iwan2.upgrade_nordic_alert";
    public static final String UPGRADE_BAND = "com.desay.iwan2.upgrade_band_alert";
    public static final String UPDATE_CONNECT_STATE = "com.desay.iwan2.update_connect_state";
    public static final String UPGRADE_DIALOG = "com.desay.iwan2.upgrade_dialog";
    // public static final int HANDLE_CONNECT_STATE = 0;
    public static final int HANDLE_UPGRADE_BAND = 1;
    public static final int HANDLE_UPGRADE_NORDIC = 2;
    public static final int HANDLE_UPGRADE_APP = 3;
    public static final int HANDLE_UPGRADE_DIALOG = 4;
    DownloadManagerBroadcastReceiver broadcastReceiver = null;
    MyViewPagerAdapter myViewPagerAdapter = null;
    boolean isDestroy = false;
    private boolean isConnect = false;
    private boolean isAppUpgrade = true;
    int vertion = 0;
    String vertionAddress = null;
    String md5_or_explains = null;
    String bandExplains = null;
    long timedata0 = 0;
    long timeToday = 0;
    private boolean flagSync = true;

//    public MainFragment() {
//    }
//
//    public MainFragment(boolean isLoadHistory) {
//        this.isLoadHistory = isLoadHistory;
//    }

    private BaseBroadcastReceiver updateReceiver = new BaseBroadcastReceiver() {

        @Override
        public void onReceive1(Context context, Intent intent) throws Throwable {
            String actString = intent.getAction();
            if (UPGRADE_ABLE.equals(actString)) {
                isAppUpgrade = false;
                return;
            }
            if (!isAppUpgrade) {
                isAppUpgrade = true;
                return;
            }
            if (UPDATE_CONNECT_STATE.equals(actString)) {
                isConnect = intent.getBooleanExtra(BleCallback.CONNECT_STATE, false);
                if (!BandManager.enableBand)
                initConnectState();
                for (int i = 0; i < viewPager.getChildCount(); i++) {
                    View v = viewPager.getChildAt(i);
                    updateUi(v, (Integer) v.getTag());
                }
            } else if (UPGRADE_APP.equals(actString)) {
                vertion = intent.getIntExtra(VersionServer.APP_VERTION, 0);
                vertionAddress = intent.getStringExtra(VersionServer.VERTION_ADDRESS);
                md5_or_explains = intent.getStringExtra(VersionServer.UPGRADE_MD5_OR_EXPLAIN);
                updateHandler.sendEmptyMessage(HANDLE_UPGRADE_APP);
            } else if (UPGRADE_NORDIC.equals(actString)) {
                vertion = intent.getIntExtra(VersionServer.NORDIC_VERTION, 0);
                vertionAddress = intent.getStringExtra(VersionServer.VERTION_ADDRESS);
                md5_or_explains = intent.getStringExtra(VersionServer.UPGRADE_MD5_OR_EXPLAIN);
                bandExplains = intent.getStringExtra(VersionServer.UPGRADE_BAND_EXPLAIN);
                updateHandler.sendEmptyMessage(HANDLE_UPGRADE_NORDIC);
            } else if (UPGRADE_BAND.equals(actString)) {
                vertion = intent.getIntExtra(VersionServer.BAND_VERTION, 0);
                vertionAddress = intent.getStringExtra(VersionServer.VERTION_ADDRESS);
                md5_or_explains = intent.getStringExtra(VersionServer.UPGRADE_MD5_OR_EXPLAIN);
                bandExplains = intent.getStringExtra(VersionServer.UPGRADE_BAND_EXPLAIN);
                updateHandler.sendEmptyMessage(HANDLE_UPGRADE_BAND);
            } else if (UPGRADE_DIALOG.equals(actString)) {
                boolean isUpgradeModel = intent.getBooleanExtra(BleManager.IS_DFU_MODEL_NAME, false);
                int whichUpgradeModel = intent.getIntExtra(BleManager.WHICH_DFU_MODEL_NAME, 0);
                Message msg = new Message();
                msg.what = HANDLE_UPGRADE_DIALOG;
                msg.obj = isUpgradeModel;
                msg.arg1 = whichUpgradeModel;
                updateHandler.sendMessage(msg);
            }
        }
    };

    private BaseBroadcastReceiver receiver = new BaseBroadcastReceiver() {
        @Override
        public void onReceive1(Context context, Intent intent) throws Throwable {
            String action = intent.getAction();
            if (SyncServer.ACTION_SYNC_SUCCESS.equalsIgnoreCase(action)) {
                initViewPagerData();
                myViewPagerAdapter.setSize(5);
                synchronized (myViewPagerAdapter) {
                    myViewPagerAdapter.notifyDataSetChanged();
                }
                for (int i = 0; i < viewPager.getChildCount(); i++) {
                    View v = viewPager.getChildAt(i);
                    updateUi(v, (Integer) v.getTag());
                }
                Calendar cal = Calendar.getInstance();
                cal.setTime(dateRange[0]);

                viewPager.setCurrentItem((int) ((currentCalendar.getTimeInMillis() - cal.getTimeInMillis())
                        / SystemContant.millisecondInDay), false);
                flagSync=true;
            }
        }
    };

    Handler updateHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case HANDLE_UPGRADE_APP:
                    new UpGradeDialog().showAlertDialog(act, vertion, vertionAddress, md5_or_explains);//弹出升级对话框
                    break;
                case HANDLE_UPGRADE_NORDIC:
                    new DownLoadHandle(act, vertion, HANDLE_UPGRADE_NORDIC, vertionAddress, md5_or_explains, bandExplains);
                    break;
                case HANDLE_UPGRADE_BAND:
                    new DownLoadHandle(act, vertion, HANDLE_UPGRADE_BAND, vertionAddress, md5_or_explains, bandExplains);
                    break;
                case HANDLE_UPGRADE_DIALOG:
                    if ((Boolean) msg.obj)
                        UpdateManager.showNoticeDialog(act, true, msg.arg1);
                    else {
                        UpdateManager.showNoticeDialog(act, false, msg.arg1);
                    }
                    break;
            }

        }
    };

    @Override
    public View onCreateView1(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) throws Throwable {
        act = getActivity();

        currentCalendar = Calendar.getInstance();
        currentCalendar.set(Calendar.HOUR_OF_DAY, 0);
        currentCalendar.set(Calendar.MINUTE, 0);
        currentCalendar.set(Calendar.SECOND, 0);
        currentCalendar.set(Calendar.MILLISECOND, 0);

        dbHelper = DatabaseHelper.getDataBaseHelper(act);
        try {
            sleepDao = dbHelper.getSleepDao();
            sportDao = dbHelper.getSportDao();
            dayDao = dbHelper.getDayDao();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        View v = inflater.inflate(R.layout.main_fragment, null);
        initView(v);

        initViewPagerData();
        initViewPager();

        IntentFilter filter = new IntentFilter();
        filter.addAction(UPGRADE_ABLE);
        filter.addAction(UPGRADE_BAND);
        filter.addAction(UPGRADE_NORDIC);
        filter.addAction(UPGRADE_APP);
        filter.addAction(UPDATE_CONNECT_STATE);
        filter.addAction(UPGRADE_DIALOG);
        act.registerReceiver(updateReceiver, filter);

        broadcastReceiver = new DownloadManagerBroadcastReceiver() {
            @Override
            public void onDownloadComplete(Context context, long id, File file) {
                if (file == null) {
                    DownloadManager downloadManager = DownloadUtil.getDownloadManager(context);
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(id);
                    Cursor cursor = downloadManager.query(query);
                    if (cursor == null) {
                        ToastUtil.shortShow(context, "下载文件失败");
                        LogUtil.i("下载文件失败");
                    } else {
                        LogUtil.i("系统下载器下载失败，使用浏览器下载");
                        if (cursor.moveToFirst()) {
                            String uriStr = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI));
                            LogUtil.i("donload uriStr=" + uriStr);
                            Uri uri = Uri.parse(uriStr);
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            act.startActivity(intent);
                        }
                    }
                } else {
                    File apkFile = file;
                    if (SystemStateUtil.isExternalStorageEnable()) {
                        File dir = new File(PathContant.getApkDir(context));
                        boolean isDirExists = dir.exists();
                        if (!isDirExists) {// 判断文件目录是否存在
                            isDirExists = dir.mkdirs();
                            LogUtil.i("创建文件夹=" + isDirExists);
                        }
                        if (isDirExists) {
                            try {
                                File oldFile = new File(dir, file.getName());
                                if (oldFile.exists()) {
                                    FileUtils.forceDelete(oldFile);
                                }
                                FileUtils.copyFileToDirectory(file, dir);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            LogUtil.i("删除缓存文件=" + DownloadUtil.getDownloadManager(context).remove(id));
                            apkFile = new File(dir, file.getName());
                        }
                    }

                    ApkUtil.installApk(context, apkFile);
                }
            }
        };
        broadcastReceiver.register(act);

        act.registerReceiver(receiver, new IntentFilter(SyncServer.ACTION_SYNC_SUCCESS));

        if (NetworkUtil.isConnected(act)) {
            new VoidAsyncTask() {
                @Override
                protected Void doInBackground(Void... params) {
                    boolean isLoadHistory = getArguments().getBoolean("isLoadHistory", false);
//        if (isLoadHistory) {
//            MainActivity.sendBroadcast(act, MainActivity.CASE_SHOW_DIALOG_1,null,
//                    getString(R.string.Menu_Download));
                    try {
                        SyncServer syncServer = SyncServer.getInstance(act);
                        syncServer.network2Local(isLoadHistory, null, null, null);
                        syncServer.local2Network();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
//        }
                    return super.doInBackground(params);
                }
            }.parallelExecute();
        } else {
//            if (tipsView == null) {
                View tipsView = inflater.inflate(R.layout.tips1, null);
                ((TextView) tipsView.findViewById(R.id.text1)).setText(R.string.tips_5);
                ((TextView) tipsView.findViewById(R.id.text2)).setText(R.string.tips_5_1);
//            }

            Toast toast = new Toast(act);
            toast.setView(tipsView);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.show();
        }
//        tipsViewConnect = inflater.inflate(R.layout.tips1, null);
        return v;
    }

	private void showConnectToast(int resTop,int resBotom) {
        View tipsViewConnect = act.getLayoutInflater().inflate(R.layout.tips1, null);
        ((TextView) tipsViewConnect.findViewById(R.id.text1)).setText(resTop);
        ((TextView) tipsViewConnect.findViewById(R.id.text2)).setText(resBotom);
        Toast toast = new Toast(act);
        toast.setView(tipsViewConnect);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
    }

	private View tipsView,tipsViewConnect;

    private void initView(View v) {
        tv_main_time = (TextView) v.findViewById(R.id.tv_main_time);
        tv_main_time.setText(TimeUtil.getDateString(currentCalendar));
        tv_main_time.setOnClickListener(this);
        main_conn_title = (ImageView) v.findViewById(R.id.main_conn_title);
        main_conn_title.setOnClickListener(this);
        main_conn_title_anim = (ImageView) v.findViewById(R.id.main_conn_title_anim);
        viewPager = (ViewPager) v.findViewById(R.id.main_viewpager);
        v.findViewById(R.id.btn_homeAsUpIndicator).setOnClickListener(this);
        v.findViewById(R.id.btn_share).setOnClickListener(this);
        v.findViewById(R.id.btn_share).setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        initConnectState();
    }

    private void initConnectState() {
        if (BleUtil.checkBleEnable(act)) {
            if (isConnect || BleConnectState.CONNECTED == BleServer.getInstance(act).mConnectionState) {
                main_conn_title.setImageResource(R.drawable.main_selector_connect);
                stopScanAnim();
            } else {
            	main_conn_title_anim.setVisibility(View.GONE);
                main_conn_title.setImageResource(R.drawable.main_conn_cut);
                if (UpdateManager.upgradeModel == MainFragment.HANDLE_UPGRADE_APP)//手环在正常模式下
                    startScanAnim();
            }
        } else {
        	main_conn_title_anim.setVisibility(View.GONE);
            main_conn_title.setImageResource(R.drawable.main_conn_cut);
        }
    }


    private void initViewPagerData() {
        try {
            dayServer = new DayServer(act);
            dateRange = dayServer.getRange(null);
            sleepStateServer = new SleepStateServer(act);
            aimServer = new AimServer(act);
            sportOther = aimServer.getSportAim();
            if (sportOther != null) {
                String sportString = sportOther.getValue();
                aimStepNum = Integer.parseInt(sportString);
            }
            sleepOther = aimServer.getSleepAim();
            if (sleepOther != null) {
            	String sleepString = sleepOther.getValue();
                if (sleepString != null) {
                    sleepAimTime = Integer.parseInt(sleepString);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Calendar calendarToday = Calendar.getInstance();
        calendarToday.set(Calendar.HOUR_OF_DAY, 0);
        calendarToday.set(Calendar.MINUTE, 0);
        calendarToday.set(Calendar.SECOND, 0);
        calendarToday.set(Calendar.MILLISECOND, 0);
        timeToday = calendarToday.getTime().getTime();
        long timedata1 = dateRange[1].getTime();
        timedata0 = dateRange[0].getTime();
        if (timeToday >= timedata1) {
            timedata1 = timeToday;
        } else if (timeToday < timedata0) {
            timedata0 = timeToday;
        }
//        allSize = (int) ((timedata1 - timedata0) / SystemContant.millisecondInDay) + 1;

    }

    private void initViewPager() {

        myViewPagerAdapter = new MyViewPagerAdapter(allSize);
        viewPager.setAdapter(myViewPagerAdapter);

        todayPage = (int) ((timeToday - timedata0) / SystemContant.millisecondInDay);
        viewPager.setCurrentItem(todayPage);
        viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
    }

    class MyViewPagerAdapter extends PagerAdapter {
        public int size;

        MyViewPagerAdapter(int size) {
            this.size = size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            LayoutInflater inflater = act.getLayoutInflater();
            View view = inflater.inflate(R.layout.main_fragment_item, null);
            view.findViewById(R.id.layout_sleep).setOnClickListener(MainFragment.this);
            view.findViewById(R.id.layout_sport).setOnClickListener(MainFragment.this);
            view.findViewById(R.id.layout_money).setOnClickListener(MainFragment.this);
            view.findViewById(R.id.go_back).setOnClickListener(MainFragment.this);
            view.findViewById(R.id.go_ahead).setOnClickListener(MainFragment.this);
            view.setTag(position);
            updateUi(view, position);
            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return size;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

    }

    private void updateUi(View v, int position) {

        TextView sleep_deep = (TextView) v.findViewById(R.id.sleep_deep);
        TextView sleep_duraction = (TextView) v.findViewById(R.id.sleep_duraction);
        TextView gold_num = (TextView) v.findViewById(R.id.gold_num);
        DoughnutProgressBar1  sleep_duraction_progress = (DoughnutProgressBar1) 
                                   v.findViewById(R.id.sleep_duraction_progress);
        DoughnutProgressBar1  sleep_deep_progress = (DoughnutProgressBar1) 
                                   v.findViewById(R.id.sleep_deep_progress);
        
        int sleepDuraction = 0;
        int sleepDuractionMax = 0;
        int deepDuration = 0;
        int deepRate = 0;
        int sleepScore = 0;
        long dayGain = position;
        int maxGain = SystemContant.defaultGainMax;
        int realStepNum = position*1000;
        int calorie = position;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(timedata0));
        calendar.add(Calendar.DATE, position);

       /* try {
            User user = new UserInfoServer(act).getUserInfo();
            if (user != null) {
                Day p1 = new Day();
                p1.setUser(user);
                p1.setDate(calendar.getTime());
                List<Day> dayList = dayDao.queryForMatchingArgs(p1);
                Day day = null;
                if (dayList != null && dayList.size() > 0) {
                    day = dayList.get(0);
                    if (day != null) {
                        dayGain = new GainServer(act).getDayGain(day);
                        maxGain = day.getMaxGain() == null ? SystemContant.defaultGainMax : day.getMaxGain();
                        maxGain = maxGain == 0 ? SystemContant.defaultGainMax : maxGain;
                    }

                    //运动data
                    try {
                        Sport sport = new Sport();
                        sport.setDay(day);
                        List<Sport> sportList = sportDao.queryForMatchingArgs(sport);
                        for (Sport sport1 : sportList) {
                            realStepNum += sport1.getStepCount() == null ? 0 : sport1.getStepCount();
                            if (sport1.getCalorie() != null)
                                calorie += (int) (Math.round(sport1.getCalorie()));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // 睡眠data
                    Sleep p2 = new Sleep();
                    p2.setDay(day);
                    List<Sleep> sleepList = sleepDao.queryForMatchingArgs(p2);
                    for (Sleep sleep : sleepList) {
                        if (sleep.getSleepStates() != null && sleep.getSleepStates().size() > 0) {
                            sleepDuraction = sleep.getTotalDuration() == null ? 0 : sleep.getTotalDuration();
                            if (sleepDuraction > sleepDuractionMax) {
                                sleepScore = (int) (sleep.getScore() == null ? 0 : sleep.getScore());
                                sleepDuractionMax = sleepDuraction;

                                GenericRawResults<SleepStateServer.SleepStateDuration> rawResults = sleepStateServer
                                        .getStateDuration(sleep);
                                for (SleepStateServer.SleepStateDuration sleepStateDuration : rawResults) {
                                    switch (SleepState.State.valueOf(sleepStateDuration.state)) {
                                        case deep:
                                            deepDuration = sleepStateDuration.duration == null ? 0 : sleepStateDuration.duration;
                                            break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        // 睡眠UI
        sleep_duraction.setText( getsleepTime(sleepDuractionMax));
        if (sleepAimTime==0){
        	sleepAimTime=(int) SystemContant.defaultSleepAim;
		}
        sleep_duraction_progress.setProgress(sleepDuractionMax*100/sleepAimTime);
        if (sleepDuractionMax != 0)
            deepRate = deepDuration * 100 / sleepDuractionMax;
        sleep_deep.setText(deepRate + "%");
        sleep_deep_progress.setProgress(deepRate);
        sleepScore = (int) (Math.round(sleepScore * 10));
        if (sleepScore > 100)
            sleepScore = 100;
        ((PlotGradientColorBarRuler) v.findViewById(R.id.ruler)).setProgress(sleepScore);
        //金币UI
        gold_num.setText(dayGain + "");
        if (dayGain > maxGain)
            dayGain = maxGain;
        ((DoughnutProgressBar) v.findViewById(R.id.doughnutProgressBar)).setProgress((int) (dayGain * 100 / maxGain));
        //运动UI
        String calorieNotice = getCalorieNotice(calorie);
        TrackProgressBar trackProgressBar = ((TrackProgressBar) v.findViewById(R.id.trackProgressBar));
        trackProgressBar.setText((realStepNum + ""), (getString(R.string.HomeCalLabel) + calorie + " ≈ " + calorieNotice));
        if (realStepNum > aimStepNum)
            realStepNum = aimStepNum;
        if (aimStepNum != 0)
            trackProgressBar.setProgress(realStepNum * 100 / aimStepNum);

    }

    private void startScanAnim() {
    	if (!BluetoothAdapter.getDefaultAdapter().isEnabled()){
    		showConnectToast(R.string.tips_4, R.string.tips_4_1);
    		return;
    	}
		/*try{
			MacServer macServer = new MacServer(act);
			if (macServer == null || macServer.getMac() == null
	                || "".equals(macServer.getMac())) {
				main_conn_title.setImageResource(R.drawable.main_conn_add);
				showConnectToast(R.string.tips_6, R.string.tips_6_1);
				return;
	        }
		} catch (SQLException e){
			e.printStackTrace();
		}*/
		LogUtil.i("operatingAnim=="+operatingAnim);
    	if (operatingAnim != null) return;
    	operatingAnim = AnimationUtils.loadAnimation(act, R.anim.main_connect_anim);
        LinearInterpolator lin = new LinearInterpolator();
        operatingAnim.setInterpolator(lin);
        if (operatingAnim != null) {
            main_conn_title_anim.setVisibility(View.VISIBLE);
            main_conn_title_anim.startAnimation(operatingAnim);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                 stopScanAnim();
            }
        }, DfuActivity.SCAN_DURATION);
        showConnectToast(R.string.tips_8, R.string.tips_8_1);
    }

    private void stopScanAnim() {
        main_conn_title_anim.setVisibility(View.GONE);
        if (operatingAnim != null) {
            main_conn_title_anim.clearAnimation();
            operatingAnim = null;
            if ( !(BleConnectState.CONNECTED == BleServer.getInstance(act).mConnectionState)&& !isDestroy ) {
            	showConnectToast(R.string.tips_7, R.string.tips_7_1);
            }
        }
    }

    private String getsleepTime(int duraction) {
        String sleepTime = "0h0'";
        if (duraction < 60) {
            sleepTime = duraction + "'";
        } else {
            sleepTime = duraction / 60 + "h" + duraction % 60 + "'";
        }
        return sleepTime;
    }

    private String getCalorieNotice(int calorie) {
        String[] calorieArray = getResources().getStringArray(R.array.array_calorie);
        float num = 1;
        String caloriesString = "1.2" + calorieArray[0];

        if (calorie >= 0 && calorie < 103) {
            num = (float) (Math.round(calorie / 86.0 * 10)) / 10;
            caloriesString = num + calorieArray[0];
        } else if (calorie >= 103 && calorie < 160) {
            num = (float) (Math.round(calorie / 120.0 * 10)) / 10;
            caloriesString = num + calorieArray[1];
        } else if (calorie >= 160 && calorie < 363) {
            num = (float) (Math.round(calorie / 200.0 * 10)) / 10;
            caloriesString = num + calorieArray[2];
        } else if (calorie >= 363) {
            num = (float) (Math.round(calorie / 525.0 * 10)) / 10;
            caloriesString = num + calorieArray[3];
        }
        return caloriesString;
    }

    class MyOnPageChangeListener implements OnPageChangeListener {
        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int position) {

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date(timedata0));
            calendar.add(Calendar.DATE, position);
            currentCalendar = calendar;
            tv_main_time.setText(TimeUtil.getDateString(calendar));
        }
    }
   
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_homeAsUpIndicator:
                showInterface.showSlid();
                break;
            case R.id.btn_share:
                rootView = v.getRootView();
                iconPathString = new CutScreen().getBitmappath(rootView);
                showShare(false, null);
                break;
            case R.id.layout_sleep:
                SleepActivity.gotoActivity(act, currentCalendar.getTime());
                break;
            case R.id.layout_sport:
                SportActivity.gotoActivity(act, currentCalendar.getTime());
                break;
            case R.id.layout_money:
                MoneyActivity.gotoActivity(act, currentCalendar.getTime());
                break;
            case R.id.go_back:
                viewPager.arrowScroll(1);//向左
                break;
            case R.id.go_ahead:
                viewPager.arrowScroll(2);//向右
                break;
            case R.id.tv_main_time:
                viewPager.setCurrentItem(todayPage);
                break;
            case R.id.main_conn_title:
            	 try
				{
					new OtherServer(getActivity().getApplicationContext()).createOrUpdate(null, Other.Type.tempTotalStep,
					         "50" + ";" + new SimpleDateFormat(SystemContant.timeFormat7Str).format(new Date()));
				} catch (SQLException e1)
				{
					e1.printStackTrace();
				}
            	 startScanAnim();
               /* if (BleUtil.checkBleEnable(act)) {
                    if (isConnect || BleConnectState.CONNECTED == BleServer.getInstance(act).mConnectionState) {
                    	if (flagSync) {
                    		flagSync = false;
                    		SyncServer.getInstance(act).syncBle();
                    		 new Handler().postDelayed(new Runnable() {
                                 @Override
                                 public void run() {
                                 	flagSync = true;
                                 }
                             }, 10000);
                        } 
                    } else {
                    	try{
                			MacServer macServer = new MacServer(act);
                			if (macServer == null || macServer.getMac() == null
                	                || "".equals(macServer.getMac())) {
                				BandManageActivity.goToActivity(act);
                				return;
                	        }
                		} catch (SQLException e){
                			e.printStackTrace();
                		}
                        startScanAnim();
                    }
                } else {
                    main_conn_title.setImageResource(R.drawable.main_conn_cut);
                }*/
                break;
        }
    }

    private void showShare(boolean silent, String platform) {
        // path  point to /mnt/sdcard/ShareSDK/your-package-name/cache
        // place your images or other cache files into this folder
        // /data/data/ is a private directory, only your app can visit it
        String path = cn.sharesdk.framework.utils.R.getCachePath(act, null);

        final OnekeyShare oks = new OnekeyShare();
        oks.setNotification(R.drawable.ic_launcher, act.getString(R.string.app_name));
        oks.setAddress("12345678901");
        oks.setTitle(getString(R.string.main_share_title));
        oks.setTitleUrl("http://care.desay.com");
        oks.setText(getString(R.string.main_share_content));
        if (SdCardUtil.ExistSDCard() && SdCardUtil.getSDFreeSize() > 1) {
            oks.setImagePath(iconPathString);
            oks.setImageUrl(iconPathString);//这句决定qq图片否
        }
        //oks.setUrl("http://care.desay.com");//这句决定微信图片或链接
        oks.setFilePath(iconPathString);
        oks.setComment(getString(R.string.share));
        oks.setSite(act.getString(R.string.app_name));
        oks.setSiteUrl("http://care.desay.com");
        oks.setVenueName("Southeast in China");
        oks.setVenueDescription("This is a beautiful place!");
        oks.setLatitude(23.016087f);
        oks.setLongitude(114.332708f);
        oks.setSilent(silent);
        if (platform != null) {
            oks.setPlatform(platform);
        }
        oks.setShareContentCustomizeCallback(new ShareContentCustomizeDemo());

        // 去除注释，演示在九宫格设置自定义的图标
        /*Bitmap logo = BitmapFactory.decodeResource(getResources(),
                R.drawable.logo_other);
        String label = getResources().getString(R.string.share_other);
        OnClickListener listener = new OnClickListener() {
            public void onClick(View v) {
                new ShareMessage().shareMsg(getActivity(), "liyang", "15",
                        "100", iconPathString);
                oks.finish();
            }
        };
        oks.setCustomerLogo(logo, label, listener);*/
        oks.show(act);
    }

    @Override
    public void onDestroyView1() throws Throwable {
        if (receiver != null)
            act.unregisterReceiver(receiver);
        if (updateReceiver != null)
            act.unregisterReceiver(updateReceiver);
        broadcastReceiver.unregister(act);
        isDestroy=true;
    }

    public static interface ShowSlidInterface {
        public void showSlid();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.showInterface = (ShowSlidInterface) activity;
        } catch (final Exception e) {
        }
    }
}
