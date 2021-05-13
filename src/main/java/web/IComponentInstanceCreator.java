package web;

import java.lang.reflect.Type;

import com.google.gson.InstanceCreator;

import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.model.ComponentInstance;

public class IComponentInstanceCreator implements InstanceCreator<IComponentInstance> {

	@Override
	public IComponentInstance createInstance(Type type) {
		return null;//new ComponentInstance();
	}
	
}
