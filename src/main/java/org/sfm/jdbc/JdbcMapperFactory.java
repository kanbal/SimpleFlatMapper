package org.sfm.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.sfm.map.FieldMapper;
import org.sfm.map.FieldMapperErrorHandler;
import org.sfm.map.MapperBuilderErrorHandler;
import org.sfm.map.MapperBuildingException;
import org.sfm.map.RethrowFieldMapperErrorHandler;
import org.sfm.map.RethrowMapperBuilderErrorHandler;
import org.sfm.osgi.BridgeClassLoader;
import org.sfm.reflect.ReflectionService;
import org.sfm.reflect.asm.AsmFactory;

public final class JdbcMapperFactory {
	
	private static final AsmFactory _asmFactory = AsmHelper.isAsmPresent() ? new AsmFactory() : null;
	
	/**
	 * instantiate a new JdbcMapperFactory
	 * @return a new JdbcMapperFactory
	 */
	public static JdbcMapperFactory newInstance() {
		return new JdbcMapperFactory();
	}
	
	private FieldMapperErrorHandler fieldMapperErrorHandler = new RethrowFieldMapperErrorHandler();
	private MapperBuilderErrorHandler mapperBuilderErrorHandler = new RethrowMapperBuilderErrorHandler();
	private Map<String, String> aliases = new HashMap<>();
	private Map<String, FieldMapper<ResultSet, ?>> customMappings = new HashMap<>();
	
	private boolean useAsm = true;
	
	private final boolean useBridgeClassLoader;
	
	
	public JdbcMapperFactory(boolean useBridgeClassLoader) {
		this.useBridgeClassLoader = useBridgeClassLoader;
	}
	
	public JdbcMapperFactory() {
		this(false);
	}
	
	/**
	 * 
	 * @param fieldMapperErrorHandler 
	 * @return the factory
	 */
	public JdbcMapperFactory fieldMapperErrorHandler(final FieldMapperErrorHandler fieldMapperErrorHandler) {
		this.fieldMapperErrorHandler = fieldMapperErrorHandler;
		return this;
	}

	/**
	 * 
	 * @param mapperBuilderErrorHandler
	 * @return the factory
	 */
	public JdbcMapperFactory mapperBuilderErrorHandler(final MapperBuilderErrorHandler mapperBuilderErrorHandler) {
		this.mapperBuilderErrorHandler = mapperBuilderErrorHandler;
		return this;
	}

	/**
	 * 
	 * @param useAsm false if you want to disable asm usage.
	 * @return the factory
	 */
	public JdbcMapperFactory useAsm(final boolean useAsm) {
		this.useAsm = useAsm;
		return this;
	}

	
	/**
	 * Will create a instance of mapper based on the metadata and the target class;
	 * @param target the target class of the mapper
	 * @param metaData the metadata to create the mapper from
	 * @return a mapper that will map the data represented by the metadata to an instance of target
	 * @throws MapperBuildingException
	 */
	public <T> JdbcMapper<T> newMapper(final Class<T> target, final ResultSetMetaData metaData) throws MapperBuildingException, SQLException {
		ResultSetMapperBuilder<T> builder = new ResultSetMapperBuilderImpl<>(target, reflectionService(target), aliases, customMappings);
		
		builder.fieldMapperErrorHandler(fieldMapperErrorHandler);
		builder.mapperBuilderErrorHandler(mapperBuilderErrorHandler);
		builder.addMapping(metaData);
		
		return builder.mapper();
	}

	/**
	 * 
	 * @param target the targeted class for the mapper
	 * @return a jdbc mapper that will map to the targeted class.
	 * @throws MapperBuildingException
	 */
	public <T> JdbcMapper<T> newMapper(final Class<T> target) throws MapperBuildingException {
		return new DynamicJdbcMapper<T>(target, reflectionService(target), fieldMapperErrorHandler, mapperBuilderErrorHandler, aliases, customMappings);
	}

	
	private ReflectionService reflectionService(Class<?> target) {
		AsmFactory asmFactory = null;
		if (AsmHelper.isAsmPresent()) {
			if (useBridgeClassLoader) {
				asmFactory = new AsmFactory(new BridgeClassLoader(getClass().getClassLoader(), target.getClassLoader()));
			} else {
				asmFactory = _asmFactory;
			}
		}
		return new ReflectionService(AsmHelper.isAsmPresent(), useAsm, asmFactory);
	}

	public void addAlias(String key, String value) {
		aliases.put(key.toUpperCase(), value.toUpperCase());
	}

	public void addCustomFieldMapper(String column,	FieldMapper<ResultSet, ?> fieldMapper) {
		customMappings.put(column.toUpperCase(), fieldMapper);
	}
}
