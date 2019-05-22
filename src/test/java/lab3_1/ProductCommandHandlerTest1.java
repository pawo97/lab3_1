package lab3_1;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.application.api.command.AddProductCommand;
import pl.com.bottega.ecommerce.sales.application.api.handler.AddProductCommandHandler;
import pl.com.bottega.ecommerce.sales.domain.client.Client;
import pl.com.bottega.ecommerce.sales.domain.client.ClientRepository;
import pl.com.bottega.ecommerce.sales.domain.equivalent.SuggestionService;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductRepository;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sales.domain.reservation.Reservation;
import pl.com.bottega.ecommerce.sales.domain.reservation.Reservation.ReservationStatus;
import pl.com.bottega.ecommerce.sales.domain.reservation.ReservationRepository;
import pl.com.bottega.ecommerce.sharedkernel.Money;
import pl.com.bottega.ecommerce.system.application.SystemContext;

public class ProductCommandHandlerTest1 {

    public AddProductCommandHandler addProductCommandHandler;
    public ReservationRepository reservationRepository;
    public ProductRepository productRepository;
    public SuggestionService suggestionService;
    public ClientRepository clientRepository;
    public SystemContext systemContext;
    public Product product;
    public Reservation reservation;
    public AddProductCommand addProductCommand;
    public Client client;
    public ClientData clientData;

    @Before
    public void setUp() {

        reservationRepository = mock(ReservationRepository.class);
        productRepository = mock(ProductRepository.class);
        suggestionService = mock(SuggestionService.class);
        clientRepository = mock(ClientRepository.class);
        systemContext = new SystemContext();
        addProductCommandHandler = new AddProductCommandHandler(reservationRepository, productRepository, suggestionService,
                clientRepository, systemContext);
        addProductCommand = new AddProductCommand(new Id("1"), new Id("2"), 15);
        client = new Client();
        product = new Product(Id.generate(), new Money(new BigDecimal(164)), "no_name", ProductType.DRUG);
        clientData = new ClientData(Id.generate(), "Kowalski");
        reservation = new Reservation(Id.generate(), ReservationStatus.OPENED, clientData, new Date());
        suggestionService = mock(SuggestionService.class);
    }

    @Test
    public void isLoadReservationOnce() {
        when(reservationRepository.load(any(Id.class))).thenReturn(reservation);
        when(productRepository.load(any(Id.class))).thenReturn(product);
        addProductCommandHandler.handle(addProductCommand);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    public void isCallEqualsIfProductIsAvailable() {
        when(reservationRepository.load(addProductCommand.getOrderId())).thenReturn(reservation);
        when(productRepository.load(addProductCommand.getProductId())).thenReturn(product);

        addProductCommandHandler.handle(addProductCommand);

        assertThat(product.isAvailable(), is(true));
        verify(suggestionService, never()).suggestEquivalent(any(Product.class), any(Client.class));
    }
}
