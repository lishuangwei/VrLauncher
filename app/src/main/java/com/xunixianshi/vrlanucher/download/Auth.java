package com.xunixianshi.vrlanucher.download;


import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;

public final class Auth {
    /**
     * 上传策略
     * 参考文档：<a href="https://developer.qiniu.com/kodo/manual/put-policy">上传策略</a>
     */
    private static final String[] policyFields = new String[]{
            "callbackUrl",
            "callbackBody",
            "callbackHost",
            "callbackBodyType",
            "callbackFetchKey",

            "returnUrl",
            "returnBody",

            "endUser",
            "saveKey",
            "insertOnly",
            "isPrefixalScope",

            "detectMime",
            "mimeLimit",
            "fsizeLimit",
            "fsizeMin",

            "persistentOps",
            "persistentNotifyUrl",
            "persistentPipeline",

            "deleteAfterDays",
            "fileType",
    };
    private static final String[] deprecatedPolicyFields = new String[]{
            "asyncOps",
    };
    public final String accessKey;
    private final SecretKeySpec secretKey;

    private Auth(String accessKey, SecretKeySpec secretKeySpec) {
        this.accessKey = accessKey;
        this.secretKey = secretKeySpec;
    }

    public static Auth create(String accessKey, String secretKey) {
        if (StringUtils.isNullOrEmpty(accessKey) || StringUtils.isNullOrEmpty(secretKey)) {
            throw new IllegalArgumentException("empty key");
        }
        byte[] sk = StringUtils.utf8Bytes(secretKey);
        SecretKeySpec secretKeySpec = new SecretKeySpec(sk, "HmacSHA1");
        return new Auth(accessKey, secretKeySpec);
    }


    private Mac createMac() {
        Mac mac;
        try {
            mac = Mac.getInstance("HmacSHA1");
            mac.init(secretKey);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
        return mac;
    }

    public String sign(byte[] data) {
        Mac mac = createMac();
        String encodedSign = UrlSafeBase64.encodeToString(mac.doFinal(data));
        return this.accessKey + ":" + encodedSign;
    }

    public String sign(String data) {
        return sign(StringUtils.utf8Bytes(data));
    }

    public String signWithData(byte[] data) {
        String s = UrlSafeBase64.encodeToString(data);
        return sign(StringUtils.utf8Bytes(s)) + ":" + s;
    }

    public String signWithData(String data) {
        return signWithData(StringUtils.utf8Bytes(data));
    }

    /**
     * 下载签名
     *
     * @param baseUrl 待签名文件url，如 http://img.domain.com/u/3.jpg 、
     *                http://img.domain.com/u/3.jpg?imageView2/1/w/120
     * @return
     */
    public String privateDownloadUrl(String baseUrl) {
        return privateDownloadUrl(baseUrl, 3600);
    }

    /**
     * 下载签名
     *
     * @param baseUrl 待签名文件url，如 http://img.domain.com/u/3.jpg 、
     *                http://img.domain.com/u/3.jpg?imageView2/1/w/120
     * @param expires 有效时长，单位秒。默认3600s
     * @return
     */
    public String privateDownloadUrl(String baseUrl, long expires) {
        long deadline = System.currentTimeMillis() / 1000 + expires;
        return privateDownloadUrlWithDeadline(baseUrl, deadline);
    }

    String privateDownloadUrlWithDeadline(String baseUrl, long deadline) {
        StringBuilder b = new StringBuilder();
        b.append(baseUrl);
        int pos = baseUrl.indexOf("?");
        if (pos > 0) {
            b.append("&e=");
        } else {
            b.append("?e=");
        }
        b.append(deadline);
        String token = sign(StringUtils.utf8Bytes(b.toString()));
        b.append("&token=");
        b.append(token);
        return b.toString();
    }

}
