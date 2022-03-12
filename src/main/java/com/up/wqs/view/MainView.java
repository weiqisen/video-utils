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

import com.up.wqs.Main;
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

    private JButton jButtonStart, confirm, confirm1;
    private static JTextField conditionRight, groupTipNum;
    private static JLabel consoleTip;
    private static ArrayList<String> finList;
    private static ArrayList<String> ins;
    private static int num;
    private static int fileCount;

    public MainView() {
        init();
    }

    private void init() {
        setTitle(AppConstants.MAINVIEW_TITLE);

        Properties prop = new Properties();

        try {

            prop.load(new FileInputStream("config/dir.properties"));

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
        jPanelNorth.setLayout(new GridLayout(2, 5));
        jPanelNorth.setPreferredSize(new Dimension(200, 100));

        JLabel tip = new JLabel("请输入导出个数：");
        jPanelNorth.add(tip);

        conditionRight = new JTextField("3");
        conditionRight.setBounds(0, 0, 10, 10);
        conditionRight.setEnabled(false);
        jPanelNorth.add(conditionRight);

        JButton modify1 = new JButton("修改");
        modify1.addActionListener(e -> {
            confirm1.setEnabled(true);
            modify1.setEnabled(false);
            conditionRight.setEnabled(true);
        });
        jPanelNorth.add(modify1);

        confirm1 = new JButton("确认");
        confirm1.setEnabled(false);
        confirm1.addActionListener(e -> {
            conditionRight.setText(String.valueOf(conditionRight.getText()));
            modify1.setEnabled(true);
            confirm1.setEnabled(false);
            conditionRight.setEnabled(false);
        });
        jPanelNorth.add(confirm1);


        JLabel groupTip = new JLabel("几段素材进行拼接：");
        jPanelNorth.add(groupTip);
        groupTipNum = new JTextField("3");
        groupTipNum.setBounds(0, 0, 10, 10);
        groupTipNum.addKeyListener(new GroupTipNumListener());
        groupTipNum.setEnabled(false);
        jPanelNorth.add(groupTipNum);

        JButton modify = new JButton("修改");
        modify.addActionListener(e -> {
            confirm.setEnabled(true);
            modify.setEnabled(false);
            groupTipNum.setEnabled(true);
        });
        jPanelNorth.add(modify);

        confirm = new JButton("确认");
        confirm.setEnabled(false);
        confirm.addActionListener(e -> {
            num = Integer.parseInt(groupTipNum.getText());
            finList = Part.getList(ins, num);
            consoleTip.setText("本地原视频条数：" + fileCount + "-----> 可组合输出数量：" + finList.size() + "条");
            conditionRight.setText(String.valueOf(finList.size()));
            confirm.setEnabled(false);
            modify.setEnabled(true);
            groupTipNum.setEnabled(false);

        });
        jPanelNorth.add(confirm);


        // center panel
        JPanel jPanelCenter = new JPanel();
        consoleTip = new JLabel("");
        JLabel console = new JLabel("");

        jPanelCenter.setLayout(new GridLayout(5, 5));

        jPanelCenter.setPreferredSize(new Dimension(100, 0));
        jPanelCenter.add(new JLabel("视频输入路径：" + inputDir));
        jPanelCenter.add(new JLabel("视频输出路径：" + outputDir));
        jPanelCenter.add(new JLabel("音频输入路径：" + audioDir));
        jPanelCenter.add(consoleTip);
        jPanelCenter.add(console);
        fileCount = FileUtils.getFileAndDirectory(fileDir);
        ins = new ArrayList<>();
        for (int i = 0; i < fileCount; i++) {
            int no = i + 1;
            ins.add(inputDir + no + ".mp4");
        }
        // 获取算法总条数
        num = Integer.parseInt(groupTipNum.getText());
        finList = Part.getList(ins, num);
        conditionRight.setText(Integer.toString(finList.size()));
        consoleTip.setText("本地原视频条数：" + fileCount + "-----> 可组合输出数量：" + finList.size() + "条");

        // south panel
        JPanel jPanelSouth = new JPanel();
        jPanelSouth.setLayout(new GridLayout(1, 5));

        jButtonStart = new JButton(AppConstants.MAINVIEW_FIRST);
        jButtonStart.addActionListener(e -> {
            System.out.println(e);

            // 开始合成视频并输出信息到中控

            String rightCount = conditionRight.getText();
            int rightCountInt = Integer.parseInt(rightCount);
            System.out.println("共计输出视频任务：" + rightCountInt + "条");
            // 这里结合算法筛选出需要的个数放进集合里面
            if (rightCountInt <= 0) {
                System.out.println("范围错误，请重新设置!");
                JOptionPane.showMessageDialog(null, "范围错误，请重新设置!");
                return;
            }
            if (rightCountInt > finList.size()) {
                System.out.println("导出数量不能大于最大组合数量，请重新设置!");
                JOptionPane.showMessageDialog(null, "导出数量不能大于最大组合数量，请重新设置!");
                return;
            }
            conditionRight.setEnabled(false);
            jButtonStart.setEnabled(false);
            ExecutorService executor = Executors.newFixedThreadPool(4);
            for (int i = 0; i < rightCountInt; i++) {
                int finalII = i;
                executor.execute(() -> {
                    int finalI = finalII + 1;
                    String s = finList.get(finalII);
                    // 根据@分割
                    String[] split = s.split("@");
                    ArrayList<String> strings2 = new ArrayList<>();
                    // 按长度增加
                    for (int j = 0; j < split.length; j++) {
                        strings2.add(split[j]);
                    }
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


    private static class GroupTipNumListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {

        }
    }

}
