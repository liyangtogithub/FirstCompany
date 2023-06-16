package com.desay.iwan2.module.userinfo;

import android.content.Context;
import dolphin.tools.util.LogUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 方奕峰 on 14-7-23.
 */
public class WechatCityServer {
 
    private final String rootRegex = "\\|[A-Za-z]*\\|[^\\|\\n]*";
    private final String subRegex = "_[^_\\n]*\\|[^\\|\\n]*";
    private final String completeRegex = "\\|[^\\|\\n]*\\|[^\\|\\n]*";
    private final String cnRegex = "[\\u4e00-\\u9fa5]";

    private InputStream getAssets(Context context, Locale locale) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = context.getAssets().open("mmregion4client_" +
                    (locale == null ? context.getResources().getConfiguration().locale : locale.toString()));
        } catch (Exception e) {
            e.printStackTrace();
            inputStream = context.getAssets().open("mmregion4client_" + Locale.ENGLISH);
        }
        return inputStream;
    }

    public String getAllData(Context context, Locale locale) throws IOException {
        StringBuffer stringBuffer = new StringBuffer();

        InputStream inputStream = getAssets(context, locale);
        InputStreamReader streamReader = new InputStreamReader(inputStream);
        BufferedReader reader = new BufferedReader(streamReader);

        for (String line; (line = reader.readLine()) != null; ) {
            stringBuffer.append(line + "\n");
        }

        return stringBuffer.toString();
    }

    public List<String> getRoot(String str) {
        ArrayList<String> results = new ArrayList<String>();

        Pattern pattern = Pattern.compile(rootRegex);
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
//            LogUtil.i1(matcher.group().substring(1));
            results.add(matcher.group().substring(1));
        }

        return results;
    }

    public List<String> getSub(String str, String prefix) {
        ArrayList<String> results = new ArrayList<String>();

        String regex = prefix + subRegex;
//        LogUtil.i1("regex=" + regex);
        Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
//            LogUtil.i1(matcher.group());
            results.add(matcher.group());
        }

        return results;
    }

    public List<Address> getAllAddress(Context context, Locale locale) throws IOException {
        ArrayList<Address> l = new ArrayList<Address>();

        InputStream inputStream = getAssets(context, locale);
        InputStreamReader streamReader = new InputStreamReader(inputStream);
        BufferedReader reader = new BufferedReader(streamReader);

        Address address = null;
        for (String line; (line = reader.readLine()) != null; ) {
            String[] split = line.split("\\|");
            address = new Address(split[1], split[2]);
            l.add(address);
        }

        return l;
    }

    public List<Address> getRootAddress(String str) {
        ArrayList<Address> results = new ArrayList<Address>();

        Pattern pattern = Pattern.compile(rootRegex);
        Matcher matcher = pattern.matcher(str);

        Address address = null;
        while (matcher.find()) {
            String[] split = matcher.group().substring(1).split("\\|");
            address = new Address(split[0], split[1]);
            results.add(address);
        }

        return results;
    }

    public List<Address> getSubAddress(String str, String prefix) {
        ArrayList<Address> results = new ArrayList<Address>();

        String regex = prefix + subRegex;
//        LogUtil.i1("regex=" + regex);
        Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(str);

        Address address = null;
        while (matcher.find()) {
//            LogUtil.i1(matcher.group());
            String[] split = matcher.group().substring(0).split("\\|");
            address = new Address(split[0], split[1]);
            results.add(address);
        }

        return results;
    }

    public Address getAddressByCode(String str, String code) {
        String regex = "\\|" + code + "\\|[^\\|\\n]*";

        Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(str);

        Address address = null;
        while (matcher.find()) {
            LogUtil.i1(matcher.group());
            String[] split = matcher.group().substring(1).split("\\|");
            address = new Address(split[0], split[1]);
            return address;
        }

        return null;
    }

    public Address getAddressBySuffix(String str, String suffix) {
        String regex = completeRegex + transferred(suffix) + "$";

        Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(str);

        Address address = null;
        while (matcher.find()) {
            String[] split = matcher.group().substring(1).split("\\|");
            address = new Address(split[0], split[1]);
            return address;
        }

        return null;
    }

    private String transferred(String str) {
        Pattern pattern1 = Pattern.compile(cnRegex);
        char[] chars = str.toCharArray();
        StringBuffer sb = new StringBuffer();
        for (char c : chars) {
            String str1 = "" + c;
            Matcher matcher = pattern1.matcher(str1);
            if (matcher.find()) {
                sb.append(toUnicode(str1));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private String toUnicode(String str) {
        char[] arChar = str.toCharArray();
        int iValue = 0;
        String uStr = "";
        for (int i = 0; i < arChar.length; i++) {
            iValue = (int) str.charAt(i);
            if (iValue <= 256) {
                uStr += "\\" + Integer.toHexString(iValue);
            } else {
                uStr += "\\u" + Integer.toHexString(iValue);
            }
        }
        return uStr;
    }

    public static class Address {

        public String code;
        public String value;

        public Address() {
        }

        public Address(String code, String value) {
            this.code = code;
            this.value = value;
        }
    }
}
