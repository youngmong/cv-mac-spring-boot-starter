package cn.lishaoshuai.service.impl;

import cn.lishaoshuai.service.JavaCVService;
import cn.lishaoshuai.utils.FFMUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author lss
 * @version 1.0
 * @date 2022/4/10 9:25 下午
 * @description 音视频处理实现类
 */
@Service("javaCVService")
public class JavaCVServiceImpl implements JavaCVService {

    static final Logger logger = LoggerFactory.getLogger(JavaCVServiceImpl.class);

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
    @Override
    public Optional<String> videoCutSide(String oldFilePath,
                                         String newFilePath,
                                         Integer width,
                                         Integer height,
                                         Integer x,
                                         Integer y,
                                         Integer tolerateTime) {
        if (Objects.isNull(width)) width = 0;
        if (Objects.isNull(height)) width = 0;
        if (Objects.isNull(x)) x = 0;
        if (Objects.isNull(y)) y = 0;
        File file = new File(newFilePath);
        if (file.exists()) {
            //当输出文件已存在时，则删除
            file.delete();
        }
        List<String> commands = new ArrayList<>();
        commands.add("-i");
        commands.add(oldFilePath);
        commands.add("-vf");
        String cutCmd = "crop=" + width + ":" + height + ":" + x + ":" + y;
        commands.add(cutCmd);
        commands.add(newFilePath);
        String executeResult = FFMUtils.executeCmd(commands, tolerateTime);
        logger.info("-- 执行结果：{} --", executeResult);
        return Optional.of(executeResult);
    }
}
