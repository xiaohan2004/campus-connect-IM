package com.campus.im.util;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dm20151123.AsyncClient;
import com.aliyun.sdk.service.dm20151123.models.SingleSendMailRequest;
import com.aliyun.sdk.service.dm20151123.models.SingleSendMailResponse;
import darabonba.core.client.ClientOverrideConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Random;

/**
 * 邮件验证码工具类
 */
@Component
public class EmailUtil {
    private static final Logger logger = LoggerFactory.getLogger(EmailUtil.class);

    private static final int CODE_LENGTH = 6;
    private static final int EXPIRE_MINUTES = 3;

    @Value("${app.aliyun.mail.accessKeyId}")
    private String accessKeyId;

    @Value("${app.aliyun.mail.accessKeySecret}")
    private String accessKeySecret;

    @Value("${app.aliyun.mail.accountName}")
    private String accountName;

    @Value("${app.aliyun.mail.fromAlias}")
    private String fromAlias;

    private AsyncClient client;

    @PostConstruct
    public void init() {
        StaticCredentialProvider provider = StaticCredentialProvider.create(
                Credential.builder()
                        .accessKeyId(accessKeyId)
                        .accessKeySecret(accessKeySecret)
                        .build()
        );

        client = AsyncClient.builder()
                .region("cn-hangzhou")
                .credentialsProvider(provider)
                .overrideConfiguration(ClientOverrideConfiguration.create()
                        .setEndpointOverride("dm.aliyuncs.com"))
                .build();
    }

    public String sendVerificationCode(String email, String type) {
        try {
            String code = generateVerificationCode();
            sendEmail(email, code, type);
            return code;
        } catch (Exception e) {
            logger.error("发送验证码邮件失败: {}", e.getMessage(), e);
            throw new RuntimeException("发送验证码邮件失败: " + e.getMessage(), e);
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    private void sendEmail(String toEmail, String code, String type) throws Exception {
        SingleSendMailRequest request = SingleSendMailRequest.builder()
                .accountName(accountName)
                .fromAlias(fromAlias)
                .addressType(1)
                .toAddress(toEmail)
                .subject(getEmailSubject(type))
                .htmlBody(getEmailContent(code, type))
                .replyToAddress(true)
                .build();

        SingleSendMailResponse response = client.singleSendMail(request).get();
        logger.info("发送验证码邮件成功, RequestId: {}", response.getBody().getRequestId());
    }

    private String getEmailSubject(String type) {
        String appName = "智联校园";
        switch (type) {
            case "register":
                return appName + " - 注册验证码";
            case "reset":
                return appName + " - 重置密码验证码";
            default:
                return appName + " - 验证码";
        }
    }

    private String getEmailContent(String code, String type) {
        String purpose = "register".equals(type) ? "注册" : ("reset".equals(type) ? "重置密码" : "获取验证码");

        return "<div style='background-color:#f7f7f7;padding:40px 0;text-align:center;'>"
                + "<div style='display:inline-block;background-color:#ffffff;padding:30px;border-radius:8px;box-shadow:0 2px 12px rgba(0,0,0,0.1);max-width:500px;width:90%;text-align:left;'>"
                + "<h2 style='color:#333333;text-align:center;'>智联校园</h2>"
                + "<p style='color:#555;font-size:15px;margin-top:20px;'>您正在进行 <strong style='color:#409EFF;'>" + purpose + "</strong> 操作，验证码如下：</p>"
                + "<div style='background-color:#f2f6fc;padding:15px;margin:20px 0;text-align:center;border-radius:6px;'>"
                + "<span style='color:#409eff;font-size:26px;font-weight:bold;letter-spacing:6px;'>" + code + "</span>"
                + "</div>"
                + "<p style='color:#999;font-size:13px;'>验证码有效期为 <strong>" + EXPIRE_MINUTES + "</strong> 分钟，请勿泄露给他人。</p>"
                + "<p style='color:#bbb;font-size:12px;margin-top:30px;text-align:center;'>如非本人操作，请忽略此邮件。</p>"
                + "</div>"
                + "</div>";
    }
}
