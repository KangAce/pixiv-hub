package ink.kangaroo.pixivhub.utils;

/**
 * @author Kang
 * @date 2020/8/29 12:03
 */

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.*;
import javax.mail.internet.MimeMessage.RecipientType;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.*;


//邮箱工具类
public class EmailUtils {

    // 发信人的Email地址
    private static String fromEmailAccount;
    // 发信人的称呼
    private static String fromEmailShow;
    // 发信人的Email邮箱密码
    private static String fromEmailPassword;
    // 收信人的Email地址
    private static String toEmailAccount;
    // 收信人的称呼
    private static String toEmailShow;
    // 发信人邮箱的SMTP服务器地址
    private static String formEmailSMTPHOST;
    // 发信人邮箱的POP3服务器地址
    private static String formEmailPOP3HOST;
    // 发信人邮箱的IMAP服务器地址
    private static String formEmailIMAPHOST;
    // 连接邮件服务器的参数配置
    private static Properties properties;
    // 是否是SMTP协议
    private static Boolean isSMTP;
    // 是否是SSL连接
    private static Boolean isSSL;
    // 邮件主题
    private static String emailSubject;
    // 邮件内容
    private static String emailContent;
    //用于添加到邮件中的图片
    private static Map<String, File> imageFilesMap;
    //用于添加到邮件中的附件
    private static List<File> attachContentsList;
    // 邮件发送日期
    private static Date emailSendDate;
    // 邮件编码
    private final static String EMAIL_CHARSET = "UTF-8";
    // 邮件内容编码
    private final static String CONTENT_CHARSET = "text/html;charset=UTF-8";

    // 初始化
    private static void initEmail() {

        // 默认配置一些邮件信息
        // 默认配置发信人的Email地址
        if (EmailUtils.fromEmailAccount == null) {
            // 发信人的Email地址（这是我注册的一个测试用的邮箱）
            EmailUtils.fromEmailAccount = "moretribes@126.com";
            // 发信人的Email邮箱密码
            EmailUtils.fromEmailPassword = "KLKCRTLICOVGQMTC";
            // 发件人的服务器地址
            EmailUtils.formEmailSMTPHOST = "smtp.126.com";
        }
        // 默认配置发信人称呼
        if (EmailUtils.fromEmailShow == null) {
            // 发信人称呼位邮箱前缀
            EmailUtils.fromEmailShow = EmailUtils.fromEmailAccount.substring(0,
                    EmailUtils.fromEmailAccount.indexOf("@"));
        }
        // 默认配置收信人的称呼
        if (EmailUtils.toEmailShow == null) {
            if (EmailUtils.toEmailAccount != null) {
                // 收信人的称呼为收件人邮箱前缀
                EmailUtils.toEmailShow = EmailUtils.toEmailAccount.substring(0,
                        EmailUtils.toEmailAccount.indexOf("@"));
            }
        }
        // 默认使用SMTP协议
        if (EmailUtils.isSMTP == null) {
            EmailUtils.isSMTP = true;
        }
        // 默认不使用SSL安全认证协议
        if (EmailUtils.isSSL == null) {
            EmailUtils.isSSL = false;
        }
        // 默认邮件内容为 Hello World
        if (EmailUtils.emailContent == null) {
            EmailUtils.emailContent = "<h1>Hello,World!</h1>";
        }
        // 默认邮件主题为内容前两个字
        if (EmailUtils.emailSubject == null) {
            // 选择21是因为默认邮件内容长度是21
            if (EmailUtils.emailContent.length() >= 21) {
                EmailUtils.emailSubject = EmailUtils.emailContent.substring(0, 21);
            } else {
                EmailUtils.emailSubject = EmailUtils.emailContent;
            }
        }
        // 默认邮件发送日期为当前时间
        if (EmailUtils.emailSendDate == null) {
            EmailUtils.emailSendDate = new Date();
        }

        // 实例化Properties
        properties = new Properties();

        // 如果是SMTP协议
        if (isSMTP) {
            properties = setSMTPProps(properties);
        }

        // 如果为 SSL 安全认证（连接失败, 要求 SSL 安全连接时使用）
        if (isSSL) {
            properties = setSSLProps(properties);
        }
    }

    // SMTP协议
    private static Properties setSMTPProps(Properties properties) {
        // 指定默认消息传输协议。该
        // Session方法getTransport()返回实现此协议的Transport对象。默认情况下，返回配置文件中的第一个传输提供程序。
        String transportProtocolKEY = "mail.transport.protocol";
        // 指定协议的邮件服务器的主机名。覆盖mail.host属性。
        String smtpHostKEY = "mail.smtp.host";
        // 指定协议的邮件服务器的端口号。如果未指定，则使用协议的默认端口号。(一般不用设置)
        String smtpPortKEY = "mail.smtp.port";
        // 使用指定的协议连接到邮件服务器时要使用的用户名。覆盖mail.user属性。(一般不用设置)
        String smtpUserKEY = "mail.smtp.user";
        // 如果为true，则尝试使用AUTH命令认证用户。默认为false。
        String smtpAuthKEY = "mail.smtp.auth";

        // 设置协议为smtp
        String transportProtocolVALUE = "smtp";
        properties.setProperty(transportProtocolKEY, transportProtocolVALUE);
        // 发件人的邮箱的 SMTP 服务器地址
        properties.setProperty(smtpHostKEY, formEmailSMTPHOST);
        // 需要请求认证
//        properties.setProperty(smtpAuthKEY, "true");
        return properties;
    }

    // SSL 安全认证
    private static Properties setSSLProps(Properties properties) {
        // 如果为 SSL 安全认证（连接失败, 要求 SSL 安全连接时使用）
        // 如果set，则指定实现该javax.net.SocketFactory接口的类的名称 。此类将用于创建SMTP套接字。
        String socketFactoryClassKEY = "mail.smtp.socketFactory.class";
        // 如果设置为true，则无法使用指定的套接字工厂类创建套接字将导致使用java.net.Socket该类创建套接字。默认为true。
        String socketFactoryFallbackKEY = "mail.smtp.socketFactory.fallback";
        // 指定使用指定套接字工厂时要连接的端口。如果未设置，将使用默认端口。
        String socketFactoryPortKEY = "mail.smtp.socketFactory.port";

        // SMTP 服务器的端口 (非 SSL 连接的端口一般默认为 25, 可以不添加, 如果开启了 SSL 连接,需要改为对应邮箱的 SMTP
        // 服务器的端口
        // QQ邮箱的SMTP(SLL)端口为465或587
        String sslSmtpPortVALUE = "465";
        // 设置端口号
        properties.setProperty(socketFactoryPortKEY, sslSmtpPortVALUE);
        // 指定javax.net.ssl.SSLSocketFactory 。此类将用于创建SMTP套接字。
        String socketFactoryClassVALUE = "javax.net.ssl.SSLSocketFactory";
        // 指定javax.net.ssl.SSLSocketFactory用于创建SMTP套接字。
        properties.setProperty(socketFactoryClassKEY, socketFactoryClassVALUE);
        // 设置使用指定的套接字工厂类创建套接字
        properties.setProperty(socketFactoryFallbackKEY, "false");
        return properties;
    }

    //发送邮件(收件人是多个)
    //toEmailAccountMap：收件人map
    //emailContent：邮件内容
    //imageFilesMap:邮件图片集
    //attachContentsList：邮件附件集
    public static void sendEmil(Map<String, String> toEmailAccountMap, String emailContent, Map<String, File> imageFilesMap, List<File> attachContentsList)
            throws UnsupportedEncodingException, MessagingException {
        sendEmil(null, null, null, null, null, null, null, null, null, emailContent, null, toEmailAccountMap, imageFilesMap, attachContentsList);
    }

    //发送邮件(收件人是多个)
    //toEmailAccountMap：收件人map
    //emailSubject:邮件主题
    //emailContent：邮件内容
    //imageFilesMap:邮件图片集
    //attachContentsList：邮件附件集
    public static void sendEmil(Map<String, String> toEmailAccountMap, String emailSubject, String emailContent, Map<String, File> imageFilesMap, List<File> attachContentsList)
            throws UnsupportedEncodingException, MessagingException {
        sendEmil(null, null, null, null, null, null, null, null, emailSubject, emailContent, null, toEmailAccountMap, imageFilesMap, attachContentsList);
    }

    //发送邮件(收件人是多个)
    //fromEmailAccount：发送人邮箱
    //fromEmailPassword：发送人邮箱密码
    //toEmailAccountMap：收件人map
    //formEmailSMTPHOST：发送人邮箱SMTP服务器地址
    //emailSubject:邮件主题
    //emailContent：邮件内容
    //imageFilesMap:邮件图片集
    //attachContentsList：邮件附件集
    public static void sendEmil(String fromEmailAccount, String fromEmailPassword, Map<String, String> toEmailAccountMap,
                                String formEmailSMTPHOST, String emailSubject, String emailContent, Map<String, File> imageFilesMap, List<File> attachContentsList)
            throws UnsupportedEncodingException, MessagingException {
        sendEmil(fromEmailAccount, null, fromEmailPassword, null, null, formEmailSMTPHOST, null, null,
                emailSubject, emailContent, null, toEmailAccountMap, imageFilesMap, attachContentsList);
    }

    //imageFilesMap:邮件图片集
    //attachContentsList：邮件附件集
    public static void sendEmil(String fromEmailAccount, String fromEmailPassword, Map<String, String> toEmailAccountMap,
                                String formEmailSMTPHOST, String emailSubject, String emailContent, Map<String, File> imageFilesMap, List<File> attachContentsList, Date emailSendDate)
            throws UnsupportedEncodingException, MessagingException {
        sendEmil(fromEmailAccount, null, fromEmailPassword, null, null, formEmailSMTPHOST, null, null,
                emailSubject, emailContent, emailSendDate, toEmailAccountMap, imageFilesMap, attachContentsList);
    }

    //imageFilesMap:邮件图片集
    //attachContentsList：邮件附件集
    public static void sendEmil(String fromEmailAccount, String fromEmailShow, String fromEmailPassword,
                                Map<String, String> toEmailAccountMap, String formEmailSMTPHOST, String emailSubject,
                                String emailContent, Map<String, File> imageFilesMap, List<File> attachContentsList, Date emailSendDate) throws UnsupportedEncodingException, MessagingException {
        sendEmil(fromEmailAccount, fromEmailShow, fromEmailPassword, null, null, formEmailSMTPHOST,
                null, null, emailSubject, emailContent, emailSendDate, toEmailAccountMap, imageFilesMap, attachContentsList);
    }

    //发送邮件(收件人是一个)
    //toEmailAccount：收件人邮箱
    //emailContent：邮件内容
    //imageFilesMap:邮件图片集
    //attachContentsList：邮件附件集
    public static void sendEmil(String toEmailAccount, String emailContent, Map<String, File> imageFilesMap, List<File> attachContentsList)
            throws UnsupportedEncodingException, MessagingException {
        sendEmil(null, null, null, toEmailAccount, null, null, null, null, null, emailContent, null, null, imageFilesMap, attachContentsList);
    }

    //发送邮件(收件人是一个)
    //toEmailAccount：收件人邮箱
    //emailSubject:邮件主题
    //emailContent：邮件内容
    //imageFilesMap:邮件图片集
    //attachContentsList：邮件附件集
    public static void sendEmil(String toEmailAccount, String emailSubject, String emailContent, Map<String, File> imageFilesMap, List<File> attachContentsList)
            throws UnsupportedEncodingException, MessagingException {
        sendEmil(null, null, null, toEmailAccount, null, null, null, null, emailSubject, emailContent, null, null, imageFilesMap, attachContentsList);
    }

    //发送邮件(收件人是多个)
    //fromEmailAccount：发送人邮箱
    //fromEmailPassword：发送人邮箱密码
    //toEmailAccount：收件人邮箱
    //formEmailSMTPHOST：发送人邮箱SMTP服务器地址
    //emailSubject:邮件主题
    //emailContent：邮件内容
    //imageFilesMap:邮件图片集
    //attachContentsList：邮件附件集
    public static void sendEmil(String fromEmailAccount, String fromEmailPassword, String toEmailAccount,
                                String formEmailSMTPHOST, String emailSubject, String emailContent, Map<String, File> imageFilesMap, List<File> attachContentsList)
            throws UnsupportedEncodingException, MessagingException {
        sendEmil(fromEmailAccount, null, fromEmailPassword, toEmailAccount, null, formEmailSMTPHOST, null, null,
                emailSubject, emailContent, null, null, imageFilesMap, attachContentsList);
    }

    //imageFilesMap:邮件图片集
    //attachContentsList：邮件附件集
    public static void sendEmil(String fromEmailAccount, String fromEmailPassword, String toEmailAccount,
                                String formEmailSMTPHOST, String emailSubject, String emailContent, Map<String, File> imageFilesMap, List<File> attachContentsList, Date emailSendDate)
            throws UnsupportedEncodingException, MessagingException {
        sendEmil(fromEmailAccount, null, fromEmailPassword, toEmailAccount, null, formEmailSMTPHOST, null, null,
                emailSubject, emailContent, emailSendDate, null, imageFilesMap, attachContentsList);
    }

    //imageFilesMap:邮件图片集
    //attachContentsList：邮件附件集
    public static void sendEmil(String fromEmailAccount, String fromEmailShow, String fromEmailPassword,
                                String toEmailAccount, String toEmailShow, String formEmailSMTPHOST, String emailSubject,
                                String emailContent, Map<String, File> imageFilesMap, List<File> attachContentsList, Date emailSendDate) throws UnsupportedEncodingException, MessagingException {
        sendEmil(fromEmailAccount, fromEmailShow, fromEmailPassword, toEmailAccount, toEmailShow, formEmailSMTPHOST,
                null, null, emailSubject, emailContent, emailSendDate, null, imageFilesMap, attachContentsList);
    }

    //发送邮件(收件人是多个)
    //toEmailAccountMap：收件人map
    //emailContent：邮件内容
    public static void sendEmil(Map<String, String> toEmailAccountMap, String emailContent)
            throws UnsupportedEncodingException, MessagingException {
        sendEmil(null, null, null, null, null, null, null, null, null, emailContent, null, toEmailAccountMap, null, null);
    }

    //发送邮件(收件人是多个)
    //toEmailAccountMap：收件人map
    //emailSubject:邮件主题
    //emailContent：邮件内容
    public static void sendEmil(Map<String, String> toEmailAccountMap, String emailSubject, String emailContent)
            throws UnsupportedEncodingException, MessagingException {
        sendEmil(null, null, null, null, null, null, null, null, emailSubject, emailContent, null, toEmailAccountMap, null, null);
    }

    //发送邮件(收件人是多个)
    //fromEmailAccount：发送人邮箱
    //fromEmailPassword：发送人邮箱密码
    //toEmailAccountMap：收件人map
    //formEmailSMTPHOST：发送人邮箱SMTP服务器地址
    //emailSubject:邮件主题
    //emailContent：邮件内容
    public static void sendEmil(String fromEmailAccount, String fromEmailPassword, Map<String, String> toEmailAccountMap,
                                String formEmailSMTPHOST, String emailSubject, String emailContent)
            throws UnsupportedEncodingException, MessagingException {
        sendEmil(fromEmailAccount, null, fromEmailPassword, null, null, formEmailSMTPHOST, null, null,
                emailSubject, emailContent, null, toEmailAccountMap, null, null);
    }

    public static void sendEmil(String fromEmailAccount, String fromEmailPassword, Map<String, String> toEmailAccountMap,
                                String formEmailSMTPHOST, String emailSubject, String emailContent, Date emailSendDate)
            throws UnsupportedEncodingException, MessagingException {
        sendEmil(fromEmailAccount, null, fromEmailPassword, null, null, formEmailSMTPHOST, null, null,
                emailSubject, emailContent, emailSendDate, toEmailAccountMap, null, null);
    }

    public static void sendEmil(String fromEmailAccount, String fromEmailShow, String fromEmailPassword,
                                Map<String, String> toEmailAccountMap, String formEmailSMTPHOST, String emailSubject,
                                String emailContent, Date emailSendDate) throws UnsupportedEncodingException, MessagingException {
        sendEmil(fromEmailAccount, fromEmailShow, fromEmailPassword, null, null, formEmailSMTPHOST,
                null, null, emailSubject, emailContent, emailSendDate, toEmailAccountMap, null, null);
    }

    //发送邮件(收件人是一个)
    //toEmailAccount：收件人邮箱
    //emailContent：邮件内容
    public static void sendEmil(String toEmailAccount, String emailContent)
            throws UnsupportedEncodingException, MessagingException {
        sendEmil(null, null, null, toEmailAccount, null, null, null, null, null, emailContent, null, null, null, null);
    }

    //发送邮件(收件人是一个)
    //toEmailAccount：收件人邮箱
    //emailSubject:邮件主题
    //emailContent：邮件内容
    public static void sendEmil(String toEmailAccount, String emailSubject, String emailContent)
            throws UnsupportedEncodingException, MessagingException {
        sendEmil(null, null, null, toEmailAccount, null, null, null, null, emailSubject, emailContent, null, null, null, null);
    }

    //发送邮件(收件人是多个)
    //fromEmailAccount：发送人邮箱
    //fromEmailPassword：发送人邮箱密码
    //toEmailAccount：收件人邮箱
    //formEmailSMTPHOST：发送人邮箱SMTP服务器地址
    //emailSubject:邮件主题
    //emailContent：邮件内容
    public static void sendEmil(String fromEmailAccount, String fromEmailPassword, String toEmailAccount,
                                String formEmailSMTPHOST, String emailSubject, String emailContent)
            throws UnsupportedEncodingException, MessagingException {
        sendEmil(fromEmailAccount, null, fromEmailPassword, toEmailAccount, null, formEmailSMTPHOST, null, null,
                emailSubject, emailContent, null, null, null, null);
    }

    public static void sendEmil(String fromEmailAccount, String fromEmailPassword, String toEmailAccount,
                                String formEmailSMTPHOST, String emailSubject, String emailContent, Date emailSendDate)
            throws UnsupportedEncodingException, MessagingException {
        sendEmil(fromEmailAccount, null, fromEmailPassword, toEmailAccount, null, formEmailSMTPHOST, null, null,
                emailSubject, emailContent, emailSendDate, null, null, null);
    }

    public static void sendEmil(String fromEmailAccount, String fromEmailShow, String fromEmailPassword,
                                String toEmailAccount, String toEmailShow, String formEmailSMTPHOST, String emailSubject,
                                String emailContent, Date emailSendDate) throws UnsupportedEncodingException, MessagingException {
        sendEmil(fromEmailAccount, fromEmailShow, fromEmailPassword, toEmailAccount, toEmailShow, formEmailSMTPHOST,
                null, null, emailSubject, emailContent, emailSendDate, null, null, null);
    }

    //发送邮件
    public static void sendEmil(String fromEmailAccount, String fromEmailShow, String fromEmailPassword,
                                String toEmailAccount, String toEmailShow, String formEmailSMTPHOST, Boolean isSMTP, Boolean isSSL,
                                String emailSubject, String emailContent, Date emailSendDate, Map<String, String> toEmailAccountMap, Map<String, File> imageFilesMap, List<File> attachContentsList) throws UnsupportedEncodingException, MessagingException {
        // 发信人的Email地址
        EmailUtils.fromEmailAccount = fromEmailAccount;
        // 发信人称呼
        EmailUtils.fromEmailShow = fromEmailShow;
        //发信人的Email邮箱密码
        EmailUtils.fromEmailPassword = fromEmailPassword;
        // 收信人的Email地址
        EmailUtils.toEmailAccount = toEmailAccount;
        // 收信人的称呼
        EmailUtils.toEmailShow = toEmailShow;
        // 发件人的服务器地址
        EmailUtils.formEmailSMTPHOST = formEmailSMTPHOST;
        // 是否使用SMTP协议
        EmailUtils.isSMTP = isSMTP;
        // 是否使用SSL安全认证协议
        EmailUtils.isSSL = isSSL;
        // 邮件主题
        EmailUtils.emailSubject = emailSubject;
        // 邮件内容
        EmailUtils.emailContent = emailContent;
        // 邮件图片集
        EmailUtils.imageFilesMap = imageFilesMap;
        // 邮件附件集
        EmailUtils.attachContentsList = attachContentsList;
        // 邮件发送日期
        EmailUtils.emailSendDate = emailSendDate;
        // 1. 配置用于连接邮件服务器的参数
        initEmail();
        // 2. 根据配置创建会话对象, 用于和邮件服务器交互
        Session session = Session.getDefaultInstance(properties);
        // 设置为debug模式, 可以查看详细的发送 log
        session.setDebug(true);
        // 3. 创建一封邮件
        MimeMessage message = createMimeMessage(session, toEmailAccountMap);
        //还可以保存邮件到本地
        //OutputStream out = new FileOutputStream("MyEmail.eml");
        // message.writeTo(out);
        // out.flush();out.close();
        // 4. 根据 Session 获取邮件传输对象
        Transport transport = session.getTransport();
        // 5. 使用 邮箱账号 和 密码 连接邮件服务器, 这里认证的邮箱必须与 message 中的发件人邮箱一致, 否则报错
        transport.connect(EmailUtils.fromEmailAccount, EmailUtils.fromEmailPassword);
        // 6. 发送邮件, 发到所有的收件地址, message.getAllRecipients() 获取到的是在创建邮件对象时添加的所有收件人,抄送人, 密送人
        transport.sendMessage(message, message.getAllRecipients());
        // 7. 关闭连接
        transport.close();

    }

    // 创建邮件信息
    private static MimeMessage createMimeMessage(Session session, Map toEmailAccountMap)
            throws UnsupportedEncodingException, MessagingException {
        // 1. 创建一封邮件
        MimeMessage message = new MimeMessage(session);
        // 2. From: 发件人
        message.setFrom(
                new InternetAddress(EmailUtils.fromEmailAccount, EmailUtils.fromEmailShow, EMAIL_CHARSET));
        // 3. To: 收件人（可以增加多个收件人）当toEmailAccountMap不为空时，是多个收件人
        //为了防止出现554 DT:SPM 126，在发送之前，给自己抄一份
        message.setRecipient(RecipientType.CC, new InternetAddress(EmailUtils.fromEmailAccount, EmailUtils.fromEmailShow, EMAIL_CHARSET));
        if (toEmailAccountMap != null) {
            if (toEmailAccountMap != null && toEmailAccountMap.size() > 0) {
                Set<String> emailShows = toEmailAccountMap.keySet();
                String[] emailShowsStr = new String[emailShows.size()];
                emailShows.toArray(emailShowsStr);
                for (int i = 0; i < emailShowsStr.length; i++) {
                    if (i == 0) {
                        //RecipientType.TO:普通发送
                        //RecipientType.CC:抄送
                        //RecipientType.BCC:密送
                        message.setRecipient(RecipientType.TO, new InternetAddress(
                                toEmailAccountMap.get(emailShowsStr[i]).toString(), emailShowsStr[i], EMAIL_CHARSET));
                    } else {
                        message.addRecipient(RecipientType.TO, new InternetAddress(
                                toEmailAccountMap.get(emailShowsStr[i]).toString(), emailShowsStr[i], EMAIL_CHARSET));
                    }
                }
            }
        } else {
            message.setRecipient(RecipientType.TO,
                    new InternetAddress(EmailUtils.toEmailAccount, EmailUtils.toEmailShow, EMAIL_CHARSET));
        }
        // 4. Subject: 邮件主题
        message.setSubject(EmailUtils.emailSubject, EmailUtils.EMAIL_CHARSET);
        // 5. Content: 邮件正文（可以使用html标签）
        //简单内容
        //message.setContent(JavaEmailUtils.emailContent, JavaEmailUtils.CONTENT_CHARSET);
        //复杂内容(带有图片、文本、附件)
        message.setContent(crateMimeMultipartContent());
        // 6. 设置发件时间
        message.setSentDate(EmailUtils.emailSendDate);
        // 7. 保存设置
        message.saveChanges();

        return message;
    }

    //创建复杂邮件内容正文
    private static MimeMultipart crateMimeMultipartContent() {
        MimeMultipart mimeMultipart = null;
        try {
            //创建文本“节点”
            MimeBodyPart text = new MimeBodyPart();
            //这里添加图片的方式是将整个图片包含到邮件内容中, 实际上也可以以 http 链接的形式添加网络图片
            text.setContent(EmailUtils.emailContent, CONTENT_CHARSET);

            //将 文本 和 图片 “节点”合成一个混合“节点”
            MimeMultipart mm_text_image = new MimeMultipart();
            //放入文本
            mm_text_image.addBodyPart(text);
            //创建并放入图片
            if (imageFilesMap != null && imageFilesMap.size() > 0) {
                Set<String> contentIDs = imageFilesMap.keySet();
                String[] contentIDsArr = new String[contentIDs.size()];
                contentIDs.toArray(contentIDsArr);
                for (int i = 0; i < contentIDsArr.length; i++) {
                    // 创建图片“节点”
                    MimeBodyPart image = new MimeBodyPart();
                    // 读取本地文件
                    DataHandler imageHander = new DataHandler(new FileDataSource(imageFilesMap.get(contentIDsArr[i])));
                    // 将图片数据添加到“节点”
                    image.setDataHandler(imageHander);
                    // 为“节点”设置一个唯一编号（在文本“节点”将引用该ID）
                    image.setContentID(contentIDsArr[i]);
                    //放入图片(可以放入多张)
                    mm_text_image.addBodyPart(image);
                }
            }
            // 关联关系 setSubType:设置子类型。该方法只能在由客户端创建的新的MimeMultipart对象上调用。这种多部分对象的默认子类型为“mixed”。
            mm_text_image.setSubType("related");

            //将 文本+图片 的混合“节点”封装成一个普通“节点”
            MimeBodyPart text_image = new MimeBodyPart();
            text_image.setContent(mm_text_image);

            //合成一个大的混合“节点”
            mimeMultipart = new MimeMultipart();
            //放入 文本+图片 节点
            mimeMultipart.addBodyPart(text_image);
            //放入附件
            if (attachContentsList != null && attachContentsList.size() > 0) {
                for (File file : attachContentsList) {
                    //创建附件“节点”
                    MimeBodyPart attachContent = new MimeBodyPart();
                    // 读取本地文件
                    DataHandler attachContentHander = new DataHandler(new FileDataSource(file));
                    // 将附件数据添加到“节点”
                    attachContent.setDataHandler(attachContentHander);
                    // 设置附件的文件名（需要编码）
                    attachContent.setFileName(MimeUtility.encodeText(attachContentHander.getName()));
                    //放入附件节点（如果有多个附件，可以创建多个多次添加）
                    mimeMultipart.addBodyPart(attachContent);
                }
            }
            //混合关系
            mimeMultipart.setSubType("mixed");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return mimeMultipart;
    }


    //main方法用于测试工具类
    public static void main(String[] args) throws Exception {
        EmailUtils.sendEmil("kang3465@icloud.com", "完成");
        // 测试只有一个收信人
        // 只有接收邮箱号和内容
        // sendEmil("chentiefeng521@163.com", "this is a test");
        // 只有接收邮箱号、主题和内容
        // sendEmil("chentiefeng521@163.com","hello World","this is a test");
        // 发送邮箱号，发送邮箱密码，接收邮箱号，发送邮箱SMTP服务器地址，主题，内容
        // sendEmil("public_test_email@126.com", "test123","chentiefeng521@163.com", "smtp.126.com","hello World2","this is a test3");

        // 测试拥有多个收信人
        Map<String, String> emails = new HashMap<String, String>();
        //map的key是收件人称呼，value是收件人邮箱地址
//        emails.put("chentiefeng007", "chentiefeng007@126.com");
//        emails.put("chentiefeng521", "chentiefeng521@163.com");
        // 只有接收邮箱号和内容
        //sendEmil(emails, "this is a test");
        // 只有接收邮箱号、主题和内容
        // sendEmil(emails,"hello World","this is a test");
        // 发送邮箱号，发送邮箱密码，接收邮箱号，发送邮箱SMTP服务器地址，主题，内容
        // sendEmil("public_test_email@126.com", "test123",emails, "smtp.126.com","XXX来电","我是XXX,请速回电话");

        //测试发送带有文本+图片+附件的文件
        //拥有多个收信人
        Map<String, String> emails2 = new HashMap<String, String>();
        //map的key是收件人称呼，value是收件人邮箱地址
//        emails.put("chentiefeng007", "chentiefeng007@126.com");
//        emails.put("chentiefeng521", "chentiefeng521@163.com");

        //图片集
//        Map<String, File> imagesMap = new HashMap<String, File>();
        //Map的key值是图片的访问路径：如：<img src='cid:test1'/>
        //value的值是图片文件
//        imagesMap.put("test1", new File(System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "testmm.jpg"));
//        imagesMap.put("test2", new File(System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "timg.jpg"));
//        imagesMap.put("test3", new File(System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "spring-overview.png"));

        //附件集
//        List<File> fileList = new ArrayList<File>();
//        fileList.add(new File(System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "MyEmail.eml"));
//        fileList.add(new File(System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "cmd.txt"));
//        fileList.add(new File(System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "linux中java.docx"));

        //发送邮件
//        sendEmil("public_test_email@126.com", "test123", emails2, "smtp.126.com", "XXX来电", "test a image<br><img src='cid:test1'/><br><img src='cid:test2'/><br><img src='cid:test3'/><br>", imagesMap, fileList);
    }

}
