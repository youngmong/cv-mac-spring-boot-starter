package cn.lishaoshuai.utils;

import org.bytedeco.javacpp.Loader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author lss
 * @version 1.0
 * @date 2022/4/5 5:49 下午
 * @description 执行ffmpeg命令的工具类
 */
public class FFMUtils {

    static final Logger logger = LoggerFactory.getLogger(FFMUtils.class);

    /**
     * 执行ffmpeg命令
     *
     * @param commands     命令集合
     * @param tolerateTime 允许执行最大耗时，单位秒
     * @return 返回结果：
     * 成功则返回："ok"
     * 失败则返回："error"
     */
    public static String executeCmd(List<String> commands,
                                    Integer tolerateTime) {
        long startTime = System.currentTimeMillis();
        if (CollectionUtils.isEmpty(commands)) {
            throw new RuntimeException("ffmpeg指令为空");
        }
        LinkedList<String> ffmpegCommands = new LinkedList<>(commands);
        ffmpegCommands.addFirst(Loader.load(org.bytedeco.ffmpeg.ffmpeg.class));
        Process process = null;
        // 创建操作系统进程
        ProcessBuilder builder = new ProcessBuilder();
        try {
            builder.redirectErrorStream(false);
            // 执行ffmpeg指令，每个ProcessBuilder 实例管理一个进程属性集，start()方法利用这些属性创建一个新的Process实例
            // start()方法可以从同一实例重复调用，以利用相同的或相关的属性创建新的子进程
            process = builder.command(ffmpegCommands)
                    //使用与当前Java进程相同的IO
                    .inheritIO()
                    .start();
            // 注意：必须要取出ffmpeg在执行命令过程中产生的输出信息，
            // 如果不取出的话，当输出流信息填满jvm存储输出流信息的缓冲区时，线程会阻塞
            PrintStream inputStream = new PrintStream(process.getInputStream());
            PrintStream errorStream = new PrintStream(process.getErrorStream());
            inputStream.start();
            errorStream.start();
            //等待ffmpeg命令执行完
            if (Objects.isNull(tolerateTime)) {
                // 设置默认值 60秒，如果当前进程60秒没执行完，则退出
                tolerateTime = 60;
            }
            process.waitFor(tolerateTime, TimeUnit.SECONDS);

            String errorInfo = errorStream.stringBuffer.append(inputStream.stringBuffer).toString();
            if (!StringUtils.isEmpty(errorInfo)) {
                logger.error("-- 执行ffmpeg命令，获取错误信息为：{} --", errorInfo);
            }
            //输出执行的命令信息
            String cmd = Arrays.toString(ffmpegCommands.toArray()).replace(",", "");
            logger.info("-- 执行ffmpeg命令： {} --", cmd);
            //执行结果
            return process.exitValue() == 0 ? "ok" : "error";
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException("ffmpeg命令执行错误: " + e.getMessage());
        } finally {
            Runtime runtime = Runtime.getRuntime();
            if (!ObjectUtils.isEmpty(process)) {
                ProcessKiller processKiller = new ProcessKiller(process);
                //JVM退出时，先通过钩子关闭FFMPEG进程
                runtime.addShutdownHook(processKiller);

            }
            long costTime = System.currentTimeMillis() - startTime;
            logger.info("execute cost : {} milliseconds", costTime);
        }
    }

    /**
     * 在程序退出前结束已有的FFMPEG进程
     */
    private static class ProcessKiller extends Thread {
        private final Process process;

        public ProcessKiller(Process process) {
            this.process = process;
        }

        @Override
        public void run() {
            this.process.destroyForcibly();
        }
    }

    static class PrintStream extends Thread {
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        StringBuffer stringBuffer = new StringBuffer();

        public PrintStream(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            try {
                if (null == inputStream) {
                    logger.error("读取输出流出错，当前输出流为空");
                }
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuffer.append(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("读取输入流出错，错误信息为：{}", e.getMessage());
            } finally {
                try {
                    if (null != bufferedReader) {
                        bufferedReader.close();
                    }
                    if (null != inputStream) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error("关闭流出错：{}", e.getMessage());
                }
            }
        }
    }
}
