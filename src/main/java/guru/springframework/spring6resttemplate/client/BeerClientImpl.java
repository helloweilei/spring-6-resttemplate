package guru.springframework.spring6resttemplate.client;

import guru.springframework.spring6resttemplate.model.BeerDTO;
import guru.springframework.spring6resttemplate.model.BeerDTOPageImp;
import guru.springframework.spring6resttemplate.model.BeerStyle;
import lombok.AllArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
@AllArgsConstructor
public class BeerClientImpl implements BeerClient {

    private RestTemplateBuilder restTemplateBuilder;
    private static final String BEER_PATH = "api/v1/beer";
    @Override
    public Page<BeerDTO> listBeers(String beerName) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromPath(BEER_PATH);
        if (StringUtils.hasText(beerName)) {
            uriComponentsBuilder.queryParam("beerName", beerName);
        }
        ResponseEntity<BeerDTOPageImp> response = restTemplate.getForEntity(
                uriComponentsBuilder.toUriString(),
                BeerDTOPageImp.class
        );

        return response.getBody();
    }

    @Override
    public BeerDTO getBeerById(String beerId) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        return restTemplate.getForObject(BEER_PATH + "/{beerId}", BeerDTO.class, beerId);
    }

    @Override
    public BeerDTO createBeer(BeerDTO beer) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        URI uri = restTemplate.postForLocation(BEER_PATH, beer);
        return restTemplate.getForObject(uri.getPath(), BeerDTO.class);
    }

    @Override
    public void deleteBeer(String id) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.delete(BEER_PATH + "/{beerId}", id);
    }

    @Override
    public BeerDTO updateBeer(BeerDTO beer) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.put(BEER_PATH + "/beerId", beer, beer.getId());
        return getBeerById(beer.getId().toString());
    }
}
