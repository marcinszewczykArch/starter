package com.starter.core.admin;

import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for parsing User-Agent strings to extract device information.
 * Provides device type, browser name/version, and operating system.
 */
@Service
public class DeviceInfoService {

    // Browser patterns (order matters - more specific first)
    private static final Pattern EDGE_PATTERN = Pattern.compile("Edg[e]?/(\\d+)");
    private static final Pattern CHROME_PATTERN = Pattern.compile("Chrome/(\\d+)");
    private static final Pattern FIREFOX_PATTERN = Pattern.compile("Firefox/(\\d+)");
    private static final Pattern SAFARI_PATTERN = Pattern.compile("Version/(\\d+).*Safari");
    private static final Pattern OPERA_PATTERN = Pattern.compile("OPR/(\\d+)");

    // OS patterns
    private static final Pattern WINDOWS_PATTERN = Pattern.compile("Windows NT ([\\d.]+)");
    private static final Pattern MAC_PATTERN = Pattern.compile("Mac OS X ([\\d_]+)");
    private static final Pattern IOS_PATTERN = Pattern.compile("iPhone|iPad|iPod");
    private static final Pattern ANDROID_PATTERN = Pattern.compile("Android ([\\d.]+)");
    private static final Pattern LINUX_PATTERN = Pattern.compile("Linux");

    // Device type patterns
    private static final Pattern MOBILE_PATTERN = Pattern.compile(
        "Mobile|Android|iPhone|iPad|iPod|webOS|BlackBerry|Opera Mini|IEMobile"
    );
    private static final Pattern TABLET_PATTERN = Pattern.compile("iPad|Tablet|PlayBook");

    /**
     * Parse User-Agent string to extract device information.
     *
     * @param userAgent User-Agent header value
     * @return DeviceInfo with device type, browser, and OS
     */
    public DeviceInfo parse(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return DeviceInfo.builder()
                .deviceType("unknown")
                .browser("Unknown")
                .os("Unknown")
                .build();
        }

        return DeviceInfo.builder()
            .deviceType(detectDeviceType(userAgent))
            .browser(detectBrowser(userAgent))
            .os(detectOs(userAgent))
            .build();
    }

    private String detectDeviceType(String ua) {
        if (TABLET_PATTERN.matcher(ua).find()) {
            return "tablet";
        }
        if (MOBILE_PATTERN.matcher(ua).find()) {
            return "mobile";
        }
        return "desktop";
    }

    private String detectBrowser(String ua) {
        Matcher matcher;

        // Check Edge first (contains Chrome in UA)
        matcher = EDGE_PATTERN.matcher(ua);
        if (matcher.find()) {
            return "Edge " + matcher.group(1);
        }

        // Opera (contains Chrome in UA)
        matcher = OPERA_PATTERN.matcher(ua);
        if (matcher.find()) {
            return "Opera " + matcher.group(1);
        }

        // Chrome
        matcher = CHROME_PATTERN.matcher(ua);
        if (matcher.find() && !ua.contains("Edg") && !ua.contains("OPR")) {
            return "Chrome " + matcher.group(1);
        }

        // Firefox
        matcher = FIREFOX_PATTERN.matcher(ua);
        if (matcher.find()) {
            return "Firefox " + matcher.group(1);
        }

        // Safari (must check after Chrome)
        matcher = SAFARI_PATTERN.matcher(ua);
        if (matcher.find()) {
            return "Safari " + matcher.group(1);
        }

        return "Other";
    }

    private String detectOs(String ua) {
        Matcher matcher;

        // iOS
        if (IOS_PATTERN.matcher(ua).find()) {
            return "iOS";
        }

        // Android
        matcher = ANDROID_PATTERN.matcher(ua);
        if (matcher.find()) {
            return "Android " + matcher.group(1);
        }

        // Windows
        matcher = WINDOWS_PATTERN.matcher(ua);
        if (matcher.find()) {
            String version = matcher.group(1);
            return "Windows " + mapWindowsVersion(version);
        }

        // macOS
        matcher = MAC_PATTERN.matcher(ua);
        if (matcher.find()) {
            String version = matcher.group(1).replace("_", ".");
            return "macOS " + version;
        }

        // Linux
        if (LINUX_PATTERN.matcher(ua).find()) {
            return "Linux";
        }

        return "Other";
    }

    private String mapWindowsVersion(String ntVersion) {
        return switch (ntVersion) {
            case "10.0" -> "10/11";
            case "6.3" -> "8.1";
            case "6.2" -> "8";
            case "6.1" -> "7";
            case "6.0" -> "Vista";
            case "5.1" -> "XP";
            default -> ntVersion;
        };
    }

    /** Parsed device information. */
    @Data
    @Builder
    public static class DeviceInfo {
        private String deviceType; // desktop, mobile, tablet
        private String browser;    // Chrome 120, Firefox 121, etc.
        private String os;         // Windows 10, macOS 14.0, iOS, Android 14
    }
}
