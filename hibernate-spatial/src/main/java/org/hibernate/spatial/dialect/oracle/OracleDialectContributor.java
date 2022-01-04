/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

package org.hibernate.spatial.dialect.oracle;

import java.util.Map;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.TypeContributions;
import org.hibernate.boot.registry.selector.spi.StrategySelector;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.engine.config.spi.StandardConverters;
import org.hibernate.query.sqm.function.SqmFunctionDescriptor;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.spatial.HSMessageLogger;
import org.hibernate.spatial.HibernateSpatialConfigurationSettings;
import org.hibernate.spatial.contributor.ContributorImplementor;
import org.hibernate.spatial.dialect.SpatialFunctionsRegistry;

import org.geolatte.geom.codec.db.oracle.ConnectionFinder;
import org.geolatte.geom.codec.db.oracle.OracleJDBCTypeFactory;

public class OracleDialectContributor implements ContributorImplementor {

	private final ServiceRegistry serviceRegistry;

	public OracleDialectContributor(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	@Override
	public void contributeJdbcTypes(TypeContributions typeContributions) {
		HSMessageLogger.LOGGER.typeContributions( this.getClass().getCanonicalName() );
		final ConfigurationService cfgService = getServiceRegistry().getService( ConfigurationService.class );
		final StrategySelector strategySelector = getServiceRegistry().getService( StrategySelector.class );

		final ConnectionFinder connectionFinder = strategySelector.resolveStrategy(
				ConnectionFinder.class,
				cfgService.getSetting(
						HibernateSpatialConfigurationSettings.CONNECTION_FINDER,
						String.class,
						"org.geolatte.geom.codec.db.oracle.DefaultConnectionFinder"
				)
		);

		HSMessageLogger.LOGGER.connectionFinder( connectionFinder.getClass().getCanonicalName() );

		SDOGeometryType sdoGeometryType = new SDOGeometryType(
				new OracleJDBCTypeFactory(
						connectionFinder
				)
		);

		typeContributions.contributeJdbcTypeDescriptor( sdoGeometryType );
	}

	@Override
	public void contributeFunctions(FunctionContributions functionContributions) {
		HSMessageLogger.LOGGER.functionContributions( this.getClass().getCanonicalName() );
		final var cfgService = getServiceRegistry().getService( ConfigurationService.class );
		boolean isOgcStrict = cfgService.getSetting(
				HibernateSpatialConfigurationSettings.ORACLE_OGC_STRICT,
				StandardConverters.BOOLEAN,
				false
		);
		OracleSDOSupport sdoSupport = new OracleSDOSupport( isOgcStrict );
		SpatialFunctionsRegistry entries = sdoSupport.functionsToRegister();
		for( Map.Entry<String, SqmFunctionDescriptor> funcToRegister : entries ) {
			functionContributions.getFunctionRegistry().register( funcToRegister.getKey(), funcToRegister.getValue() );
		}
	}

	@Override
	public ServiceRegistry getServiceRegistry() {
		return serviceRegistry;
	}
}
