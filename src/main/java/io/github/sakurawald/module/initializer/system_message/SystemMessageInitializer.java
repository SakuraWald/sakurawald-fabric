package io.github.sakurawald.module.initializer.system_message;

import io.github.sakurawald.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.core.config.handler.abst.BaseConfigurationHandler;
import io.github.sakurawald.core.config.handler.impl.ObjectConfigurationHandler;
import io.github.sakurawald.module.initializer.ModuleInitializer;
import io.github.sakurawald.module.initializer.system_message.config.model.SystemMessageConfigModel;

public class SystemMessageInitializer extends ModuleInitializer {

    public final BaseConfigurationHandler<SystemMessageConfigModel> config = new ObjectConfigurationHandler<>(ReflectionUtil.getModuleControlFileName(this), SystemMessageConfigModel.class);

}
