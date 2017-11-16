package brave.dubbo;

import brave.SpanCustomizer;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;

public class DubboServerParser extends DubboParser {
  @Override
  public void request(DubboAdapter adapter, RpcContext rpcContext, SpanCustomizer customizer) {
    customizer.name(spanName(adapter, rpcContext));
    String path = adapter.getRemoteAddress(rpcContext);
    if (path != null) {
      customizer.tag("consumer.address", path);
    }
  }

  @Override
  public void response(DubboAdapter adapter, Result rpcResult, SpanCustomizer customizer) {
    if (!rpcResult.hasException()) {
      customizer.tag("provider.result", "true");
    } else {
      customizer.tag("provider.exception", rpcResult.getException().getMessage());
    }
  }
}
