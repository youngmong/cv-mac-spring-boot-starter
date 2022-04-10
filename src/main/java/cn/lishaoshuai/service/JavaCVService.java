package cn.lishaoshuai.service;

import java.util.Optional;

/**
 * @author lss
 * @version 1.0
 * @date 2022/4/10 8:54 下午
 * @description
 * 常用的音视频操作接口
 */
public interface JavaCVService {

    /**
     * 视频剪切
     *
     * @param oldFilePath 源文件
     * @param newFilePath 输出文件
     * @param width       输出视频宽度
     * @param height      输出视频高度
     * @param x           从源视频x轴的x位置开始
     * @param y           从源视频y轴的y位置开始
     * @return 返回执行结果
     */
    Optional<String> videoCutSide(String oldFilePath,
                                  String newFilePath,
                                  Integer width,
                                  Integer height,
                                  Integer x,
                                  Integer y,
                                  Integer tolerateTime);
}
