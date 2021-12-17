package ink.kangaroo.pixivhub.service;

import ink.kangaroo.pixiv.sdk.model.rank.PixivRankContent;
import ink.kangaroo.pixiv.sdk.model.rank.PixivRankMode;
import ink.kangaroo.pixivhub.utils.ApplicationContextUtils;
import ink.kangaroo.pixivhub.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

@Component
@Slf4j
public class DailyTaskService {
    //    private final SpotlightService spotlightService;
    @Qualifier("taskScheduler")
    @Autowired
    private Executor executor;
    private PixivService pixivService;
//    private final MainCrawlerService mainCrawlerService;

//    @Scheduled(cron = "0 30 2 * * ?")
//    public void spotlight() {
//        spotlightService.pullAllSpotlight();
//    }

    //    @Scheduled(cron = "0 26 17 * * ?")
    @Scheduled(cron = "0 0 12 * * ?")
    public void rank() {
        pixivService = ApplicationContextUtils.getBean(PixivService.class);
        for (PixivRankMode value : PixivRankMode.values()) {
            for (int i = 0; i < 5; i++) {
                String date = DateUtils.parseDateToStr(DateUtils.YYYYMMDD, DateUtils.addMinutes(DateUtils.getNowDate(), -(35 * 60 + 25)));
                pixivService.getPixivRankResultRealTime(PageRequest.of(i, 50), date, value.getValue(), PixivRankContent.PIXIV_RANK_ALL.getValue());
            }
        }
    }

    @Scheduled(cron = "0 * * * * ?")
    public void mainCrawler() throws InterruptedException {
        log.info("Time -> {}", DateUtils.dateTimeNow());
    }
//    @Scheduled(cron = "0 * * * * ?")
//    public void header() throws InterruptedException {
//        pixivService = ApplicationContextUtils.getBean(PixivService.class);
////        String date = DateUtils.parseDateToStr(DateUtils.YYYYMMDD, DateUtils.addMinutes(DateUtils.getNowDate(), -(35 * 60 + 25)));
//
//        String date = DateUtils.parseDateToStr(DateUtils.YYYYMMDD, DateUtils.addMinutes(DateUtils.getNowDate(), -(36 * 60)));
//        PixivRankResult all = pixivService.getPixivRankResultRealTime(PageRequest.of(1, 50), date, PixivRankMode.PIXIV_RANK_DAILY.getValue(), "all");
//        if (CollectionUtils.isEmpty(all.getContents())){
//            try {
//                EmailUtils.sendEmil("26599114@qq.com",String.format("内容为空： Page -> %s,date -> %s,RankMode -> %s,content -> %s,",PageRequest.of(1, 50).toString(), date, PixivRankMode.PIXIV_RANK_DAILY.getValue(), "all"));
//            } catch (UnsupportedEncodingException | MessagingException e) {
//                e.printStackTrace();
//            }
//        }
//
//    }
}