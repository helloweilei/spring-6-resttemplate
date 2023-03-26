package guru.springframework.spring6resttemplate.client;

import guru.springframework.spring6resttemplate.model.BeerDTO;
import org.springframework.data.domain.Page;

interface BeerClient {
    Page<BeerDTO> listBeers(String beerName);

    BeerDTO getBeerById(String beerId);

    BeerDTO createBeer(BeerDTO beer);

    BeerDTO updateBeer(BeerDTO beer);

    void deleteBeer(String id);

}
