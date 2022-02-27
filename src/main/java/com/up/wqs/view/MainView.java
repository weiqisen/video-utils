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
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.*;

import javax.swing.*;

import com.up.wqs.Service.FileUtils;
import com.up.wqs.Service.Part;
import com.up.wqs.Service.Practice;
import com.up.wqs.Service.VideoUtil;
import com.up.wqs.constant.AppConstants;
import org.bytedeco.javacv.FrameGrabber;

import static com.up.wqs.constant.AppConstants.*;

/**
 * 模块说明： 首页
 */
public class MainView extends JFrame {

    private static final long serialVersionUID = 5870864087464173884L;

    private JPanel jPanelNorth, jPanelSouth, jPanelCenter;
    private JButton jButtonStart, jButtonStop;
    private JTextField condition;

    public MainView() {
        init();
    }

    private void init() {
        setTitle(AppConstants.MAINVIEW_TITLE);

        // north panel
        jPanelNorth = new JPanel();
        jPanelNorth.setLayout(new GridLayout(1, 6, 5, 5));

        JLabel tip = new JLabel("请输入导出个数：");
        jPanelNorth.add(tip);
        condition = new JTextField("1");
        condition.setBounds(0, 0, 10, 10);

        condition.addKeyListener(new FindListener());
        jPanelNorth.add(condition);

        // center panel
        jPanelCenter = new JPanel();
        JLabel consoleTip = new JLabel("控制台信息:");
        JLabel console = new JLabel("");

        jPanelCenter.setLayout(new GridLayout(5, 5));

        jPanelCenter.setPreferredSize(new Dimension(100, 0));
        jPanelCenter.add(new JLabel("视频输入路径：E:\\input\\1.mp4  2.mp4  3.mp4"));
        jPanelCenter.add(new JLabel("视频输出路径：E:\\output\\1.mp4 ...."));
        jPanelCenter.add(new JLabel("音频输入路径：E:\\audio\\1.mp3"));
        jPanelCenter.add(consoleTip);
        jPanelCenter.add(console);
        Integer fileCount = FileUtils.getFileAndDirectory();
        ArrayList<String> ins = new ArrayList<>();
        for (int i = 0; i < fileCount; i++) {
            int no = i + 1;
            ins.add(INPUTDIR + no + ".mp4");
        }
        ArrayList<String> finList = Part.getList(ins);
        console.setText("输入视频条数：" + fileCount +"-----> 可组合数量：" + finList.size() + "条");

        // south panel
        jPanelSouth = new JPanel();
        jPanelSouth.setLayout(new GridLayout(1, 5));

        jButtonStart = new JButton(AppConstants.MAINVIEW_FIRST);
        jButtonStart.addActionListener(e -> {
            System.out.println(e);
            condition.setEnabled(false);
            jButtonStart.setEnabled(false);
            //开始合成视频并输出信息到中控
            String text = condition.getText();
            int taskCount = Integer.parseInt(text);
            System.out.println("共计输出视频任务：" + taskCount + "条");
            //这里结合算法筛选出需要的个数放进集合里面
            if (taskCount > finList.size()){
               JOptionPane.showMessageDialog(null, "导出数量已超出组合数!");
                return;
            }
            ExecutorService executor = Executors.newFixedThreadPool(1);
            for (int i = 0; i < taskCount; i++) {
                int finalII = i;
                executor.execute(() -> {
                   int finalI = finalII + 1;
                    String s = finList.get(finalII);
                    //根据@分割
                    String[] split = s.split("@");
                    ArrayList<String> strings2 = new ArrayList<>();
                    strings2.add(split[0]);
                    strings2.add(split[1]);
                    strings2.add(split[2]);
                    System.out.println(Thread.currentThread().getName() + "开始组合--第" + finalII + "次--------" + strings2);
                    try {
                        String mergeInput = VideoUtil.mergeVideos(strings2, TEMPDIR, AUDIODIR, finalI);
                        VideoUtil.mergeAV(AUDIODIR, mergeInput, finalI);
                        console.setText(Thread.currentThread().getName() + "----------完成任务：" + finalI);
                    } catch (FrameGrabber.Exception | InterruptedException ex) {
                        ex.printStackTrace();
                    }
                });
            }
            executor.shutdown();
            try {
                executor.awaitTermination(1L,TimeUnit.DAYS);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        });


        jButtonStop = new JButton(AppConstants.STOP);
        jButtonStop.addActionListener(e -> {
            System.out.println("程序退出...");
            //退出软件
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


    private class FindListener extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                find();
            }
        }
    }

    private void find() {

    }

}
