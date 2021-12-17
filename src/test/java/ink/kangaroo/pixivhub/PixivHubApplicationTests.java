package ink.kangaroo.pixivhub;

import ink.kangaroo.pixivhub.service.PixivService;
import ink.kangaroo.pixivhub.utils.ApplicationContextUtils;
import ink.kangaroo.pixivhub.utils.JDBCUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class PixivHubApplicationTests {


    public static void main(String[] args) {

        HttpClient
//        new Cookie ()
        JDBCUtils.update("insert into info (url,name) values (?,?)", "url", "title");
    }
    @Test
    void contextLoads() {
        PixivService bean = ApplicationContextUtils.getBean(PixivService.class);
        bean.getPixivArtist("648285");
        System.out.println(bean);
    }

}
