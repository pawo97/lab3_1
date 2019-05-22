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
    public ProductData productData1;
    public ProductData productData2;
    public InvoiceRequest invoiceRequest;
    public TaxPolicy taxPolicy;
    public Invoice invoice;
    public Money totalCost;
    public int quantity1;
    public int quantity2;
    public RequestItem item1;
    public RequestItem item2;

    @Before
    public void setUp() {
        bookKeeper = new BookKeeper(new InvoiceFactory());
        id = Id.generate();
        clientData = new ClientData(id, "Kowalski");
        invoiceRequest = new InvoiceRequest(clientData);
        taxPolicy = mock(TaxPolicy.class);
        productData1 = new ProductData(id, new Money(new BigDecimal(100), Currency.getInstance("PLN")), "name of Product",
                ProductType.STANDARD, new Date());
        productData2 = new ProductData(id, new Money(new BigDecimal(100), Currency.getInstance("PLN")), "name of Product1",
                ProductType.DRUG, new Date());
        totalCost = new Money(new BigDecimal(150), Currency.getInstance("PLN"));
        item1 = new RequestItem(productData1, quantity1, totalCost);
        item2 = new RequestItem(productData2, quantity2, totalCost);
        quantity1 = 10;
        quantity2 = 15;
        when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(
                new Tax(new Money(new BigDecimal(150), Currency.getInstance("PLN")), "Podatek"));
    }

    @Test
    public void isReturnedInvoiceWithOnePosition() {
        invoiceRequest.add(item1);
        invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);
        assertThat(invoice.getItems()
                          .get(0)
                          .getProduct(),
                Matchers.is(productData1));

        assertThat(invoice.getItems()
                          .size(),
                Matchers.is(1));
    }

    @Test
    public void isCallCalculateTaxTwiceWithInvoiceWithTwoPositions() {
        invoiceRequest = new InvoiceRequest(clientData);
        invoiceRequest.add(item1);
        invoiceRequest.add(item2);

        invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);

        verify(taxPolicy, times(2)).calculateTax(any(), any());
    }

    @Test
    public void isReturnedEmptyInvoice() {
        when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(
                new Tax(new Money(new BigDecimal(150), Currency.getInstance("PLN")), "Podatek"));

        invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);

        assertThat(invoice.getItems()
                          .size(),
                Matchers.is(0));
    }

    @Test
    public void isNotCalculateTaxForEmptyInvoice() {
        invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);
        verify(taxPolicy, times(0)).calculateTax(any(), any());
    }

    @Test
    public void isReturnedInvoiceWithTwoPositions() {
        invoiceRequest = new InvoiceRequest(clientData);
        invoiceRequest.add(item1);
        invoiceRequest.add(item2);
        invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);

        assertThat(invoice.getItems()
                          .get(0)
                          .getProduct(),
                Matchers.is(productData1));

        assertThat(invoice.getItems()
                          .get(1)
                          .getProduct(),
                Matchers.is(productData2));

        assertThat(invoice.getItems()
                          .size(),
                Matchers.is(2));
    }

    @Test
    public void isCallCalculateTaxOnceWithInvoiceWithOnePosition() {
        invoiceRequest = new InvoiceRequest(clientData);
        invoiceRequest.add(item1);
        invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);

        verify(taxPolicy, times(1)).calculateTax(any(), any());
    }
}
