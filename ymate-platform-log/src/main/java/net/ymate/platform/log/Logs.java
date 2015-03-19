/*
 * Copyright 2007-2107 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ymate.platform.log;

import net.ymate.platform.core.Version;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.module.IModule;
import net.ymate.platform.core.module.annotation.Module;
import net.ymate.platform.core.util.ClassUtils;
import net.ymate.platform.log.impl.DefaultLogger;
import net.ymate.platform.log.impl.DefaultModuleCfg;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日志记录器模块管理器
 *
 * @author 刘镇 (suninformation@163.com) on 15-3-9 上午1:10
 * @version 1.0
 */
@Module
public class Logs implements IModule, ILog {

    public static final Version VERSION = new Version(2, 0, 0, Logs.class.getPackage().getImplementationVersion(), Version.VersionType.Alphal);

    private static Map<String, ILogger> __LOGGER_CACHE = new ConcurrentHashMap<String, ILogger>();

    private static ILog __instance;

    private YMP __owner;

    private ILogModuleCfg __moduleCfg;

    private boolean __inited;

    private ILogger __currentLogger;

    /**
     * @return 返回默认日志记录器模块管理器实例对象
     */
    public static ILog get() {
        if (__instance == null) {
            synchronized (VERSION) {
                if (__instance == null) {
                    __instance = YMP.get().getModule(Logs.class);
                }
            }
        }
        return __instance;
    }

    /**
     * @param owner YMP框架管理器实例
     * @return 返回指定YMP框架管理器容器内的日志记录器模块实例
     */
    public static ILog get(YMP owner) {
        return owner.getModule(Logs.class);
    }

    public void init(YMP owner) throws Exception {
        if (!__inited) {
            __owner = owner;
            __moduleCfg = new DefaultModuleCfg(__owner);
            // 设置全局变量，便于配置文件内引用
            System.getProperties().put("LOG_OUT_DIR", __moduleCfg.getOutputDir().getPath());
            //
            if (__moduleCfg.getLoggerClass() != null) {
                __currentLogger = ClassUtils.impl(__moduleCfg.getLoggerClass(), ILogger.class);
            }
            if (__currentLogger == null) {
                __currentLogger = new DefaultLogger();
            }
            __LOGGER_CACHE.put(__moduleCfg.getLoggerName(), __currentLogger);
            //
            __currentLogger.init(this, __moduleCfg.getLoggerName());
            __currentLogger.console(__moduleCfg.allowOutputConsole());
            //
            __inited = true;
        }
    }

    public boolean isInited() {
        return __inited;
    }

    public void destroy() throws Exception {
        if (__inited) {
            for (ILogger _logger : __LOGGER_CACHE.values()) {
                _logger.destroy();
            }
            __currentLogger = null;
        }
    }

    public ILogModuleCfg getModuleCfg() {
        return __moduleCfg;
    }

    public ILogger getLogger() {
        return __currentLogger;
    }

    public ILogger getLogger(String loggerName) throws Exception {
        ILogger _logger = __LOGGER_CACHE.get(loggerName);
        if (_logger == null) {
            _logger = __currentLogger.getLogger(loggerName);
            if (_logger != null) {
                __LOGGER_CACHE.put(loggerName, _logger);
            }
        }
        return _logger;
    }
}