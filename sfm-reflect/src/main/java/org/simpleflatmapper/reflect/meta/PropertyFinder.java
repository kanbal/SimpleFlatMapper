package org.simpleflatmapper.reflect.meta;

import org.simpleflatmapper.reflect.InstantiatorDefinition;
import org.simpleflatmapper.reflect.Parameter;
import org.simpleflatmapper.reflect.getter.NullGetter;
import org.simpleflatmapper.reflect.setter.NullSetter;
import org.simpleflatmapper.util.Predicate;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class PropertyFinder<T> {
	protected final Predicate<PropertyMeta<?, ?>> propertyFilter;

	protected PropertyFinder(Predicate<PropertyMeta<?, ?>> propertyFilter) {
		this.propertyFilter = propertyFilter;
	}

	@SuppressWarnings("unchecked")
	public final <E> PropertyMeta<T, E> findProperty(PropertyNameMatcher propertyNameMatcher) {
		MatchingProperties matchingProperties = new MatchingProperties(propertyFilter);
		lookForProperties(propertyNameMatcher, matchingProperties, PropertyMatchingScore.INITIAL, true, IDENTITY_TRANSFORMER);
		return (PropertyMeta<T, E>)matchingProperties.selectBestMatch();
	}

	public abstract void lookForProperties(
			PropertyNameMatcher propertyNameMatcher,
			FoundProperty<T> matchingProperties,
			PropertyMatchingScore score,
			boolean allowSelfReference,
			PropertyFinderTransformer propertyFinderTransformer);


	public abstract List<InstantiatorDefinition> getEligibleInstantiatorDefinitions();

    public abstract PropertyFinder<?> getSubPropertyFinder(PropertyMeta<?, ?> owner);
	public abstract PropertyFinder<?> getOrCreateSubPropertyFinder(SubPropertyMeta<?, ?, ?> subPropertyMeta);

	public Predicate<PropertyMeta<?, ?>> getPropertyFilter() {
		return propertyFilter;
	}

	public abstract Type getOwnerType();

	protected static class MatchingProperties<T> implements FoundProperty<T> {
		private final List<MatchedProperty<T, ?>> matchedProperties = new ArrayList<MatchedProperty<T, ?>>();
		private final Predicate<PropertyMeta<?, ?>> propertyFilter;

		public MatchingProperties(Predicate<PropertyMeta<?, ?>> propertyFilter) {
			this.propertyFilter = propertyFilter;
		}

		@Override
		public <P extends  PropertyMeta<T, ?>> void found(P propertyMeta,
														  Runnable selectionCallback,
														  PropertyMatchingScore score) {
			if (propertyFilter.test(propertyMeta)) {
				matchedProperties.add(new MatchedProperty<T, P>(propertyMeta, selectionCallback, score));
			}
		}

		public PropertyMeta<T, ?> selectBestMatch() {
			if (matchedProperties.isEmpty()) return null;
			Collections.sort(matchedProperties);
			MatchedProperty<T, ?> selectedMatchedProperty = matchedProperties.get(0);
			selectedMatchedProperty.select();
			return selectedMatchedProperty.propertyMeta;
		}
	}

	private static class MatchedProperty<T, P extends PropertyMeta<T, ?>> implements Comparable<MatchedProperty<T, ?>>{
		private final P propertyMeta;
		private final Runnable selectionCallback;
		private final PropertyMatchingScore score;

		private MatchedProperty(P propertyMeta, Runnable selectionCallback, PropertyMatchingScore score) {
			this.propertyMeta = propertyMeta;
			this.selectionCallback = selectionCallback;
			this.score = score;
		}


		@Override
		public int compareTo(MatchedProperty<T, ?> o) {
			int i =  this.score.compareTo(o.score);
			
			if (i == 0) {
				return compare(this.propertyMeta, o.propertyMeta);
			} else {
				return i;
			}
		}

		private int compare(PropertyMeta<?, ?> p1, PropertyMeta<?, ?> p2) {
			if (p1.isConstructorProperty()) {
				if (!p2.isConstructorProperty()) {
					return -1;
				}
			} else if (p2.isConstructorProperty()) {
				return 1;
			} else if (!p1.isSelf()) {
				if (p2.isSelf()) {
					return -1;
				}
			} else if (!p2.isSelf()) {
				return 1;
			} else if (!p1.isSubProperty()) {
				if (p2.isSubProperty()) {
					return -1;
				}
			} else if (!p2.isSubProperty()) {
				return 1;
			}
			return getterSetterCompare(p1, p2);
		}

		private int getterSetterCompare(PropertyMeta<?, ?> p1, PropertyMeta<?, ?> p2) {
			return nbGetterSetter(p2) - nbGetterSetter(p1);
		}

		private int nbGetterSetter(PropertyMeta<?, ?> p) {
			int c = 0;
			if (!NullGetter.isNull(p.getGetter())) {
				c++;
			}
			if (!NullSetter.isNull(p.getSetter())) {
				c++;
			}
			return c;
		}

		public void select() {
			if (selectionCallback != null) selectionCallback.run();
		}

		@Override
		public String toString() {
			return "MatchedProperty{" +
					"propertyMeta=" + propertyMeta +
					", score=" + score + ":" + nbGetterSetter(propertyMeta) +
					", getter=" + propertyMeta.getGetter() +
					", setter=" + propertyMeta.getSetter() +
					'}';
		}
	}

    public interface FoundProperty<T> {
        <P extends  PropertyMeta<T, ?>> void found(P propertyMeta,
                                                   Runnable selectionCallback,
                                                   PropertyMatchingScore score);
    }


	public void manualMatch(PropertyMeta<?, ?> prop) {
		if (prop.isSubProperty()) {
			SubPropertyMeta subPropertyMeta = (SubPropertyMeta) prop;
			PropertyMeta ownerProperty = subPropertyMeta.getOwnerProperty();
			
			manualMatch(ownerProperty);
			
			PropertyFinder<?> subPropertyFinder = getOrCreateSubPropertyFinder(subPropertyMeta);
			subPropertyFinder.manualMatch(subPropertyMeta.getSubProperty());
		}
	}

    public interface PropertyFinderTransformer {
		<T> PropertyFinder<T> apply(PropertyFinder<T> propertyFinder);
	}

	public static PropertyFinderTransformer IDENTITY_TRANSFORMER = new PropertyFinderTransformer() {
		@Override
		public <T> PropertyFinder<T> apply(PropertyFinder<T> propertyFinder) {
			return propertyFinder;
		}

		@Override
		public String toString() {
			return "IDENTITY_TRANSFORMER";
		}
	};
}