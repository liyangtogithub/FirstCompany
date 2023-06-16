package com.desay.iwan2.common.server.ble.handler;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import com.desay.fitband.R;
import com.desay.iwan2.common.api.ble.BleApi1;
import com.desay.iwan2.common.api.ble.BleApi1.Callback;
import com.desay.iwan2.common.api.ble.BleApi1.DefaultCallback;
import com.desay.iwan2.common.contant.SystemContant;
import com.desay.iwan2.common.db.entity.Other;
import com.desay.iwan2.common.db.entity.User;
import com.desay.iwan2.common.server.BtDevServer;
import com.desay.iwan2.common.server.LoginInfoServer;
import com.desay.iwan2.common.server.MacServer;
import com.desay.iwan2.common.server.OtherServer;
import com.desay.iwan2.common.server.SetServer;
import com.desay.iwan2.common.server.UserInfoServer;
import com.desay.iwan2.common.server.ble.BleManager;
import com.desay.iwan2.common.server.ble.OrderQueue;
import com.desay.iwan2.common.server.ble.OrderQueue.Cmd;
import com.desay.iwan2.module.MainActivity;
import com.desay.iwan2.module.MainFragment;
import com.desay.iwan2.module.alarmclock.AlertActivity;
import com.desay.iwan2.module.band.BandConnectFragment;
import com.desay.iwan2.module.band.BandManageFragment;
import com.desay.iwan2.module.band.BandSleepFragment;
import com.desay.iwan2.module.loadfile.UpdateManager;
import com.desay.iwan2.module.music.MusicAlarmManagerReceiver;
import com.desay.iwan2.module.music.MusicMain;
import com.desay.iwan2.module.music.MusicService;
import com.desay.iwan2.module.music.sportDB;
import dolphin.tools.util.LogUtil;
import dolphin.tools.util.ToastUtil;

import java.util.Calendar;
import java.util.Date;

public class BandManager {
    public static boolean enableBand = false;
    public static SoundPool soundPoolBeep = null;
    public static MediaPlayer mp = null;

    public static void deleteOder(Context context, String id) {
//		OrderQueue.BleResponse bleResponse = new OrderQueue.BleResponse();
//        bleResponse.id = id;
//        OrderQueue.response(context, bleResponse);
        OrderQueue.response(context, id);
    }

    public static void cancelBindMode() {
        enableBand = false;
    }

    public static void bandEnable(Context context) {
        deleteOder(context, BleApi1.BizApi.enable);
    	if (enableBand == true)
            cancelBindMode();
    	context.sendBroadcast(new Intent(BandConnectFragment.BROADCAST_BOND_OK));
    	BleApi1.BizApi.setSlpTime( context, BandSleepFragment.SLPTIME);
    }

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static void bandBind(Context context, String contentStr, BluetoothGatt gatt) {
        deleteOder(context, BleApi1.BizApi.bind);
        if (contentStr != null && "OK".equalsIgnoreCase(contentStr)) {
            String newMac = gatt.getDevice().getAddress();
            try {
                MacServer macServer = new MacServer(context);
                macServer.storeMac(newMac, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            BleApi1.BizApi.getSn(context);
            BleApi1.VersionApi.getNordicVersion(context);
        }
    }

    public static void storeOrUpgradeCore(Context context, String coreVertion) {
        deleteOder(context, BleApi1.BizApi.getEfm32Version);
        try {
            BtDevServer btDevServer = new BtDevServer(context);
            btDevServer.storeCoreVersion(null, coreVertion);
            if (BandManager.enableBand) {
                LoginInfoServer.LoginInfo loginInfo = new LoginInfoServer(context).getLoginInfo();
                if (loginInfo == null) {
                    ToastUtil.shortShow(context, context.getString(R.string.toast_no_user));
                    return;
                }
                BleApi1.BizApi.setName(context, loginInfo.getAccount());
                return;
            }
            
            int coreVer = Integer.parseInt(coreVertion);
            Other netEfm32Ver = new OtherServer(context).getOther(null, Other.Type.netEf32Ver);
            String netCoreVertion = netEfm32Ver == null ? "0" : netEfm32Ver.getValue();
            int netVer = 0;
            if (netCoreVertion != null)
                netVer = Integer.parseInt(netCoreVertion);
            //点取消，不再升级EFM32
            Other coreNoticeVerOther = new OtherServer(context).getOther(null, Other.Type.coreNoticeVer);
            if (coreNoticeVerOther != null && coreNoticeVerOther.getValue() != null &&
            	!BandManageFragment.manualUpgrade){
            	LogUtil.i(" coreNoticeVer=="+coreNoticeVerOther.getValue() );
            	int coreNoticeVer = Integer.parseInt(coreNoticeVerOther.getValue());
            	if ( netVer == coreNoticeVer )
				return;
			}
            
            if (coreVer < netVer) {
                LogUtil.i("UpdateManager.upgradeModel (EFM = 1)==" + UpdateManager.upgradeModel);
                if (UpdateManager.upgradeModel == MainFragment.HANDLE_UPGRADE_NORDIC)
                    return;
                UpdateManager.upgradeModel = MainFragment.HANDLE_UPGRADE_BAND;
                Intent intent = new Intent(MainFragment.UPGRADE_DIALOG);
                intent.putExtra(BleManager.WHICH_DFU_MODEL_NAME, MainFragment.HANDLE_UPGRADE_BAND);
                context.sendBroadcast(intent);
                context.sendBroadcast(new Intent(BandManageFragment.BAND_UPGRADE));
                LogUtil.i("netCoreVertion==" + netCoreVertion + "....bandCoreVer==" + coreVer);
            }else if (BandManageFragment.manualUpgrade) {
            	BleApi1.VersionApi.getNordicVersion(context);
			}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getBandEfm32Version(final Context context) {
        Callback[] callbacks = {new DefaultCallback() {
            @Override
            public void onPopFromQueue(Cmd cmd) {
                super.onPopFromQueue(cmd);
                if (cmd.retryCount == 0 && UpdateManager.upgradeModel != MainFragment.HANDLE_UPGRADE_NORDIC) {
                    UpdateManager.upgradeModel = MainFragment.HANDLE_UPGRADE_BAND;
                    BleApi1.VersionApi.setDfuMode4Efm32(context);
                    LogUtil.i("EFM32升级纠错*************setDfuMode4Efm32==");
                }
            }
        }};
        BleApi1.BizApi.getEfm32Version(context, callbacks);
    }

    public static void setBandAlarmClock(final Context context, String str) {
        try {
            final OtherServer otherServer = new OtherServer(context);
            final Other alarmToBandOther = otherServer.getOther(null, Other.Type.alarmToBand);
            String alarmToBandString = alarmToBandOther == null ? "0" : alarmToBandOther.getValue();
            if ("0".equals(alarmToBandString)) {
                Callback[] callbacks = {new DefaultCallback() {
                    @Override
                    public void onPopFromQueue(Cmd cmd) {
                        super.onPopFromQueue(cmd);
                        if (cmd.retryCount == -1) {
                            try {
                            	if (alarmToBandOther!=null){
                            		alarmToBandOther.setValue("1");
                                    otherServer.update(alarmToBandOther);
							      }
                            	else {
                            		otherServer.createOrUpdate(null, Other.Type.alarmToBand, "1");
								}
                                
                                BandManager.bandAlertBroadcast(context, AlertActivity.UPDATE_ALARM);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }};
                BleApi1.BizApi.setAlarmClock(context, str, callbacks);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void setBandAlarmClock2(final Context context, String str) {
    	try {
    		final OtherServer otherServer = new OtherServer(context);
    		final Other alarmToBandOther = otherServer.getOther(null, Other.Type.alarmToBand2);
    		String alarmToBandString = alarmToBandOther == null ? "0" : alarmToBandOther.getValue();
    		LogUtil.i("alarmToBandString=="+alarmToBandString);
    		if ("0".equals(alarmToBandString)) {
    			Callback[] callbacks = {new DefaultCallback() {
    				@Override
    				public void onPopFromQueue(Cmd cmd) {
    					super.onPopFromQueue(cmd);
    					if (cmd.retryCount == -1) {
    						try {
    							if (alarmToBandOther!=null){
    								alarmToBandOther.setValue("1");
    								otherServer.update(alarmToBandOther);
    							}
    							else {
    								otherServer.createOrUpdate(null, Other.Type.alarmToBand2, "1");
    							}
    							BandManager.bandAlertBroadcast(context, AlertActivity.UPDATE_ALARM);
    						} catch (Exception e) {
    							e.printStackTrace();
    						}
    					}
    				}
    			}};
    			BleApi1.BizApi.setAlarmClock2(context, str, callbacks);
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

    public static void setBandSitAlarm(final Context context, String str) {
        try {
            final OtherServer otherServer = new OtherServer(context);
            final Other sedentaryToBandOther = otherServer.getOther(null, Other.Type.sedentaryToBand);
            String sedentaryToBandString = sedentaryToBandOther == null ? "0" : sedentaryToBandOther.getValue();
            if ("0".equals(sedentaryToBandString)) {
                Callback[] callbacks = {new DefaultCallback() {
                    @Override
                    public void onPopFromQueue(Cmd cmd) {
                        super.onPopFromQueue(cmd);
                        if (cmd.retryCount == -1) {
                            try {
                            	if (sedentaryToBandOther!=null){
                            		sedentaryToBandOther.setValue("1");
                            		otherServer.update(sedentaryToBandOther);
							      }
                            	else {
                            		otherServer.createOrUpdate(null, Other.Type.sedentaryToBand, "1");
								}
                                BandManager.bandAlertBroadcast(context, AlertActivity.UPDATE_SIT);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }};
                BleApi1.BizApi.setSitAlarm(context, str, callbacks);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void storeOrUpgradeNordic(Context context, String notifyString) {
        deleteOder(context, BleApi1.VersionApi.getNordicVersion);
        try {
            String[] strArr0 = notifyString.split(":");
            String contentStr = null;
            if (strArr0.length > 1)
                contentStr = strArr0[1].substring(0, strArr0[1].indexOf("\r\n"));
            BtDevServer btDevServer = new BtDevServer(context);
            btDevServer.storeNordicVersion(null, contentStr);
            if (BandManager.enableBand) {
                getBandEfm32Version(context);
                return;
            }
            
            int nordicVer = Integer.parseInt(contentStr);
            Other netNordicVer = new OtherServer(context).getOther(null, Other.Type.netNodicVer);
            String netNordicVertion = netNordicVer == null ? "0" : netNordicVer.getValue();
            int netVer = 0;
            if (netNordicVertion != null)
                netVer = Integer.parseInt(netNordicVertion);
            //点取消，当天不再升级蓝牙
            Other blueNoticeVerOther = new OtherServer(context).getOther(null, Other.Type.blueNoticeVer);
            if (blueNoticeVerOther != null && blueNoticeVerOther.getValue() != null &&
            	!BandManageFragment.manualUpgrade){
            	LogUtil.i(" blueNoticeVer=="+blueNoticeVerOther.getValue() );
            	int blueNoticeVer = Integer.parseInt(blueNoticeVerOther.getValue());
            	if ( netVer == blueNoticeVer )
				return;
			}
            
            if (nordicVer < netVer) {
            	LogUtil.i("UpdateManager.upgradeModel (Nordic=2)==" + UpdateManager.upgradeModel);
                if (UpdateManager.upgradeModel == MainFragment.HANDLE_UPGRADE_BAND)
                    return;
                UpdateManager.upgradeModel = MainFragment.HANDLE_UPGRADE_NORDIC;
                Intent intent = new Intent(MainFragment.UPGRADE_DIALOG);
                intent.putExtra(BleManager.WHICH_DFU_MODEL_NAME, MainFragment.HANDLE_UPGRADE_NORDIC);
                context.sendBroadcast(intent);
                context.sendBroadcast(new Intent(BandManageFragment.BAND_UPGRADE));
                LogUtil.i("netNordicVertion==" + netNordicVertion + "....bandNordicVer==" + nordicVer);
            }else if (BandManageFragment.manualUpgrade) {
                context.sendBroadcast(new Intent(BandManageFragment.BAND_NO_UPGRADE));
			}

        } catch (Exception e) {
            LogUtil.i("Exception notifyString==" + notifyString);
            e.printStackTrace();
        }
    }

    public static void bandName(Context context) {
        deleteOder(context, BleApi1.BizApi.setName);
        User userInfo = null;
        try {
            userInfo = new UserInfoServer(context).getUserInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (userInfo == null) {
            ToastUtil.shortShow(context, context.getString(R.string.toast_no_user));
            return;
        }
        if (enableBand)
            BleApi1.BizApi.setHeight(context, Integer.valueOf(userInfo.getHeight()));
    }

    public static void bandHeight(Context context) {
        deleteOder(context, BleApi1.BizApi.setHeight);
        User userInfo = null;
        try {
            userInfo = new UserInfoServer(context).getUserInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (userInfo == null) {
            ToastUtil.shortShow(context, context.getString(R.string.toast_no_user));
            return;
        }
        if (enableBand)
            BleApi1.BizApi.setWeight(context, Integer.valueOf(userInfo.getWeight()));
        else {
            MainActivity.sendBroadcast(context, MainActivity.CASE_SHOW_DIALOG_2, 97, null);
        }
    }

    public static void bandWeight(Context context) {
        deleteOder(context, BleApi1.BizApi.setWeight);
        if (enableBand)
            BleApi1.BizApi.setTime(context, SystemContant.timeFormat9.format(new Date()));
        else {
            MainActivity.sendBroadcast(context, MainActivity.CASE_SHOW_DIALOG_2, 98, null);
        }
    }

    public static void bandTime(Context context) {
        deleteOder(context, BleApi1.BizApi.setTime);
        if (enableBand)
            BleApi1.BizApi.enable(context);
        else {
//            MainActivity.sendBroadcast(context, MainActivity.CASE_SHOW_DIALOG_2, 2, null);
        }
    }

    public static void bandSleep(Context context, String string) {
        boolean musicEnable = false;
        String musicEnableString = null;
        try {
            SetServer setServer = new SetServer(context);
            String musicSetString = setServer.getMusicSet();
            if (musicSetString != null && musicSetString.indexOf(",") != -1) {
                musicEnableString = musicSetString.substring(0, 2);
                if ("01".equals(musicEnableString))
                    musicEnable = true;
            }

            if (string.indexOf("0") != -1) {
                //isPoolPlaying = false;;
                exitAllMusic(context);
            } else if (string.indexOf("1") != -1 && musicEnable) {
                //isPoolPlaying = true;
                selectMusicToPlay(context, R.raw.sleep1, musicSetString);
                selectMixedMusic(context, R.raw.sleep1_bo);
            } else if (string.indexOf("2") != -1 && musicEnable && MusicService.isPlaying) {
                selectMusicToPlay(context, R.raw.sleep2, musicSetString);
                selectMixedMusic(context, R.raw.sleep2_bo);
            } else if (string.indexOf("3") != -1 && musicEnable && MusicService.isPlaying) {
            	selectMusicToPlay(context, R.raw.sleep3, musicSetString);
            	selectMixedMusic(context, R.raw.sleep3_bo);
            } else if (string.indexOf("4") != -1 && musicEnable && MusicService.isPlaying) {
                selectMusicToPlay(context, R.raw.sleep4, musicSetString);
                selectMixedMusic(context, R.raw.sleep4_bo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static void exitAllMusic(Context context) {
        stopMixedMusic();
        context.stopService(new Intent(context,MusicService.class));
        context.sendBroadcast(new Intent(MusicMain.MUSIC_UPDATE_UI));
        context.sendBroadcast(new Intent(MusicService.RECEIVER_EXITSPORT));
        context.sendBroadcast(new Intent(MusicService.NOTIFICATION_EXIT));
    }

    private static void selectMusicToPlay(Context context, int musicId,
                                          String musicSetString) {
        sportDB sportDb = new sportDB(context);
        MusicService.list = sportDb.getSelfMusic();
        Intent intent = new Intent();
        intent.setClass(context, MusicService.class);
        intent.putExtra("op", MusicMain.MUSIC_BEGIN);
        intent.putExtra("position", musicId);
        intent.putExtra(MusicService.MUSIC_MODEL, MusicService.MUSIC_MODEL_SELF);
        context.startService(intent);
        LogUtil.i("musicSetString=="+musicSetString);
        if (musicId==R.raw.sleep1){
            try{
        	    String musicTimetring = musicSetString.substring(musicSetString.indexOf(",")+1, musicSetString.length());
                LogUtil.i("musicTimetring=="+musicTimetring);
                setMusicAlarm(context, musicTimetring);
			   } catch (Exception e){
				        e.printStackTrace();
				        setMusicAlarm(context, "30");
			    }
		}
    }

    private static void setMusicAlarm(Context context, String musicTimetring) {
    	LogUtil.i("setMusicAlarm=="+musicTimetring);
        int musicTime = Integer.parseInt(musicTimetring);
        Calendar calendar = Calendar.getInstance();
        long todayTime = calendar.getTimeInMillis() + musicTime * 60 * 1000;

        PendingIntent pendIntent = PendingIntent.getBroadcast(context, 0, new Intent(
                context, MusicAlarmManagerReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        am.set(AlarmManager.RTC, todayTime, pendIntent);
    }

    public static void bandBeep(Context context) {
        if (soundPoolBeep == null) {
            soundPoolBeep = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);
            soundPoolBeep.load(context, R.raw.alarm1, 1);
        }
        soundPoolBeep.play(1, 1, 1, 0, 0, 1);
    }

    public static void selectMixedMusic(Context context, final int soundId) {
        stopMixedMusic();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mp = MediaPlayer.create(context, soundId);
        mp.setLooping(true);
        mp.start();
        if (soundId == R.raw.sleep1_bo)
            mp.setVolume(0.2f, 0.2f);
    }

    public static void stopMixedMusic() {
        if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }
    }

    public static void bandAlertBroadcast(Context context, int alert) {
        Intent intent = new Intent();
        intent.setAction(AlertActivity.BROADCAST_UPDATE_AlERT);
        intent.putExtra(AlertActivity.UPDATE_AlERT, alert);
        context.sendBroadcast(intent);
    }

}
