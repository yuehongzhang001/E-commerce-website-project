package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import org.apache.commons.io.FilenameUtils;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author mqx
 * @date 2021-4-12 15:15:09
 */
@RestController
@RequestMapping("admin/product")
public class FileUploadController {

    @Value("${fileServer.url}")
    private String fileServerUrl ; // fileServerUrl=http://192.168.200.128:8080/

    //  方法真正返回的路径：http://192.168.200.128:8080/group1/M00/00/01/wKjIgF9zVVGEavWOAAAAAO_LJ4k561.png
    //  http://api.gmall.com/admin/product/fileUpload
    //  file 什么样的? 在后台管理项目中 vue 上传组件，给的名字就是file！
    @PostMapping("fileUpload")
    public Result fileUpload(MultipartFile file) throws Exception {
        /*
        1.  加载配置文件tracker.conf
        2.  初始化当前文件
        3.  创建TrackerClient
        4.  创建TrackerServer
        5.  创建StorageClient1
        6.  文件上传
         */
        String configFile = this.getClass().getResource("/tracker.conf").getFile();
        String path = "";
        //  判断
        if(configFile!=null){
            //  初始化
            ClientGlobal.init(configFile);
            //  创建TrackerClient
            TrackerClient trackerClient = new TrackerClient();
            //  创建TrackerServer
            TrackerServer trackerServer = trackerClient.getConnection();
            //  创建StorageClient1
            StorageClient1 storageClient1 = new StorageClient1(trackerServer,null);
            //  文件上传
            //  获取到文件后缀名  zly.jpg  ---> xdfsfarwr1234554542as9082304.jpg
            String extName = FilenameUtils.getExtension(file.getOriginalFilename());
            //  获取到文件上传之后的url！
            path = storageClient1.upload_appender_file1(file.getBytes(), extName, null);
            //  group1/M00/00/01/wKjIgF9zVVGEavWOAAAAAO_LJ4k561.png
            System.out.println("文件上传之后的路径：\t"+path);
        }
        //  返回最终的路径！
        return Result.ok(fileServerUrl+path);
    }
}
