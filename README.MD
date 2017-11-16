# brave-instrumentation-dubbo

This module contains tracing client and server filters for [Dubbo](https://github.com/alibaba/dubbo)(a remote procedure call framework open-sourced by Alibaba) that will report to Zipkin how long each request takes.

## Getting started.

```java
@Configuration
public class TraceAutoConfiguration{

    @Bean
    public Sender sender() {
        return OkHttpSender.create("http://127.0.0.1:9411/api/v2/spans");
    }

    @Bean
    public AsyncReporter<Span> spanReporter() {
        return AsyncReporter.create(sender());
    }
    
    @Bean
    public Tracing tracing(){
        return Tracing.newBuilder()
                .localServiceName("your application name")
                .spanReporter(spanReporter())
                .sampler(Sampler.create(1.0)).build();
    }

    @Bean
    DubboTracing dubboTracing(Tracing tracing) {
        return DubboTracing.create(tracing);
    }
} 
```




