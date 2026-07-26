package just.demo.config;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import org.springframework.ai.openai.http.okhttp.OpenAiHttpClientBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.lang.Long.MAX_VALUE;

// This class is needed just to log raw request/response to LLM
@Slf4j
@Configuration
public class OpenAiLoggingConfig {

    // Spring AI 2.x talks to OpenAI via the official okhttp-based SDK client, so raw wire logging
    // has to be plugged in as an OkHttp interceptor rather than a Spring RestClient/WebClient one.
    @Bean
    OpenAiHttpClientBuilderCustomizer requestLoggingCustomizer() {
        return builder -> builder.interceptor(chain -> {
            Request request = chain.request();
            RequestBody requestBody = request.body();
            if (requestBody != null) {
                Buffer buffer = new Buffer();
                requestBody.writeTo(buffer);
                log.debug("LLM request {} {}\n{}", request.method(), request.url(), buffer.readUtf8());
            }

            Response response = chain.proceed(request);
            log.debug("LLM response ({}):\n{}", response.code(), response.peekBody(MAX_VALUE).string());
            return response;
        });
    }
}
