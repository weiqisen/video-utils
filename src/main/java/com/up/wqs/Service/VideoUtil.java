package com.up.wqs.Service;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.*;
import sun.font.FontDesignMetrics;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import static com.up.wqs.constant.AppConstants.OUTPUTDIR;

/**
 * @author weiqisen
 */
public class VideoUtil {

    public static String videoPath_temp_path = "/Users/weiqisen/Desktop/";

    /**
     * @param videoPath   视频路径
     * @param start       视频开始时间 ，单位秒
     * @param end         视频结束时间
     * @param recodeAudio 是否录制音频
     * @return 生成文件路径
     * @apiNote 切割视频指定的位置
     */

    public static String cutVideo(String videoPath, Integer start, Integer end, boolean recodeAudio) throws FFmpegFrameGrabber.Exception, FFmpegFrameRecorder.Exception {
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath);
        grabber.start();
        String s = UUID.randomUUID() + ".mp4";
        FFmpegFrameRecorder fFmpegFrameRecorder = new FFmpegFrameRecorder(videoPath_temp_path + s, grabber.grabImage().imageWidth, grabber.grabImage().imageHeight, recodeAudio ? 1 : 0);
        fFmpegFrameRecorder.start();
        Frame frame = null;
        while ((frame = grabber.grabFrame(recodeAudio, true, true, false)) != null) {
            if ((start == null ? 0 : (start * 1000000)) < frame.timestamp && (end == null || ((end * 1000000) > frame.timestamp))) {
                fFmpegFrameRecorder.record(frame);
            }
        }

        fFmpegFrameRecorder.stop();
        fFmpegFrameRecorder.release();
        return s;
    }

    /**
     * @param videoPath 视频路径
     * @param text      水印文本
     * @param x         水印位置x
     * @param y         水印位置y
     * @param color     水印颜色
     * @param fontSize  水印文字大小
     * @return 生成文件路径
     * @apiNote 视频添加水印
     */
    public static String waterMark(String videoPath, String text, Integer x, Integer y, Color color, Integer fontSize) throws Exception {
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath);
        grabber.start();
        Frame frame = grabber.grabImage();

        String s = videoPath_temp_path + UUID.randomUUID() + ".mp4";
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(s, frame.imageWidth, frame.imageHeight, 0);
        recorder.start();
        int j = 0;
        while ((frame = grabber.grabImage()) != null) {
            Java2DFrameConverter converter = new Java2DFrameConverter();
            BufferedImage bufImg = converter.getBufferedImage(frame);

            Font font = new Font("微软雅黑", Font.BOLD, 64);
//            String content = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
//            String content1 = "字符滚动效果";
            FontDesignMetrics metrics = FontDesignMetrics.getMetrics(font);
            int width = bufImg.getWidth();//计算图片的宽
            int height = bufImg.getHeight();//计算高
            Graphics2D graphics = bufImg.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            graphics.drawImage(bufImg, 0, 0, bufImg.getWidth(), bufImg.getHeight(), null);

            graphics.setColor(color);
            graphics.setFont(new Font("微软雅黑", Font.BOLD, fontSize));
            graphics.drawString(text, x, y);
            graphics.dispose();
            frame = converter.getFrame(bufImg);
            recorder.record(frame);
            j++;
        }
        grabber.stop();
        recorder.stop();
        recorder.release();
        return s;
    }

    /**
     * @param audioPath 音频路径
     * @param videoPath 视频路径
     * @return 最终生成文件地址
     * @apiNote 合并音视频
     */

    public static String mergeAV(String audioPath, String videoPath, Integer taskId) throws InterruptedException {
        System.out.println("-----mergeAV------" + videoPath);

        String outputPath = OUTPUTDIR + "final" + taskId + ".mp4";
        try (FFmpegFrameGrabber imageGrabber = new FFmpegFrameGrabber(videoPath);
             FFmpegFrameGrabber audioGrabber = new FFmpegFrameGrabber(audioPath)) {
            imageGrabber.start();
            audioGrabber.start();
            try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputPath, imageGrabber.getImageWidth(), imageGrabber.getImageHeight(),
                    0)) {
                recorder.setInterleaved(true);
                recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                recorder.setFormat("mp4");
                recorder.setVideoQuality(0);
                System.out.println("音视频合成比特率：" + imageGrabber.getVideoBitrate());
                recorder.setVideoBitrate(imageGrabber.getVideoBitrate());
                recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P); // yuv420p
                int frameRate = 30;
                recorder.setFrameRate(imageGrabber.getFrameRate());
                recorder.setGopSize(frameRate * 2);
                recorder.setAudioOption("crf", "0");
                recorder.setAudioQuality(0);// 最高质量
                recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
                recorder.setAudioChannels(2);
                recorder.setSampleRate(44100);
                recorder.start();
                long videoTime = imageGrabber.getLengthInTime();
                Frame imageFrame;
                while ((imageFrame = imageGrabber.grabImage()) != null) {
                    recorder.record(imageFrame);
                }
                long audioPlayTime = 0L;
                Frame sampleFrame;
                while ((sampleFrame = audioGrabber.grabSamples()) != null || audioPlayTime < videoTime) {
                    if (sampleFrame == null) {
                        audioGrabber.restart();//重新开始
                        sampleFrame = audioGrabber.grabSamples();
                        videoTime -= audioPlayTime;
                    }
                    recorder.record(sampleFrame);
                    audioPlayTime = audioGrabber.getTimestamp();
                    if (audioPlayTime >= videoTime) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return outputPath;
    }


    /**
     * @param list       要合并的视频列表
     * @param outputPath 输出文件位置
     * @return 返回输出文件位置
     * @apiNote 合并多个视频
     */
    public static String mergeVideo(java.util.List<String> list, String outputPath) throws FrameGrabber.Exception {
        ArrayList<FFmpegFrameGrabber> grabbers = new ArrayList<>();
        OpenCVFrameGrabber grabber1 = new OpenCVFrameGrabber(list.get(0));
        grabber1.start();
        Frame frame1 = grabber1.grabFrame();
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
            grabbers.add(new FFmpegFrameGrabber(list.get(i)));
            FFmpegFrameGrabber grabber = grabbers.get(i);
            grabber.start();
        }
        Map<String, String> metadata = grabbers.get(0).getMetadata();
        FFmpegFrameRecorder fFmpegFrameRecorder = new FFmpegFrameRecorder(outputPath + "temp0" + ".mp4", frame1.imageWidth, frame1.imageHeight, grabbers.get(0).getAudioChannels());
        grabber1.stop();
        fFmpegFrameRecorder.setFormat("mp4");
        fFmpegFrameRecorder.setGopSize(4);

        fFmpegFrameRecorder.setAudioOption("crf", "0");
        fFmpegFrameRecorder.setAudioQuality(0);// 最高质量
        fFmpegFrameRecorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        fFmpegFrameRecorder.setAudioChannels(2);
        fFmpegFrameRecorder.setSampleRate(44100);
        try {
            fFmpegFrameRecorder.start();
            for (int i = 0; i < grabbers.size(); i++) {
                FFmpegFrameGrabber grabber = grabbers.get(i);
                if (i > 0) {

                    FFmpegFrameGrabber grabber2 = new FFmpegFrameGrabber(outputPath + "temp" + (i - 1) + ".mp4");
                    grabber2.start();
                    fFmpegFrameRecorder = new FFmpegFrameRecorder(outputPath + "temp" + (i) + ".mp4", frame1.imageWidth, frame1.imageHeight, grabber2.getAudioChannels());
                    fFmpegFrameRecorder.start();
                    grabber2.setFormat("mp4");
                    System.out.println(grabber2.getFormat());
                    Frame avFrame = null;
                    while ((avFrame = grabber2.grabFrame()) != null) {
                        fFmpegFrameRecorder.record(avFrame);
                    }
                    grabber2.release();
                    grabber2.stop();
                    grabber2.close();
                    new File(outputPath + "_temp_" + (i - 1) + ".mp4").deleteOnExit();
                }

                Frame avFrame = null;
                while ((avFrame = grabber.grabFrame(true, true, true, false)) != null) {
                    //fFmpegFrameRecorder.record(avFrame);
                    fFmpegFrameRecorder.record(avFrame);
                }
                grabber.stop();
                fFmpegFrameRecorder.stop();
                System.gc();
            }
            fFmpegFrameRecorder.stop();
            File file = new File(outputPath + "temp" + (grabbers.size() - 1) + ".mp4");
            file.renameTo(new File(outputPath + "final.mp4"));
            return outputPath;
        } catch (FFmpegFrameRecorder.Exception | FrameGrabber.Exception e) {
            e.printStackTrace();
            try {
                fFmpegFrameRecorder.stop();
                for (int i = 0; i < grabbers.size(); i++) {
                    grabbers.get(i).stop();
                }
            } catch (FFmpegFrameRecorder.Exception | FrameGrabber.Exception ex) {
                ex.printStackTrace();
            }
            throw new RuntimeException("合成失败");
        }

    }

    /**
     * @param list       要合并的视频列表
     * @param outputPath 输出文件位置
     * @return 返回输出文件位置
     * @apiNote 合并多个视频
     */
    public static String mergeVideos(java.util.List<String> list, String outputPath, String finalName, Integer taskId) throws FrameGrabber.Exception {
        System.out.println("-----start to mergeVideos ,taskId:" +taskId+ "----" + list);
        //新建视频帧采集器集合
        ArrayList<FFmpegFrameGrabber> grabbers = new ArrayList<>();

        //获取第一个视频的帧采集器
        OpenCVFrameGrabber grabber1 = new OpenCVFrameGrabber(list.get(0));
        //初始化
        grabber1.start();
        //获取第一帧视频
        Frame frame1 = grabber1.grabFrame();

        //遍历添加视频帧采集器并初始化
        for (int i = 0; i < list.size(); i++) {
            grabbers.add(new FFmpegFrameGrabber(list.get(i)));
            FFmpegFrameGrabber grabber = grabbers.get(i);
            grabber.start();
        }

        //获取第一个视频的帧率
        double frameRateStd = grabbers.get(0).getFrameRate();
        System.out.println("视频分辨率：" + frame1.imageWidth + "x" + frame1.imageHeight);

        //新建第一个合成器
        FFmpegFrameRecorder fFmpegFrameRecorder = new FFmpegFrameRecorder(outputPath + "task" + taskId + "temp0" + ".mp4", frame1.imageWidth, frame1.imageHeight, grabber1.getAudioChannels());
        try {
        //停止第一个视频的帧采集器
        grabber1.stop();

        fFmpegFrameRecorder.setFormat("mp4");
        fFmpegFrameRecorder.setAudioChannels(2);

        fFmpegFrameRecorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        fFmpegFrameRecorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P); // yuv420p

        fFmpegFrameRecorder.setVideoQuality(0);
        System.out.println("帧率：" + frameRateStd);
        fFmpegFrameRecorder.setFrameRate(frameRateStd);

        int videoBitrate = grabbers.get(0).getVideoBitrate();
        System.out.println("比特率：" + videoBitrate);
        fFmpegFrameRecorder.setVideoBitrate(videoBitrate);


            fFmpegFrameRecorder.start();

            for (int i = 0; i < grabbers.size(); i++) {
                FFmpegFrameGrabber grabber = grabbers.get(i);
                if (i > 0) {

                    FFmpegFrameGrabber grabber2 = new FFmpegFrameGrabber(outputPath + "task" + taskId + "temp" + (i - 1) + ".mp4");
                    grabber2.start();
                    fFmpegFrameRecorder = new FFmpegFrameRecorder(outputPath + "task" + taskId + "temp" + i + ".mp4", frame1.imageWidth, frame1.imageHeight, grabber2.getAudioChannels());

                    fFmpegFrameRecorder.setFormat("mp4");
                    fFmpegFrameRecorder.setAudioChannels(2);

                    fFmpegFrameRecorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                    fFmpegFrameRecorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P); // yuv420p

                    fFmpegFrameRecorder.setVideoQuality(0);
                    System.out.println("帧率：" + frameRateStd);
                    fFmpegFrameRecorder.setFrameRate(frameRateStd);


                    System.out.println("比特率：" + videoBitrate);
                    fFmpegFrameRecorder.setVideoBitrate(videoBitrate);

                    fFmpegFrameRecorder.start();
                    grabber2.setFormat("mp4");


                    Frame avFrame;
                    while ((avFrame = grabber2.grabFrame()) != null) {
                        fFmpegFrameRecorder.record(avFrame);
                    }
                    grabber2.release();
                    grabber2.stop();
                    grabber2.close();
                    new File(outputPath + "task" + taskId + "temp" + (i - 1) + ".mp4").deleteOnExit();
                }

                Frame avFrame;
                while ((avFrame = grabber.grabFrame(true, true, true, false)) != null) {
                    fFmpegFrameRecorder.record(avFrame);
                }

                grabber.stop();
                fFmpegFrameRecorder.stop();
                System.gc();
            }

            fFmpegFrameRecorder.stop();
            File file = new File(outputPath + "task" + taskId + "temp" + (grabbers.size() - 1) + ".mp4");
            //taskId += 1;
            String finalDir = outputPath + "OriginFinal" + taskId + ".mp4";
            if(!file.exists()){
                System.out.println("error...");
            }
            file.renameTo(new File(finalDir));
            System.out.println("merge output dir:  -->  " + finalDir);
            return finalDir;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                fFmpegFrameRecorder.stop();
                for (FFmpegFrameGrabber grabber : grabbers) {
                    grabber.stop();
                }
            } catch (FFmpegFrameRecorder.Exception | FrameGrabber.Exception ex) {
                ex.printStackTrace();
            }
            throw new RuntimeException("合成失败");
        }

    }

}
