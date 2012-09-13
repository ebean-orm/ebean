package com.avaje.ebeaninternal.server.deploy;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Determine the Many Type for a property.
 * <p>
 * Scala types require CollectionTypeConverter's to wrap and unwrap from the
 * underlying java types used.
 * </p>
 */
public class DetermineManyType {

    private final boolean withScalaSupport;
    private final ManyType scalaBufMany;
    private final ManyType scalaSetMany;
    private final ManyType scalaMapMany;
    
    public DetermineManyType(boolean withScalaSupport) {
        this.withScalaSupport = withScalaSupport;
        if (withScalaSupport){
            
            CollectionTypeConverter bufConverter = new ScalaBufferConverter();
            CollectionTypeConverter setConverter = new ScalaSetConverter();
            CollectionTypeConverter mapConverter = new ScalaMapConverter();
            
            this.scalaBufMany = new ManyType(ManyType.Underlying.LIST, bufConverter);
            this.scalaSetMany = new ManyType(ManyType.Underlying.SET, setConverter);
            this.scalaMapMany = new ManyType(ManyType.Underlying.MAP, mapConverter);
            
        } else {
            this.scalaBufMany = null;
            this.scalaSetMany = null;
            this.scalaMapMany = null;
        }
    }
    
	public ManyType getManyType(Class<?> type) {
        if (type.equals(List.class)){
        	return ManyType.JAVA_LIST;
        }
        if (type.equals(Set.class)){
        	return ManyType.JAVA_SET;
        } 
        if (type.equals(Map.class)){
        	return ManyType.JAVA_MAP;
        }
        if (withScalaSupport){
            // only get in here when scala in classpath
            if (type.equals(scala.collection.mutable.Buffer.class)){
                return scalaBufMany;
            }
            if (type.equals(scala.collection.mutable.Set.class)){
                return scalaSetMany;
            }
            if (type.equals(scala.collection.mutable.Map.class)){
                return scalaMapMany;
            }
        }
        return null;
    }
}
