package deserialization.gson;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import model.Order;
import model.Product;

import org.junit.Before;
import org.junit.Test;

import br.com.caelum.gson.deserialization.gson.GsonDeserialization;
import br.com.caelum.vraptor.http.ParameterNameProvider;
import br.com.caelum.vraptor.resource.DefaultResourceClass;
import br.com.caelum.vraptor.resource.DefaultResourceMethod;

public class GsonDeserializationTest {

	private GsonDeserialization deserializer;
	private ParameterNameProvider provider;
	private DefaultResourceMethod list;
	private DefaultResourceMethod listLimit;
	private DefaultResourceMethod add;
	private DefaultResourceMethod addProduct;

	@Before
	public void setup() throws Exception {
		provider = mock(ParameterNameProvider.class);
		deserializer = new GsonDeserialization(provider);

		DefaultResourceClass resourceClass = new DefaultResourceClass(OrderController.class);

		list = new DefaultResourceMethod(resourceClass,
				OrderController.class.getDeclaredMethod("list"));
		listLimit = new DefaultResourceMethod(resourceClass,
				OrderController.class.getDeclaredMethod("list", Integer.class));
		add = new DefaultResourceMethod(resourceClass, OrderController.class.getDeclaredMethod(
				"add", Order.class));
		addProduct = new DefaultResourceMethod(resourceClass,
				OrderController.class.getDeclaredMethod("addProduct", Order.class, Product.class));
	}

	static class OrderController {

		public void list() {
		}

		public void list(Integer limit) {
		}

		public void add(Order order) {
		}

		public void addProduct(Order order, Product product) {
		}

	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldMethodNotAcceptedWithoutArguments() {
		deserializer.deserialize(new ByteArrayInputStream(new byte[0]), list);
	}

	@Test
	public void shouldSkipArgumentsNotDefined() {
		InputStream input = new ByteArrayInputStream(
				"{\"product\":{\"id\":1,\"name\":\"Product 1\"}}".getBytes());

		when(provider.parameterNamesFor(listLimit.getMethod()))
				.thenReturn(new String[] { "limit" });

		Object[] deserialized = deserializer.deserialize(input, listLimit);

		assertThat(deserialized.length, is(1));
		assertThat(deserialized[0], is(nullValue()));
	}

	@Test
	public void shouldDeserializeAnOrderWhenMethodHasOneArgument() {
		String json = "{\"order\":{\"id\":1,\"products\":[{\"id\":1,\"name\":\"Product 1\","
				+ "\"group\":{\"id\":1,\"name\":\"Group 1\"}},{\"id\":2,\"name\":\"Product 2\","
				+ "\"group\":{\"id\":2,\"name\":\"Group 2\"}}]}}";
		InputStream input = new ByteArrayInputStream(json.getBytes());

		when(provider.parameterNamesFor(add.getMethod())).thenReturn(new String[] { "order" });

		Object[] deserialized = deserializer.deserialize(input, add);

		assertThat(deserialized.length, is(1));
		assertThat(deserialized[0], is(instanceOf(Order.class)));
		Order order = (Order) deserialized[0];
		assertThat(order.getId(), is(1L));
		assertThat(order.getProducts().size(), is(2));
		assertThat(order.getProducts().get(0).getId(), is(1L));
		assertThat(order.getProducts().get(0).getGroup().getId(), is(1L));
		assertThat(order.getProducts().get(1).getId(), is(2L));
		assertThat(order.getProducts().get(1).getGroup().getId(), is(2L));
	}

	@Test
	public void shouldDeserializeAnOrderAndProductWhenMethodHasTwoArguments() {
		String json = "{\"order\":{\"id\":1,\"products\":[{\"id\":1,\"name\":\"Product 1\","
				+ "\"group\":{\"id\":1,\"name\":\"Group 1\"}},{\"id\":2,\"name\":\"Product 2\","
				+ "\"group\":{\"id\":2,\"name\":\"Group 2\"}}]},"
				+ "\"product\":{\"id\":1,\"name\":\"Product 1\"}}";
		InputStream input = new ByteArrayInputStream(json.getBytes());

		when(provider.parameterNamesFor(addProduct.getMethod())).thenReturn(
				new String[] { "order", "product" });

		Object[] deserialized = deserializer.deserialize(input, addProduct);

		assertThat(deserialized.length, is(2));
		assertThat(deserialized[0], is(instanceOf(Order.class)));
		assertThat(deserialized[1], is(instanceOf(Product.class)));

		Order order = (Order) deserialized[0];
		assertThat(order.getId(), is(1L));
		assertThat(order.getProducts().size(), is(2));
		assertThat(order.getProducts().get(0).getId(), is(1L));
		assertThat(order.getProducts().get(0).getGroup().getId(), is(1L));
		assertThat(order.getProducts().get(1).getId(), is(2L));
		assertThat(order.getProducts().get(1).getGroup().getId(), is(2L));

		Product product = (Product) deserialized[1];
		assertThat(product.getId(), is(1L));
		assertThat(product.getName(), is("Product 1"));
	}

	@Test
	public void shouldDeserializeOneOrderWhenMethodHasTwoArguments() {
		String json = "{\"order\":{\"id\":1,\"products\":[{\"id\":1,\"name\":\"Product 1\","
				+ "\"group\":{\"id\":1,\"name\":\"Group 1\"}},{\"id\":2,\"name\":\"Product 2\","
				+ "\"group\":{\"id\":2,\"name\":\"Group 2\"}}]}}";
		InputStream input = new ByteArrayInputStream(json.getBytes());

		when(provider.parameterNamesFor(addProduct.getMethod())).thenReturn(
				new String[] { "order", "product" });

		Object[] deserialized = deserializer.deserialize(input, addProduct);

		assertThat(deserialized.length, is(2));
		assertThat(deserialized[0], is(instanceOf(Order.class)));
		assertThat(deserialized[1], is(nullValue()));

		Order order = (Order) deserialized[0];
		assertThat(order.getId(), is(1L));
		assertThat(order.getProducts().size(), is(2));
		assertThat(order.getProducts().get(0).getId(), is(1L));
		assertThat(order.getProducts().get(0).getGroup().getId(), is(1L));
		assertThat(order.getProducts().get(1).getId(), is(2L));
		assertThat(order.getProducts().get(1).getGroup().getId(), is(2L));
	}

}
