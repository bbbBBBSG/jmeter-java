package com.dahai.demo;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

    private static String tmpFile = "D:\\jmeter-data\\web\\tmpFile.jmx";

    public static void start(String scriptFile) throws Exception {

        int count = 100;
        int sec = 1;

        String logFile = scriptFile.substring(0, scriptFile.lastIndexOf("."))+".log";
        File file = new File(logFile);
        if (file.exists()) {
            file.delete();
        }

        System.out.println("开始启动："+getCurrDate());
        while (true) {
            parse(scriptFile,String.valueOf(count),String.valueOf(sec));

            String cmd = "D:\\apache-jmeter-5.1\\bin\\jmeter.bat -n -t " + tmpFile;
            try {
                Process ps = Runtime.getRuntime().exec(cmd);
                BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
                String result = "";
                String line;
                while ((line = br.readLine()) != null) {

                    if (line.contains("summary =")) {
                        result = line;
                    }
                }

                saveLog(scriptFile,result,count,sec);

                String err = result.split("Err:")[1].trim();
                String s = err.substring(0, 1);
                if (Integer.valueOf(s)>0) {
                    if (sec<=10) {
                        sec ++;
                    } else if (sec<=30) {
                        sec += 3;
                    } else {
                        sec += 5;
                    }
                } else {
                    if (count<=1000) {
                        count = count + 100;
                    } else if (count<=3000) {
                        count = count + 300;
                    } else {
                        count = count + 500;
                    }

                }

                System.out.println("一个周期: "+getCurrDate());
                if (sec>65) {
                    break;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("结束："+getCurrDate());
    }


    private static void saveLog(String scriptFile,String result,int count,int sec) throws Exception {
        String logFile = scriptFile.substring(0, scriptFile.lastIndexOf("."))+".log";
        File file = new File(logFile);

        result = "执行线程数：" + count + "   时间：" + sec + "s    " + result;
        OutputStreamWriter bo = new OutputStreamWriter(new FileOutputStream(file, true));
        BufferedWriter writer = new BufferedWriter(bo);
        writer.write(result);
        writer.newLine();
        writer.flush();
        writer.close();

        System.out.println(result);
    }

    public static void parse(String scriptFile,String count,String sec) throws Exception {
        if (new File(tmpFile).exists()) {
            new File(tmpFile).delete();
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(scriptFile)));
        OutputStreamWriter bo = new OutputStreamWriter(new FileOutputStream(tmpFile, true));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.contains("ThreadGroup.num_threads")) {
                line = replaceNum(line,count);
            } else if (line.contains("ThreadGroup.ramp_time")) {
                line = replaceNum(line,sec);
            }
            bo.write(line);
            bo.write("\n");
            bo.flush();
        }
        br.close();
        bo.close();
    }


    /**
     * 替换数字
     * @param value
     * @param number
     */
    private static String replaceNum(String value,String number) {
        Pattern p = Pattern.compile("[\\d]");
        Matcher matcher = p.matcher(value);
        String result = matcher.replaceAll("&");

        String[] split = result.split("&");
        return split[0] + number + split[split.length - 1];
    }


    private static String getCurrDate() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(new Date());
    }
}
