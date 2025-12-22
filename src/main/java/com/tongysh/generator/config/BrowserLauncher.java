package com.tongysh.generator.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.net.URI;

/**
 * 应用启动后自动打开浏览器
 * 
 * @author tongysh
 */
@Slf4j
@Component
public class BrowserLauncher implements CommandLineRunner {

    @Value("${server.port:8080}")
    private String serverPort;

    @Override
    public void run(String... args) throws Exception {
        try {
            // 等待应用完全启动
            Thread.sleep(1000);
            
            String url = "http://localhost:" + serverPort;
            log.info("正在打开浏览器访问: {}", url);
            
            // 检查是否支持桌面操作
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(new URI(url));
                    log.info("浏览器已打开");
                } else {
                    log.warn("当前系统不支持浏览器操作，请手动访问: {}", url);
                }
            } else {
                // 如果不支持Desktop，尝试使用系统命令
                String os = System.getProperty("os.name").toLowerCase();
                Runtime runtime = Runtime.getRuntime();
                
                if (os.contains("mac")) {
                    runtime.exec("open " + url);
                } else if (os.contains("win")) {
                    runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
                } else if (os.contains("nix") || os.contains("nux")) {
                    runtime.exec("xdg-open " + url);
                } else {
                    log.warn("无法自动打开浏览器，请手动访问: {}", url);
                }
            }
        } catch (Exception e) {
            log.error("打开浏览器失败", e);
            log.info("请手动访问: http://localhost:{}", serverPort);
        }
    }
}
