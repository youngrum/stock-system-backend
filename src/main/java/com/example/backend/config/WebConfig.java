package com.example.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Next.jsのクライアントサイドルーティングをサポートするためのフォールバック設定
        // /api/ を含まない全てのパスを / にフォワード
        // パターンはよりシンプルにし、静的ファイル（.html, .js, .cssなど）を除外するために.がないパスに限定
        registry.addViewController("/{path:[^\\.]*}").setViewName("forward:/"); // ルート直下のファイル拡張子がないパス
        registry.addViewController("/{path:^(?!api$).*}/**").setViewName("forward:/"); // APIパス以外で、任意のサブパス
        registry.addViewController("/").setViewName("forward:/"); // ルートパス

        // Note: If you're using Spring Boot 2.x and still facing issues,
        // the 'ant_path_matcher' strategy might be needed.
        // For Spring Boot 3.x, the default 'path_pattern_parser' is generally preferred,
        // and patterns like the above should work.
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Next.jsが生成する `_next/static` パスへのリクエストを
        // Spring Bootの `static/_next/static` ディレクトリにマッピング
        registry.addResourceHandler("/_next/static/**")
                .addResourceLocations("classpath:/static/_next/static/");

        // publicフォルダやその他の静的アセットへのマッピング
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");

        WebMvcConfigurer.super.addResourceHandlers(registry);
    }
}