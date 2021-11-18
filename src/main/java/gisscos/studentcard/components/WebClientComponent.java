package gisscos.studentcard.components;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Компонент для получения бинов webClient различной конфигурации
 */
@Component
public class WebClientComponent {

    /** Адрес DEV АПИ VAM */
    @Value("${WEB_CLIENT_DEV_VAM_URL}")
    private String DEV_VAM_URL;
    /** Адрес TEST АПИ VAM */
    @Value("${WEB_CLIENT_TEST_VAM_URL}")
    private String TEST_VAM_URL;
    /** Адрес DEV АПИ ГИС СЦОС */
    @Value("${WEB_CLIENT_DEV_SCOS_URL}")
    private String DEV_SCOS_URL;
    /** Заголовок для идентификации нашего микросервиса (название) */
    @Value("${WEB_CLIENT_DEV_SCOS_HEADER_NAME}")
    private String HEADER_NAME;
    /** Заголовок для идентификации нашего микросервиса (значение) */
    @Value("${WEB_CLIENT_DEV_SCOS_HEADER_VALUE}")
    private String HEADER_VALUE;

    /**
     * Бин веб клиента для взаимодействия с DEV АПИ ГИС СЦОС
     * @return веб клиент
     */
    @Bean
    public WebClient devScosApiClient() {
        return WebClient
                .builder()
                .baseUrl(DEV_SCOS_URL)
                .defaultHeader(HEADER_NAME, HEADER_VALUE)
                .build();
    }

    /**
     * Бин веб клиента для взаимодействия с TEST АПИ ВАМа
     * @return веб клиент
     */
    @Bean
    public WebClient testVamApiClient() {
        return WebClient
                .builder()
                .baseUrl(TEST_VAM_URL)
                .defaultHeader(HEADER_NAME, HEADER_VALUE)
                .build();
    }

    /**
     * Бин веб клиента для взаимодействия с DEV АПИ ВАМа
     * @return веб клиент
     */
    @Bean
    public WebClient devVamApiClient() {
        return WebClient
                .builder()
                .baseUrl(DEV_VAM_URL)
                .defaultHeader(HEADER_NAME, HEADER_VALUE)
                .build();
    }
}
