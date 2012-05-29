package br.com.caelum.vraptor.test.gson;

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
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;

import br.com.caelum.vraptor.serialization.gson.GsonSerialization;
import br.com.caelum.vraptor.test.model.gson.Address;
import br.com.caelum.vraptor.test.model.gson.Customer;
import br.com.caelum.vraptor.test.model.gson.Group;
import br.com.caelum.vraptor.test.model.gson.HardDisk;
import br.com.caelum.vraptor.test.model.gson.Order;
import br.com.caelum.vraptor.test.model.gson.Product;

public class GsonSerializeTest {

	private ByteArrayOutputStream output;

	private HttpServletResponse response;

	private GsonSerialization gsonSerialization;

	private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

	private String currentDateAsStr;

	private Date currentDate;

	@Before
	public void setup() throws Exception {
		this.output = new ByteArrayOutputStream();
		this.response = mock(HttpServletResponse.class);
		when(response.getWriter()).thenReturn(new PrintWriter(output));
		when(response.getLocale()).thenReturn(new Locale("pt", "BR"));

		this.gsonSerialization = new GsonSerialization(response);
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
		String expectedResult = "{\"product\":{\"id\":1,\"creationDate\":\"" + currentDateAsStr
				+ "\",\"name\":\"Product 1\"}}";

		Product product = createProduct(1L);

		gsonSerialization.from(product).serialize();
		assertThat(jsonResult(), is(equalTo(expectedResult)));
	}

	@Test
	public void shouldSerializeCollectionOfPojo() {
		String expectedResult = "{\"productList\":[{\"id\":1,\"creationDate\":\"" + currentDateAsStr
				+ "\",\"name\":\"Product 1\"},{\"id\":2,\"creationDate\":\"" + currentDateAsStr
				+ "\",\"name\":\"Product 2\"}]}";

		List<Product> products = new ArrayList<Product>();
		products.add(createProduct(1L));
		products.add(createProduct(2L));

		gsonSerialization.from(products).serialize();
		assertThat(jsonResult(), is(equalTo(expectedResult)));
	}

	@Test
	public void shouldUseAlias() {
		String expectedResult = "{\"myProduct\":{\"id\":1,\"creationDate\":\"" + currentDateAsStr
				+ "\",\"name\":\"Product 1\"}}";
		Product product = createProduct(1L);

		gsonSerialization.from(product, "myProduct").serialize();
		assertThat(jsonResult(), is(equalTo(expectedResult)));
	}

	@Test
	public void shouldSerializeMap() {
		String expectedResult = "{\"person\":{\"email\":\"ff@gmail.com\",\"idade\":28,\"name\":\"fabio franco\"}}";
		Map<String, Object> json = new HashMap<String, Object>();
		json.put("name", "fabio franco");
		json.put("idade", 28);
		json.put("email", "ff@gmail.com");

		gsonSerialization.from(json, "person").serialize();
		assertThat(jsonResult(), is(equalTo(expectedResult)));
	}

	@Test
	public void shouldSerializeList() {
		String expectedResult = "{\"list\":[1,2,3,4,5,6,\"json\"]}";

		List<Object> list = new ArrayList<Object>();
		list.addAll(Arrays.asList(1, 2, 3, 4, 5, 6));
		list.add("json");

		gsonSerialization.from(list, "list").serialize();
		assertThat(jsonResult(), is(equalTo(expectedResult)));
	}

	@Test
	public void shouldIncludeField() {
		String expectedResult = "{\"product\":{\"id\":1,\"creationDate\":\"" + currentDateAsStr
				+ "\",\"name\":\"Product 1\",\"group\":{\"id\":1,\"name\":\"Group 1\"}}}";

		Product product = createProductWithGroup(1L, 1L);

		gsonSerialization.from(product).include("group").serialize();
		assertThat(jsonResult(), is(equalTo(expectedResult)));
	}

	@Test
	public void shouldIncludeFieldFromCollection() {
		String expectedResult = "{\"order\":{\"id\":1,\"products\":[{\"id\":1,\"creationDate\":\"" + currentDateAsStr
				+ "\",\"name\":\"Product 1\",\"group\":{\"id\":1,\"name\":\"Group 1\"}},{\"id\":2,\"creationDate\":\""
				+ currentDateAsStr + "\",\"name\":\"Product 2\",\"group\":{\"id\":2,\"name\":\"Group 2\"}}]}}";

		Order order = new Order(1L, new Customer(1L, "Franco", new Address("rua", "cidade", "9800989")));
		order.addProduct(createProductWithGroup(1L, 1L));
		order.addProduct(createProductWithGroup(2L, 2L));

		gsonSerialization.from(order).include("products", "products.group").serialize();
		assertThat(jsonResult(), is(equalTo(expectedResult)));
	}

	@Test
	public void shouldSerializeParentFields() {
		String expectedResult = "{\"hardDisk\":{\"id\":1,\"name\":\"Samsumg ZTX A9000\",\"capacity\":2987000009}}";

		HardDisk hd = new HardDisk(1L, "Samsumg ZTX A9000", 2987000009L);

		gsonSerialization.from(hd).serialize();
		assertThat(jsonResult(), is(equalTo(expectedResult)));
	}

	@Test
	public void shouldExcludeParentField() {
		String expectedResult = "{\"hardDisk\":{\"name\":\"Samsumg ZTX A9000\",\"capacity\":2987000009}}";

		HardDisk hd = new HardDisk(1L, "Samsumg ZTX A9000", 2987000009L);

		gsonSerialization.from(hd).exclude("id").serialize();
		assertThat(jsonResult(), is(equalTo(expectedResult)));
	}

	@Test
	public void shouldExcludeField() {
		String expectedResult = "{\"product\":{\"id\":1,\"creationDate\":\"" + currentDateAsStr
				+ "\",\"name\":\"Product 1\",\"group\":{\"name\":\"Group 1\"}}}";

		Group group = new Group(1L, "Group 1");
		Product product = new Product(1L, "Product 1", currentDate, group);

		gsonSerialization.from(product).include("group").exclude("group.id").serialize();
		assertThat(jsonResult(), is(equalTo(expectedResult)));
	}

	@Test
	public void shouldExcudeHierarchicalField() {
		String expectedResult = "{\"order\":{\"id\":1,\"delivery\":{\"zipCode\":\"09887990\",\"street\":\"delivery street\",\"city\":\"Bristol\"},\"customer\":{\"id\":1,\"name\":\"Franco\"},\"products\":[{\"id\":1,\"creationDate\":\""
				+ currentDateAsStr
				+ "\",\"name\":\"Product 1\",\"group\":{\"name\":\"Group 1\"}},{\"id\":2,\"creationDate\":\""
				+ currentDateAsStr + "\",\"name\":\"Product 2\",\"group\":{\"name\":\"Group 2\"}}]}}";

		Order order = new Order(1L, new Customer(1L, "Franco", new Address("rua", "cidade", "9800989")), new Address(
				"delivery street", "Bristol", "09887990"));
		order.addProduct(createProductWithGroup(1L, 1L));
		order.addProduct(createProductWithGroup(2L, 2L));

		gsonSerialization.from(order).include("customer", "delivery", "products", "products.group")
				.exclude("products.group.id").serialize();
		assertThat(jsonResult(), is(equalTo(expectedResult)));
	}

	@Test
	public void shouldSerializeWithouRoot() {
		String expectedResult = "{\"id\":1,\"creationDate\":\"" + currentDateAsStr
				+ "\",\"name\":\"Product 1\",\"group\":{\"name\":\"Group 1\"}}";

		Group group = new Group(1L, "Group 1");
		Product product = new Product(1L, "Product 1", currentDate, group);

		gsonSerialization.withoutRoot().from(product).include("group").exclude("group.id").serialize();
		assertThat(jsonResult(), is(equalTo(expectedResult)));
	}

	@Test
	public void shouldSerializeIndented() {
		String expectedResult = "{\n  \"product\": {\n    \"id\": 1,\n    \"creationDate\": \""
				+ currentDateAsStr
				+ "\",\n    \"name\": \"Product 1\",\n    \"group\": {\n      \"id\": 1,\n      \"name\": \"Group 1\"\n    }\n  }\n}";

		Group group = new Group(1L, "Group 1");
		Product product = new Product(1L, "Product 1", currentDate, group);

		gsonSerialization.indented().from(product).include("group").serialize();
		assertThat(jsonResult(), is(equalTo(expectedResult)));
	}

	@Test
	public void shouldSerializeObjectAttribute() {
		String expectedResult = "{\"product\":{\"id\":1,\"creationDate\":\"" + currentDateAsStr
				+ "\",\"name\":\"Product 1\",\"data\":\"data object for product\"}}";

		Product product = new Product(1L, "Product 1", currentDate);
		product.setData("data object for product");

		gsonSerialization.from(product).serialize();
		assertThat(jsonResult(), is(equalTo(expectedResult)));
	}

	@Test
	public void shouldSerializeByteArrayAttribute() {
		String expectedResult = "{\"product\":{\"id\":1,\"name\":\"Product 1\",\"creationDate\":\""
				+ currentDateAsStr
				+ "\",\"image\":\"iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABHNCSVQICAgIfAhkiAAAAAlwSFlzAAAK8AAACvABQqw0mAAAABV0RVh0Q3JlYXRpb24gVGltZQA2LzI0LzA59sFr4wAAABx0RVh0U29mdHdhcmUAQWRvYmUgRmlyZXdvcmtzIENTNAay06AAAAJ3SURBVDiNlZO/S2phGMc/R+tkQnIMiZRMCgeVY4S2tUYQbS0hlUNDa1tLyyVaqhMh1NbgH9DSYEP+BQ1BS4OLBYFRqb1w9FR43rc7hHUv3eH2wDN9eeD7fH9o9Xo95jjO1dvbm8EPpq+vT/j9/knt5ubm2fiYn9wjhEAIITztdtvw+Xy4rvttn56eWF9fZ2dnB8dx/sJ8Ph/tdtvwSCnRNA0p5bdtNBqcnp5SLpfpdDq0Wi06nQ5/3vS4rotSCoDX11ceHx8JBAL09/cTCoXY398nHA7TbDbZ2toik8mwsLDA4OAgruvSI6VEKUWj0cCyLMrlMvF4nFQqRSaT4eHhAV3XKZVKFItFisUilUqFvb09pJR4unTX1tY4Pj4ml8tRrVYpFAq4rotlWRweHpJIJIhEImSzWeLx+Ocrnq4od3d3RKNRZmZmCAaDpNNp5ubmCIVChMNh5ufnCQaDLC8vk8/nP8X0SCkBODo6QtM0crkcw8PD7O7u4jgOtVqN9/d3bNum2Wxye3vLy8sLwBcDpRSmaRIIBGi1WmxubpJOp3EcB9u2qdfr6LrOyMgIhUKBlZUVlFK4rot3aWnp19DQENfX15ydndHb28vFxQXVapWpqSmi0SimaZJMJkmlUui6zujoKNPT09zf33/ZmM/nMQyDk5MTDg4OsCyLWCzG4uIiXacmJiZIJpMAnww8XXB2dhbbttne3uby8pJsNotpmiil0DQNpRRKKbxeL16vF6XUR5C6idvY2GBsbIzz83MSiQSrq6uMj4/TFflfXZBSopVKpWfAiEQi/G+hhBDUajUA0TMwMDAphLiqVCo/qqPf7xeGYUz+Bgm0dbIWFetGAAAAAElFTkSuQmCC\"}}";

		Product product = new Product(1L, "Product 1", currentDate);
		product.setImage(Base64
				.decodeBase64("iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAABHNCSVQICAgIfAhkiAAAAAlwSFlzAAAK8AAACvABQqw0mAAAABV0RVh0Q3JlYXRpb24gVGltZQA2LzI0LzA59sFr4wAAABx0RVh0U29mdHdhcmUAQWRvYmUgRmlyZXdvcmtzIENTNAay06AAAAJ3SURBVDiNlZO/S2phGMc/R+tkQnIMiZRMCgeVY4S2tUYQbS0hlUNDa1tLyyVaqhMh1NbgH9DSYEP+BQ1BS4OLBYFRqb1w9FR43rc7hHUv3eH2wDN9eeD7fH9o9Xo95jjO1dvbm8EPpq+vT/j9/knt5ubm2fiYn9wjhEAIITztdtvw+Xy4rvttn56eWF9fZ2dnB8dx/sJ8Ph/tdtvwSCnRNA0p5bdtNBqcnp5SLpfpdDq0Wi06nQ5/3vS4rotSCoDX11ceHx8JBAL09/cTCoXY398nHA7TbDbZ2toik8mwsLDA4OAgruvSI6VEKUWj0cCyLMrlMvF4nFQqRSaT4eHhAV3XKZVKFItFisUilUqFvb09pJR4unTX1tY4Pj4ml8tRrVYpFAq4rotlWRweHpJIJIhEImSzWeLx+Ocrnq4od3d3RKNRZmZmCAaDpNNp5ubmCIVChMNh5ufnCQaDLC8vk8/nP8X0SCkBODo6QtM0crkcw8PD7O7u4jgOtVqN9/d3bNum2Wxye3vLy8sLwBcDpRSmaRIIBGi1WmxubpJOp3EcB9u2qdfr6LrOyMgIhUKBlZUVlFK4rot3aWnp19DQENfX15ydndHb28vFxQXVapWpqSmi0SimaZJMJkmlUui6zujoKNPT09zf33/ZmM/nMQyDk5MTDg4OsCyLWCzG4uIiXacmJiZIJpMAnww8XXB2dhbbttne3uby8pJsNotpmiil0DQNpRRKKbxeL16vF6XUR5C6idvY2GBsbIzz83MSiQSrq6uMj4/TFflfXZBSopVKpWfAiEQi/G+hhBDUajUA0TMwMDAphLiqVCo/qqPf7xeGYUz+Bgm0dbIWFetGAAAAAElFTkSuQmCC"
						.getBytes()));

		gsonSerialization.from(product).serialize();
		assertThat(jsonResult(), is(equalTo(expectedResult)));
	}

	@Test
	public void shouldSerializeNull() {
		String expectedResult = "{}";

		gsonSerialization.from(null).serialize();
		assertThat(jsonResult(), is(equalTo(expectedResult)));
	}

	@Test
	public void shouldSerializeNullWithInclude() {
		String expectedResult = "{}";

		gsonSerialization.from(null).include("name").serialize();
		assertThat(jsonResult(), is(equalTo(expectedResult)));
	}

	@Test
	public void shouldSerializeNullWithExclude() {
		String expectedResult = "{}";

		gsonSerialization.from(null).exclude("name").serialize();
		assertThat(jsonResult(), is(equalTo(expectedResult)));
	}

	@Test
	public void shouldSerializeNullWithAlias() {
		String expectedResult = "{\"products\":{}}";

		gsonSerialization.from(null, "products").serialize();
		assertThat(jsonResult(), is(equalTo(expectedResult)));
	}

	@Test
	public void shouldSerializeWhenAttributObjectIsNull() {
		Product product = new Product(1l, "name", new Date(), null);
		gsonSerialization.from(product).include("group").serialize();
		assertThat(jsonResult(), is(equalTo("{\"product\":{\"id\":1,\"creationDate\":\""+ currentDateAsStr +"\",\"name\":\"name\"}}")));
	}
}