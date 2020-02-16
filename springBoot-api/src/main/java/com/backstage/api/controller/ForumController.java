package com.backstage.api.controller;

import com.alibaba.fastjson.JSON;
import com.backstage.base.models.ForumMessage;
import com.backstage.base.models.ForumReply;
import com.backstage.base.service.ForumService;
import com.backstage.result.ApiResultType;
import com.backstage.result.CommonResult;
import com.backstage.util.FileUtil;
import com.backstage.util.PropertyUtil;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.*;

@SuppressWarnings("Duplicates")
@Api(description = "商品管理接口")
@CrossOrigin
@RestController
public class ForumController {

    @Resource
    private ForumService forumService;

    @PostMapping(value = "/postMessage")
    @ResponseBody
    public CommonResult postMessage(MultipartFile[] files, ForumMessage forumMessage) {
        try {
            String fileDir = PropertyUtil.getProperties("forumImgFilePath");
            String filePath = PropertyUtil.getProperties("baseImgFilePath") + fileDir;
            List<Map> pictures = new ArrayList<>();
            Integer pictureId = 0;

            if(files != null && files.length != 0) {
                //遍历并保存文件
                for(MultipartFile file : files){
                    String fileName = FileUtil.writeToLocal(file, filePath);
                    Map<String, Object> picture = new HashMap<>();
                    picture.put("id", pictureId);
                    String imgUrls = fileDir + fileName;
                    picture.put("url", imgUrls);
                    pictureId++;
                    pictures.add(picture);
                }
            }

            if(!pictures.isEmpty()) {
                String picturesStr = JSON.toJSONString(pictures);
                forumMessage.setPicture(picturesStr);
                forumMessage.setCtime(new Date());
                forumService.savePost(forumMessage);
                ApiResultType resultType = ApiResultType.SUCCESS;
                return CommonResult.Builder.SUCC().initSuccData(forumMessage)
                        .initSuccCodeAndMsg(resultType.getCode(), resultType.getMessage());
            } else {
                ApiResultType resultType = ApiResultType.IMG_UPLOAD_FAIL;
                return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            ApiResultType resultType = ApiResultType.SYS_ERROR;
            return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
        }
    }

    @GetMapping(value = "/showPost")
    @ResponseBody
    public CommonResult showPost(Long userId) {
        try {
            List<ForumMessage> forumMessages = forumService.showPost(userId);
            return CommonResult.Builder.SUCC().initSuccCodeAndMsg("0", "suc").initSuccData(forumMessages);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResultType resultType = ApiResultType.SYS_ERROR;
            return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
        }
    }

    @GetMapping(value = "/like")
    @ResponseBody
    public CommonResult likeStatusChange(Long userId, Long fcmid) {
        try {
            Map likeMap = forumService.likeStatusChange(userId, fcmid);
            return CommonResult.Builder.SUCC().initSuccCodeAndMsg("0", "suc").initSuccData(likeMap);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResultType resultType = ApiResultType.SYS_ERROR;
            return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
        }
    }


    @GetMapping(value = "/reply")
    @ResponseBody
    public CommonResult postReply(ForumReply forumReply) {
        try {
            forumService.saveForumReply(forumReply);
            return CommonResult.Builder.SUCC().initSuccCodeAndMsg("0", "suc");
        } catch (Exception e) {
            e.printStackTrace();
            ApiResultType resultType = ApiResultType.SYS_ERROR;
            return CommonResult.Builder.FAIL().initErrCodeAndMsg(resultType.getCode(), resultType.getMessage());
        }
    }

}
