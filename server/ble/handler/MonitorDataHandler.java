package com.desay.iwan2.common.server.ble.handler;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.alibaba.fastjson.JSON;
import com.desay.iwan2.common.api.ble.BleApi1;
import com.desay.iwan2.common.contant.SystemContant;
import com.desay.iwan2.common.db.DatabaseHelper;
import com.desay.iwan2.common.db.entity.*;
import com.desay.iwan2.common.server.*;
import com.desay.iwan2.common.server.ble.OrderQueue;
import com.desay.iwan2.module.MainActivity;
import com.desay.iwan2.module.sport.server.SportAimServer;
import com.desay.iwan2.util.TimeUtil;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.misc.TransactionManager;
import dolphin.tools.util.LogUtil;
import dolphin.tools.util.StringUtil;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created by 方奕峰 on 14-8-1.
 */
public class MonitorDataHandler {

    private Context context;
    private DatabaseHelper dbHelper;
    private Dao<Day, Integer> dayDao;
    private Dao<Sport, Integer> sportDao;
    private Dao<Sleep, Integer> sleepDao;
    private Dao<HeartRate, Integer> heartRateDao;
    private Dao<SleepMotion, Integer> sleepMotionDao;

    private SleepServer sleepServer;

    public List<MonitorData> monitorDataList = new ArrayList<MonitorData>();

    public boolean isReceiving = false;

    private Handler handler;

    public MonitorDataHandler(Context context) throws SQLException {
        this.context = context;
        dbHelper = DatabaseHelper.getDataBaseHelper(context);
        dayDao = dbHelper.getDayDao();
        sleepDao = dbHelper.getSleepDao();
        heartRateDao = dbHelper.getHeartRateDao();
        sleepMotionDao = dbHelper.getSleepMotionDao();
        sportDao = dbHelper.getSportDao();

        sleepServer = new SleepServer(context);

        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                handler = new Handler();
                Looper.loop();
                super.run();
            }
        }.start();
    }

    private synchronized Header paserHeader(String str) {
        String[] arr = str.split(",");
        Header header = new Header();
        header.original = str;
        header.type = Integer.valueOf(arr[0]);
        header.len = Integer.valueOf(arr[1]);
        header.num = Integer.valueOf(arr[2]);
        header.total = Integer.valueOf(arr[3]);
        header.crc = arr[4];
        header.en = arr[5];

        return header;
    }

    public boolean isReceiveAllFinish() {
        if (monitorDataList.size() == 0 ? false : monitorDataList.get(monitorDataList.size() - 1).header.len == 0) {
            isReceiving = false;
            return true;
        }
        return false;
    }

    public void receiveData(byte[] bytes) throws Exception {
        MonitorData monitorData = monitorDataList.get(monitorDataList.size() - 1);
        monitorData.addData(bytes);
        LogUtil.i("接收BLE字节bytes.len=" + bytes.length);
        if (monitorData.data.size() >= SystemContant.monitorDataLength) {
//        if (monitorData.data.size() >= monitorData.header.len) {
            LogUtil.i("接收BLE字节完毕");
            isReceiving = false;
            SyncServer.getInstance(context).syncBleData(monitorData.header.num + 1, new SyncServer.DelayCallback() {
                @Override
                public void callback(OrderQueue.Cmd cmd) {
                    super.callback(cmd);
                    timeoutEvent(cmd.id);
                }
            });
            OrderQueue.response(context, BleApi1.BizApi.requestData + "=" + monitorData.header.num + "\r\n");
        }
    }

    public void generateMotionData(String str) throws Exception {
        MonitorData monitorData = new MonitorData();
        monitorData.header = paserHeader(str);
        if (monitorData.header.num == 0)
            clean();
//        else if (monitorDataList.size() != monitorData.header.num) {
//            clean();
//            return;
//        }

        MonitorData tmp = null;
        for (MonitorData data : monitorDataList) {
            if (data.header.original.equalsIgnoreCase(monitorData.header.original)) {
                tmp = data;
                break;
            }
        }
        if (tmp != null)
            monitorDataList.remove(tmp);

        monitorDataList.add(monitorData);
        isReceiving = true;

        int progress = (monitorData.header.num + 1) * 70 / (monitorData.header.total + 1);
        MainActivity.sendBroadcast(context, MainActivity.CASE_SHOW_DIALOG_2, progress, null);
    }

    public void handle() throws SQLException {
        final ArrayList<MonitorData> sportList = new ArrayList<MonitorData>();
        final ArrayList<MonitorData> realTimeList = new ArrayList<MonitorData>();
        final ArrayList<MonitorData> sleepMotionList = new ArrayList<MonitorData>();
        final ArrayList<MonitorData> sleepHeartList = new ArrayList<MonitorData>();

        for (MonitorData monitorData : monitorDataList) {
            if (monitorData.data.size() > 0) {
                if (monitorData.data.size() != monitorData.header.len) {
                    monitorData.data = monitorData.data.subList(0, monitorData.header.len);
                }
                //
                StringBuffer logBf = new StringBuffer();
                for (byte b : monitorData.data) {
                    logBf.append(((int) b) + ",");
                }
                LogUtil.i("一块监测字节1");
                LogUtil.i1(logBf.toString());
                switch (monitorData.header.type) {
                    case 0://运动
                        sportList.add(monitorData);
                        break;
                    case 1://实时运动
                        realTimeList.add(monitorData);
                        break;
                    case 2://睡眠动作
                        sleepMotionList.add(monitorData);
                        break;
                    case 3://睡眠心率
                        sleepHeartList.add(monitorData);
                        break;
                }
            }
        }
        MainActivity.sendBroadcast(context, MainActivity.CASE_SHOW_DIALOG_2, 75, null);

        LogUtil.i("monitorDataList.size()=" + monitorDataList.size());
        LogUtil.i("sportList.size()=" + sportList.size());
        LogUtil.i("realTimeList.size()=" + realTimeList.size());
        LogUtil.i("sleepMotionList.size()=" + sleepMotionList.size());
        LogUtil.i("sleepHeartList.size()=" + sleepHeartList.size());
        TransactionManager.callInTransaction(dbHelper.getConnectionSource(), new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                saveMonitorData(sportList, realTimeList, sleepMotionList, sleepHeartList);
                return null;
            }
        });

        clean();

        new SportServer(context).local2Network(null);
        sleepServer.local2Network(null);
    }

    private void saveMonitorData(List<MonitorData> sportList, List<MonitorData> realTimeList,
                                 List<MonitorData> sleepMotionList, List<MonitorData> sleepHeartList) throws SQLException {
        User user = new UserInfoServer(context).getUserInfo();
        try {
            saveSportMonitorData(user, sportList);
            SyncServer.syncSuccess(context);
            MainActivity.sendBroadcast(context, MainActivity.CASE_SHOW_DIALOG_2, 80, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
//            saveSportMonitorData(user, realTimeList);//8位

        try {
            saveSleepMotionMonitorData(user, sleepMotionList);
            MainActivity.sendBroadcast(context, MainActivity.CASE_SHOW_DIALOG_2, 85, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            saveSleepHeartMonitorData(user, sleepHeartList);
            MainActivity.sendBroadcast(context, MainActivity.CASE_SHOW_DIALOG_2, 99, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<int[]> splitData(List<Byte> bytes, int size) {
        int[] bs = null;
        List<int[]> l = new ArrayList<int[]>();

        for (int i = 0; i < bytes.size(); i++) {
            if (i % size == 0) {
                bs = new int[size];
                l.add(bs);
            }
            bs[i % size] = bytes.get(i) & 0xff;
        }
        return l;
    }

    private void saveSportMonitorData(User user, List<MonitorData> sportList) throws Exception {
//        Day lastDay = null;
//        Date lastTime = null;
//        if (sportList.size() == 0) {
        for (MonitorData monitorData : sportList) {
            List<int[]> l = splitData(monitorData.data, 6);

            for (int[] bsTmp : l) {
                LogUtil.i("bsTmp.length=" + bsTmp.length);
                StringBuffer sb = new StringBuffer();
                for (int b : bsTmp) {
                    sb.append(b + " , ");
                }
                LogUtil.i("bsTmp=" + sb.toString());
                ParserData parserData = parser(bsTmp, false);
                if (parserData == null) continue;
                LogUtil.i("运动监测数据0：");
                LogUtil.i(JSON.toJSONString(parserData));
                if (parserData.flag == 0) {
                } else if (parserData.flag == 3) {
                } else {
                    Date time = transformTime(parserData.secondTime);

                    Day day = generateDay(user, time);

//                    lastTime = time;
//                    lastDay = day;

                    if (parserData.value == 0) {
                        continue;
                    }

//                    Sport sport = new Sport();
//                    sport.setDay(day);
//                    sport.setStartTime(time);
////                    Calendar calendar = Calendar.getInstance();
////                    calendar.setTime(time);
////                    calendar.add(Calendar.MINUTE, SystemContant.sportMotionInterval);
////                    sport.setEndTime(calendar.getTime());
//                    sport.setTypeCode(0);//TODO 运动类型？
//                    int height = Integer.valueOf(user.getHeight());
//                    height = height == 0 ? SystemContant.defaultHeightMin : height;
//                    sport.setDistance((int) SportAimServer.getDistanceByStep(parserData.value, height));
//                    double weight = Double.valueOf(user.getWeight());
//                    weight = weight == 0 ? SystemContant.defaultWeightMin : weight;
//                    sport.setCalorie((float) SportAimServer.getCalorie(weight, sport.getDistance()));
//                    sport.setAerobics(parserData.flag == 2);
//                    sport.setStepCount(parserData.value);
//                    sportDao.create(sport);
                    Sport sport = createSport(user, day, time, parserData.value, parserData.flag == 2);

                    logSport(sport);
                }
            }
        }
//        }
        //
        OtherServer otherServer = new OtherServer(context);
        Other tempTotalStep = otherServer.getOther(user, Other.Type.tempTotalStep);
        if (tempTotalStep != null) {
            Calendar todayCal = Calendar.getInstance();
            Date date = todayCal.getTime();
            TimeUtil.getDateStart(todayCal);
            DayServer dayServer = new DayServer(context);
            Day today = dayServer.getDay(user, todayCal.getTime());
            Date date1 = null;
            int totalStep = 0;
//            try {
            if (!StringUtil.isBlank(tempTotalStep.getValue())) {
                String[] arr = tempTotalStep.getValue().split(";");
                totalStep = Integer.valueOf(arr[0]);

                date1 = SystemContant.timeFormat7.parse(arr[1]);

                if (Math.abs(date.getTime() - date1.getTime()) < 3600000L) {
                    if (today == null) {
                        today = generateDay(user, todayCal.getTime());
                    }
//                        syncLastSport(user, totalStep, today, date);
                    int appStep = getDayTotalStep(today);
                    if (totalStep < appStep) {
                        todayCal.add(Calendar.DATE, 1);
                        todayCal.add(Calendar.MINUTE, 5);
                        long exclusionTime = todayCal.getTimeInMillis();
                        if (date1 != null && date1.getTime() < exclusionTime) {
                            BleApi1.BizApi.setStep(context, appStep);
                        }
                    }
                }
            }
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                int appStep = getDayTotalStep(today);
//                if (totalStep < appStep) {
//                    todayCal.add(Calendar.DATE, 1);
//                    todayCal.add(Calendar.MINUTE, 5);
//                    long exclusionTime = todayCal.getTimeInMillis();
//                    if (date1 != null && date1.getTime() < exclusionTime) {
//                        BleApi1.BizApi.setStep(context, appStep);
//                    }
//                }
//            }
        }
    }

    private int getDayTotalStep(Day day) throws SQLException {
        int sum = 0;
        if (day != null) {
            String sql = "select sum(" + Sport.COUNT + ")" +
                    " from " + Sport.TABLE +
                    " where " + Sport.DAY_ID + " = '" + day.getId() + "'";
            GenericRawResults<Integer> rawResults = sleepDao.queryRaw(
                    sql.toString(), new RawRowMapper<Integer>() {
                        public Integer mapRow(String[] columnNames,
                                              String[] resultColumns) {
                            return StringUtil.isBlank(resultColumns[0]) ? 0 : Integer.valueOf(resultColumns[0]);
                        }
                    });
            if (rawResults != null)
                for (Integer rawResult : rawResults) {
                    sum += rawResult;
                }
        }
        LogUtil.i("app当前今天总步数=" + sum + ";day=" + SystemContant.timeFormat12.format(day.getDate()));
        return sum;
    }

    private void syncLastSport(User user, int totalStep, Day day, Date startTime) throws SQLException {
//        String sql = "select sum(" + Sport.COUNT + ")" +
//                " from " + Sport.TABLE +
//                " where " + Sport.DAY_ID + " = '" + day.getId() + "'";
//        GenericRawResults<Integer> rawResults = sleepDao.queryRaw(
//                sql.toString(), new RawRowMapper<Integer>() {
//                    public Integer mapRow(String[] columnNames,
//                                          String[] resultColumns) {
//                        return StringUtil.isBlank(resultColumns[0]) ? 0 : Integer.valueOf(resultColumns[0]);
//                    }
//                });
//        int sum = 0;
//        for (Integer rawResult : rawResults) {
//            sum += rawResult;
//        }
        int sum = getDayTotalStep(day);
        if (totalStep > sum) {
            int a = totalStep - sum;

            Sport sport = createSport(user, day, startTime, a, false);
            logSport(sport);
//            sum = totalStep;
        }
    }

    private void logSport(Sport sport) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("\n").append("date=" + SystemContant.timeFormat10.format(sport.getDay().getDate())).append("\n");
        stringBuffer.append("sport.getStartTime()=" + SystemContant.timeFormat8.format(sport.getStartTime())).append("\n");
//        stringBuffer.append("sport.getEndTime()=" + SystemContant.timeFormat8.format(sport.getEndTime())).append("\n");
        stringBuffer.append("sport.getStepCount()=" + sport.getStepCount()).append("\n");
        stringBuffer.append("sport.getAerobics()=" + sport.getAerobics());
        LogUtil.i(stringBuffer.toString());
    }

    private Sport createSport(User user, Day day, Date time, int step, boolean isAerobics) throws SQLException {
        Sport sport = new Sport();
        sport.setDay(day);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar.set(Calendar.MILLISECOND, 0);
        sport.setStartTime(calendar.getTime());
//                    Calendar calendar = Calendar.getInstance();
//                    calendar.setTime(time);
//                    calendar.add(Calendar.MINUTE, SystemContant.sportMotionInterval);
//                    sport.setEndTime(calendar.getTime());
        sport.setTypeCode(0);//TODO 运动类型？
        int height = Integer.valueOf(user.getHeight());
        height = height == 0 ? SystemContant.defaultHeightMin : height;
        float distance = SportAimServer.getDistanceByStep(step, height);
        sport.setDistance((int) distance);
        double weight = Double.valueOf(user.getWeight());
        weight = weight == 0 ? SystemContant.defaultWeightMin : weight;
        sport.setCalorie(SportAimServer.getCalorie(weight, distance));
        sport.setAerobics(isAerobics);
        sport.setStepCount(step);
        sportDao.create(sport);
        return sport;
    }

    private void saveSleepMotionMonitorData(User user, List<MonitorData> sleepMotionList) throws Exception {
        ArrayList<SleepMotion> dbSleepMotionList = new ArrayList<SleepMotion>();

        for (MonitorData monitorData : sleepMotionList) {
            List<int[]> l = splitData(monitorData.data, 6);
            for (int[] bsTmp : l) {
                StringBuffer sb = new StringBuffer();
                for (int b : bsTmp) {
                    sb.append(b + " , ");
                }
                LogUtil.i("bsTmp=" + sb.toString());
                LogUtil.i("动作数据0：");
                ParserData parserData = parser(bsTmp, false);
                if (parserData == null) continue;
                LogUtil.i(JSON.toJSONString(parserData));
                if (parserData.flag == 0) {
                    dbSleepMotionList.clear();
                } else if (parserData.flag == 3) {
                    // 处理
                    if (dbSleepMotionList.size() > 0) {
                        Date time = dbSleepMotionList.get(dbSleepMotionList.size() - 1).getTime();
                        Day day = generateDay(user, time);

                        Sleep sleep = generateSleep(day, dbSleepMotionList.get(0).getTime(), dbSleepMotionList.get(dbSleepMotionList.size() - 1).getTime());

                        LogUtil.i("动作监测数据2：");
                        for (SleepMotion sleepMotion : dbSleepMotionList) {
                            sleepMotion.setSleep(sleep);
                            sleepMotionDao.create(sleepMotion);
//                            LogUtil.i(JSON.toJSONString(sleepMotion));
                            LogUtil.i("time=" + sleepMotion.getTime() + " ; value" + sleepMotion.getValue() +
                                    " ; sleepId=" + sleepMotion.getSleep().getId() + " ; date=" + sleepMotion.getSleep().getDay().getDate());
                        }
                    }
                } else {
                    Date time = transformTime(parserData.secondTime);

                    SleepMotion sleepMotion = new SleepMotion();
                    sleepMotion.setTime(time);
                    sleepMotion.setValue(parserData.value);
                    dbSleepMotionList.add(sleepMotion);

                    LogUtil.i("动作监测数据1：");
                    LogUtil.i(JSON.toJSONString(sleepMotion));
                }
            }
        }
    }

    private void saveSleepHeartMonitorData(User user, List<MonitorData> sleepHeartList) throws Exception {
        ArrayList<HeartRate> dbHeartRateList = new ArrayList<HeartRate>();

        for (MonitorData monitorData : sleepHeartList) {
            List<int[]> l = splitData(monitorData.data, 6);
            //心率
            for (int[] bsTmp : l) {
                StringBuffer sb = new StringBuffer();
                for (int b : bsTmp) {
                    sb.append(b + " , ");
                }
                LogUtil.i("bsTmp=" + sb.toString());
                LogUtil.i("心率数据0：");
                ParserData parserData = parser(bsTmp, false);
                if (parserData == null) continue;
                LogUtil.i(JSON.toJSONString(parserData));
                if (parserData.flag == 0) {
                    dbHeartRateList.clear();
                } else if (parserData.flag == 3) {
                    // 处理
                    if (dbHeartRateList.size() > 0) {
                        Date time = dbHeartRateList.get(dbHeartRateList.size() - 1).getTime();
                        Day day = generateDay(user, time);

                        Sleep sleep = generateSleep(day, dbHeartRateList.get(0).getTime(), dbHeartRateList.get(dbHeartRateList.size() - 1).getTime());

                        LogUtil.i("心率监测数据2：");
                        for (HeartRate heartRate : dbHeartRateList) {
                            heartRate.setSleep(sleep);
                            heartRateDao.create(heartRate);
//                            LogUtil.i(JSON.toJSONString(heartRate));

                            LogUtil.i("time=" + heartRate.getTime() + " ; value=" + heartRate.getValue() +
                                    " ; sleepId=" + heartRate.getSleep().getId() + " ; date=" + heartRate.getSleep().getDay().getDate());
                        }
                    }
                } else {
                    Date time = transformTime(parserData.secondTime);

                    HeartRate heartRate = new HeartRate();
                    heartRate.setTime(time);
                    heartRate.setValue(parserData.value);
                    dbHeartRateList.add(heartRate);

                    LogUtil.i("心率监测数据1：");
                    LogUtil.i(JSON.toJSONString(heartRate));
                }
            }
        }
    }

    private Sleep generateSleep(Day day, Date startTime, Date endTime) throws SQLException {
//        String sql = "select " + Sleep.ID + "," + Sleep.START_TIME + "," + Sleep.END_TIME +
//                " from " + Sleep.TABLE +
//                " where " + Sleep.DAY_ID + " = '" + day.getId() +
//                "' and (" +
//                "strftime('%Y-%m-%d %H:%M'," + Sleep.END_TIME + ") between '" + SystemContant.timeFormat12.format(startTime) + "' and '" + SystemContant.timeFormat12.format(endTime) +"'"+
//                " or " +
//                "'" + SystemContant.timeFormat12.format(endTime) + "' between strftime('%Y-%m-%d %H:%M'," + Sleep.START_TIME + ")" + " and strftime('%Y-%m-%d %H:%M'," + Sleep.END_TIME + ")" +
//                ")";
//        GenericRawResults<Sleep> rawResults = sleepDao.queryRaw(
//                sql.toString(), new RawRowMapper<Sleep>() {
//                    public Sleep mapRow(String[] columnNames,
//                                        String[] resultColumns) {
//                        Sleep sleep = new Sleep();
//                        sleep.setId(Integer.valueOf(resultColumns[0]));
//                        try {
//                            if (!StringUtil.isBlank(resultColumns[1]))
//                                sleep.setStartTime(SystemContant.timeFormat8.parse(resultColumns[1]));
//                            if (!StringUtil.isBlank(resultColumns[2]))
//                                sleep.setEndTime(SystemContant.timeFormat8.parse(resultColumns[2]));
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                        }
//                        return sleep;
//                    }
//                });
//        Sleep sleep = null;
//        for (Sleep rawResult : rawResults) {
//            sleep = sleepDao.queryForId(rawResult.getId());
//            break;
//        }
        Sleep sleep = sleepServer.getSleepByRange(day, startTime, endTime);

        if (sleep == null) {
            sleep = new Sleep();
            sleep.setDay(day);
            sleep.setStartTime(startTime);
            sleep.setEndTime(endTime);
            sleepDao.create(sleep);
        } else {
            if (sleep.getStartTime().after(startTime)) {
                sleep.setStartTime(startTime);
            }
            if (sleep.getEndTime().before(endTime)) {
                sleep.setEndTime(endTime);
            }
            sleepDao.update(sleep);
        }
        return sleep;
    }

    private Day generateDay(User user, Date time) throws SQLException {
        DayServer dayServer = new DayServer(context);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        calendar = TimeUtil.getDateStart(calendar);
        Date date = calendar.getTime();
        Day day = dayServer.getDay(user, date);
        if (day == null) {
            day = new Day();
            day.setUser(user);
            day.setDate(date);
            dayDao.create(day);
        }
        return day;
    }

    private Date transformTime(long second) {
        long timeInMillisecond = second * 1000 + SystemContant.wristbandDefaultTime;
        return new Date(timeInMillisecond);
    }

    public static ParserData parser(int[] arr, boolean realTime) {
        if (!realTime) {
            boolean b = true;
            for (int i : arr) {
                if (i != 0xff) {
                    b = false;
                    break;
                }
            }
            if (b) return null;
        }

        int flag = 0, s = 0;
        int time = 0, temp = 0, value = 0, usetime = 0;
        flag = (arr[0] >> 6) & 0x03;
        for (s = 0; s < 4; s++) {
            time <<= 16;
            temp = (arr[s++] << 8);
            time |= (temp | arr[s]);
        }
        time &= 0x3fffffff;
        temp = (arr[s++] << 8);
        value = temp | arr[s++];
        if (realTime) {
            temp = (arr[s++] << 8);
            usetime = temp | arr[s++];
        }

        ParserData parserData = new ParserData();
        parserData.flag = flag;
        parserData.secondTime = time;
        parserData.value = value;
        parserData.value1 = usetime;
        return parserData;
    }

    public void clean() {
        isReceiving = false;
        monitorDataList.clear();
    }

    public void timeoutEvent(String id) {
        OrderQueue.response(context, BleApi1.BizApi.requestData);
        if (monitorDataList.size() > 0 && monitorDataList.get(0).header.total == monitorDataList.get(0).header.num) {
            try {
                handle();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        clean();
        LogUtil.i("取消同步监测数据");
    }

    public static class ParserData {
        public int flag;
        public long secondTime;
        public int value;
        public int value1;
    }

    public static class Header {
        public String original;
        public int type;//0：为运动，1：为实时运动，2：为睡眠动作 3：为睡眠心率
        public int len;//包的长度
        public int num;//包序号
        public int total;//包总数
        public String crc;//包校验
        public String en;//加密标识
    }

    public static class MonitorData {
        public Header header;
        public List<Byte> data = new LinkedList<Byte>();

        public void addData(byte[] bytes) {
//            StringBuffer logBf = new StringBuffer();
            for (byte b : bytes) {
                data.add(b);
//                logBf.append(((int) b) + ",");
            }
//            LogUtil.i("监测字节");
//            LogUtil.i1(logBf.toString());
        }

        public boolean isFull() {
            LogUtil.i("data.size()=" + data.size() + " ; header.len=" + header.len);
            return !(data.size() < header.len);
        }
    }
}
