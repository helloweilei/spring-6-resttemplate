package guru.springframework.spring6resttemplate.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6resttemplate.config.RestTemplateBuilderConfig;
import guru.springframework.spring6resttemplate.model.BeerDTO;
import guru.springframework.spring6resttemplate.model.BeerStyle;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.client.RestTemplateBuilderConfigurer;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.RequestMatcher;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestToUriTemplate;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withAccepted;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;


@RestClientTest
@Import({RestTemplateBuilderConfig.class})
class BeerClientImplTest {

    public static String URL = "http://localhost:8080/api/v1/";

    BeerClient beerClient;

    @Autowired
    RestTemplateBuilder restTemplateBuilderConfigured;

    @Mock
    RestTemplateBuilder mockedRestTemplateBuilder = new RestTemplateBuilder(new MockServerRestTemplateCustomizer());

    MockRestServiceServer server;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        RestTemplate restTemplate = restTemplateBuilderConfigured.build();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        beerClient = new BeerClientImpl(mockedRestTemplateBuilder);
        Mockito.when(mockedRestTemplateBuilder.build()).thenReturn(restTemplate);
    }

    public Page<BeerDTO> getPage() {
        List<BeerDTO> beers = new ArrayList<>();
        beers.add(createBeer());
        return new PageImpl<BeerDTO>(beers);
    }

    public BeerDTO createBeer() {
        return BeerDTO.builder()
                .beerName("Test")
                .id(UUID.randomUUID())
                .beerStyle(BeerStyle.LAGER)
                .quantityOnHand(500)
                .upc("12345")
                .price(new BigDecimal("123.4"))
                .build();
    }

    @Test
    public void testListBeers() throws JsonProcessingException {
        String payload = objectMapper.writeValueAsString(getPage());
        server.expect(method(HttpMethod.GET))
                .andExpect(requestTo(URL + "beer"))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));
        Page<BeerDTO> beerPage = beerClient.listBeers(null);
        Assertions.assertThat(beerPage.getTotalElements()).isGreaterThan(0);
    }

    @Test
    public void testGetById() throws JsonProcessingException {
        BeerDTO beerDTO = createBeer();
        String payload = objectMapper.writeValueAsString(beerDTO);
        server.expect(method(HttpMethod.GET))
                .andExpect(requestToUriTemplate(URL + "beer/" + beerDTO.getId()))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        BeerDTO response = beerClient.getBeerById(beerDTO.getId().toString());
        Assertions.assertThat(response.getId()).isEqualTo(beerDTO.getId());
    }

    @Test
    public void testCreateBeer() throws JsonProcessingException {
        BeerDTO beerDTO = createBeer();
        String payload = objectMapper.writeValueAsString(beerDTO);
        URI uri = UriComponentsBuilder.fromPath("/api/v1/beer/{beerId}").build(beerDTO.getId());
        server.expect(method(HttpMethod.POST))
                .andExpect(requestTo(URL + "beer"))
                .andRespond(withAccepted().location(uri));

        server.expect(method(HttpMethod.GET))
                .andExpect(requestTo(URL + "beer/" + beerDTO.getId()))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));
        BeerDTO createdBeer = beerClient.createBeer(beerDTO);
        Assertions.assertThat(createdBeer.getId()).isEqualTo(beerDTO.getId());
    }

    @Test
    void deleteBeer() {
        BeerDTO beer = beerClient.listBeers(null).getContent().get(0);
        beerClient.deleteBeer(beer.getId().toString());

        assertThrows(HttpClientErrorException.class, () -> {
           beerClient.getBeerById(beer.getId().toString());
        });
    }

    @Test
    public void testDeleteBeer() {
        BeerDTO dto = createBeer();
        server.expect(method(HttpMethod.DELETE))
                .andExpect(requestTo(URL + "beer/" + dto.getId()))
                .andRespond(withNoContent());
        beerClient.deleteBeer(dto.getId().toString());
        server.verify();
    }
}
