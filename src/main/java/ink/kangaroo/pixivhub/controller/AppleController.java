package ink.kangaroo.pixivhub.controller;


import ink.kangaroo.pixiv.sdk.PixivClient;
import ink.kangaroo.pixiv.sdk.model.illust.PixivIllustDetailResult;
import ink.kangaroo.pixiv.sdk.model.rank.PixivRankContent;
import ink.kangaroo.pixiv.sdk.model.rank.PixivRankMode;
import ink.kangaroo.pixiv.sdk.model.rank.result.PixivRankContentResult;
import ink.kangaroo.pixiv.sdk.model.rank.result.PixivRankResult;
import ink.kangaroo.pixivhub.service.PixivService;
import ink.kangaroo.pixivhub.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Random;

@Slf4j
@RestController
@RequestMapping("/api/apple")
public class AppleController {
    private final PixivService pixivService;
    private final PixivClient pixivClient;

    public AppleController(PixivService pixivService, PixivClient pixivClient) {
        this.pixivService = pixivService;
        this.pixivClient = pixivClient;
    }

    @RequestMapping("bizhi")
    public void bizhi(HttpServletRequest request, HttpServletResponse response, String text) {
        String date = DateUtils.parseDateToStr(DateUtils.YYYYMMDD, DateUtils.addMinutes(DateUtils.getNowDate(), -(35 * 60 + 25)));
        Random random = new Random();
        int page = random.nextInt(5);
        PageRequest of = PageRequest.of(page, 50);

        int id = random.nextInt(3);
        PixivRankResult all = pixivService.getPixivRankResultRealTime(of, date, PixivRankMode.getById(id).getValue(), PixivRankContent.PIXIV_RANK_ILLUST.getValue());

        try {
            if (all != null) {
                List<PixivRankContentResult> contents = all.getContents();
                if (CollectionUtils.isEmpty(contents)) {
                    return;
                }
                int index = random.nextInt(contents.size());
                PixivRankContentResult pixivRankContentResult = contents.get(index);
                PixivIllustDetailResult pixivIllustDetail = pixivService.getPixivIllustDetail(pixivRankContentResult.getIllustId());
                String original = pixivIllustDetail.getUrls().getOriginal();
                InputStream it = pixivClient.getInputStream(original);
                if ("png".equals(FilenameUtils.getExtension(original))) {
                    response.setContentType("image/png");
                } else {
                    response.setContentType("image/jpeg");
                }
                ServletOutputStream os = response.getOutputStream();
                to(it, os, null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }

    private void to(InputStream it, OutputStream os, Object size) {
        try {
            /**
             * 加个限速，在下载原图的时候加上进入条，增加一个下载等待时间；
             */
            //文件拷贝
            byte[] flush = new byte[1024];
            int len = 0;

            while (0 <= (len = it.read(flush))) {
                os.write(flush, 0, len);
//                Thread.sleep(500);
            }
           /* while (true) {
                assert it != null;
                if (!(0 <= (len = it.read(flush)))) break;
                os.write(flush, 0, len);
                Thread.sleep(500);
            }*/
            os.flush();
            //关闭流的注意 先打开的后关
            os.close();
            it.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
