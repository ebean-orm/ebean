package com.avaje.ebeaninternal.server.deploy;

import java.util.HashSet;
import java.util.Set;

import com.avaje.ebeaninternal.server.el.ElPropertyDeploy;

/**
 * Converts logical property names to database columns with table alias.
 * <p>
 * In doing so it builds an 'includes' set which becomes the joins required to
 * support the properties parsed.
 * </p>
 */
public final class DeployPropertyParser extends DeployParser {

	
	private final BeanDescriptor<?> beanDescriptor;
	
	private final Set<String> includes = new HashSet<String>();

	public DeployPropertyParser(BeanDescriptor<?> beanDescriptor) {
		this.beanDescriptor = beanDescriptor;
	}

    public Set<String> getIncludes() {
		return includes;
	}
	
	@Override
    public String getDeployWord(String expression) {
        ElPropertyDeploy elProp = beanDescriptor.getElPropertyDeploy(expression);
        if (elProp == null){
            return null;
        } else {
            addIncludes(elProp.getElPrefix());
            return elProp.getElPlaceholder(encrypted);
        }
    }

	@Override
    public String convertWord() {
		String r = getDeployWord(word);
		return r == null ? word : r;
	}

	private void addIncludes(String prefix) {
		if (prefix != null){
			includes.add(prefix);
		}
	}
}
