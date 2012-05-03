package serialization;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import model.Address;
import model.Customer;
import model.Group;
import model.HardDisk;
import model.Order;
import model.Product;
import net.gregio.testjackson.serialization.JacksonSerialization;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;

public class JacksonSerializeTest {

    private ByteArrayOutputStream output;
    private HttpServletResponse response;
    private JacksonSerialization jacksonSerialization;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private String currentDateAsStr;
    private Date currentDate;

    @Before
    public void setup() throws Exception {
        this.output = new ByteArrayOutputStream();
        this.response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenReturn(new PrintWriter(output));
        this.jacksonSerialization = new JacksonSerialization(response);
        this.currentDate = new Date();
        this.currentDateAsStr = sdf.format(currentDate);
    }

    private String jsonResult() {
        return output.toString();
    }

    private Group createGroup(Long id) {
        return new Group(id, "Group " + id);
    }

    private Product createProduct(Long id) {
        return new Product(id, "Product " + id, currentDate);
    }

    private Product createProductWithGroup(Long idProduct, Long idGroup) {
        Product product = createProduct(idProduct);
        product.setGroup(createGroup(idGroup));
        return product;
    }

    @Test
    public void shouldSerializePojo() {
        String expectedResult = "{\"product\":{\"id\":1,\"name\":\"Product 1\",\"creationDate\":\"" + currentDateAsStr
                + "\"}}";
        Product product = createProduct(1L);

        jacksonSerialization.from(product).serialize();
        assertThat(jsonResult(), is(equalTo(expectedResult)));
    }

    @Test
    public void shouldSerializeCollectionOfPojo() {
        String expectedResult = "{\"productList\":[{\"id\":1,\"name\":\"Product 1\",\"creationDate\":\""
                + currentDateAsStr + "\"},{\"id\":2,\"name\":\"Product 2\",\"creationDate\":\"" + currentDateAsStr
                + "\"}]}";
        List<Product> products = new ArrayList<Product>();
        products.add(createProduct(1L));
        products.add(createProduct(2L));

        jacksonSerialization.from(products).serialize();
        assertThat(jsonResult(), is(equalTo(expectedResult)));
    }

    @Test
    public void shouldUseAlias() {
        String expectedResult = "{\"myProduct\":{\"id\":1,\"name\":\"Product 1\",\"creationDate\":\""
                + currentDateAsStr + "\"}}";
        Product product = createProduct(1L);

        jacksonSerialization.from(product, "myProduct").serialize();
        assertThat(jsonResult(), is(equalTo(expectedResult)));
    }

    @Test
    public void shouldSerializeMap() {
        String expectedResult = "{\"person\":{\"email\":\"ff@gmail.com\",\"idade\":28,\"name\":\"fabio franco\"}}";
        Map<String, Object> json = new HashMap<String, Object>();
        json.put("name", "fabio franco");
        json.put("idade", 28);
        json.put("email", "ff@gmail.com");

        jacksonSerialization.from(json, "person").serialize();
        assertThat(jsonResult(), is(equalTo(expectedResult)));
    }

    @Test
    public void shouldSerializeList() {
        String expectedResult = "{\"list\":[1,2,3,4,5,6,\"json\"]}";

        List<Object> list = new ArrayList<Object>();
        list.addAll(Arrays.asList(1, 2, 3, 4, 5, 6));
        list.add("json");

        jacksonSerialization.from(list, "list").serialize();
        assertThat(jsonResult(), is(equalTo(expectedResult)));
    }

    @Test
    public void shouldIncludeField() {
        String expectedResult = "{\"product\":{\"id\":1,\"name\":\"Product 1\",\"creationDate\":\"" + currentDateAsStr
                + "\",\"group\":{\"id\":1,\"name\":\"Group 1\"}}}";

        Product product = createProductWithGroup(1L, 1L);

        jacksonSerialization.from(product).include("group").serialize();
        assertThat(jsonResult(), is(equalTo(expectedResult)));
    }

    @Test
    public void shouldIncludeFieldFromCollection() {
        String expectedResult = "{\"order\":{\"id\":1,\"products\":[{\"id\":1,\"name\":\"Product 1\",\"creationDate\":\""
                + currentDateAsStr
                + "\",\"group\":{\"id\":1,\"name\":\"Group 1\"}},{\"id\":2,\"name\":\"Product 2\",\"creationDate\":\""
                + currentDateAsStr + "\",\"group\":{\"id\":2,\"name\":\"Group 2\"}}]}}";

        Order order = new Order(1L, new Customer(1L, "Franco", new Address("rua", "cidade", "9800989")));
        order.addProduct(createProductWithGroup(1L, 1L));
        order.addProduct(createProductWithGroup(2L, 2L));

        jacksonSerialization.from(order).include("products", "products.group").serialize();
        assertThat(jsonResult(), is(equalTo(expectedResult)));
    }

    @Test
    public void shouldSerializeParentFields() {
        String expectedResult = "{\"hardDisk\":{\"capacity\":2987000009,\"id\":1,\"name\":\"Samsumg ZTX A9000\"}}";

        HardDisk hd = new HardDisk(1L, "Samsumg ZTX A9000", 2987000009L);

        jacksonSerialization.from(hd).serialize();
        assertThat(jsonResult(), is(equalTo(expectedResult)));
    }

    @Test
    public void shouldExcludeParentField() {
        String expectedResult = "{\"hardDisk\":{\"capacity\":2987000009,\"name\":\"Samsumg ZTX A9000\"}}";

        HardDisk hd = new HardDisk(1L, "Samsumg ZTX A9000", 2987000009L);

        jacksonSerialization.from(hd).exclude("id").serialize();
        assertThat(jsonResult(), is(equalTo(expectedResult)));
    }

    @Test
    public void shouldExcludeField() {
        String expectedResult = "{\"product\":{\"id\":1,\"name\":\"Product 1\",\"creationDate\":\"" + currentDateAsStr
                + "\",\"group\":{\"name\":\"Group 1\"}}}";

        Group group = new Group(1L, "Group 1");
        Product product = new Product(1L, "Product 1", currentDate, group);

        jacksonSerialization.from(product).include("group").exclude("group.id").serialize();
        assertThat(jsonResult(), is(equalTo(expectedResult)));
    }

    @Test
    public void shouldExcudeHierarchicalField() {
        String expectedResult = "{\"order\":{\"id\":1,\"customer\":{\"id\":1,\"name\":\"Franco\"},"
                + "\"delivery\":{\"street\":\"delivery street\",\"city\":\"Bristol\",\"zipCode\":\"09887990\"},"
                + "\"products\":[{\"id\":1,\"name\":\"Product 1\",\"creationDate\":\"" + currentDateAsStr
                + "\",\"group\":{\"name\":\"Group 1\"}},{\"id\":2,\"name\":\"Product 2\",\"creationDate\":\""
                + currentDateAsStr + "\",\"group\":{\"name\":\"Group 2\"}}]}}";

        Order order = new Order(1L, new Customer(1L, "Franco", new Address("rua", "cidade", "9800989")), new Address(
                "delivery street", "Bristol", "09887990"));
        order.addProduct(createProductWithGroup(1L, 1L));
        order.addProduct(createProductWithGroup(2L, 2L));

        jacksonSerialization.from(order).include("customer", "delivery", "products", "products.group")
                .exclude("products.group.id").serialize();
        assertThat(jsonResult(), is(equalTo(expectedResult)));
    }

    @Test
    public void shouldSerializeWithouRoot() {
        String expectedResult = "{\"id\":1,\"name\":\"Product 1\",\"creationDate\":\"" + currentDateAsStr
                + "\",\"group\":{\"name\":\"Group 1\"}}";

        Group group = new Group(1L, "Group 1");
        Product product = new Product(1L, "Product 1", currentDate, group);

        jacksonSerialization.withoutRoot().from(product).include("group").exclude("group.id").serialize();
        assertThat(jsonResult(), is(equalTo(expectedResult)));
    }

    @Test
    public void shouldSerializeIndented() {
        String expectedResult = "{\n  \"product\" : {\n    \"id\" : 1,\n    \"name\" : \"Product 1\",\n    \"creationDate\" : \""
                + currentDateAsStr
                + "\",\n    \"group\" : {\n      \"id\" : 1,\n      \"name\" : \"Group 1\"\n    }\n  }\n}";

        Group group = new Group(1L, "Group 1");
        Product product = new Product(1L, "Product 1", currentDate, group);

        jacksonSerialization.indented().from(product).include("group").serialize();
        assertThat(jsonResult(), is(equalTo(expectedResult)));
    }

    @Test
    public void shouldSerializeObjectAttribute() {
        String expectedResult = "{\"product\":{\"id\":1,\"name\":\"Product 1\",\"creationDate\":\"" + currentDateAsStr
                + "\",\"data\":\"data object for product\"}}";

        Product product = new Product(1L, "Product 1", currentDate);
        product.setData("data object for product");

        jacksonSerialization.from(product).serialize();
        assertThat(jsonResult(), is(equalTo(expectedResult)));
    }

    @Test
    public void shouldSerializeByteArrayAttribute() {
        String expectedResult = "{\"product\":{\"id\":1,\"name\":\"Product 1\",\"creationDate\":\"" + currentDateAsStr
                + "\",\"image\":\"iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABHNCSVQICAgIfAhkiAAAAAlwSFlzAAAK8AAACvABQqw0mAAAABV0RVh0Q3JlYXRpb24gVGltZQA2LzI0LzA59sFr4wAAABx0RVh0U29mdHdhcmUAQWRvYmUgRmlyZXdvcmtzIENTNAay06AAAAJ3SURBVDiNlZO/S2phGMc/R+tkQnIMiZRMCgeVY4S2tUYQbS0hlUNDa1tLyyVaqhMh1NbgH9DSYEP+BQ1BS4OLBYFRqb1w9FR43rc7hHUv3eH2wDN9eeD7fH9o9Xo95jjO1dvbm8EPpq+vT/j9/knt5ubm2fiYn9wjhEAIITztdtvw+Xy4rvttn56eWF9fZ2dnB8dx/sJ8Ph/tdtvwSCnRNA0p5bdtNBqcnp5SLpfpdDq0Wi06nQ5/3vS4rotSCoDX11ceHx8JBAL09/cTCoXY398nHA7TbDbZ2toik8mwsLDA4OAgruvSI6VEKUWj0cCyLMrlMvF4nFQqRSaT4eHhAV3XKZVKFItFisUilUqFvb09pJR4unTX1tY4Pj4ml8tRrVYpFAq4rotlWRweHpJIJIhEImSzWeLx+Ocrnq4od3d3RKNRZmZmCAaDpNNp5ubmCIVChMNh5ufnCQaDLC8vk8/nP8X0SCkBODo6QtM0crkcw8PD7O7u4jgOtVqN9/d3bNum2Wxye3vLy8sLwBcDpRSmaRIIBGi1WmxubpJOp3EcB9u2qdfr6LrOyMgIhUKBlZUVlFK4rot3aWnp19DQENfX15ydndHb28vFxQXVapWpqSmi0SimaZJMJkmlUui6zujoKNPT09zf33/ZmM/nMQyDk5MTDg4OsCyLWCzG4uIiXacmJiZIJpMAnww8XXB2dhbbttne3uby8pJsNotpmiil0DQNpRRKKbxeL16vF6XUR5C6idvY2GBsbIzz83MSiQSrq6uMj4/TFflfXZBSopVKpWfAiEQi/G+hhBDUajUA0TMwMDAphLiqVCo/qqPf7xeGYUz+Bgm0dbIWFetGAAAAAElFTkSuQmCC\"}}";

        Product product = new Product(1L, "Product 1", currentDate);
        product.setImage(Base64.decodeBase64("iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABHNCSVQICAgIfAhkiAAAAAlwSFlzAAAK8AAACvABQqw0mAAAABV0RVh0Q3JlYXRpb24gVGltZQA2LzI0LzA59sFr4wAAABx0RVh0U29mdHdhcmUAQWRvYmUgRmlyZXdvcmtzIENTNAay06AAAAJ3SURBVDiNlZO/S2phGMc/R+tkQnIMiZRMCgeVY4S2tUYQbS0hlUNDa1tLyyVaqhMh1NbgH9DSYEP+BQ1BS4OLBYFRqb1w9FR43rc7hHUv3eH2wDN9eeD7fH9o9Xo95jjO1dvbm8EPpq+vT/j9/knt5ubm2fiYn9wjhEAIITztdtvw+Xy4rvttn56eWF9fZ2dnB8dx/sJ8Ph/tdtvwSCnRNA0p5bdtNBqcnp5SLpfpdDq0Wi06nQ5/3vS4rotSCoDX11ceHx8JBAL09/cTCoXY398nHA7TbDbZ2toik8mwsLDA4OAgruvSI6VEKUWj0cCyLMrlMvF4nFQqRSaT4eHhAV3XKZVKFItFisUilUqFvb09pJR4unTX1tY4Pj4ml8tRrVYpFAq4rotlWRweHpJIJIhEImSzWeLx+Ocrnq4od3d3RKNRZmZmCAaDpNNp5ubmCIVChMNh5ufnCQaDLC8vk8/nP8X0SCkBODo6QtM0crkcw8PD7O7u4jgOtVqN9/d3bNum2Wxye3vLy8sLwBcDpRSmaRIIBGi1WmxubpJOp3EcB9u2qdfr6LrOyMgIhUKBlZUVlFK4rot3aWnp19DQENfX15ydndHb28vFxQXVapWpqSmi0SimaZJMJkmlUui6zujoKNPT09zf33/ZmM/nMQyDk5MTDg4OsCyLWCzG4uIiXacmJiZIJpMAnww8XXB2dhbbttne3uby8pJsNotpmiil0DQNpRRKKbxeL16vF6XUR5C6idvY2GBsbIzz83MSiQSrq6uMj4/TFflfXZBSopVKpWfAiEQi/G+hhBDUajUA0TMwMDAphLiqVCo/qqPf7xeGYUz+Bgm0dbIWFetGAAAAAElFTkSuQmCC".getBytes()));

        jacksonSerialization.from(product).serialize();
        assertThat(jsonResult(), is(equalTo(expectedResult)));
    }

    @Test
    public void shouldSerializeNull() {
        String expectedResult = "{}";

        jacksonSerialization.from(null).serialize();
        assertThat(jsonResult(), is(equalTo(expectedResult)));
    }

    @Test
    public void shouldSerializeNullWithInclude() {
        String expectedResult = "{}";
        
        jacksonSerialization.from(null).include("name").serialize();
        assertThat(jsonResult(), is(equalTo(expectedResult)));
    }

    @Test
    public void shouldSerializeNullWithExclude() {
        String expectedResult = "{}";
        
        jacksonSerialization.from(null).exclude("name").serialize();
        assertThat(jsonResult(), is(equalTo(expectedResult)));
    }
    
    @Test
    public void shouldSerializeNullWithAlias() {
        String expectedResult = "{\"products\":{}}";

        jacksonSerialization.from(null, "products").serialize();
        assertThat(jsonResult(), is(equalTo(expectedResult)));
    }

}
