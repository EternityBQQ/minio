package com.itcast.minio.controller;

import com.itcast.minio.model.base.ResponseModel;
import com.itcast.minio.utils.MinioUtil;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 文件服务接口
 * @author zheng.zhang
 */
@Slf4j
@RestController
@RequestMapping("/images")
@Api(tags = "文件服务接口")
public class FileController {
    @Autowired
    private MinioUtil minioUtil;

    /**
     * 桶名
     */
    @Value("${minio.bucketName.facility}")
    private String bucketName;

    /**
     * 文件上传
     * @param file 上传的文件
     * @return 返回响应
     */
    @PutMapping(value = "/upload")
    @ApiOperation(value = "上传接口", authorizations = {@Authorization(value = "token")})
    public ResponseModel fileUpload(@RequestParam("file") MultipartFile file) {
        ResponseModel responseModel = null;
        try {
            // 获取上传文件名称
            String orgName;
            String fileName = "";
            if (file != null) {
                orgName = file.getOriginalFilename();
                if (orgName != null) {
                    fileName = System.currentTimeMillis() + "_" + orgName.replaceAll(" ", "-");
                }
            }
            // 步骤一、判断文件是否存在过 存在则不能上传（Minio服务器上传同样位置的同样文件名的文件时，新的文件会把旧的文件覆盖掉）
            boolean fileExisted = minioUtil.isFileExisted(fileName, bucketName);
            if (fileExisted) {
                log.error("文件" + fileName + "已经存在");
                return ResponseModel.build(500, "文件已存在");
            }
            // 步骤二、上传文件
            responseModel = minioUtil.minioUpload(file, fileName, bucketName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseModel;
    }

    /**
     * 预览图片
     * @param bucketName 桶名
     * @param fileName 文件名
     * @param response 响应
     */
    @GetMapping(value = "/view")
    @ApiOperation(value = "预览图片接口", authorizations = {@Authorization(value = "token")})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "bucketName", value = "桶名", paramType = "query"),
            @ApiImplicitParam(name = "fileName", value = "文件名", paramType = "query"),
    })
    public ResponseModel imageView(String bucketName, String fileName, HttpServletResponse response) {
        // ISO8859-1转码成utf-8
        InputStream fileInputStream = null;
        OutputStream outputStream = null;
        ResponseModel responseModel;
        try {
            // 判断文件是否存在
            boolean fileExisted = minioUtil.isFileExisted(fileName, bucketName);
            if (!fileExisted) {
                return ResponseModel.build(500, "文件不存在！");
            }

            fileInputStream = minioUtil.getFileInputStream(bucketName, fileName);

            outputStream = response.getOutputStream();
            int len;
            byte[] buff = new byte[1024];
            while ((len = fileInputStream.read(buff)) > 0) {
                outputStream.write(buff, 0, len);
            }
            response.flushBuffer();
            responseModel = ResponseModel.ok();
        } catch (IOException e) {
            log.error("预览图片失败" + e.getMessage());
            responseModel = ResponseModel.build(500, "预览图片失败：" + e.getMessage());
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
        }
        return responseModel;
    }

    @GetMapping(value = "/downloadFile")
    @ApiOperation(value = "下载图片接口", authorizations = {@Authorization(value = "token")})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "bucketName", value = "桶名", paramType = "query"),
            @ApiImplicitParam(name = "fileName", value = "文件名", paramType = "query"),
    })
    public ResponseModel downloadFile(String bucketName, String fileName, HttpServletResponse response) {
        // 判断文件是否存在
        boolean fileExisted = minioUtil.isFileExisted(fileName, bucketName);
        if (!fileExisted) {
            return ResponseModel.build(500, "文件不存在！");
        }
        return minioUtil.downloadFile(bucketName, fileName, response);
    }

    @DeleteMapping(value = "/deleteFile")
    @ApiOperation(value = "删除图片接口", authorizations = {@Authorization(value = "token")})
    @ApiImplicitParams({
            @ApiImplicitParam(name = "bucketName", value = "桶名", paramType = "query"),
            @ApiImplicitParam(name = "fileName", value = "文件名", paramType = "query"),
    })
    public ResponseModel deleteFile(String bucketName, String fileName) {
        ResponseModel responseModel;
        // 判断文件是否存在
        boolean fileExisted = minioUtil.isFileExisted(fileName, bucketName);
        if (!fileExisted) {
            return ResponseModel.build(500, "文件不存在！");
        }
        boolean result = minioUtil.deleteFile(bucketName, fileName);
        if (!result) {
            responseModel = ResponseModel.build(500, "删除失败!");
        } else {
            responseModel = ResponseModel.ok();
        }
        return responseModel;
    }
}
