package lab3_1;

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.invoicing.BookKeeper;
import pl.com.bottega.ecommerce.sales.domain.invoicing.Invoice;
import pl.com.bottega.ecommerce.sales.domain.invoicing.InvoiceFactory;
import pl.com.bottega.ecommerce.sales.domain.invoicing.InvoiceRequest;
import pl.com.bottega.ecommerce.sales.domain.invoicing.RequestItem;
import pl.com.bottega.ecommerce.sales.domain.invoicing.Tax;
import pl.com.bottega.ecommerce.sales.domain.invoicing.TaxPolicy;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductData;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

public class BookKeeperTestCase1 {

    public Id id;
    public BookKeeper bookKeeper;
    public ClientData clientData;
    public ProductData productData;
    public InvoiceRequest invoiceRequest;
    public TaxPolicy taxPolicy;

    @Before
    public void setUp() {
        bookKeeper = new BookKeeper(new InvoiceFactory());
        id = Id.generate();
        clientData = new ClientData(id, "Kowalski");
        invoiceRequest = new InvoiceRequest(clientData);
        taxPolicy = mock(TaxPolicy.class);
        productData = new ProductData(id, new Money(new BigDecimal(100), Currency.getInstance("PLN")), "name of Product",
                ProductType.STANDARD, new Date());
    }

    @Test
    public void isReturnedInvoiceWithOnePosition() {
        invoiceRequest = new InvoiceRequest(clientData);
        int quantity = 10;
        Money totalCost = new Money(new BigDecimal(100), Currency.getInstance("PLN"));
        RequestItem item = new RequestItem(productData, quantity, totalCost);
        invoiceRequest.add(item);

        when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(
                new Tax(new Money(new BigDecimal(130), Currency.getInstance("PLN")), "Podatek"));

        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);

        assertThat(invoice.getItems()
                          .get(0)
                          .getProduct(),
                Matchers.is(productData));

        assertThat(invoice.getItems()
                          .size(),
                Matchers.is(1));
    }

    @Test
    public void isCallCalculateTaxTwiceWithInvoiceWithTwoPositions() {

        productData = new ProductData(id, new Money(new BigDecimal(100), Currency.getInstance("PLN")), "name of Product1",
                ProductType.STANDARD, new Date());
        invoiceRequest = new InvoiceRequest(clientData);
        int quantity = 10;
        Money totalCost = new Money(new BigDecimal(100), Currency.getInstance("PLN"));
        RequestItem item = new RequestItem(productData, quantity, totalCost);
        invoiceRequest.add(item);

        productData = new ProductData(id, new Money(new BigDecimal(100), Currency.getInstance("PLN")), "name of Product1", ProductType.DRUG,
                new Date());

        quantity = 15;
        totalCost = new Money(new BigDecimal(150), Currency.getInstance("PLN"));
        item = new RequestItem(productData, quantity, totalCost);
        invoiceRequest.add(item);

        when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(
                new Tax(new Money(new BigDecimal(103), Currency.getInstance("PLN")), "Podatek"));

        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);

        verify(taxPolicy, times(2)).calculateTax(any(), any());
    }

    @Test
    public void isReturnedEmptyInvoice() {
        when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(
                new Tax(new Money(new BigDecimal(104), Currency.getInstance("PLN")), "Podatek"));

        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);

        assertThat(invoice.getItems()
                          .size(),
                Matchers.is(0));

    }

    @Test
    public void isNotCalculateTaxForEmptyInvoice() {
        when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(
                new Tax(new Money(new BigDecimal(102), Currency.getInstance("PLN")), "Podatek"));

        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);

        verify(taxPolicy, times(0)).calculateTax(any(), any());

    }
}
