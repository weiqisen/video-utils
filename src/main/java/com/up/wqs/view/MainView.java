/**
 * 项目名：student
 * 修改历史：
 * 作者： MZ
 * 创建时间： 2016年1月6日-下午1:37:39
 */
package com.up.wqs.view;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.*;

import javax.swing.*;

import com.up.wqs.service.FileUtils;
import com.up.wqs.service.Part;
import com.up.wqs.service.VideoUtil;
import com.up.wqs.constant.AppConstants;
import org.bytedeco.javacv.FrameGrabber;


/**
 * 模块说明： 首页
 */
public class MainView extends JFrame {

    private static final long serialVersionUID = 5870864087464173884L;

    private JButton jButtonStart;
    private JTextField conditionLeft, conditionRight;

    public MainView() {
        init();
    }

    private void init() {
        setTitle(AppConstants.MAINVIEW_TITLE);

        Properties prop = new Properties();

        try {

            prop.load(new FileInputStream("res/my.properties"));

        } catch (IOException e) {

            e.printStackTrace();

        }
        String fileDir = prop.getProperty("FILEDIR");
        String inputDir = prop.getProperty("INPUTDIR");
        String tempDir = prop.getProperty("TEMPDIR");
        String outputDir = prop.getProperty("OUTPUTDIR");
        String audioDir = prop.getProperty("AUDIODIR");

        System.out.println("文件目录配置----->");
        System.out.println(fileDir);
        System.out.println(inputDir);
        System.out.println(tempDir);
        System.out.println(outputDir);
        System.out.println(audioDir);

        // north panel
        JPanel jPanelNorth = new JPanel();
        jPanelNorth.setLayout(new GridLayout(1, 6, 5, 5));

        JLabel tip = new JLabel("请输入导出范围：");
        jPanelNorth.add(tip);


        conditionLeft = new JTextField("1");
        conditionLeft.setBounds(0, 0, 10, 10);
        conditionLeft.addKeyListener(new FindListener());
        jPanelNorth.add(conditionLeft);

        conditionRight = new JTextField("3");
        conditionRight.setBounds(0, 0, 10, 10);
        jPanelNorth.add(conditionRight);

        // center panel
        JPanel jPanelCenter = new JPanel();
        JLabel consoleTip = new JLabel("");
        JLabel console = new JLabel("");

        jPanelCenter.setLayout(new GridLayout(5, 5));

        jPanelCenter.setPreferredSize(new Dimension(100, 0));
        jPanelCenter.add(new JLabel("视频输入路径：E:\\deploy\\input\\1.mp4  2.mp4  3.mp4"));
        jPanelCenter.add(new JLabel("视频输出路径：E:\\deploy\\output\\output1.mp4 ...."));
        jPanelCenter.add(new JLabel("音频输入路径：E:\\deploy\\audio\\1.mp3"));
        jPanelCenter.add(consoleTip);
        jPanelCenter.add(console);
        Integer fileCount = FileUtils.getFileAndDirectory(fileDir);
        ArrayList<String> ins = new ArrayList<>();
        for (int i = 0; i < fileCount; i++) {
            int no = i + 1;
            ins.add(inputDir + no + ".mp4");
        }
        // 获取算法总条数
        ArrayList<String> finList = Part.getList(ins);
        conditionRight.setText(Integer.toString(finList.size()));
        consoleTip.setText("本地原视频条数：" + fileCount + "-----> 可组合输出数量：" + finList.size() + "条");


        // south panel
        JPanel jPanelSouth = new JPanel();
        jPanelSouth.setLayout(new GridLayout(1, 5));

        jButtonStart = new JButton(AppConstants.MAINVIEW_FIRST);
        jButtonStart.addActionListener(e -> {
            System.out.println(e);

            // 开始合成视频并输出信息到中控
            String leftCount = conditionLeft.getText();
            String rightCount = conditionRight.getText();

            int leftCountInt = Integer.parseInt(leftCount);
            int rightCountInt = Integer.parseInt(rightCount);
            int range = rightCountInt - leftCountInt;
            System.out.println("共计输出视频任务：" + range + "条");
            // 这里结合算法筛选出需要的个数放进集合里面
            if (range <= 0) {
                System.out.println("范围错误，请重新设置!");
                JOptionPane.showMessageDialog(null, "范围错误，请重新设置!");
                return;
            }
            if (leftCountInt <= 0) {
                System.out.println("左边范围错误，请重新设置!");
                JOptionPane.showMessageDialog(null, "左边范围错误，请重新设置!");
                return;
            }
            if (rightCountInt > finList.size()) {
                System.out.println("导出数量不能大于最大组合数量，请重新设置!");
                JOptionPane.showMessageDialog(null, "导出数量不能大于最大组合数量，请重新设置!");
                return;
            }
            conditionLeft.setEnabled(false);
            conditionRight.setEnabled(false);
            jButtonStart.setEnabled(false);
            ExecutorService executor = Executors.newFixedThreadPool(4);
            for (int i = leftCountInt - 1; i < range; i++) {
                int finalII = i;
                executor.execute(() -> {
                    int finalI = finalII + 1;
                    String s = finList.get(finalII);
                    // 根据@分割
                    String[] split = s.split("@");
                    ArrayList<String> strings2 = new ArrayList<>();
                    strings2.add(split[0]);
                    strings2.add(split[1]);
                    strings2.add(split[2]);
                    System.out.println(Thread.currentThread().getName() + "开始组合-->第" + finalII + "次--------" + strings2);
                    try {
                        String mergeInput = VideoUtil.mergeVideos(strings2, tempDir, audioDir, finalI);
                        VideoUtil.mergeAV(audioDir, mergeInput, finalI, outputDir);
                        console.setText(Thread.currentThread().getName() + "----------> 完成第" + finalI + "条视频输出任务：");
                    } catch (FrameGrabber.Exception | InterruptedException ex) {
                        ex.printStackTrace();
                    }
                });
            }
            executor.shutdown();
            try {
                executor.awaitTermination(1L, TimeUnit.DAYS);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        });


        JButton jButtonStop = new JButton(AppConstants.STOP);
        jButtonStop.addActionListener(e -> {
            System.out.println("程序退出...");
            // 退出软件
            System.exit(0);
        });

        jPanelSouth.add(jButtonStart);
        jPanelSouth.add(jButtonStop);

        this.add(jPanelNorth, BorderLayout.NORTH);
        this.add(jPanelCenter, BorderLayout.CENTER);
        this.add(jPanelSouth, BorderLayout.SOUTH);

        setBounds(400, 200, 750, 340);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
    }


    private static class FindListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
        }
    }

}
