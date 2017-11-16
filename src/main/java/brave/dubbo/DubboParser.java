package brave.dubbo;

import brave.SpanCustomizer;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;

public abstract class DubboParser {

  public abstract void request(DubboAdapter adapter, RpcContext rpcContext,
      SpanCustomizer customizer);

  protected String spanName(DubboAdapter adapter, RpcContext rpcContext) {
    return adapter.getSpanName(rpcContext);
  }

  public abstract void response(DubboAdapter adapter, Result rpcResult, SpanCustomizer customizer);
}
