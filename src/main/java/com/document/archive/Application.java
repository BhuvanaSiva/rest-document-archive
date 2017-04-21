package com.document.archive;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.servlet.MultipartConfigElement;

import org.apache.commons.io.FileUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.MultipartConfigFactory;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.filters.CompositeFileListFilter;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;
import org.springframework.integration.file.transformer.FileToStringTransformer;
import org.springframework.messaging.MessageChannel;

import com.document.archive.filter.LastModifiedFileFilter;
import com.document.archive.processor.FileProcessor;

@Configuration
@ComponentScan
@EnableAutoConfiguration

public class Application extends SpringBootServletInitializer {

    @Bean
    public MultipartConfigElement multipartConfigElement() {
		MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize("5120KB");
        factory.setMaxRequestSize("5120KB");
        return factory.createMultipartConfig();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        SpringApplication.run(Application.class, args);
      
    }
    

	@Bean
	public IntegrationFlow processFileFlow() {
		return IntegrationFlows
				.from("fileInputChannel")
				.transform(fileToStringTransformer())
				.handle("fileProcessor", "process").get();
	}

  @Bean
  public MessageChannel fileInputChannel() {
      return new DirectChannel();
  }

	@Bean
	@InboundChannelAdapter(value = "fileInputChannel", poller = @Poller(fixedDelay = "1000"))
	public MessageSource<File> fileReadingMessageSource() {
		CompositeFileListFilter<File> filters =new CompositeFileListFilter<File>();
		filters.addFilter(new SimplePatternFileListFilter("*.*"));
		filters.addFilter(new LastModifiedFileFilter());

		FileReadingMessageSource source = new FileReadingMessageSource();
		source.setAutoCreateDirectory(true);
		source.setDirectory(new File("archive"));
		source.setFilter(filters);

		return source;
	}

	@Bean
	public FileToStringTransformer fileToStringTransformer() {
		return new FileToStringTransformer();
	}

	@Bean
	public FileProcessor fileProcessor() {
		return new FileProcessor();
	}

    
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(applicationClass);
    }

    private static Class<Application> applicationClass = Application.class;
    
   
}
