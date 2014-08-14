package org.sfm.map.primitive;

import org.sfm.map.Mapper;
import org.sfm.reflect.primitive.ShortGetter;
import org.sfm.reflect.primitive.ShortSetter;

public class ShortFieldMapper<S, T> implements Mapper<S, T> {

	private final ShortGetter<S> getter;
	private final ShortSetter<T> setter;
	
	
 	public ShortFieldMapper(ShortGetter<S> getter, ShortSetter<T> setter) {
		this.getter = getter;
		this.setter = setter;
	}


	@Override
	public void map(S source, T target) throws Exception {
		setter.setShort(target, getter.getShort(source));
	}


	public ShortGetter<S> getGetter() {
		return getter;
	}


	public ShortSetter<T> getSetter() {
		return setter;
	}

}
