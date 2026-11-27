package org.example.buffet;

import org.apache.catalina.startup.Tomcat;
import org.example.buffet.config.AppConfig;
import org.springframework.web.context.support
        .AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.nio.file.Files;

public class Main {
    public static void main(String[] args) throws Exception {

        var tomcat = new Tomcat();

        tomcat.setBaseDir(
                Files.createTempDirectory("buffet-tomcat").toString()
        );
        tomcat.setPort(8080);
        tomcat.getConnector();

        var servletContext = tomcat.addContext(
                "",
                Files.createTempDirectory("buffet-web").toString()
        );

        var springContext =
                new AnnotationConfigWebApplicationContext();

        springContext.register(AppConfig.class);

        var dispatcher = new DispatcherServlet(springContext);

        var wrapper = Tomcat.addServlet(
                servletContext,
                "dispatcher",
                dispatcher
        );

        wrapper.setLoadOnStartup(1);

        servletContext.addServletMappingDecoded(
                "/", "dispatcher"
        );

        tomcat.start();
        tomcat.getServer().await();
    }
}