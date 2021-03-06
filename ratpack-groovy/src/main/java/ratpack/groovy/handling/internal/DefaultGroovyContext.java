/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ratpack.groovy.handling.internal;

import com.google.common.reflect.TypeToken;
import groovy.lang.Closure;
import org.reactivestreams.Publisher;
import ratpack.api.NonBlocking;
import ratpack.api.Nullable;
import ratpack.exec.*;
import ratpack.func.Action;
import ratpack.func.Function;
import ratpack.func.NoArgAction;
import ratpack.groovy.handling.GroovyByContentSpec;
import ratpack.groovy.handling.GroovyByMethodSpec;
import ratpack.groovy.handling.GroovyContext;
import ratpack.groovy.internal.ClosureUtil;
import ratpack.handling.*;
import ratpack.handling.direct.DirectChannelAccess;
import ratpack.handling.internal.DefaultByContentSpec;
import ratpack.handling.internal.DefaultByMethodSpec;
import ratpack.http.Request;
import ratpack.http.Response;
import ratpack.http.internal.ContentNegotiationHandler;
import ratpack.http.internal.MultiMethodHandler;
import ratpack.parse.Parse;
import ratpack.path.PathTokens;
import ratpack.registry.NotInRegistryException;
import ratpack.registry.Registry;
import ratpack.server.ServerConfig;
import ratpack.stream.TransformablePublisher;

import java.nio.file.Path;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

public class DefaultGroovyContext implements GroovyContext {

  private final Context delegate;

  public DefaultGroovyContext(Context delegate) {
    this.delegate = delegate;
  }

  @Override
  public GroovyContext getContext() {
    return this;
  }

  @Override
  public ExecController getController() {
    return delegate.getController();
  }

  @Override
  public Execution getExecution() {
    return delegate.getExecution();
  }

  @Override
  public ServerConfig getServerConfig() {
    return delegate.getServerConfig();
  }

  @Override
  public DirectChannelAccess getDirectChannelAccess() {
    return delegate.getDirectChannelAccess();
  }


  @Override
  public void addInterceptor(ExecInterceptor execInterceptor, NoArgAction continuation) throws Exception {
    delegate.addInterceptor(execInterceptor, continuation);
  }

  @Override
  public void byMethod(Closure<?> closure) throws Exception {
    Map<String, Handler> handlers = new LinkedHashMap<>(2);
    ByMethodSpec delegate = new DefaultByMethodSpec(handlers);
    GroovyByMethodSpec spec = new DefaultGroovyByMethodSpec(delegate);
    ClosureUtil.configureDelegateFirst(spec, closure);
    new MultiMethodHandler(handlers).handle(this);
  }

  @Override
  public void byContent(Closure<?> closure) throws Exception {
    Map<String, Handler> handlers = new LinkedHashMap<>(2);
    DefaultByContentSpec delegate = new DefaultByContentSpec(handlers);
    GroovyByContentSpec spec = new DefaultGroovyByContentSpec(delegate);
    ClosureUtil.configureDelegateFirst(spec, closure);
    new ContentNegotiationHandler(handlers, delegate.getNoMatchHandler()).handle(this);
  }

  @Override
  public void onClose(Closure<?> callback) {
    onClose(ClosureUtil.delegatingAction(callback));
  }

  @Override
  public Request getRequest() {
    return delegate.getRequest();
  }

  @Override
  public Response getResponse() {
    return delegate.getResponse();
  }

  @Override
  @NonBlocking
  public void next() {
    delegate.next();
  }

  @Override
  @NonBlocking
  public void next(Registry registry) {
    delegate.next(registry);
  }

  @Override
  @NonBlocking
  public void insert(Handler... handlers) {
    delegate.insert(handlers);
  }

  @Override
  @NonBlocking
  public void insert(Registry registry, Handler... handlers) {
    delegate.insert(registry, handlers);
  }

  @Override
  public void byMethod(Action<? super ByMethodSpec> action) throws Exception {
    delegate.byMethod(action);
  }

  @Override
  public void byContent(Action<? super ByContentSpec> action) throws Exception {
    delegate.byContent(action);
  }

  @Override
  @NonBlocking
  public void error(Throwable throwable) throws NotInRegistryException {
    delegate.error(throwable);
  }

  @Override
  @NonBlocking
  public void clientError(int statusCode) throws NotInRegistryException {
    delegate.clientError(statusCode);
  }

  @Override
  public PathTokens getPathTokens() throws NotInRegistryException {
    return delegate.getPathTokens();
  }

  @Override
  public PathTokens getAllPathTokens() throws NotInRegistryException {
    return delegate.getAllPathTokens();
  }

  @Override
  public Path file(String path) throws NotInRegistryException {
    return delegate.file(path);
  }

  @Override
  @NonBlocking
  public void render(Object object) {
    delegate.render(object);
  }

  @Override
  public <T> Promise<T> blocking(Callable<T> blockingOperation) {
    return delegate.blocking(blockingOperation);
  }

  @Override
  public <T> Promise<T> promise(Action<? super Fulfiller<T>> action) {
    return delegate.promise(action);
  }

  @Override
  public ExecStarter exec() {
    return delegate.exec();
  }

  @Override
  public <T> TransformablePublisher<T> stream(Publisher<T> publisher) {
    return delegate.stream(publisher);
  }

  @Override
  public void redirect(String location) throws NotInRegistryException {
    delegate.redirect(location);
  }

  @Override
  public void redirect(int code, String location) throws NotInRegistryException {
    delegate.redirect(code, location);
  }

  @Override
  @NonBlocking
  public void lastModified(Date date, Runnable runnable) {
    delegate.lastModified(date, runnable);
  }

  @Override
  public <T> T parse(Class<T> type) throws Exception {
    return delegate.parse(type);
  }

  @Override
  public <T> T parse(TypeToken<T> type) throws Exception {
    return delegate.parse(type);
  }

  @Override
  public <T, O> T parse(TypeToken<T> type, O options) throws Exception {
    return delegate.parse(type, options);
  }

  @Override
  public <T, O> T parse(Class<T> type, O options) throws Exception {
    return delegate.parse(type, options);
  }

  @Override
  public <T, O> T parse(Parse<T, O> parse) throws Exception {
    return delegate.parse(parse);
  }

  @Override
  public void onClose(Action<? super RequestOutcome> callback) {
    delegate.onClose(callback);
  }

  @Override
  public <O> O get(Class<O> type) throws NotInRegistryException {
    return delegate.get(type);
  }

  @Override
  public <O> Optional<O> maybeGet(Class<O> type) {
    return delegate.maybeGet(type);
  }

  @Override
  public <O> Iterable<? extends O> getAll(Class<O> type) {
    return delegate.getAll(type);
  }

  public <O> O get(TypeToken<O> type) throws NotInRegistryException {
    return delegate.get(type);
  }

  @Override
  @Nullable
  public <O> Optional<O> maybeGet(TypeToken<O> type) {
    return delegate.maybeGet(type);
  }

  @Override
  public <O> Iterable<? extends O> getAll(TypeToken<O> type) {
    return delegate.getAll(type);
  }

  @Override
  public <T, O> Optional<O> first(TypeToken<T> type, Function<? super T, ? extends O> function) throws Exception {
    return delegate.first(type, function);
  }

}
