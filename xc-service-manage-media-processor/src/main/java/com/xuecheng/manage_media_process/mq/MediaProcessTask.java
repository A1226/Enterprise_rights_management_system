package com.xuecheng.manage_media_process.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.MediaFileProcess_m3u8;
import com.xuecheng.framework.utils.HlsVideoUtil;
import com.xuecheng.framework.utils.Mp4VideoUtil;
import com.xuecheng.manage_media_process.dao.MediaFileRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 监听视频处理队列，并进行视频处理。
 */
@Component
public class MediaProcessTask {

    //根路径
    @Value("${xc-service-manage-media.video-location}")
    private String file_path;

    //ffmpeg绝对路径
    @Value("${xc-service-manage-media.ffmpeg-path}")
    private String ffmpeg_path;

    @Autowired
    MediaFileRepository mediaFileRepository;

    /**
     * 监听视频 接收视频处理消息进行视频处理
     * @param msg
     */
    //配置队列，配置多线程
    @RabbitListener(queues = "${xc-service-manage-media.mq.queue-media-video-processor}",containerFactory = "customContainerFactory")
    public void receiveMediaProcessTask(String msg){
        //1.解析消息内容，得到mediaId
        Map msgMap = JSON.parseObject(msg, Map.class);
        String mediaId = (String) msgMap.get("mediaId");
        //2.拿mediaID从数据库查询文件信息
        Optional<MediaFile> fileOptional = mediaFileRepository.findById(mediaId);
        if (!fileOptional.isPresent()){
            return;
        }
        //获取到数据查看视频类型是否是avi
        MediaFile mediaFile = fileOptional.get();
        if (!mediaFile.getFileType().equals("avi")){
            //无需处理参数
            mediaFile.setProcessStatus("303004");
            mediaFileRepository.save(mediaFile);
        }else {
            //需要处理 处理中
            mediaFile.setProcessStatus("303001");
            mediaFileRepository.save(mediaFile);
        }
        //3.将mp4转换成m3u8和ts文件
        //要处理的视频文件具体路径
        String video_path = file_path + mediaFile.getFilePath() + mediaFile.getFileName();
        //生成mp4的视频名称
        String mp4_name = mediaFile.getFileId() + ".mp4";
        //生成mp4视频保存路径
        String mp4folder_path = file_path + mediaFile.getFilePath();
        //String ffmpeg_path, String video_path, String mp4_name, String mp4folder_path
        //创建处理视频的工具类
        Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpeg_path,video_path,mp4_name,mp4folder_path);
        //进行处理
        String results = mp4VideoUtil.generateMp4();
        if (results == null || !results.equals("success")){
            //处理失败
            mediaFile.setProcessStatus("303003");
            //操作失败写入处理日志
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            mediaFileProcess_m3u8.setErrormsg(results);
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
            mediaFileRepository.save(mediaFile);
            return;
        }

        //4.将mp4生成m3u8和ts文件
        //mp4的视频文件路径 String video_path
        String mp4_video_path = file_path + mediaFile.getFilePath() + mp4_name;
        //m3u8的文件视频名称 String m3u8_name
        String m3u8_name = mediaFile.getFileId() + ".m3u8";
        //m3u8的文件视频路径
        String m3u8_path = file_path + mediaFile.getFilePath() + "hls/";
        //String ffmpeg_path, String video_path, String m3u8_name,String m3u8folder_path
        HlsVideoUtil hlsVideoUtil = new HlsVideoUtil(ffmpeg_path,mp4_video_path,m3u8_name,m3u8_path);
        String result = hlsVideoUtil.generateM3u8();
        if (result == null || !result.equals("success")){
            //处理失败
            mediaFile.setProcessStatus("303003");
            //操作失败写入处理日志
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            mediaFileProcess_m3u8.setErrormsg(result);
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
            mediaFileRepository.save(mediaFile);
            return;
        }

        //处理成功
        mediaFile.setProcessStatus("303002");
        //获取m3u8列表 存入MongoDB
        List<String> ts_list = hlsVideoUtil.get_ts_list();
        MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
        mediaFileProcess_m3u8.setTslist(ts_list);
        mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
        //保存fileURL（此URL就是视频播放的相对路径）
        String fileUrl = mediaFile.getFilePath() + "hls/" + m3u8_name;
        mediaFile.setFileUrl(fileUrl);
        mediaFileRepository.save(mediaFile);
    }
}
