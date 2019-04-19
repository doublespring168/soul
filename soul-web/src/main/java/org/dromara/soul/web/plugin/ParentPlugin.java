package org.dromara.soul.web.plugin;

import org.dromara.soul.common.enums.PluginTypeEnum;

/**
 * 功能说明：各种plugin的父类
 * Author：spring
 * Date：2019-04-19 16:17
 */
public abstract class ParentPlugin implements SoulPlugin {

    @Override
    public PluginTypeEnum getPluginType() {
        return pluginType();
    }

    @Override
    public String getNamed() {
        return named();
    }

}
