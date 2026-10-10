package com.example.client;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.boot.http.client.InetAddressFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.service.registry.HttpServiceGroup;
import org.springframework.web.service.registry.ImportHttpServices;

import java.net.InetAddress;
import java.util.Collection;

@ImportHttpServices(value = CatFactsClient.class,group = ClientApplication.CAT_FACTS_GROUP )
@SpringBootApplication
public class ClientApplication {
  
    static final String CAT_FACTS_GROUP = "cat-facts";
    
    @Component
    static class CatFactsGroupConfigurer implements RestClientHttpServiceGroupConfigurer {

        @Override
        public void configureGroups(Groups<RestClient.Builder> groups) {
            groups.filterByName(CAT_FACTS_GROUP).forEachClient((group, clientBuilder) -> clientBuilder.requestFactory(ClientHttpRequestFactoryBuilder.jdk().build(HttpClientSettings.defaults()
                    .withInetAddressFilter(InetAddressFilter.all()))));
        }
    }
    
//
//    @Bean
//    CatFactsClient catFactsClient(RestClient client) {
//        var rca = RestClientAdapter.create(client);
//        return HttpServiceProxyFactory.builderFor(rca)
//                .build()
//                .createClient(CatFactsClient.class);
//    }

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }

    //   @Bean
    InetAddressFilter inetAddressFilter() {
        return InetAddressFilter.none();
    }

    @Bean
    ApplicationRunner runner(CatFactsClient client) {
        return _ -> {
            var facts = client.facts();
            IO.println(facts);
        };
    }

//    @Bean
    RestClient restClient(RestClient.Builder builder) {
        var inetAddressFilter = InetAddressFilter.none();
        var httpClientSettings = HttpClientSettings.defaults().withInetAddressFilter(inetAddressFilter);
        return builder
                .requestFactory(ClientHttpRequestFactoryBuilder.jdk().build(httpClientSettings))
                .build();
    }

}


interface CatFactsClient {

    @GetExchange("https://www.catfacts.net/api")
    CatFacts facts();
}


/*
@Component
class CatFactsClient {

    private final RestClient http;

    CatFactsClient(RestClient http) {
        this.http = http;
    }

    CatFacts facts() {
        return this.http.get()
                .uri("https://www.catfacts.net/api")
                .retrieve()
                .body(CatFacts.class);
    }

}*/

record CatFact(String fact) {
}

record CatFacts(Collection<CatFact> facts) {
}