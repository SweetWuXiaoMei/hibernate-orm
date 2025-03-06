/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.orm.test.function.json;

import org.hibernate.cfg.QuerySettings;

import org.hibernate.dialect.GaussDBDialect;
import org.hibernate.testing.orm.domain.StandardDomainModel;
import org.hibernate.testing.orm.junit.DialectFeatureChecks;
import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.RequiresDialectFeature;
import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.hibernate.testing.orm.junit.Setting;
import org.hibernate.testing.orm.junit.SkipForDialect;
import org.junit.jupiter.api.Test;

/**
 * @author Christian Beikov
 */
@DomainModel(standardModels = StandardDomainModel.GAMBIT)
@SessionFactory
@ServiceRegistry(settings = @Setting(name = QuerySettings.JSON_FUNCTIONS_ENABLED, value = "true"))
@RequiresDialectFeature( feature = DialectFeatureChecks.SupportsJsonInsert.class)
public class JsonInsertTest {

	@Test
	@SkipForDialect( dialectClass = GaussDBDialect.class, reason = "not support")
	public void testSimple(SessionFactoryScope scope) {
		scope.inSession( em -> {
			//tag::hql-json-insert-example[]
			em.createQuery( "select json_insert('{\"a\":1}', '$.b', 2)" ).getResultList();
			//end::hql-json-insert-example[]
		} );
	}

}
