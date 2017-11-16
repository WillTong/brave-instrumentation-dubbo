package brave.dubbo;

import brave.Span;
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
 * Tracing filter for dubbo provider,returns sr and ss.
 *
 * @author Will Tong
 */
@Activate(group = Constants.PROVIDER)
public class DubboProviderFilter implements Filter {

  private DubboServerHandler handler;
  private TraceContext.Extractor<Map<String, String>> extractor;

  public void setDubboTracing(DubboTracing dubboTracing) {
    handler = DubboServerHandler.create(dubboTracing, new DubboServerAdapter());
    extractor = dubboTracing.tracing().propagation().extractor(Map::get);
  }

  @Override
  public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
    Span span = handler.handleReceive(extractor);
    Result rpcResult = invoker.invoke(invocation);
    handler.handleSend(rpcResult, span);
    return rpcResult;
  }
}
