/*
 * Copyright 2007-2016 the original author or authors.
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
package net.ymate.platform.serv;

import net.ymate.platform.core.Version;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.module.IModule;
import net.ymate.platform.core.module.annotation.Module;
import net.ymate.platform.serv.annotation.Client;
import net.ymate.platform.serv.annotation.Server;
import net.ymate.platform.serv.handle.ClientHandler;
import net.ymate.platform.serv.handle.ServerHandler;
import net.ymate.platform.serv.impl.DefaultModuleCfg;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务模块管理器
 *
 * @author 刘镇 (suninformation@163.com) on 15/10/15 上午10:22
 * @version 1.0
 */
@Module
public class Servs implements IModule, IServ {

    public static final Version VERSION = new Version(2, 0, 0, Servs.class.getPackage().getImplementationVersion(), Version.VersionType.Alphal);

    private final Log _LOG = LogFactory.getLog(Servs.class);

    private static IServ __instance;

    private YMP __owner;

    private IServModuleCfg __moduleCfg;

    private boolean __inited;

    private Map<Class<? extends IListener>, IServer> __servers;

    private Map<Class<? extends IListener>, IClient> __clients;

    /**
     * @return 返回默认服务模块管理器实例对象
     */
    public static IServ get() {
        if (__instance == null) {
            synchronized (VERSION) {
                if (__instance == null) {
                    __instance = YMP.get().getModule(Servs.class);
                }
            }
        }
        return __instance;
    }

    /**
     * @param owner YMP框架管理器实例
     * @return 返回指定YMP框架管理器容器内的服务模块实例
     */
    public static IServ get(YMP owner) {
        return owner.getModule(Servs.class);
    }

    public Servs() {
        __servers = new HashMap<Class<? extends IListener>, IServer>();
        __clients = new HashMap<Class<? extends IListener>, IClient>();
    }

    public String getName() {
        return IServ.MODULE_NAME;
    }

    public void init(YMP owner) throws Exception {
        if (!__inited) {
            //
            _LOG.info("Initializing ymate-platform-serv-" + VERSION);
            //
            __owner = owner;
            __moduleCfg = new DefaultModuleCfg(owner);
            //
            __owner.registerExcludedClass(IServer.class);
            __owner.registerExcludedClass(IServerCfg.class);
            __owner.registerExcludedClass(IClient.class);
            __owner.registerExcludedClass(IClientCfg.class);
            __owner.registerExcludedClass(ICodec.class);
            __owner.registerExcludedClass(IListener.class);
            //
            __owner.registerHandler(Server.class, new ServerHandler(this));
            __owner.registerHandler(Client.class, new ClientHandler(this));
            //
            __inited = true;
        }
    }

    public boolean isInited() {
        return __inited;
    }

    public YMP getOwner() {
        return __owner;
    }

    public IServModuleCfg getModuleCfg() {
        return __moduleCfg;
    }

    @SuppressWarnings("unchecked")
    public <T> T getServer(Class<? extends IListener> clazz) {
        return (T) __servers.get(clazz);
    }

    @SuppressWarnings("unchecked")
    public <T> T getClient(Class<? extends IListener> clazz) {
        return (T) __clients.get(clazz);
    }

    public void registerServer(Class<? extends IListener> listenerClass, IServer server) throws Exception {
        server.start();
        __servers.put(listenerClass, server);
    }

    public void registerClient(Class<? extends IListener> listenerClass, IClient client) throws Exception {
        client.connect();
        __clients.put(listenerClass, client);
    }

    public void destroy() throws Exception {
        if (__inited) {
            __inited = false;
            //
            for (IClient _client : __clients.values()) {
                _client.close();
            }
            for (IServer _server : __servers.values()) {
                _server.close();
            }
            //
            __moduleCfg = null;
            __owner = null;
        }
    }
}