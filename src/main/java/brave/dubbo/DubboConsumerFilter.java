package brave.dubbo;

import brave.Span;
import brave.Tracer;
import brave.propagation.TraceContext;
import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import java.util.Map;

/**
 * Tracing filter for dubbo consumer,returns cs and cr.
 *
 * @author Will Tong
 */
@Activate(group = Constants.CONSUMER)
public class DubboConsumerFilter implements Filter {
  private Tracer tracer;
  private DubboClientHandler handler;
  private TraceContext.Injector<Map<String, String>> injector;
  private TraceContext.Extractor<Map<String, String>> extractor;

  public void setDubboTracing(DubboTracing dubboTracing) {
    tracer = dubboTracing.tracing().tracer();
    handler = DubboClientHandler.create(dubboTracing, new DubboClientAdapter());
    injector = dubboTracing.tracing().propagation().injector(Map::put);
    extractor = dubboTracing.tracing().propagation().extractor(Map::get);
  }

  @Override
  public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
    Span span = handler.handleSend(extractor, injector);
    Result rpcResult = null;
    try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
      return rpcResult = invoker.invoke(invocation);
    } catch (RuntimeException | Error e) {
      throw e;
    } finally {
      handler.handleReceive(rpcResult, span);
    }
  }
}
