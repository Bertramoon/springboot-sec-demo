package com.example.demo.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.example.demo.utils.ExcelSaxReader;
import com.example.demo.vo.Response;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/excel-dos")
public class ExcelDosController {
    // 限制文件最大为1MB
    private final int sizeLimit = 1024 * 1024;

    /**
     * 存在OOM风险：使用XSSFWorkbook读取用户上传的excel文件时会一次性将文件内容读取到内存中
     */
    @PostMapping("/xssfwb")
    public Response<?> xssfwb(@RequestPart MultipartFile file) throws IOException {
        Response<?> response = baseCheck(file);
        if (response != null) {
            return response;
        }
        XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
        return Response.success();
    }

    /**
     * 存在OOM风险：使用WorkbookFactory读取用户上传的excel文件时会一次性将文件内容读取到内存中
     */
    @PostMapping("/wbfactory")
    public Response<?> wbfactory(@RequestPart MultipartFile file) throws IOException {
        Response<?> response = baseCheck(file);
        if (response != null) {
            return response;
        }
        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        return Response.success();
    }

    /**
     * 没有OOM风险：落地临时文件并使用OPCPackage流式打开，不会一次性把数据读取内存中
     */
    @PostMapping("/opc")
    public Response<?> opc(@RequestPart MultipartFile file) throws IOException {
        Response<?> response = baseCheck(file);
        if (response != null) {
            return response;
        }
        String filename = UUID.randomUUID() + ".xlsx";
        try {
            Files.copy(file.getInputStream(), Paths.get(filename));
            ExcelSaxReader.read(filename);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            Files.deleteIfExists(Paths.get(filename));
        }
        return Response.success();
    }

    /**
     * 没有OOM风险：使用Easyexcel读取用户上传的excel文件，不会一次性把数据读取内存中
     */
    @PostMapping("/easyexcel")
    public Response<?> easyexcel(@RequestPart MultipartFile file) throws IOException {
        Response<?> response = baseCheck(file);
        if (response != null) {
            return response;
        }
        EasyExcel.read(file.getInputStream(), new ReadListener() {
            @Override
            public void invoke(Object o, AnalysisContext analysisContext) {
                // 读取一行
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext analysisContext) {
                // 读取完成后
            }
        }).sheet().doRead();
        return Response.success();
    }

    /**
     * 没有OOM风险：设置低压缩比，避免存在OOM风险，但是可能会影响正常的业务。
     * 该配置属于全局配置，因此调用该接口后，上面其他接口的OOM问题会消失，如需测试可以重启。
     * 在实际的生产代码中，一般将ZipSecureFile.xxx配置到Bean中，在服务启动时完成设置
     *
     * MIN_INFLATE_RATIO：最小膨胀比，默认0.01，即压缩后大小不能小于原来的0.01
     * MAX_ENTRY_SIZE：压缩包内最大单个文件解压后最大大小，默认4GB
     * MAX_FILE_COUNT：最大文件数量，默认1000
     */
    @PostMapping("/xssfwb-ratio")
    public Response<?> xssfwbRadio(@RequestPart MultipartFile file) throws IOException {
        ZipSecureFile.setMinInflateRatio(0.2D);
        Response<?> response = baseCheck(file);
        if (response != null) {
            return response;
        }
        XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
        return Response.success();
    }



    private Response<?> baseCheck(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return Response.fail("File is empty");
        }
        if (file.getSize() > sizeLimit) {
            return Response.fail("File is too large");
        }
        return null;
    }
}
