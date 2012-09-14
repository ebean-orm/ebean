package com.avaje.ebeaninternal.server.type;

import scala.Option;

import com.avaje.ebean.config.ScalarTypeConverter;

/**
 * A type converter to support scala.Option.
 * 
 * @author rbygrave
 *
 * @param <S> the underlying type
 */
public class ScalaOptionTypeConverter<S> implements ScalarTypeConverter<scala.Option<S>, S>{
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Option<S> getNullValue() {
        return (scala.Option)scala.None$.MODULE$;
    }

    public S unwrapValue(Option<S> beanType) {
        
        if (beanType.isEmpty()){
            return null;
        } else {
            return beanType.get();
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Option<S> wrapValue(S scalarType) {
        if (scalarType == null){
            return (scala.Option)scala.None$.MODULE$;
        }
        if (scalarType instanceof scala.Some){
            return (Option<S>)scalarType;
        }
        return new scala.Some<S>(scalarType);
    }

    
}
