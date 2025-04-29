/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.orm.test.jpa.callbacks.xml.replace;

import java.util.List;

import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests overriding entity-listeners via XML
 *
 * @author Steve Ebersole
 */
@SuppressWarnings("JUnitMalformedDeclaration")
@DomainModel(
		annotatedClasses = {Product.class,Order.class, LineItemSuper.class, LineItem.class},
		xmlMappings = "mappings/callbacks/replace.xml"
)
@SessionFactory
public class ListenerReplacementTests {
	public static final String[] LISTENER_ABC = {"ListenerA", "ListenerB", "ListenerC"};
	public static final String[] LISTENER_BC = {"ListenerB", "ListenerC"};

	@Test
	void testSimplePersist(SessionFactoryScope scope) {
		final Product product = new Product( 1, "987654321", 123 );
		scope.inTransaction( (session) -> {
			session.persist( product );
			assertThat( product.getPrePersistCallbacks() ).containsExactlyInAnyOrder( LISTENER_ABC );
		} );
		assertThat( product.getPostPersistCallbacks() ).containsExactlyInAnyOrder( LISTENER_ABC );
	}

	@Test
	void testCascadingPersist(SessionFactoryScope scope) {
		final Product product = new Product( 1, "987654321", 123 );
		final Order order = new Order( 1, 246 );
		final LineItem lineItem = new LineItem( 1, order, product, 2 );
		order.addLineItem( lineItem );

		scope.inTransaction( (session) -> {
			session.persist( product );
			session.persist( order );

			assertThat( product.getPrePersistCallbacks() ).containsExactlyInAnyOrder( LISTENER_ABC );
			assertThat( order.getPrePersistCallbacks() ).containsExactlyInAnyOrder( LISTENER_ABC );
			assertThat( lineItem.getPrePersistCallbacks() ).containsExactlyInAnyOrder( LISTENER_BC );
		} );

		assertThat( product.getPostPersistCallbacks() ).containsExactlyInAnyOrder( LISTENER_ABC );
		assertThat( order.getPostPersistCallbacks() ).containsExactlyInAnyOrder( LISTENER_ABC );
		assertThat( lineItem.getPostPersistCallbacks() ).containsExactlyInAnyOrder( LISTENER_BC );
	}

	@Test
	void testSimpleRemove(SessionFactoryScope scope) {
		final Product product = new Product( 1, "987654321", 123 );
		scope.inTransaction( (session) -> {
			session.persist( product );
		} );
		scope.inTransaction( (session) -> {
			session.remove( product );
			assertThat( product.getPreRemoveCallbacks() ).containsExactlyInAnyOrder( LISTENER_ABC );
		} );
		assertThat( product.getPostRemoveCallbacks() ).containsExactlyInAnyOrder( LISTENER_ABC );
	}

	@Test
	void testCascadingRemove(SessionFactoryScope scope) {
		final Product product = new Product( 1, "987654321", 123 );
		final Order order = new Order( 1, 246 );
		final LineItem lineItem = new LineItem( 1, order, product, 2 );
		order.addLineItem( lineItem );

		scope.inTransaction( (session) -> {
			session.persist( product );
			session.persist( order );
		} );

		scope.inTransaction( (session) -> {
			session.remove( order );
			assertThat( order.getPreRemoveCallbacks() ).containsExactlyInAnyOrder( LISTENER_ABC );
			assertThat( lineItem.getPreRemoveCallbacks() ).containsExactlyInAnyOrder( LISTENER_BC );
		} );

		assertThat( order.getPostRemoveCallbacks() ).containsExactlyInAnyOrder( LISTENER_ABC );
		assertThat( lineItem.getPostRemoveCallbacks() ).containsExactlyInAnyOrder( LISTENER_BC );
	}

	@Test
	void testSimpleUpdate(SessionFactoryScope scope) {
		scope.inTransaction( (session) -> {
			final Product product = new Product( 1, "987654321", 123 );
			session.persist( product );
		} );


		final Product updated = scope.fromTransaction( (session) -> {
			final Product product = session.find( Product.class, 1 );

			assertThat( product.getPreUpdateCallbacks() ).isEmpty();
			assertThat( product.getPostUpdateCallbacks() ).isEmpty();

			product.setCost( 789 );

			return product;
		} );

		assertThat( updated.getPreUpdateCallbacks() ).containsExactlyInAnyOrder( LISTENER_ABC );
		assertThat( updated.getPostUpdateCallbacks() ).containsExactlyInAnyOrder( LISTENER_ABC );
	}

	@Test
	void testLoading(SessionFactoryScope scope) {
		scope.inTransaction( (session) -> {
			final Product product = new Product( 1, "987654321", 123 );
			final Order order = new Order( 1, 246 );
			final LineItem lineItem = new LineItem( 1, order, product, 2 );
			order.addLineItem( lineItem );

			session.persist( product );
			session.persist( order );
		} );

		scope.inTransaction( (session) -> {
			final Product product = session.find( Product.class, 1 );
			assertThat( product.getPostLoadCallbacks() ).containsExactlyInAnyOrder( LISTENER_ABC );
		} );

		scope.inTransaction( (session) -> {
			final List<Product> products = session.createSelectionQuery( "from Product", Product.class ).list();
			products.forEach( (product) -> {
				assertThat( product.getPostLoadCallbacks() ).containsExactlyInAnyOrder( LISTENER_ABC );
			} );
		} );

		scope.inTransaction( (session) -> {
			final LineItem lineItem = session.find( LineItem.class, 1 );
			assertThat( lineItem.getPostLoadCallbacks() ).containsExactlyInAnyOrder( LISTENER_BC );
			assertThat( lineItem.getOrder().getPostLoadCallbacks() ).containsExactlyInAnyOrder( LISTENER_ABC );
		} );
	}

	@AfterEach
	void dropData(SessionFactoryScope scope) {
		scope.inTransaction( (session) -> {
			session.createMutationQuery( "delete LineItem" ).executeUpdate();
			session.createMutationQuery( "delete Order" ).executeUpdate();
			session.createMutationQuery( "delete Product" ).executeUpdate();
		} );
	}
}
