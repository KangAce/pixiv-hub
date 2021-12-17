package ink.kangaroo.pixivhub.utils;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.LaunchOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class PuppeteerUtils {
    private static Browser browser = null;
    private Integer poolSize;
    private static Set<Page> pagePool = new HashSet<>();
    private static Set<Page> usePagePool = new HashSet<>();

    static {
//        String path = "D:\\Devsoftware\\chrome-win\\chrome.exe";
        String path = "/usr/local/chrome-linux/chrome";
//        path = new String("D:\\Devsoftware\\chrome-win\\chrome.exe".getBytes(), StandardCharsets.UTF_8);
        ArrayList<String> arrayList = new ArrayList<>();
        new LaunchOptions();
        LaunchOptions launchOptions = new LaunchOptions();
        launchOptions.setArgs(arrayList);
        launchOptions.setTimeout(30000);
        launchOptions.setHeadless(false);
        launchOptions.setExecutablePath(path);
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        arrayList.add("--disable-dev-shm-usage");
        arrayList.add("--window-size=1920x1080");
        arrayList.add("--disable-gpu");
        arrayList.add("--hide-scrollbars");
        arrayList.add("--blink-settings=imagesEnabled=false");
//        arrayList.add("--headless");
        try {
            browser = Puppeteer.launch(launchOptions);
            pagePool.addAll(browser.pages());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Browser getBrowser() {
        return browser;
    }

    public static Page createPage() {
        Page tmpPage = null;
        if (pagePool.size() > 0) {
            for (Page page : pagePool) {
                tmpPage = page;
            }
            pagePool.remove(tmpPage);
            usePagePool.add(tmpPage);
        } else {
            if (pagePool.size() + usePagePool.size() < 5) {
                tmpPage = browser.newPage();
                usePagePool.add(tmpPage);
            }
        }
        return tmpPage;
    }

    public static void destroyPage(Page page) {
        usePagePool.remove(page);
        pagePool.add(page);
    }

}
