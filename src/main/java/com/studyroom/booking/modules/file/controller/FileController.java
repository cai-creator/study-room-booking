package com.studyroom.booking.modules.file.controller;

import com.studyroom.booking.common.Result;
import com.studyroom.booking.common.annotation.RequireRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/files")
@Tag(name = "文件存储", description = "通用文件上传下载接口（预留扩展）")
@SecurityRequirement(name = "BearerAuth")
public class FileController {

    @PostMapping("/upload")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "文件上传", description = "上传文件到对象存储")
    public Result<Map<String, Object>> uploadFile(
            @Parameter(description = "文件") @RequestParam MultipartFile file,
            @Parameter(description = "业务类型") @RequestParam(required = false) String bizType) {
        log.info("文件上传: name={}, size={}, bizType={}",
                file.getOriginalFilename(), file.getSize(), bizType);
        Map<String, Object> result = new HashMap<>();
        result.put("filename", file.getOriginalFilename());
        result.put("size", file.getSize());
        result.put("bizType", bizType);
        result.put("message", "文件上传接口已预留，待后续对接OSS/MinIO");
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "文件下载", description = "通过文件ID下载文件")
    public Result<Map<String, Object>> getFileInfo(@Parameter(description = "文件ID") @PathVariable String id) {
        log.info("查询文件信息: id={}", id);
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("message", "文件下载接口已预留");
        return Result.success(result);
    }

    @DeleteMapping("/{id}")
    @RequireRole({"ADMIN", "SUPER_ADMIN"})
    @Operation(summary = "删除文件", description = "删除已上传的文件")
    public Result<Void> deleteFile(@Parameter(description = "文件ID") @PathVariable String id) {
        log.info("删除文件: id={}", id);
        return Result.success("文件删除接口已预留", null);
    }
}