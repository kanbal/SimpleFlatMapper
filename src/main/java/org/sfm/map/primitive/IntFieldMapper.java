package org.sfm.map.primitive;

import org.sfm.map.Mapper;
import org.sfm.reflect.primitive.IntGetter;
import org.sfm.reflect.primitive.IntSetter;

public class IntFieldMapper<S, T> implements Mapper<S, T> {

	private final IntGetter<S> getter;
	private final IntSetter<T> setter;
	
	
 	public IntFieldMapper(IntGetter<S> getter, IntSetter<T> setter) {
		this.getter = getter;
		this.setter = setter;
	}


	@Override
	public void map(S source, T target) throws Exception {
		setter.setInt(target, getter.getInt(source));
	}


	public IntGetter<S> getGetter() {
		return getter;
	}


	public IntSetter<T> getSetter() {
		return setter;
	}

}
