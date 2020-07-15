package com.itcast.minio.utils;

import com.itcast.minio.model.base.ResponseModel;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Minio文件存储云服务相关工具类
 * @author zheng.zhang
 */
@Component
@Slf4j
public class MinioUtil {
    /**
     * 服务器地址
     */
    @Value("${minio.url}")
    private String url;

    /**
     * 登录账号
     */
    @Value("${minio.accessKey}")
    private String accessKey;

    /**
     * 登录密码
     */
    @Value("${minio.secretKey}")
    private String secretKey;

    /**
     * 缩略图大小
     */
    @Value("${minio.thumbor.width}")
    private String thumborWidth;

    /**
     * Minio文件上传
     * @param bucketName 桶
     * @param file 文件
     * @param fileName 文件名
     * @return 统一响应结果集
     */
    public ResponseModel minioUpload(MultipartFile file, String fileName, String bucketName) {
        try {
            MinioClient minioClient = new MinioClient(url, accessKey, secretKey);
            boolean bucketExist = minioClient.bucketExists(bucketName);
            if (bucketExist) {
                log.info("仓库" + bucketName + "已经存在，可以直接上传文件");
            } else {
                // 创建Bucket
                minioClient.makeBucket(bucketName);
            }

            // 如果文件小于20兆
            if (file.getSize() <= 20971520) {
                // 如果源文件名为空或者指定文件名为空，随机生成文件名
                if (StringUtils.isEmpty(file.getOriginalFilename())) {
                    fileName = UUID.randomUUID().toString().replaceAll("-", "");
                }

                // minio仓库名
                minioClient.putObject(bucketName, fileName, file.getInputStream(), file.getContentType());
                log.info("上传文件成功！" + fileName + "存至" + bucketName);
                String fileUrl = bucketName + "/" + fileName;

                // 响应结果详细信息
                Map<String, Object> map = new HashMap<>();
                map.put("fileUrl", fileUrl);
                map.put("bucketName", bucketName);
                map.put("originFileName", fileName);
                return ResponseModel.ok(map);
            } else {
                throw new Exception("请上传小于20兆的文件！");
            }
        } catch (Exception e) {
            if (e.getMessage().contains("ORA")) {
                return ResponseModel.build(500, "上传失败，查询参数错误！");
            }
            return ResponseModel.build(500, e.getMessage());
        }
    }

    /**
     * 判断文件是否存在
     * @param fileName 文件名
     * @param bucketName 桶名
     * @return 是否存在
     */
    public boolean isFileExisted(String fileName, String bucketName) {
        boolean flag = false;
        InputStream inputStream = null;
        try {
            MinioClient minioClient = new MinioClient(url, accessKey, secretKey);
            inputStream = minioClient.getObject(bucketName, fileName);
            if (inputStream != null) {
                flag = true;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            flag = false;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
        }
        return flag;
    }

    /**
     * 删除文件
     * @param bucketName 桶
     * @param fileName 文件名
     * @return 是否成功
     */
    public boolean deleteFile(String bucketName, String fileName) {
        boolean flag = false;
        try {
            MinioClient minioClient = new MinioClient(url, accessKey, secretKey);
            minioClient.removeObject(bucketName, fileName);
            flag = true;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return flag;
    }

    /**
     * 下载文件
     * @param bucketName 桶名
     * @param fileName 文件名
     * @param response 响应
     * @return 统一响应结果集
     */
    public ResponseModel downloadFile(String bucketName, String fileName, HttpServletResponse response) {
        try {
            MinioClient minioClient = new MinioClient(url, accessKey, secretKey);
            InputStream fileStream = minioClient.getObject(bucketName, fileName);
            String fileNameCode = new String(fileName.getBytes("ISO8859-1"), StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment;filename=" + fileNameCode);
            ServletOutputStream outputStream = response.getOutputStream();

            // 输出文件
            int length;
            byte[] buffer = new byte[1024];
            while ((length = fileStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            fileStream.close();
            outputStream.close();
            return ResponseModel.ok(fileName + "下载成功");
        } catch (Exception e) {
            if (e.getMessage().contains("ORA")) {
                return ResponseModel.build(500, "下载失败，查询参数错误");
            }
            return ResponseModel.build(500, "下载出错" + e.getMessage());
        }
    }

    /**
     * 获取文件流
     * @param bucketName 桶
     * @param fileName 文件名
     * @return 文件流
     */
    public InputStream getFileInputStream(String bucketName, String fileName) {
        InputStream inputStream = null;
        try {
            MinioClient minioClient = new MinioClient(url, accessKey, secretKey);
            inputStream = minioClient.getObject(bucketName, fileName);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return inputStream;
    }
}
